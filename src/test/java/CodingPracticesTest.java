import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodingPracticesTest {

    private static final Logger logger = LogManager.getLogger(CodingPracticesTest.class);
    private static final String propertyFileName = "popper.properties";
    private static final Properties propertyFile = getPropertiesFile();
    private static final String basePathString = propertyFile.getProperty("popper.codingPractices.javaFilesBasePath");
    private static final List<Path> javaFilesPath = getJavaFilesPath();

    /**
     * Get all Java Files in basePathString.
     * @return the list of paths to the Java Files.
     */
    private static List<Path> getJavaFilesPath(){
        Path basePath = Paths.get(basePathString).toAbsolutePath();
        String javaFileExtensionPattern = propertyFile.getProperty("popper.codingPractices.javaFileExtension");
        try {
            return Files.walk(basePath)
                    .filter((path) ->{
                        Pattern pattern = Pattern.compile(javaFileExtensionPattern);
                        Matcher matcher = pattern.matcher(path.toString());
                        return matcher.find();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get properties file
     * @return properties file
     */
    private static Properties getPropertiesFile(){
        Properties propertyFile = new Properties();
        try(InputStream inputStream = CodingPracticesTest.class.getResourceAsStream(propertyFileName)) {
            propertyFile.load(inputStream);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        return propertyFile;
    }

    /**
     * Get a mapping of the lines where print statements were found for a specific Java file.
     * @param path the path of the Java file to look for print statements.
     * @return A map of the lines where print statements were found.
     * @throws IOException if there is an issue reading the file.
     */
    private static HashMap<Long, String> getStdOutErrLines(Path path) throws IOException {
        String printPattern = propertyFile.getProperty("popper.codingPractices.printPattern");
        Stream<String> lines = Files.lines(path);
        HashMap<Long, String> stdOutErrLines = new HashMap<>();
        AtomicLong lineNumber = new AtomicLong(0);
        lines.forEach(line-> {

            Matcher matcher = Pattern.compile(printPattern)
                    .matcher(line);
            if(matcher.find()) {
                stdOutErrLines.put(lineNumber.get(), line);
            }
            lineNumber.getAndIncrement();
        });

        lines.close();
        return stdOutErrLines;
    }

    /**
     * Test that all Java files in the project don't have print statements.
     */
    @Test
    void testJavaFilesForPrintStatements(){
        HashMap<String, HashMap<Long, String>> filesPrintStatements = new HashMap<>();
        javaFilesPath.forEach(javaFilePath -> {
                try {
                    HashMap<Long, String> foundPrintStatements = getStdOutErrLines(javaFilePath);
                    if (!foundPrintStatements.isEmpty()){
                        filesPrintStatements.put(
                                javaFilePath.toAbsolutePath()
                                        .toString(),
                                foundPrintStatements
                        );
                    }

                } catch (IOException e) {
                    logger.error(e);
                    throw new RuntimeException(e);
                }

            });
        String errorMessage = filesPrintStatements.entrySet()
                .stream()
                .map(filePrintStatements -> String.format("%s \n\n%s",
                        filePrintStatements.getKey(),
                        filePrintStatements.getValue()
                                .entrySet()
                                .stream()
                                .map(foundPrintStatement-> String.format("\tLine %5d | %-10s",
                                        foundPrintStatement.getKey(),
                                        foundPrintStatement.getValue()
                                ))
                                .collect(Collectors.joining("\n")
                                )
                        )
                )
                .collect(Collectors.joining("\n"));
        Assertions.assertTrue(
                filesPrintStatements.isEmpty(),
                String.format(
                        "The following files output to STDOUT/STDERR directly. Please change them to use the logger.\n%s",
                        errorMessage
                )
        );
    }
}
