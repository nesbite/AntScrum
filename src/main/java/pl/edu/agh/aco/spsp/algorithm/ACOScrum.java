package pl.edu.agh.aco.spsp.algorithm;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;

import static pl.edu.agh.aco.spsp.config.ProblemConfiguration.getProperty;


public class ACOScrum {
    private static final Logger logger = LogManager.getLogger(ACOScrum.class);

    private static String CSV_EXTENSION = ".csv";

    private Ant antColony[];
    private double[][] graph;
    private double pheromoneTrails[][][];
    private int numberOfEmployees;
    private int numberOfJobs;

    private int[] bestTour;
    private String bestScheduleAsString = "";
    private double bestScheduleMakespan = -1.0;
    private double duration = 0;
    private String dataFileName;
    private double currentIterationSolutionMakespan;

    public ACOScrum(String dataFileName){
        this.dataFileName = dataFileName;
        this.graph = getProblemGraphFromFile(getProperty("dataSetDir") + dataFileName + CSV_EXTENSION);
        this.numberOfJobs = graph.length;
        logger.debug("Number of Jobs: " + numberOfJobs);

        numberOfEmployees = graph[0].length;
        for (int i = 1; i < numberOfJobs; i++) {
            if (graph[i].length != numberOfEmployees) {
                throw new RuntimeException("The input file is incorrect");
            }
        }
        logger.debug("Number of Machines: " + numberOfEmployees);

        int numberOfAnts = Integer.parseInt(getProperty("numberOfAnts"));
        logger.debug("Number of Ants in Colony: " + numberOfAnts);

        this.pheromoneTrails = new double[numberOfEmployees][numberOfEmployees][numberOfJobs];
        this.antColony = new Ant[numberOfAnts];
        for (int j = 0; j < antColony.length; j++) {
            antColony[j] = new Ant(numberOfJobs);
        }
    }

