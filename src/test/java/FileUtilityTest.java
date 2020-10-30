import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.FileUtility;
import utilities.I18NUtility;
import utilities.PropertyUtility;
import utilities.ShellUtility;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class FileUtilityTest {

    private Logger logger = LogManager.getLogger(FileUtilityTest.class);
    private static int thread_sleep = Integer.parseInt(
            PropertyUtility.getProperty("test.FileUtilityTest.action.perform.waittime.milliseconds")
    );
    private final Path testBedPath;
    private static final ConcurrentHashMap<String, Queue<WatchEvent.Kind<?>>> actualPathToEventKindMap = new ConcurrentHashMap<>();

    public FileUtilityTest() {
        testBedPath = Paths.get(PropertyUtility.getProperty("common.dir.temp")).resolve("FUT");
    }

    public void createTestBed() {
        clearTestBed();

        try {
            Files.createDirectory(testBedPath);
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

        /* Create 3 folders (fold3 to fold5) with a txt file in each with the same name of the folder and another
        child folder and grandchild folder. */
        IntStream.range(3,6)
                .forEach(i -> {
                    try {
                        Path folderToCreate = testBedPath.resolve("fold" + i);
                        Path grandChildFolderToCreate = folderToCreate.resolve("fold" + i + "_child")
                                .resolve("fold" + i+ "_grandchild");
                        Files.createDirectory(folderToCreate);
                        Files.createFile(folderToCreate.resolve("fold" + i + ".txt"));
                        Files.createDirectories(grandChildFolderToCreate);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                });

        // Create folderToDelete and folders2/fileToDelete.txt
        try {
            Files.createDirectory(testBedPath.resolve("folderToDelete"));
            Files.createFile(testBedPath.resolve("folders2").resolve("fileToDelete.txt"));
        } catch (IOException e) {
            logger.error(e);
        }

    }

    public void clearTestBed() {
        try {
            try {
                Path testBedAbsolutePath = testBedPath.toAbsolutePath();
                ShellUtility.Command command = new ShellUtility.Command();
                command.setCommand(
                        ShellUtility.TypeOfShell.POWERSHELL,
                        String.format(
                                PropertyUtility.getProperty("test.FileUtilityTest.command.powershell.remove.directory.recursively"),
                                testBedAbsolutePath.toString()
                        )
                );
                command.setCommand(
                        ShellUtility.TypeOfShell.BASH,
                        String.format(
                                PropertyUtility.getProperty("test.FileUtilityTest.command.bash.remove.directory.recursively"),
                                testBedAbsolutePath.toString()
                        )
                );

                ShellUtility.executeCommand(
                        command,
                        Duration.ofSeconds(
                                Integer.parseInt(
                                    PropertyUtility.getProperty("test.FileUtilityTest.shell.command.timeout.seconds")
                                )
                        )
                );
            } catch (InterruptedException
                    | TimeoutException
                    | ShellUtility.ShellNotFoundException
                    | ShellUtility.OsNotFoundException e) {
                logger.error(e);
            }
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

    /**
     * Test if isSubPath checks that a path is a sub path of another.
     * @param parentPathString Path to check for sub paths
     * @param pathToCheckString Path to check it is sub path of parentPathsString
     * @param expectedResult The expected result
     */
    @ParameterizedTest
    @CsvSource(
        {
            "fold3, fold4, false", // Two subdirectories of /tmp/FUT
            "fold3, fold3/fold3_child, true", // Subdirectory of fold3
            "fold4/fold4_child, fold4/fold4_child, true", // Same directory
            "fold5/fold5_child, fold5/fold5_child/fold5_grandchild, true", // Subdirectory of fold5/fold5_child
            "fold5/fold5_child, fold4/fold4_child/fold4_grandchild, false", // Different subdirectory of fold4/fold4_child
            "fold5/fold5_child, fold5/fold5.txt, false" // Comparing file in main directory
        }
    )
    public void testIsSubPath(String parentPathString, String pathToCheckString, boolean expectedResult) {
        createTestBed();
        Path parentPath = testBedPath.resolve(parentPathString);
        Path pathToCheck = testBedPath.resolve(pathToCheckString);
        boolean actualResult = FileUtility.isSubPath(parentPath, pathToCheck);
        Assertions.assertEquals(expectedResult, actualResult);
        clearTestBed();
    }

    /**
     * Test isSubPath method with invalid inputs
     */
    @Test
    public void testIsSubPathWithInvalidInputs() {
        createTestBed();

        // Null parent path
        Path testPath = testBedPath.resolve("folder1");
        Assertions.assertThrows(
            NullPointerException.class,
            () -> FileUtility.isSubPath(null, testPath)
        );

        // Null potential sub path
        Assertions.assertThrows(
            NullPointerException.class,
            () -> FileUtility.isSubPath(testPath, null)
        );

        // Null parent path and potential sub path
        Assertions.assertThrows(
            NullPointerException.class,
            () -> FileUtility.isSubPath(null, null)
        );

        // File as parent path
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> FileUtility.isSubPath(testPath.resolve("folders1.txt"), testPath)
        );
        clearTestBed();
    }

    /**
     * Test getClosestExistingParent with invalid inputs.
     */
    @Test
    public void testGetClosestExistingParentWithInvalidInputs() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> FileUtility.getClosestExistingParent(null)
        );
    }

    /**
     * Test get closest existing parent from a specific directory or file path.
     * @param path Path to get the closest existing parent from
     * @param expectedClosestExistingParentPath Expected parent path of the provided path
     */
    @ParameterizedTest
    @CsvSource(
            {
                "fold4, fold4", // existing directory
                "fold4/sdsds/, fold4/", // non existing directory
                "fold1, .", // current directory
                "../../../../../.., /", // root directory
                "../lol, .." // parent directory
            }
    )
    public void testGetClosestExistingParent(String path, String expectedClosestExistingParentPath) {
        createTestBed();
        Path pathToFindClosestExistingParent = testBedPath.resolve(path);
        Path expectedClosestExistingParent = testBedPath.resolve(expectedClosestExistingParentPath)
                .toAbsolutePath()
                .normalize();
        Path actualClosestExistingParent = FileUtility.getClosestExistingParent(pathToFindClosestExistingParent)
                .toAbsolutePath()
                .normalize();

        Assertions.assertEquals(expectedClosestExistingParent.toString(), actualClosestExistingParent.toString());
        clearTestBed();
    }

    /**
     * Test registeringPath with invalid input.
     */
    @Test
    public void testRegisteringPathWithInputValidation() {
        createTestBed();
        // Individual null inputs
        Assertions.assertThrows(
            NullPointerException.class,
            () -> {
                FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                    null,
                    1,
                    (path, watchEvent) -> {},
                    StandardWatchEventKinds.ENTRY_CREATE
                );
            }
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath,
                            -1,
                            (path, watchEvent) -> {},
                            StandardWatchEventKinds.ENTRY_CREATE
                    );
                }
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath,
                            1,
                            null,
                            StandardWatchEventKinds.ENTRY_CREATE
                    );
                }
        );
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath,
                            1,
                            (path, watchEvent) -> {},
                            null
                    );
                }
        );

        // All null inputs
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            null,
                            -1,
                            null,
                            null
                    );
                }
        );

        // Non existing registration path
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath.resolve("wasntme"),
                            1,
                            (path, watchEvent) -> {},
                            StandardWatchEventKinds.ENTRY_CREATE
                    );
                }
        );

        // Path is not directory
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath.resolve("fold4")
                                    .resolve("fold4.txt"),
                            1,
                            (path, watchEvent) -> {},
                            StandardWatchEventKinds.ENTRY_CREATE
                    );
                }
        );

        // Empty WatchEventKind
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath,
                            1,
                            (path, watchEvent) -> {},
                            new WatchEvent.Kind[]{}
                    );
                }
        );

        // More than four WatchEventKind
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                            testBedPath,
                            1,
                            (path, watchEvent) -> {},
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_CREATE
                    );
                }
        );
        clearTestBed();
    }

    /**
     * Test deRegisteringPaths with invalid inputs.
     */
    @Test
    public void testDeregisterWithInputValidation() {
        createTestBed();
        // Null path
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    FileUtility.deRegisterWatchServiceForDirectory(null);
                }
        );

        // Existing file
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.deRegisterWatchServiceForDirectory(testBedPath.resolve("fold4").resolve("fold4.txt"));
                }
        );

        // Non-existing path
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    FileUtility.deRegisterWatchServiceForDirectory(testBedPath.resolve("wasntme.txt"));
                }
        );
        clearTestBed();
    }

    /**
     * Test registering paths with service events.
     * @param relativePath Relative path of the file or directory to modify
     * @param action Action to perform on the relative path provided
     * @param maxDepth Maximum children depth from specified path to register and trigger events
     * @throws IOException Thrown if registering a WatchService or walking through a path  or modifying it fails.
     * @throws InterruptedException Thrown if the current is interrupted
     */
    @ParameterizedTest
    @CsvSource(
        {
            "fold4/fold4_child/fold4_grandchild, FOLDER_MODIFY, 2", // Modify subdirectory of /fold4 at second level
            "fold4, FOLDER_MODIFY, 2", // Modify existing subdirectory of /tmp/FUT
            "createdFolder/1/2/3/4/5, FOLDER_CREATE, 6", // Create subdirectories of /tmp/FUT with a depth value equal to the number of directories created
            "createdFolder/1/2/3/4/5, FOLDER_CREATE, 3", // Create subdirectories of /tmp/FUT with a depth value less than the number of directories created
            "createdFolder/1/2.txt, FILE_CREATE, 1", // Create file in a subdirectory of /tmp/FUT that doesn't exist and low max depth
            "folderToDelete, FOLDER_DELETE, 2", // Delete existing subdirectory of /tmp/FUT
            "fold4/fold4_child/fold4_grandchild, FOLDER_DELETE, 3", // Delete subdirectory of a specific subdirectory of /tmp/FUT with a depth level to catch event
            "fold4/fold4.txt, FILE_MODIFY, 1", // Modify file in subdirectory of /tmp/FUT
            "folders2/fileToDelete.txt, FILE_DELETE, 1" // Delete existing file in subdirectory of /tmp/FUT
        }
    )
    public void testFolderWatchServiceRegistering(String relativePath, String action, int maxDepth) throws IOException, InterruptedException {
        createTestBed();
        final HashMap<String, Queue<WatchEvent.Kind<?>>> expectedPathToEventKindMap;
        actualPathToEventKindMap.clear();

        Path pathToRegister = testBedPath.resolve(relativePath);
        FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                testBedPath,
                maxDepth,
                (path, watchEvent) -> {
                    String absolutePathString = path.toAbsolutePath().toString();
                    actualPathToEventKindMap.putIfAbsent(absolutePathString, new LinkedList<>());
                    actualPathToEventKindMap.get(absolutePathString)
                            .add(watchEvent.kind());

                    logger.info(
                            String.format(
                                    I18NUtility.getString("test.FileUtilityTest.loggedConsumerEventMessage"),
                                    Instant.now(),
                                    watchEvent.kind(),
                                    path
                            )
                    );
                },
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW
        );

        Function<Path, Queue<WatchEvent.Kind<?>>> expectedEventsOracle = null;
        switch (action) {
            case "FOLDER_CREATE" -> {
                expectedEventsOracle = path -> {
                    Queue<WatchEvent.Kind<?>> expectedEvents = new LinkedList<>();
                    expectedEvents.add(StandardWatchEventKinds.ENTRY_CREATE);
                    if (!Files.isDirectory(path)) {
                        expectedEvents.add(StandardWatchEventKinds.ENTRY_MODIFY);
                    }
                    return expectedEvents;
                };
                expectedPathToEventKindMap = getExpectedEvents(pathToRegister, expectedEventsOracle, maxDepth);
                createDirectoriesRecursively(
                        pathToRegister,
                        Integer.parseInt(
                                PropertyUtility.getProperty("test.FileUtilityTest.create.directories.recursively.waittime.milliseconds")
                        )
                );
            }
            case "FILE_CREATE" -> {
                expectedEventsOracle = path -> {
                    Queue<WatchEvent.Kind<?>> expectedEvents = new LinkedList<>();
                    expectedEvents.add(StandardWatchEventKinds.ENTRY_CREATE);
                    if (!Files.isDirectory(path)) {
                        expectedEvents.add(StandardWatchEventKinds.ENTRY_MODIFY);
                    }
                    return expectedEvents;
                };
                expectedPathToEventKindMap = getExpectedEvents(pathToRegister, expectedEventsOracle, maxDepth);
                createDirectoriesRecursively(
                        pathToRegister.getParent(),
                        Integer.parseInt(
                            PropertyUtility.getProperty("test.FileUtilityTest.create.directories.recursively.waittime.milliseconds")
                        )
                );
                Files.createFile(pathToRegister);
            }
            case "FOLDER_MODIFY", "FILE_MODIFY" -> {
                expectedEventsOracle = path -> {
                    Queue<WatchEvent.Kind<?>> expectedEvents = new LinkedList<>();
                    expectedEvents.add(StandardWatchEventKinds.ENTRY_MODIFY);
                    return expectedEvents;
                };
                expectedPathToEventKindMap = getExpectedEvents(pathToRegister, expectedEventsOracle, maxDepth);
                Files.setLastModifiedTime(pathToRegister, FileTime.from(Instant.now()));
            }
            case "FOLDER_DELETE", "FILE_DELETE" -> {
                expectedEventsOracle = path -> {
                    Queue<WatchEvent.Kind<?>> expectedEvents = new LinkedList<>();
                    expectedEvents.add(StandardWatchEventKinds.ENTRY_DELETE);
                    return expectedEvents;
                };
                expectedPathToEventKindMap = getExpectedEvents(pathToRegister, expectedEventsOracle, maxDepth);
                FileUtility.deleteRecursively(pathToRegister);
            }
            default -> throw new IllegalArgumentException(
                    String.format(
                            I18NUtility.getString("input.validation.invalidActionOrArgumentMessage"),
                            action
                    )
            );
        }

        logger.info(
                String.format(
                        I18NUtility.getString("test.FileUtilityTest.waitingForFileChangesMessage"),
                        thread_sleep/1000
                )
        );
        Thread.sleep(thread_sleep);
        HashMap<String, Queue<WatchEvent.Kind<?>>> actualPathToEventKindMapCopy = new HashMap<>(actualPathToEventKindMap);
        Set<String> expectedPathsToMonitor = expectedPathToEventKindMap.keySet();
        Set<String> actualPathsToMonitor = actualPathToEventKindMapCopy.keySet();

        FileUtility.deRegisterWatchServiceForDirectory(testBedPath);
        FileUtility.clearAllWatchServiceEvents();
        clearTestBed();

        Assertions.assertEquals(expectedPathsToMonitor, actualPathsToMonitor);
        expectedPathsToMonitor.forEach(expectedPath -> {
            WatchEvent.Kind<?>[] actualEvents = new WatchEvent.Kind<?>[actualPathToEventKindMapCopy.get(expectedPath).size()];
            actualPathToEventKindMapCopy.get(expectedPath)
                    .toArray(actualEvents);
            WatchEvent.Kind<?>[] expectedEvents = new WatchEvent.Kind<?>[actualPathToEventKindMapCopy.get(expectedPath).size()];
            actualPathToEventKindMapCopy.get(expectedPath)
                    .toArray(expectedEvents);
            Assertions.assertArrayEquals(expectedEvents,
                    actualEvents,
                    String.format(
                            I18NUtility.getString("test.FileUtilityTest.noMatchingSetOfEventsErrorMessage"),
                            expectedPath
                    )
            );
        });
    }

    /**
     * Test unregistering a directory.
     * @param pathToRegisterString Path to register
     * @param pathToDeRegisterString Sub path of the pathToRegisterString
     * @param maxDepth  Maximum children depth from specified path to register and trigger events
     * @throws IOException Thrown if registering a WatchService or walking through a path  or modifying it fails
     * @throws InterruptedException Thrown if the current thread is interrupted
     */
    @ParameterizedTest
    @CsvSource(
        {
            "fold4, fold4/fold4_child, 3", // existing directory to register and path to deregister
            ", fold3, 3" // null with existing directory
        }
    )
    public void testFolderWatchServiceDeRegistering(String pathToRegisterString, String pathToDeRegisterString, int maxDepth) throws IOException, InterruptedException {
        // Test set up
        createTestBed();

        if(pathToRegisterString != null) {
            Path pathToRegister = testBedPath.resolve(pathToRegisterString);
            actualPathToEventKindMap.clear();
            FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                    pathToRegister,
                    maxDepth,
                    (path, watchEvent) -> {
                        logger.info(
                                String.format(
                                    I18NUtility.getString("test.FileUtilityTest.loggedConsumerEventMessage"),
                                    Instant.now(),
                                    watchEvent.kind(),
                                    path
                                )
                        );
                        String absolutePathString = path.toAbsolutePath().toString();
                        actualPathToEventKindMap.putIfAbsent(absolutePathString, new LinkedList<>());
                        actualPathToEventKindMap.get(absolutePathString)
                                .add(watchEvent.kind());

                    },
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.OVERFLOW
            );

            // Perform actions to trigger the events
            Path createdFilePath = pathToRegister.resolve("parent_random.txt");
            Files.createFile(createdFilePath);
            Thread.sleep(thread_sleep);

            Queue<WatchEvent.Kind<?>> eventsForRegisteredPath = actualPathToEventKindMap.get(
                    createdFilePath.toAbsolutePath()
                            .toString()
            );
            // Assert actions have been logged
            Assertions.assertNotNull(eventsForRegisteredPath);
            Assertions.assertTrue(eventsForRegisteredPath.contains(StandardWatchEventKinds.ENTRY_CREATE));
        }

        // Deregister sub path
        Path pathToDeRegister = testBedPath.resolve(pathToDeRegisterString);
        FileUtility.deRegisterWatchServiceForDirectory(pathToDeRegister);
        // Perform actions in sub path
        Files.createFile(pathToDeRegister.resolve("random.txt"));
        Thread.sleep(thread_sleep);
        Queue<WatchEvent.Kind<?>> eventsForDeRegisteredPath = actualPathToEventKindMap.get(
                pathToDeRegister.toAbsolutePath()
                        .toString()
        );
        // Assert actions have not been logged
        Assertions.assertNull(eventsForDeRegisteredPath);
        clearTestBed();
    }

    /**
     * Test register events with only one type of event.
     * @param eventToRegisterString Event to register
     * @throws IOException Thrown if registering a WatchService or walking through a path  or modifying it fails
     * @throws InterruptedException Thrown if the current thread is interrupted
     */
    @ParameterizedTest
    @CsvSource(
        {
            "ENTRY_CREATE", // Create event
            "ENTRY_MODIFY", // Modify event
            "ENTRY_DELETE", // Delete event
        }
    )
    public void testSelectiveRegisteringWatchService(String eventToRegisterString) throws IOException, InterruptedException {
        actualPathToEventKindMap.clear();
        createTestBed();

        WatchEvent.Kind<?>  eventToRegister = switch(eventToRegisterString){
            case("ENTRY_CREATE") ->  StandardWatchEventKinds.ENTRY_CREATE;
            case("ENTRY_MODIFY") -> StandardWatchEventKinds.ENTRY_MODIFY;
            case("ENTRY_DELETE") -> StandardWatchEventKinds.ENTRY_DELETE;
            default -> throw new IllegalArgumentException(
                    String.format(
                            I18NUtility.getString("input.validation.invalidActionOrArgumentMessage"),
                            "eventToRegisterString",
                            eventToRegisterString
                    )
            );
        };

        FileUtility.registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
                testBedPath,
                3,
                (path, watchEvent) -> {
                    logger.info(
                            String.format(
                                    I18NUtility.getString("test.FileUtilityTest.loggedConsumerEventMessage"),
                                    Instant.now(),
                                    watchEvent.kind(),
                                    path
                            )
                    );
                    String absolutePathString = path.toAbsolutePath().toString();
                    actualPathToEventKindMap.putIfAbsent(absolutePathString, new LinkedList<>());
                    actualPathToEventKindMap.get(absolutePathString)
                            .add(watchEvent.kind());
                },
                eventToRegister
        );
        // Perform the actions to trigger the events
        Thread.sleep(thread_sleep);
        Path fileToCreatePath = testBedPath.resolve("selectiveRegistering.txt");
        Files.createFile(fileToCreatePath);
        Files.setLastModifiedTime(fileToCreatePath, FileTime.from(Instant.now()));
        Files.delete(fileToCreatePath);
        Thread.sleep(thread_sleep);

        String fileToCreatePathString = fileToCreatePath.toAbsolutePath().toString();
        HashMap<String, Queue<WatchEvent.Kind<?>>> actualPathToEventKindMapCopy = new HashMap<>(actualPathToEventKindMap);
        Queue<WatchEvent.Kind<?>> generatedEventsQueue = actualPathToEventKindMapCopy.get(fileToCreatePathString);
        // Assert that only the provided event has been logged
        Assertions.assertNotNull(
                generatedEventsQueue,
                String.format(
                        I18NUtility.getString("test.FileUtilityTest.noEventsGeneratedForActionMessage"),
                        fileToCreatePathString
                )
        );
        Assertions.assertEquals(generatedEventsQueue.size(),1);
        Assertions.assertEquals(eventToRegister, generatedEventsQueue.remove());

        FileUtility.deRegisterWatchServiceForDirectory(testBedPath);
        clearTestBed();
    }

    /**
     * Get the expected logged events from the provided path, maxDepth and the types of events
     * @param path Registered path to provide the events for
     * @param expectedEventsOracle Function to get the expected events to register
     * @param maxDepth Maximum children depth from specified path to register and trigger events
     * @return Hashmap of the paths with their respective expected logged events
     */
    private static HashMap<String, Queue<WatchEvent.Kind<?>>> getExpectedEvents(Path path, Function<Path, Queue<WatchEvent.Kind<?>>> expectedEventsOracle, int maxDepth){
        HashMap<String, Queue<WatchEvent.Kind<?>>> expectedEvents = new HashMap<>();
        Path absolutePath = path.toAbsolutePath();
        Path closestExistingParentPathForRegisteredPath = FileUtility.getClosestExistingParent(absolutePath);
        Path relativePathToBeModified = closestExistingParentPathForRegisteredPath.relativize(absolutePath);
        int nameCount = relativePathToBeModified.getNameCount();
        int nameCountToUse = Math.min(nameCount, maxDepth + 1);
        IntStream.range(1, nameCountToUse + 1)
                .mapToObj(subPathEndIndex -> relativePathToBeModified.subpath(0, subPathEndIndex))
                .map(closestExistingParentPathForRegisteredPath::resolve)
                .forEach(subPath -> {
                        Queue<WatchEvent.Kind<?>> subPathExpectedEvents = expectedEventsOracle.apply(subPath);
                        expectedEvents.put(subPath.toAbsolutePath().toString(), subPathExpectedEvents);
                    }
                );
        return expectedEvents;
    }

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * @param path Path of the directory to create
     * @param delayInMSBetweenDirectoryCreation Time to delay between each directory creation in milliseconds
     */
    private void createDirectoriesRecursively(Path path, long delayInMSBetweenDirectoryCreation) {
        // Input validation
        Objects.requireNonNull(path);
        if(Files.exists(path) && !Files.isDirectory(path)) {
            throw new IllegalArgumentException(
                    String.format(
                            I18NUtility.getString("utilities.FileUtility.invalidDirPathMessage"),
                            path
                    )
            );
        }

        Path absoluteRPath = testBedPath.toAbsolutePath()
                .relativize(path.toAbsolutePath());
        int nameCount = absoluteRPath.getNameCount();
        IntStream.range(1, nameCount + 1)
                .mapToObj(index -> absoluteRPath.subpath(0, index))
                .forEach(relativePathToCreate -> {
                    try {
                        Path pathToCreate = testBedPath.resolve(relativePathToCreate);
                        if (!Files.exists(pathToCreate)) {
                            Files.createDirectory(pathToCreate);
                            Thread.sleep(delayInMSBetweenDirectoryCreation);
                        }
                    } catch (IOException | InterruptedException e) {
                        logger.error(e);
                    }
                });
    }
}
