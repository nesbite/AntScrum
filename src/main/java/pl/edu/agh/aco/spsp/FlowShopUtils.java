package pl.edu.agh.aco.spsp;

public class FlowShopUtils {

    public static double getScheduleMakespan(int[] solution, double[][] graph, int employees) {

        double[] employeesTime = new double[employees];
        for (int i = 0; i < solution.length; i++) {
            int empId = solution[i];
            employeesTime[empId] += graph[i][empId];
        }

        return max(employeesTime);

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
