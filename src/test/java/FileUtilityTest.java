import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import utilities.FileUtility;
import utilities.PropertyUtility;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class FileUtilityTest {

    private Logger logger = LogManager.getLogger(FileUtilityTest.class);
    final Path testBedPath;

    public FileUtilityTest() {
        testBedPath = Paths.get(PropertyUtility.getProperty("common.dir.temp")).resolve("FUT").toAbsolutePath();
    }

    public void createTestBed() {
        try {
            Files.createDirectories(testBedPath);
        } catch (IOException e) {
            logger.error(e);
        }
        // Create 2 folders with a txt file in each with the same name of the folder - folders1 to folders2
        // Create 2 folders with a txt file in each with the same name of the folder - folder1 to folder2
        Arrays.asList("folder", "folders")
                .stream()
                .forEach(baseName ->
                        IntStream.range(1,3)
                                .forEach(i -> {
                                    try {
                                        Files.createDirectory(testBedPath.resolve(baseName+i));
                                        Files.createFile(testBedPath.resolve(baseName+i).resolve(baseName+i+".txt"));
                                    } catch (IOException e) {
                                        logger.error(e);
                                    }
                                })
                );

        // Create 3 folders with a txt file in each with the same name of the folder - fold3 to fold5
        IntStream.range(3,6)
                .forEach(i -> {
                    try {
                        Files.createDirectory(testBedPath.resolve("fold"+i));
                        Files.createFile(testBedPath.resolve("fold"+i).resolve("fold"+i+".txt"));
                    } catch (IOException e) {
                        logger.error(e);
                    }
                });
    }

    public void clearTestBed() {
        try {
            Runtime.getRuntime()
                    .exec(String.format("rm -rf %s", testBedPath.toString()));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @Test
    public void testCreateBatchFolders() {
        String baseName = "batchFolder";
        try {
            createTestBed();
            FileUtility.createFolders(testBedPath, baseName, false, 100);
            // Verify that all 100 folders have been created
            IntStream.range(1, 101)
                    .forEach(i -> {
                                // Verify that each folder has been created
                                Assertions.assertTrue(Files.exists(testBedPath.resolve(baseName.concat(Integer.toString(i)))));
                                // Delete the folder
                                //FileUtility.deleteRecursively(TestResources.TMP_PATH.resolve(folderName));
                            }
                    );
            // Generate a list of folder names starting with "folder"
            List<String> folderPaths = Files.list(Paths.get(PropertyUtility.getProperty("common.dir.temp")))
                    .parallel()
                    .filter(folderPath -> folderPath.getFileName().toString().startsWith(baseName))
                    .map(folderPath -> folderPath.getFileName().toString())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, folderPaths.size());
        } catch (IOException e) {
            logger.error(e);
        }
        finally {
            clearTestBed();
        }
    }

    @Test
    @Disabled
    public void testDeleteRecursively() {
        boolean folderDeleted = false;
        try {
            createTestBed();
            Path path = testBedPath.resolve("folder1");
            Files.createDirectory(path.resolve("child_folder"));
            FileUtility.deleteRecursively(path);
            folderDeleted = !Files.exists(path);
        } catch (Exception e) {
            logger.error(e);
        }
        finally {
            clearTestBed();
        }
        Assertions.assertTrue(folderDeleted);
    }

    @Test
    @Disabled
    public void testListMatches() {
        try {
            createTestBed();

            /*
                Try to listMatch with the following regexs
                    folder.*
                    folder\d+
                    fold.*\d+
                    fold\d+
                    fold.*[3-4]
                    .*old.*
                    abc.*
             */

            // Map mapping listMatches input to expected output
            HashMap<String, List<String>> opMap = new HashMap<>();
            opMap.put("folder.*", Arrays.asList("folder1", "folder2", "folders1", "folders2"));
            opMap.put("folder\\d+", Arrays.asList("folder1", "folder2"));
            opMap.put("fold.*\\d+", Arrays.asList("folder1", "folder2", "folders1", "folders2", "fold3", "fold4",
                    "fold5"));
            opMap.put("fold\\d+", Arrays.asList("fold3", "fold4", "fold5"));
            opMap.put("fold.*[3-4]", Arrays.asList("fold3", "fold4"));
            opMap.put(".*old.*", Arrays.asList("folder1", "folder2", "folders1", "folders2", "fold3", "fold4",
                    "fold5"));
            opMap.put("abc.*", Arrays.asList());
            for (String regex : opMap.keySet()) {
                List<String> actualOp = FileUtility.listMatches(testBedPath, regex)
                        .stream()
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                Assertions.assertEquals(new TreeSet(opMap.get(regex)), new TreeSet(actualOp), regex);
            }
        }
        catch(Exception e) {
            logger.error(e);
        }
        finally {
            // Cleanup files
            clearTestBed();
        }
    }

    public void testTreeMatches() {
                /*
        Try to listMatch with the following regexs
            folder.*
            folder\d+
            fold.*\d+
            fold\d+
            fold.*[3-5]
            abc
            abc.*
        */
    }

    public void testDeleteIfMatches() {
                        /*
        Try to DeleteIfMatch with the following regexs, between each regex delete all remnant and recreate all folders/
        files
            abc
            abc.*
            folder.*
            folder\d+
            fold.*\d+
            fold\d+
            fold.*[3-5]
         */
    }

    public void testSetPermissionsIfMatches() {
                       /*
        Try to setPermissionsIfMatches followed by DeleteIfMatch with the following regexs, between each regex delete
        all remnant and recreate all folders/files
            abc
            abc.*
            folder.*
            folder\d+
            fold.*\d+
            fold\d+
            fold.*[3-5]
         */
    }

    public void testSetAttributeIfMatches() {
                               /*
        Try to setAttributeIfMatches and verify with the following regexs, between each regex delete
        all remnant and recreate all folders/files
            abc
            abc.*
            folder.*
            folder\d+
            fold.*\d+
            fold\d+
            fold.*[3-5]
         */
    }

    @Test
    @Disabled
    public void testSetPermissions() throws IOException {
        createTestBed();
        Path folderPath = testBedPath.resolve("folder1");
        Path filePath = folderPath.resolve("1.txt");
        boolean actualResult = Boolean.FALSE;
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(folderPath);
            permissions.remove(PosixFilePermission.OWNER_WRITE);
            FileUtility.setPermissions(folderPath, permissions, false);
            Files.createFile(filePath);
        } catch (AccessDeniedException e) {
            actualResult = Boolean.TRUE;
        } catch (IOException e) {
            logger.error(e);
        } finally {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(folderPath);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            FileUtility.setPermissions(folderPath, permissions, false);

            FileUtility.deleteRecursively(folderPath);
            Assertions.assertTrue(actualResult,
                    "Able to create "
                            .concat(filePath.toString())
                            .concat(". Failed to set READ ONLY permission to ".concat(folderPath.toString())));
            clearTestBed();
        }
    }
}
