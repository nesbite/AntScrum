package pl.edu.agh.aco.spsp;


import pl.edu.agh.aco.spsp.config.ProblemConfiguration;
import pl.edu.agh.aco.spsp.view.SchedulingFrame;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class ACOFlowShop {

    private double[][] graph;
    private double pheromoneTrails[][][] = null;
    private Ant antColony[] = null;

    private int numberOfJobs;
    private int numberOfAnts;

    public int[] bestTour;
    String bestScheduleAsString = "";
    public double bestScheduleMakespan = -1.0;
    private Random random = new Random();
    private int numberOfEmployees;
    private double duration = 0;
    private String solutionFile = "";
    private String dataFileName = "";

    public ACOFlowShop(String dataFileName) throws IOException {
        this.dataFileName = dataFileName;
        this.graph = getProblemGraphFromFile(ProblemConfiguration.DATASET_DIR + dataFileName + ".csv");
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

        this.pheromoneTrails = new double[numberOfEmployees][numberOfEmployees][numberOfJobs];
        this.antColony = new Ant[numberOfAnts];
        for (int j = 0; j < antColony.length; j++) {
            antColony[j] = new Ant(numberOfJobs);
        }
    }

    public void generateResult() {
        System.out.println("ACO FOR FLOW SHOP SCHEDULLING");
        System.out.println("=============================");

        try {
            String fileDataset = dataFileName + ".csv";
            System.out.println("Data file: " + fileDataset);
            System.out.println("Starting computation at: " + new Date());
            long startTime = System.nanoTime();
            solveProblem();
            long endTime = System.nanoTime();
            System.out.println("Finishing computation at: " + new Date());
            duration = ((double) (endTime - startTime) / 1000000000.0);
            System.out.println("Duration (in seconds): "
                    + duration);
            saveResultToFile(ProblemConfiguration.SOLUTIONS_DIR + dataFileName, ".csv");
            saveResultDataToFile(ProblemConfiguration.SOLUTIONS_DATA_DIR + dataFileName + ".csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        for (int h = 0; h < numberOfEmployees; h++) {
            for (int i = 0; i < numberOfEmployees; i++) {
                for (int j = 0; j < numberOfJobs; j++) {
                    pheromoneTrails[h][i][j] = initialPheromoneValue;
                }
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
        for (int h = 0; h < numberOfEmployees; h++) {
            for (int i = 0; i < numberOfEmployees; i++) {
                for (int j = 0; j < numberOfJobs; j++) {
                    double newValue = pheromoneTrails[h][i][j]
                            * ProblemConfiguration.EVAPORATION;
                    if (newValue >= ProblemConfiguration.MINIMUM_PHEROMONE) {
                        pheromoneTrails[h][i][j] = newValue;
                    } else {
                        pheromoneTrails[h][i][j] = ProblemConfiguration.MINIMUM_PHEROMONE;
                    }
                }
            }
        }

        System.out.println("Depositing pheromone on Best Ant trail.");
        Ant bestAnt = getBestAnt();
        double contribution = ProblemConfiguration.Q
                / bestAnt.getSolutionMakespan(graph);
        System.out.println("Contibution for best ant: " + contribution);

        for (int i = 0; i < numberOfJobs; i++) {
            int index;
            if(i == 0){
                index = 0;
            } else {
                index = bestAnt.getSolution()[i-1];
            }
            double newValue = pheromoneTrails[index][bestAnt.getSolution()[i]][i]
                    + contribution;
            if (newValue <= ProblemConfiguration.MAXIMUM_PHEROMONE) {
                pheromoneTrails[index][bestAnt.getSolution()[i]][i] = newValue;
            } else {
                pheromoneTrails[index][bestAnt.getSolution()[i]][i] = ProblemConfiguration.MAXIMUM_PHEROMONE;
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
                String splitA[] = line.split(ProblemConfiguration.DELIMITER);
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
                String firstLine[] = line.split(ProblemConfiguration.DELIMITER);
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

    private File createFile(String name, int count, String ext) throws IOException {
        File f;

        solutionFile = name+ "(" + count + ")"+ext;
        f = new File(solutionFile);
        if (!f.exists()) {
            f.createNewFile();
            return f;
        }
        else {
            return createFile(name, ++count, ext);
        }

    }

    public void saveResultToFile(String path, String ext) throws IOException {

        File file = createFile(path, 1, ext);
        FileWriter fw = new FileWriter(file);
        BufferedWriter buf = new BufferedWriter(fw);
        int result[][] = getSolutionAsArray();

        buf.write(result.length + ProblemConfiguration.DELIMITER + result[0].length+"\n");
        String line="";
        for (int i = 0; i < result.length; i++){
            for (int j = 0; j < result[i].length; j++) {
                if(result[i][j] != -1){
                    line += result[i][j] + ProblemConfiguration.DELIMITER;
                }
            }
            buf.write(line+"\n");
            line="";
        }
        buf.close();
    }

    public void saveResultDataToFile(String path) throws IOException {

        FileWriter fw = new FileWriter(path, true);
        BufferedWriter buf = new BufferedWriter(fw);


        buf.write("Best solution makespan: " + bestScheduleMakespan+"\n");
        buf.write("Iterations: " + ProblemConfiguration.MAX_ITERATIONS+"\n");
        buf.write("Ants: " + ProblemConfiguration.NUMBER_OF_ANTS+"\n");
        buf.write("Probability: " + ProblemConfiguration.PROBABILITY+"\n");
        buf.write("Evaporation: " + ProblemConfiguration.EVAPORATION+"\n");
        buf.write("Q: " + ProblemConfiguration.Q+"\n");
        buf.write("Maximum pheromone: " + ProblemConfiguration.MAXIMUM_PHEROMONE+"\n");
        buf.write("Minimum pheromone: " + ProblemConfiguration.MINIMUM_PHEROMONE+"\n");
        buf.write("Duration (in seconds): " + duration+"\n");
        buf.write("Coresponding solution file: " + solutionFile+"\n");
        buf.write("\n");
        buf.close();

    }



}