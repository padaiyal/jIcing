import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.ArchiveUtility;
import utilities.FileUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArchiveUtilityTest {
    private static Logger logger = LogManager.getLogger(ArchiveUtilityTest.class);
    private static Path testBedPath;
    private static Path sampleDirPath;

    @BeforeAll
    static void createTestBed() throws IOException {
        testBedPath = Files.createTempDirectory("ArchiveUtilityTest");
        logger.debug("Created test bed - " + testBedPath);

        sampleDirPath = Files.createDirectory(testBedPath.resolve("parentDir"));
        logger.debug("Created sample dir - " + sampleDirPath);

        Path childDir1 = Files.createDirectory(sampleDirPath.resolve("childDir1"));
        logger.debug("Created directory - " + childDir1);
        Path aTxt = Files.createFile(childDir1.resolve("a.txt"));
        logger.debug("Created file - " + aTxt);

        Path childDir2 = Files.createDirectory(sampleDirPath.resolve("childDir2"));
        logger.debug("Created directory - " + childDir2);
        Path bTxt = Files.createFile(childDir2.resolve("b.txt"));
        logger.debug("Created file - " + bTxt);

        Path childDir3 = Files.createDirectory(sampleDirPath.resolve("childDir3"));
        logger.debug("Created directory - " + childDir3);
    }

    @ParameterizedTest
    @CsvSource(
        {
            "ZIP,abc.zip,",
            "ZIP,abc_enc.zip,Test123"
        }
    )
    void testCreateArchive(ArchiveUtility.ArchiveFileFormat archiveFileFormat, String outputArchiveName, String password) throws IOException {
        ArchiveUtility.createArchive(
            sampleDirPath,
            testBedPath.resolve(outputArchiveName),
            archiveFileFormat,
            password
        );
    }

    @AfterAll
    static void clearTestBed() {
//        FileUtility.deleteRecursively(testBedPath);
    }
}
