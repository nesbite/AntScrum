package pl.edu.agh.aco.spsp.config;


public class ProblemConfiguration {

    public static final String DELIMITER = ",";

    public static  int NUMBER_OF_ANTS = 1;
    public static  double PROBABILITY = 1;
    public static  double EVAPORATION = 0.5;
    public static  int Q = 20;
    public static final double MAXIMUM_PHEROMONE = 1.0;
    public static final double MINIMUM_PHEROMONE = MAXIMUM_PHEROMONE / 5;
    public static  int MAX_ITERATIONS = 1000;


//    public static final String FILENAME = "100x5";
//    public static final String FILENAME = "100x5"+"_ants_"+NUMBER_OF_ANTS+"_prob_"+PROBABILITY+"_evap_"+EVAPORATION+"_q_"+Q;
//    public static final String EXTENSION = ".csv";
    public static final String DATASET_DIR = "data/data/";
    public static final String SOLUTIONS_DATA_DIR = "data/solutions/data/";
    public static final String SOLUTIONS_DIR = "data/solutions/solution/";
    public static final String IMAGES_DIR = "data/solutions/charts/";
}
