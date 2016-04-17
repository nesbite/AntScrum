package pl.edu.agh.aco;

public class AcoSPSP {
    private int employees;
    private int tasks;
    private int skills;
    private int[] tasksDurationMatrix;
    private int[][] skillsTasksMatrix;
    private int[][] skillsEmployeesMatrix;
    private int[][] taskDependencyMatrix;
    private int[][] result;

    private void init(){
        this.tasksDurationMatrix = new int[tasks];
        this.skillsEmployeesMatrix = new int[skills][employees];
        this.taskDependencyMatrix = new int[tasks][tasks];
        this.skillsTasksMatrix = new int[skills][tasks];
        this.result = new int[tasks][employees];
    }


}
