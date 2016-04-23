package pl.edu.agh.aco.spsp.config;


public class ProblemConfiguration {

    public static final String FILE_DATASET = "data/MOCK_DATA.csv";
    public static final String FILE_SOLUTION = "data/solution.data";
    public static final String DELIMITER = ",";

    public static final int NUMBER_OF_ANTS = 2;
    public static final double EVAPORATION = 0.5;
    public static final int Q = 1;
    public static final double MAXIMUM_PHEROMONE = 1.0;
    public static final double MINIMUM_PHEROMONE = MAXIMUM_PHEROMONE / 5;
    public static final int MAX_ITERATIONS = 500;
}
