package pl.edu.agh.aco.spsp;

import pl.edu.agh.aco.spsp.config.ProblemConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Ant {

    private int currentIndex = 0;
    private int lastEmployee = -1;
    private int solution[];
    public boolean visited[];

    public Ant(int graphLenght) {
        this.solution = new int[graphLenght];
        this.visited = new boolean[graphLenght];
    }


    public void visitNode(int visitedNode) {
        solution[currentIndex] = visitedNode;
        visited[currentIndex] = true;
        currentIndex++;
        lastEmployee = visitedNode;
    }


    public boolean isNodeVisited(int node) {
        return visited[node];
    }


    public int selectNextNode(double[][][] trails, double[][] graph) {
        int nextNode = 0;
        Random random = new Random();
        double randomValue = random.nextDouble();
        // Probability Setting from Paper
        double bestChoiceProbability = ((double) ProblemConfiguration.PROBABILITY)
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


    public int[] getSolution() {
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


    public void clear() {
        for (int i = 0; i < visited.length; i++) {
            visited[i] = false;

        }
    }


    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }


    public int getCurrentIndex() {
        return currentIndex;
    }


    public double getSolutionMakespan(double[][] graph) {
        return FlowShopUtils.getScheduleMakespan(solution, graph, graph[0].length);
    }

    public void improveSolution(double[][] graph) {
        double makespan = getSolutionMakespan(graph);

        int[] localSolutionJobs = new int[solution.length];
        List<Integer> jobsList = new ArrayList<Integer>();

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

                double newMakespan = FlowShopUtils.getScheduleMakespan(
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


    public String getSolutionAsString() {
        String solutionString = new String();
        for (int i = 0; i < solution.length; i++) {
            solutionString = solutionString + " " + solution[i];
        }
        return solutionString;
    }
}