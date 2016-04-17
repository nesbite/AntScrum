package pl.edu.agh.aco.spsp.config;

/**
 * Parameter settings for the algorithm.
 *
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 * @author Adrián Pareja (adrian@pareja.com)
 */
public class Config {

    public static final double ALPHA = 1;
    public static final double BETA = 5;

    public static final String DATA_PATH = "data/simple_problem.data";

    public static final int NUMBER_OF_ANTS = 10;
    public static final double EVAPORATION = 0.5;
    public static final int Q = 1;
    public static final double MAXIMUM_PHEROMONE = 1.0;
    public static final double MINIMUM_PHEROMONE = MAXIMUM_PHEROMONE / 5;
    public static final int MAX_ITERATIONS = 100;
}
