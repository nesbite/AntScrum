package pl.edu.agh.aco.spsp;

import pl.edu.agh.aco.spsp.config.Config;
import pl.edu.agh.aco.spsp.view.SchedulingFrame;

import javax.swing.*;

/**
 * Appies the MAX-MIN Ant System algorithm to Flow-Shop Problem instance.
 *
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 * @author Adrián Pareja (adrian@pareja.com)
 */
public class ACOFlowShop {

    private final int numberOfEmployees;
    private double[] graph;
    private double pheromoneTrails[][] = null;
    private Ant antColony[] = null;

    private int numberOfJobs;
    private int numberOfAnts;

    public int[] bestTour;
    String bestScheduleAsString = "";
    public double bestScheduleMakespan = -1.0;

    public ACOFlowShop(double[] graph, int numberOfEmployees) {
        this.numberOfJobs = graph.length;
        this.numberOfEmployees = numberOfEmployees;

        System.out.println("Number of Jobs: " + numberOfJobs);
        System.out.println("Number of Machines: " + numberOfEmployees);

        this.numberOfAnts = Config.NUMBER_OF_ANTS;
        System.out.println("Number of Ants in Colony: " + numberOfAnts);
        this.graph = graph;

        this.pheromoneTrails = new double[numberOfJobs][numberOfJobs];
        this.antColony = new Ant[numberOfAnts];
        for (int j = 0; j < antColony.length; j++) {
            antColony[j] = new Ant(numberOfJobs);
        }
    }

    public void showSolution() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {
        for (UIManager.LookAndFeelInfo info : UIManager
                .getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
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

    /**
     * Solves a Flow-Shop instance using Ant Colony Optimization.
     *
     * @return Array representing a solution.
     */
    public int[] solveProblem() {
        System.out.println("INITIALIZING PHEROMONE MATRIX");
        double initialPheromoneValue = Config.MAXIMUM_PHEROMONE;
        System.out.println("Initial pheromone value: " + initialPheromoneValue);
        for (int i = 0; i < numberOfJobs; i++) {
            for (int j = 0; j < numberOfJobs; j++) {
                pheromoneTrails[i][j] = initialPheromoneValue;
            }
        }

        int iteration = 0;
        System.out.println("STARTING ITERATIONS");
        System.out.println("Number of iterations: "
                + Config.MAX_ITERATIONS);

        while (iteration < Config.MAX_ITERATIONS) {
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

    /**
     * Updates pheromone trail values
     */
    private void updatePheromoneTrails() {
        System.out.println("UPDATING PHEROMONE TRAILS");

        System.out.println("Performing evaporation on all edges");
        System.out.println("Evaporation ratio: "
                + Config.EVAPORATION);

        for (int i = 0; i < numberOfJobs; i++) {
            for (int j = 0; j < numberOfJobs; j++) {
                double newValue = pheromoneTrails[i][j]
                        * Config.EVAPORATION;
                if (newValue >= Config.MINIMUM_PHEROMONE) {
                    pheromoneTrails[i][j] = newValue;
                } else {
                    pheromoneTrails[i][j] = Config.MINIMUM_PHEROMONE;
                }
            }
        }

        System.out.println("Depositing pheromone on Best Ant trail.");
        Ant bestAnt = getBestAnt();
        double contribution = Config.Q
                / bestAnt.getSolutionMakespan(graph);
        System.out.println("Contibution for best ant: " + contribution);

        for (int i = 0; i < numberOfJobs; i++) {
            double newValue = pheromoneTrails[bestAnt.getSolution()[i]][i]
                    + contribution;
            if (newValue <= Config.MAXIMUM_PHEROMONE) {
                pheromoneTrails[bestAnt.getSolution()[i]][i] = newValue;
            } else {
                pheromoneTrails[bestAnt.getSolution()[i]][i] = Config.MAXIMUM_PHEROMONE;
            }
        }
    }

    /**
     * Build a solution for every Ant in the Colony.
     */
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

    /**
     * Clears solution build for every Ant in the colony.
     */
    private void clearAntSolutions() {
        System.out.println("CLEARING ANT SOLUTIONS");
        for (Ant ant : antColony) {
            ant.setCurrentIndex(0);
            ant.clear();
        }
    }

    /**
     * Returns the best performing Ant in Colony
     *
     * @return The Best Ant
     */
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

    /**
     * Selects the best solution found so far.
     *
     * @return
     */
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

}