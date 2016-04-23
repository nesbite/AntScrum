package pl.edu.agh.aco.spsp;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.edu.agh.aco.spsp.config.ProblemConfiguration;
import pl.edu.agh.aco.spsp.view.DrawChart;
import pl.edu.agh.aco.spsp.view.SchedulingFrame;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Appies the MAX-MIN Ant System algorithm to Flow-Shop Problem instance.
 *
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 * @author Adrián Pareja (adrian@pareja.com)
 */
public class ACOFlowShop {

    private double[][] graph;
    private double pheromoneTrails[][] = null;
    private Ant antColony[] = null;

    private int numberOfJobs;
    private int numberOfAnts;

    public int[] bestTour;
    String bestScheduleAsString = "";
    public double bestScheduleMakespan = -1.0;
    private Random random = new Random();
    private int numberOfEmployees;

    public ACOFlowShop(double[][] graph) {
        this.numberOfJobs = graph.length;
        System.out.println("Number of Jobs: " + numberOfJobs);

        numberOfEmployees = graph[0].length;
        for (int i = 1; i < numberOfJobs; i++) {
            if (graph[i].length != numberOfEmployees) {
                throw new RuntimeException("The input file is incorrect");
            }
        }
        System.out.println("Number of Machines: " + numberOfEmployees);

        this.numberOfAnts = ProblemConfiguration.NUMBER_OF_ANTS;
        System.out.println("Number of Ants in Colony: " + numberOfAnts);
        this.graph = graph;
        this.pheromoneTrails = new double[numberOfEmployees][numberOfJobs];
        this.antColony = new Ant[numberOfAnts];
        for (int j = 0; j < antColony.length; j++) {
            antColony[j] = new Ant(numberOfJobs);
        }
    }