    public void generateResult() {
        logger.debug("ACO FOR FLOW SHOP SCHEDULLING");
        logger.debug("=============================");

        try {
            String fileDataSet = dataFileName + CSV_EXTENSION;
            logger.debug("Data file: " + fileDataSet);
            logger.debug("Starting computation at: " + new Date());
            long startTime = System.nanoTime();
            solveProblem();
            long endTime = System.nanoTime();
            logger.debug("Finishing computation at: " + new Date());
            duration = ((double) (endTime - startTime) / 1000000000.0);
            logger.debug("Duration (in seconds): "
                    + duration);
            saveResultToFile(getProperty("solutionsDir") + fileDataSet);
            saveResultDataToFile(getProperty("solutionsDataDir") + fileDataSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[][] getSolutionAsArray(){
        int max=0;
        int employee[] = new int[numberOfEmployees];
        int employee2[] = new int[numberOfEmployees];

        for (int employeeId : bestTour) {
            employee[employeeId]++;
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

    private int[] solveProblem() throws IOException {
        logger.debug("INITIALIZING PHEROMONE MATRIX");
        double initialPheromoneValue = Double.parseDouble(getProperty("maxPheromoneValue"));
        logger.debug("Initial pheromone value: " + initialPheromoneValue);
        for (int h = 0; h < numberOfEmployees; h++) {
            for (int i = 0; i < numberOfEmployees; i++) {
                for (int j = 0; j < numberOfJobs; j++) {
                    pheromoneTrails[h][i][j] = initialPheromoneValue;
                }
            }
        }

        int iteration = 0;
        logger.debug("STARTING ITERATIONS");
        logger.debug("Number of iterations: "
                + getProperty("maxIterations"));
        File file = new File(new File("").getAbsolutePath() + "/data/iterations.csv");
        file.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(file);
        BufferedWriter buf = new BufferedWriter(fw);
        while (iteration < Integer.parseInt(getProperty("maxIterations")) ){
            logger.debug("Current iteration: " + iteration);
            clearAntSolutions();
            buildSolutions();
            updatePheromoneTrails();
            updateBestSolution();
            saveIterationResultToFile(buf, iteration);
            iteration++;
        }
        buf.close();
        logger.debug("EXECUTION FINISHED");
        logger.debug("Best schedule makespan: " + bestScheduleMakespan);
        logger.debug("Best schedule:" + bestScheduleAsString);
        return bestTour.clone();
    }

    private void saveIterationResultToFile(BufferedWriter buf, int iteration) throws IOException {
        writeToFileWithNewLine(buf, iteration + "," + (int)currentIterationSolutionMakespan);
    }


    private void updatePheromoneTrails() {
        logger.debug("UPDATING PHEROMONE TRAILS");

        logger.debug("Performing evaporation on all edges");
        logger.debug("Evaporation ratio: "
                + getProperty("evaporation"));
        for (int h = 0; h < numberOfEmployees; h++) {
            for (int i = 0; i < numberOfEmployees; i++) {
                for (int j = 0; j < numberOfJobs; j++) {
                    double newValue = pheromoneTrails[h][i][j]
                            * Double.parseDouble(getProperty("evaporation"));
                    if (newValue >= Double.parseDouble(getProperty("minPheromoneValue"))) {
                        pheromoneTrails[h][i][j] = newValue;
                    } else {
                        pheromoneTrails[h][i][j] = Double.parseDouble(getProperty("minPheromoneValue"));
                    }
                }
            }
        }

        logger.debug("Depositing pheromone on Best Ant trail.");
        Ant bestAnt = getBestAnt();
        double contribution = Double.parseDouble(getProperty("q"))
                / bestAnt.getSolutionMakespan(graph);
        logger.debug("Contribution for best ant: " + contribution);

        for (int i = 0; i < numberOfJobs; i++) {
            int index;
            if(i == 0){
                index = 0;
            } else {
                index = bestAnt.getSolution()[i-1];
            }
            double newValue = pheromoneTrails[index][bestAnt.getSolution()[i]][i]
                    + contribution;
            if (newValue <= Double.parseDouble(getProperty("minPheromoneValue"))) {
                pheromoneTrails[index][bestAnt.getSolution()[i]][i] = newValue;
            } else {
                pheromoneTrails[index][bestAnt.getSolution()[i]][i] = Double.parseDouble(getProperty("maxPheromoneValue"));
            }
        }
    }

    private void buildSolutions() {
        logger.debug("BUILDING ANT SOLUTIONS");
        int antCounter = 0;
        for (Ant ant : antColony) {
            logger.debug("Current ant: " + antCounter);
            while (ant.getCurrentIndex() < numberOfJobs) {
                int nextNode = ant.selectNextNode(pheromoneTrails, graph);
                ant.visitNode(nextNode);
            }
            logger.debug("Original Solution > Makespan: "
                    + ant.getSolutionMakespan(graph) + ", Schedule: "
                    + ant.getSolutionAsString());
            ant.improveSolution(graph);
            logger.debug("After Local Search > Makespan: "
					+ ant.getSolutionMakespan(graph) + ", Schedule: "
					+ ant.getSolutionAsString());
            antCounter++;
        }
    }

    private void clearAntSolutions() {
        logger.debug("CLEARING ANT SOLUTIONS");
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
        logger.debug("GETTING BEST SOLUTION FOUND");
        Ant bestAnt = getBestAnt();
        currentIterationSolutionMakespan = bestAnt.getSolutionMakespan(graph);
        if (bestTour == null
                || bestScheduleMakespan > bestAnt.getSolutionMakespan(graph)) {
            bestTour = bestAnt.getSolution().clone();
            bestScheduleMakespan = bestAnt.getSolutionMakespan(graph);
            bestScheduleAsString = bestAnt.getSolutionAsString();
        }
        logger.debug("Best solution so far > Makespan: "
                + bestScheduleMakespan + ", Schedule: " + bestScheduleAsString);
    }


    public static double[][] getProblemGraphFromFile(String path) {
        double graph[][] = null;
        try(BufferedReader buf = new BufferedReader(new FileReader(path))) {
            graph = parseFile(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    private static double[][] parseFile(BufferedReader buf) throws IOException {
        double graph[][] = null;
        String line;
        int row = 0;
        boolean isHeader = true;
        while ((line = buf.readLine()) != null) {
            if (isHeader) {
                String firstLine[] = line.split(getProperty("delimiter"));
                String numberOfJobs = firstLine[0];
                String numberOfMachines = firstLine[1];
                graph = new double[Integer.parseInt(numberOfJobs)][Integer
                        .parseInt(numberOfMachines)];

            } else {
                String splitA[] = line.split(getProperty("delimiter"));
                LinkedList<String> split = new LinkedList<>();
                for (String s : splitA) {
                    if (!s.isEmpty()) {
                        split.add(s);
                    }
                }
                int j = 0;
                for (String s : split) {
                    if (!s.isEmpty()) {
                        graph[row][j++] = Integer.parseInt(s);
                    }
                }
                row++;
            }
            isHeader = false;
        }
        return graph;
    }


    private void saveResultToFile(String path) throws IOException {

        File file = new File(path);
        file.getParentFile().mkdirs();
        FileWriter fileWriter = new FileWriter(file, false);
        BufferedWriter buf = new BufferedWriter(fileWriter);
        int solution[][] = getSolutionAsArray();

        writeToFileWithNewLine(buf, solution.length + getProperty("delimiter") + solution[0].length);
        for (int[] tasksForEmployee : solution) {
            String line = "";
            for (int taskNo : tasksForEmployee) {
                if (taskNo != -1) {
                    line += taskNo + getProperty("delimiter");
                }
            }
            writeToFileWithNewLine(buf, line);

        }
        buf.close();
    }

    private void saveResultDataToFile(String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(file, false);
        BufferedWriter buf = new BufferedWriter(fw);

        writeToFileWithNewLine(buf, "Best solution makespan: " + bestScheduleMakespan);
        writeToFileWithNewLine(buf, "Iterations: " + getProperty("maxIterations"));
        writeToFileWithNewLine(buf, "Ants: " + getProperty("numberOfAnts"));
        writeToFileWithNewLine(buf, "Probability: " + getProperty("probability"));
        writeToFileWithNewLine(buf, "Evaporation: " + getProperty("evaporation"));
        writeToFileWithNewLine(buf, "Q: " + getProperty("q"));
        writeToFileWithNewLine(buf, "Maximum pheromone: " + getProperty("maxPheromoneValue"));
        writeToFileWithNewLine(buf, "Minimum pheromone: " + getProperty("minPheromoneValue"));
        writeToFileWithNewLine(buf, "Duration (in seconds): " + duration);
        writeToFileWithNewLine(buf, "");
        buf.close();

    }

    private void writeToFileWithNewLine(BufferedWriter buf, String text) throws IOException {
            buf.write(text + "\n");
    }






}