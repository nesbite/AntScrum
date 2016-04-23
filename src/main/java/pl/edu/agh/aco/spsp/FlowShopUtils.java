package pl.edu.agh.aco.spsp;

/**
 * Utilities methods for Flow-Shop problem solving.
 *
 * @author Adrián Pareja (adrian@pareja.com)
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 */
public class FlowShopUtils {

    /**
     * Gets the makespan for an Schedule.
     *
     * @param solution Schedule to evaluate.
     * @param graph    Problem graph.
     * @return Schedule makespan.
     */
    public static double getScheduleMakespan(int[] solution, double[][] graph, int employees) {
        int machines = graph[0].length;

        double[] employeesTime = new double[employees];
        for (int i = 0; i < solution.length; i++) {
            int empId = solution[i];
            employeesTime[empId] += graph[i][empId];
        }

        return max(employeesTime);
/*

        double[] machinesTime = new double[machines];

        for (int job : solution) {
            for (int i = 0; i < machines; i++) {
                tempo = graph[job][i];
                if (i == 0) {
                    machinesTime[i] = machinesTime[i] + tempo;
                } else {
                    if (machinesTime[i] > machinesTime[i - 1]) {
                        machinesTime[i] = machinesTime[i] + tempo;
                    } else {
                        machinesTime[i] = machinesTime[i - 1] + tempo;
                    }
                }
            }
        }
        return machinesTime[machines - 1];
*/
    }

    public static double max(double[] array) {
        double max = -1;
        for (double i : array) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

}
