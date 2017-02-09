package pl.edu.agh.aco.spsp.config;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProblemConfiguration {

    private static Properties configuration = loadProperties();

    private static Properties loadProperties(){
        Properties properties = new Properties();
        try {
            populatePropertiesWithData(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static void populatePropertiesWithData(Properties properties) throws IOException {
        InputStream propertiesFile = new FileInputStream("config.properties");
        properties.load(propertiesFile);
        propertiesFile.close();
    }

    public static Properties getConfiguration(){
        return configuration;
    }

    public static String getProperty(String property){
        return configuration.getProperty(property);
    }
}
