package pl.edu.agh.aco.spsp.chart;


import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import pl.edu.agh.aco.spsp.algorithm.ACOScrum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static pl.edu.agh.aco.spsp.config.ProblemConfiguration.getConfiguration;
import static pl.edu.agh.aco.spsp.config.ProblemConfiguration.getProperty;

public class ChartCreator extends Application {
    private static String CSV_EXTENSION = ".csv";

    private final CategoryAxis yAxis = new CategoryAxis();
    private final NumberAxis xAxis = new NumberAxis();

    private double[][] graph;
    private int[][] solution;
    private static String fileName;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws InterruptedException {

        File[] listOfFiles = new File[] {new File("data/solutions/solution/100x5.csv")};
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileName = listOfFile.getName().substring(0, listOfFile.getName().indexOf("."));
            } else {
                continue;
            }

            try {
                graph = ACOScrum.getProblemGraphFromFile(getConfiguration().getProperty("dataSetDir") + fileName + CSV_EXTENSION);
                solution = getSolutionFromFile(getConfiguration().getProperty("solutionsDir") + fileName + CSV_EXTENSION);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StackedBarChart<Number, String> sbc = new StackedBarChart<>(xAxis, yAxis);
            for (int i = 0; i < solution[0].length; i++) {
                XYChart.Series<Number, String> series = new XYChart.Series<>();
                for (int j = 0; j < solution.length; j++) {
                    series.getData().add(new XYChart.Data((solution[j][i] != -1) ? graph[solution[j][i]][j] : 0, "" + (j + 1)));

                }
                sbc.getData().add(series);
            }
            stage.setTitle("Bar Chart Sample");
            sbc.setTitle("Scrum optimization");
            yAxis.setLabel("Employees");
            xAxis.setLabel("Tasks");
            ScrollPane pane = new ScrollPane();
            Scene scene = new Scene(pane, 800, 600);
            sbc.setMinHeight(800.0 * (graph[0].length / 30.0));
            pane.setContent(sbc);
            pane.setFitToWidth(true);
            pane.setFitToHeight(true);

            stage.setScene(scene);
            stage.show();

            saveSnapshot(sbc);
        }

    }

    private static Image createImage(Node node) {
        WritableImage wi;
        SnapshotParameters parameters = new SnapshotParameters();

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;
    }

    private void saveSnapshot(Node node) {
        Image image = createImage(node);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            File file =  new File(getProperty("imagesDir") + fileName +".jpg");
            file.getParentFile().mkdirs();
            ImageIO.write(bufImageRGB, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();
    }

    private int[][] getSolutionFromFile(String path) throws IOException {
        int[][] solution = null;
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i=0;
        while ((line = buf.readLine()) != null) {
            if(i>0) {
                String splitA[] = line.split(getProperty("delimiter"));
                int j = 0;
                for (String s : splitA) {
                    if (!s.isEmpty()) {
                        solution[i-1][j] = Integer.parseInt(s);
                    }
                    j++;
                }
                i++;
            } else {
                String firstLine[] = line.split(getProperty("delimiter"));
                int numberOfEmployees = Integer.parseInt(firstLine[0]);
                int maxNumberOfTasks = Integer.parseInt(firstLine[1]);
                solution = new int[numberOfEmployees][maxNumberOfTasks];
                for(int a = 0; a< numberOfEmployees; a++)
                    for(int b = 0; b< maxNumberOfTasks; b++)
                        solution[a][b]= -1;
                i++;
            }
        }
        return solution;
    }


}

