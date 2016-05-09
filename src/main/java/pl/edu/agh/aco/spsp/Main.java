package pl.edu.agh.aco.spsp;

import pl.edu.agh.aco.spsp.config.ProblemConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws Exception {

//        createData();
        if(args.length != 5){
            System.out.println("Wrong number of arguments! Should be: <number_of_ants> <probability> <evaporation> <q> <max_iterations>");
            return;
        }
        ProblemConfiguration.NUMBER_OF_ANTS = new Integer(args[0]);
        ProblemConfiguration.PROBABILITY = new Double(args[1]);
        ProblemConfiguration.EVAPORATION = new Double(args[2]);
        ProblemConfiguration.Q = new Integer(args[3]);
        ProblemConfiguration.MAX_ITERATIONS = new Integer(args[4]);
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
            new ACOScrum(fileName).generateResult();

        }


    }

    private static void createData() throws IOException {
        int employees = 20;
        int tasks = 500;
        Random rand = new Random();
        FileWriter fw = new FileWriter("data/data/"+tasks+"x"+employees+".csv");
        BufferedWriter buf = new BufferedWriter(fw);
        buf.write(tasks+","+employees+"\n");
        for(int i =0;i<tasks;i++){
            int time = rand.nextInt(40)+1;
            String line ="";
            for(int j=0;j<employees;j++){
                line += time + ",";
            }
            line = line.substring(0, line.length()-1);
            buf.write(line + "\n");
        }
        buf.close();
    }

}
