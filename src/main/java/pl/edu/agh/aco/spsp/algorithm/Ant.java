package pl.edu.agh.aco.spsp.algorithm;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pl.edu.agh.aco.spsp.util.ScrumUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static pl.edu.agh.aco.spsp.config.ProblemConfiguration.getProperty;


class Ant {
    private static final Logger logger = LogManager.getLogger(Ant.class);

    private int currentIndex = 0;
    private int lastEmployee = -1;
    private int solution[];
    private boolean visited[];

    Ant(int graphLength) {
        this.solution = new int[graphLength];
        this.visited = new boolean[graphLength];
    }


    void visitNode(int visitedNode) {
        solution[currentIndex] = visitedNode;
        visited[currentIndex] = true;
        currentIndex++;
        lastEmployee = visitedNode;
    }


    private boolean isNodeVisited(int node) {
        return visited[node];
    }


    int selectNextNode(double[][][] trails, double[][] graph) {
        int nextNode = 0;
        Random random = new Random();
        double randomValue = random.nextDouble();

        double bestChoiceProbability = Double.parseDouble(getProperty("probability"))
                / graph.length;
        if (randomValue < bestChoiceProbability) {
            double currentMaximumFeromone = -1;
            for (int i = 0; i < graph[0].length; i++) {
                double currentFeromone=0;
                if(lastEmployee == -1){
                    for(int j=0;j<graph[0].length;j++){
                        currentFeromone = trails[j][i][currentIndex];
                        if (!isNodeVisited(currentIndex)
                                && currentFeromone > currentMaximumFeromone) {
                            nextNode = i;
                            currentMaximumFeromone = currentFeromone;
                        }
                    }
                } else {
                     currentFeromone = trails[lastEmployee][i][currentIndex];
                    if (!isNodeVisited(currentIndex)
                            && currentFeromone > currentMaximumFeromone) {
                        nextNode = i;
                        currentMaximumFeromone = currentFeromone;
                    }
                }

            }
            return nextNode;
        } else {
            double probabilities[] = getProbabilities(trails);
            double r = randomValue;
            double total = 0;
            for (int i = 0; i < graph[0].length; i++) {
                total += probabilities[i];
                if (total >= r) {
                    nextNode = i;
                    return nextNode;
                }
            }
        }
        return nextNode;
    }


    int[] getSolution() {
        return solution;
    }

    private double[] getProbabilities(double[][][] trails) {
        double probabilities[] = new double[trails.length];
        int employee = (lastEmployee == -1) ? 0 : lastEmployee;
        double denom = 0.0;
        for (int l = 0; l < trails.length; l++) {
            if (!isNodeVisited(currentIndex)) {
                denom += trails[employee][l][currentIndex];
            }

        }

        for (int j = 0; j < trails.length; j++) {
            if (isNodeVisited(currentIndex)) {
                probabilities[j] = 0.0;
            } else {
                double numerator = trails[employee][j][currentIndex];
                probabilities[j] = numerator / denom;
            }
        }
        return probabilities;
    }


    void clear() {
        for (int i = 0; i < visited.length; i++) {
            visited[i] = false;

        }
    }


    void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }


    int getCurrentIndex() {
        return currentIndex;
    }


    double getSolutionMakespan(double[][] graph) {
        return ScrumUtils.getScheduleMakespan(solution, graph, graph[0].length);
    }

    void improveSolution(double[][] graph) {
        double makespan = getSolutionMakespan(graph);

        int[] localSolutionJobs = new int[solution.length];
        List<Integer> jobsList = new ArrayList<>();

        for (int job : solution) {
            jobsList.add(job);
        }

        List<Integer> localSolution = jobsList;

        int indexI = 0;
        boolean lessMakespan = true;

        while (indexI < (solution.length) && lessMakespan) {
            int jobI = localSolution.get(indexI);

            localSolution.remove(indexI);

            int indexJ = 0;

            while (indexJ < solution.length && lessMakespan) {
                localSolution.add(indexJ, jobI);

                int[] intermediateSolution = new int[solution.length];
                int t = 0;
                for (int sol : localSolution) {
                    intermediateSolution[t] = sol;
                    t++;
                }

                double newMakespan = ScrumUtils.getScheduleMakespan(
                        intermediateSolution, graph, graph[0].length);

                if (newMakespan < makespan) {
                    makespan = newMakespan;
                    lessMakespan = false;
                } else {
                    localSolution.remove(indexJ);
                }

                indexJ++;
            }
            if (lessMakespan) {
                localSolution.add(indexI, jobI);
            }
            indexI++;
        }

        int k = 0;
        for (int job : localSolution) {
            localSolutionJobs[k] = job;
            k++;
        }
        solution = localSolutionJobs;
    }


    String getSolutionAsString() {
        StringBuilder solutionString = new StringBuilder();
        for (int employeeNo : solution) {
            solutionString.append(" ").append(employeeNo);
        }
        return solutionString.toString();
    }
}