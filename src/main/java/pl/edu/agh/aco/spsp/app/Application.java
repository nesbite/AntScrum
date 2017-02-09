package pl.edu.agh.aco.spsp.app;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pl.edu.agh.aco.spsp.algorithm.ACOScrum;

import java.io.File;

import static pl.edu.agh.aco.spsp.config.ProblemConfiguration.getConfiguration;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        String fileName;
        File folder = new File(getConfiguration().getProperty("dataSetDir"));
        File[] listOfFilesAndDirectories = folder.listFiles();
        if (listOfFilesAndDirectories != null) {
            for (File fileOrDirectory : listOfFilesAndDirectories) {
                if (!fileOrDirectory.isFile()) {
                    continue;
                }
                fileName = fileOrDirectory.getName().substring(0, fileOrDirectory.getName().length() - 4);
                logger.debug(fileOrDirectory.getName());
                new ACOScrum(fileName).generateResult();
            }
        }
    }
}
