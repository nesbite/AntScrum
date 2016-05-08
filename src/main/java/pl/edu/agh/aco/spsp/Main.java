package pl.edu.agh.aco.spsp;

import javafx.stage.Stage;
import pl.edu.agh.aco.spsp.config.ProblemConfiguration;
import pl.edu.agh.aco.spsp.view.DrawChart;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {

        String fileName;
        File folder = new File(ProblemConfiguration.DATASET_DIR);
        File[] listOfFiles = folder.listFiles();
        for (int f = 0; f < listOfFiles.length; f++) {
            if (listOfFiles[f].isFile()) {
                fileName = listOfFiles[f].getName().substring(0, listOfFiles[f].getName().length() - 4);
                System.out.printf(fileName);
            } else {
                continue;
            }
            for(int i=0;i<5;i++){
                new ACOFlowShop(fileName).generateResult();
            }

        }


    }
}
