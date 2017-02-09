package pl.edu.agh.aco.spsp.util;

public class ScrumUtils {

    public static double getScheduleMakespan(int[] solution, double[][] graph, int employees) {
        double[] employeesTime = new double[employees];
        for (int i = 0; i < solution.length; i++) {
            int employeeId = solution[i];
            employeesTime[employeeId] += graph[i][employeeId];
        }
        return max(employeesTime);
    }

    private static double max(double[] array) {
        double max = -1;
        for (double i : array) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }
}
