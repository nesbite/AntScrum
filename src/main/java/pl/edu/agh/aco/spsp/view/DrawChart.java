package pl.edu.agh.aco.spsp.view;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import pl.edu.agh.aco.spsp.ACOFlowShop;
import pl.edu.agh.aco.spsp.config.ProblemConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DrawChart extends Application {


    final CategoryAxis yAxis = new CategoryAxis();
    final NumberAxis xAxis = new NumberAxis();
    final StackedBarChart<Number, String> sbc =
            new StackedBarChart<>(xAxis, yAxis);
    private double[][] graph;
    private int[][] solution;
    private int maxNumberOfTasks;
    private int numberOfEmployees;

    @Override
    public void start(Stage stage) {
        try {
            graph = ACOFlowShop.getProblemGraphFromFile(ProblemConfiguration.FILE_DATASET);
            solution = getSolutionFromFile(ProblemConfiguration.FILE_SOLUTION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0;i<solution[0].length;i++){
            XYChart.Series<Number,String> series = new XYChart.Series<>();
            for(int j=0;j<solution.length;j++){
                    series.getData().add(new XYChart.Data((solution[j][i] != -1) ? graph[solution[j][i]][j] : 0,""+(j+1)));

            }
            sbc.getData().add(series);
        }
        stage.setTitle("Bar Chart Sample");
        sbc.setTitle("Scrum optimization");
        yAxis.setLabel("Employees");
        xAxis.setLabel("Tasks");

        Scene scene = new Scene(sbc, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    public int[][] getSolutionFromFile(String path) throws IOException {
        int[][] solution = null;
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i=0;
        while ((line = buf.readLine()) != null) {
            if(i>0) {

                String splitA[] = line.split(" ");
                int j = 0;
                for (String s : splitA) {
                    if (!s.isEmpty()) {
                        solution[i-1][j] = Integer.parseInt(s);
                    }
                    j++;
                }
                i++;
            } else {
                String firstLine[] = line.split(" ");
                numberOfEmployees = Integer.parseInt(firstLine[0]);
                maxNumberOfTasks = Integer.parseInt(firstLine[1]);
                solution = new int[numberOfEmployees][maxNumberOfTasks];
                for(int a=0;a<numberOfEmployees;a++)
                    for(int b=0;b<maxNumberOfTasks;b++)
                        solution[a][b]= -1;
                i++;
            }
        }
        for (int k=0;k<solution.length;k++){
            for(int j=0;j<solution[k].length;j++){
                System.out.print(solution[k][j] + " ");
            }
            System.out.println();
        }
        return solution;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

