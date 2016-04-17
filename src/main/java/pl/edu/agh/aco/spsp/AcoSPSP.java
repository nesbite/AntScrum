package pl.edu.agh.aco.spsp;

import pl.edu.agh.aco.spsp.config.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class AcoSPSP {

    private static double[] graph;
    private static int numberOfEmployees;

    public static void main(String[] args) {
        System.out.println("ACO FOR FLOW SHOP SCHEDULLING");
        System.out.println("=============================");

        try {
            String dataPath = Config.DATA_PATH;
            System.out.println("Data file: " + dataPath);

            getProblemGraphFromFile(dataPath);
            ACOFlowShop acoFlowShop = new ACOFlowShop(graph, numberOfEmployees);
            System.out.println("Starting computation at: " + new Date());
            long startTime = System.nanoTime();
            acoFlowShop.solveProblem();
            long endTime = System.nanoTime();
            System.out.println("Finishing computation at: " + new Date());
            System.out.println("Duration (in seconds): "
                    + ((double) (endTime - startTime) / 1000000000.0));
            acoFlowShop.showSolution();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a text file and returns a problem matrix.
     *
     * @param path File to read.
     * @return Problem matrix.
     * @throws IOException
     */
    public static void getProblemGraphFromFile(String path)
            throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line = buf.readLine();

        String firstLine[] = line.split(" ");
        String numberOfJobs = firstLine[0];
        numberOfEmployees = Integer.parseInt(firstLine[1]);

        graph = new double[Integer.parseInt(numberOfJobs)];

        line = buf.readLine();
        String splitA[] = line.split(" ");

        for (int i = 0; i < splitA.length; i++) {
            graph[i] = Integer.parseInt(splitA[i]);
        }
    }
}
