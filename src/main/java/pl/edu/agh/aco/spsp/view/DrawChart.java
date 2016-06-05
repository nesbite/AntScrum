package pl.edu.agh.aco.spsp.view;


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
import pl.edu.agh.aco.spsp.ACOScrum;
import pl.edu.agh.aco.spsp.Main;
import pl.edu.agh.aco.spsp.config.ProblemConfiguration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DrawChart extends Application {


    final CategoryAxis yAxis = new CategoryAxis();
    final NumberAxis xAxis = new NumberAxis();
    StackedBarChart<Number, String> sbc;

    private double[][] graph;
    private int[][] solution;
    private int maxNumberOfTasks;
    private int numberOfEmployees;
    public static String fileName;

//    @Override
    public void start(Stage stage) throws InterruptedException {

        Main.main(null);
        File folder = new File(ProblemConfiguration.SOLUTIONS_DIR);
//        File[] listOfFiles = folder.listFiles();
        File[] listOfFiles = new File[] {new File("data/solutions/solution/solution.csv")};
        for (int f = 0; f < listOfFiles.length; f++) {
            if (listOfFiles[f].isFile()) {
                fileName = listOfFiles[f].getName().substring(0, listOfFiles[f].getName().indexOf("."));
                System.out.printf(fileName);
            } else {
                continue;
            }

        try {
            graph = ACOScrum.getProblemGraphFromFile(ProblemConfiguration.DATASET_DIR /*+ fileName.substring(0,fileName.indexOf("("))*/ + "data.csv");
            solution = getSolutionFromFile(ProblemConfiguration.SOLUTIONS_DIR + fileName + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sbc = new StackedBarChart<>(xAxis, yAxis);
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
            ScrollPane pane = new ScrollPane();
            Scene scene = new Scene(pane, 800, 600);
            sbc.setMinHeight(800.0*(graph[0].length/30.0));
            pane.setContent(sbc);
            pane.setFitToWidth(true);
            pane.setFitToHeight(true);

        stage.setScene(scene);
        stage.show();

        saveSnapshot(sbc);
//        stage.close();
        }

    }

    public static Image createImage(Node node) {

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

        // save image !!! has bug because of transparency (use approach below) !!!
        // ImageIO.write(SwingFXUtils.fromFXImage( selectedImage.getImage(), null), "jpg", file);

        // save image (without alpha)
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            ImageIO.write(bufImageRGB, "jpg", new File(ProblemConfiguration.IMAGES_DIR + fileName +".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();

        System.out.println( "Image saved");

    }



    public int[][] getSolutionFromFile(String path) throws IOException {
        int[][] solution = null;
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i=0;
        while ((line = buf.readLine()) != null) {
            if(i>0) {

                String splitA[] = line.split(ProblemConfiguration.DELIMITER);
                int j = 0;
                for (String s : splitA) {
                    if (!s.isEmpty()) {
                        solution[i-1][j] = Integer.parseInt(s);
                    }
                    j++;
                }
                i++;
            } else {
                String firstLine[] = line.split(ProblemConfiguration.DELIMITER);
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

