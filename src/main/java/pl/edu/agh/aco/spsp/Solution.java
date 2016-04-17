package pl.edu.agh.aco.spsp;

public interface Solution {

    int getEmployeesAmount();

    int getTasksAmount();

    double[] getTasksLength();

    double[][] getEmployeesTasks();
}