    public static void main(String... args) {
        System.out.println("ACO FOR FLOW SHOP SCHEDULLING");
        System.out.println("=============================");

        try {
            String fileDataset = ProblemConfiguration.FILE_DATASET;
            System.out.println("Data file: " + fileDataset);
            double[][] graph = getProblemGraphFromFile(fileDataset);
            ACOFlowShop acoFlowShop = new ACOFlowShop(graph);
            System.out.println("Starting computation at: " + new Date());
            long startTime = System.nanoTime();
            acoFlowShop.solveProblem();
            long endTime = System.nanoTime();
            System.out.println("Finishing computation at: " + new Date());
            System.out.println("Duration (in seconds): "
                    + ((double) (endTime - startTime) / 1000000000.0));
//            acoFlowShop.showSolution();
            acoFlowShop.saveResultToFile(ProblemConfiguration.FILE_SOLUTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSolution() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                .getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SchedulingFrame frame = new SchedulingFrame();
                frame.setSolutionMakespan(bestScheduleMakespan);
                frame.setProblemGraph(graph);
                frame.setSolution(bestTour);
                frame.setVisible(true);
            }
        });
    }

    private int[][] getSolutionAsArray(){
        int max=0;
        int employee[] = new int[numberOfEmployees];
        int employee2[] = new int[numberOfEmployees];

        for (int i=0;i<bestTour.length;i++) {
            employee[bestTour[i]]++;
        }

        for (int i : employee) {
            if(i>max)
                max = i;
        }
        int[][] result = new int[numberOfEmployees][max];
        for(int i=0;i<numberOfEmployees;i++){
            for (int j=0;j<max;j++){
                result[i][j] = -1;
            }
        }
        for (int i=0;i<bestTour.length;i++){
            result[bestTour[i]][employee2[bestTour[i]]++] = i;
        }

        return result;
    }

    public int[] solveProblem() {
        System.out.println("INITIALIZING PHEROMONE MATRIX");
        double initialPheromoneValue = ProblemConfiguration.MAXIMUM_PHEROMONE;
        System.out.println("Initial pheromone value: " + initialPheromoneValue);
        for (int i = 0; i < numberOfEmployees; i++) {
            for (int j = 0; j < numberOfJobs; j++) {
                pheromoneTrails[i][j] = initialPheromoneValue;
            }
        }

        int iteration = 0;
        System.out.println("STARTING ITERATIONS");
        System.out.println("Number of iterations: "
                + ProblemConfiguration.MAX_ITERATIONS);

        while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
            System.out.println("Current iteration: " + iteration);
            clearAntSolutions();
            buildSolutions();
            updatePheromoneTrails();
            updateBestSolution();
            iteration++;
        }
        System.out.println("EXECUTION FINISHED");
        System.out.println("Best schedule makespam: " + bestScheduleMakespan);
        System.out.println("Best schedule:" + bestScheduleAsString);
        return bestTour.clone();
    }


    private void updatePheromoneTrails() {
        System.out.println("UPDATING PHEROMONE TRAILS");

        System.out.println("Performing evaporation on all edges");
        System.out.println("Evaporation ratio: "
                + ProblemConfiguration.EVAPORATION);

        for (int i = 0; i < numberOfEmployees; i++) {
            for (int j = 0; j < numberOfJobs; j++) {
                double newValue = pheromoneTrails[i][j]
                        * ProblemConfiguration.EVAPORATION;
                if (newValue >= ProblemConfiguration.MINIMUM_PHEROMONE) {
                    pheromoneTrails[i][j] = newValue;
                } else {
                    pheromoneTrails[i][j] = ProblemConfiguration.MINIMUM_PHEROMONE;
                }
            }
        }

        System.out.println("Depositing pheromone on Best Ant trail.");
        Ant bestAnt = getBestAnt();
        double contribution = ProblemConfiguration.Q
                / bestAnt.getSolutionMakespan(graph);
        System.out.println("Contibution for best ant: " + contribution);

        for (int i = 0; i < numberOfJobs; i++) {
            double newValue = pheromoneTrails[bestAnt.getSolution()[i]][i]
                    + contribution;
            if (newValue <= ProblemConfiguration.MAXIMUM_PHEROMONE) {
                pheromoneTrails[bestAnt.getSolution()[i]][i] = newValue;
            } else {
                pheromoneTrails[bestAnt.getSolution()[i]][i] = ProblemConfiguration.MAXIMUM_PHEROMONE;
            }
        }
    }

    private void buildSolutions() {
        System.out.println("BUILDING ANT SOLUTIONS");
        int antCounter = 0;
        for (Ant ant : antColony) {
            System.out.println("Current ant: " + antCounter);
            while (ant.getCurrentIndex() < numberOfJobs) {
                int nextNode = ant.selectNextNode(pheromoneTrails, graph);
                ant.visitNode(nextNode);
            }
            System.out.println("Original Solution > Makespan: "
                    + ant.getSolutionMakespan(graph) + ", Schedule: "
                    + ant.getSolutionAsString());
            ant.improveSolution(graph);
			System.out.println("After Local Search > Makespan: "
					+ ant.getSolutionMakespan(graph) + ", Schedule: "
					+ ant.getSolutionAsString());
            antCounter++;
        }
    }

    private void clearAntSolutions() {
        System.out.println("CLEARING ANT SOLUTIONS");
        for (Ant ant : antColony) {
            ant.setCurrentIndex(0);
            ant.clear();
        }
    }

    private Ant getBestAnt() {
        Ant bestAnt = antColony[0];
        for (Ant ant : antColony) {
            if (ant.getSolutionMakespan(graph) < bestAnt
                    .getSolutionMakespan(graph)) {
                bestAnt = ant;
            }
        }
        return bestAnt;
    }

    private void updateBestSolution() {
        System.out.println("GETTING BEST SOLUTION FOUND");
        Ant bestAnt = getBestAnt();
        if (bestTour == null
                || bestScheduleMakespan > bestAnt.getSolutionMakespan(graph)) {
            bestTour = bestAnt.getSolution().clone();
            bestScheduleMakespan = bestAnt.getSolutionMakespan(graph);
            bestScheduleAsString = bestAnt.getSolutionAsString();
        }
        System.out.println("Best solution so far > Makespan: "
                + bestScheduleMakespan + ", Schedule: " + bestScheduleAsString);
    }


    public static double[][] getProblemGraphFromFile(String path)
            throws IOException {
        double graph[][] = null;
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i = 0;

        while ((line = buf.readLine()) != null) {
            if (i > 0) {
                String splitA[] = line.split(" ");
                LinkedList<String> split = new LinkedList<String>();
                for (String s : splitA) {
                    if (!s.isEmpty()) {
                        split.add(s);
                    }
                }
                int j = 0;
                for (String s : split) {
                    if (!s.isEmpty()) {
                        graph[i - 1][j++] = Integer.parseInt(s);
                    }
                }
            } else {
                String firstLine[] = line.split(" ");
                String numberOfJobs = firstLine[0];
                String numberOfMachines = firstLine[1];

                if (graph == null) {
                    graph = new double[Integer.parseInt(numberOfJobs)][Integer
                            .parseInt(numberOfMachines)];
                }
            }
            i++;
        }
        return graph;
    }

    public void saveResultToFile(String path) throws IOException {

        Path file = Paths.get(ProblemConfiguration.FILE_SOLUTION);
        FileWriter fw = new FileWriter(path);
        BufferedWriter buf = new BufferedWriter(fw);
        int result[][] = getSolutionAsArray();

        List<String> list = new ArrayList<>();
        list.add(result.length + " " + result[0].length);
        Files.write(file, list, Charset.forName("UTF-8"));
        String line="";
        for (int i = 0; i < result.length; i++){
            for (int j = 0; j < result[i].length; j++) {
                if(result[i][j] != -1){
                    line += result[i][j] + " ";
                }
            }
            list.add(line);
            Files.write(file, list, Charset.forName("UTF-8"));
            line="";
        }
    }



}