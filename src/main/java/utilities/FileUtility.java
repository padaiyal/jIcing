package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileUtility {

    static class WatchServiceRegisteringFileVisitor extends SimpleFileVisitor<Path> {

        private long successfulFileVisitsCount = 0;
        private long successfulDirectoryVisitsCount = 0;
        private long failedVisitsCount = 0;
        private final Path basePath;
        private final WatchEvent.Kind<?>[] eventsToWatch;
        private final int maxDepth;

        /**
         * The WatchServiceRegisteringFileVisitor is a SimpleFileVisitor implementation that visits all children
         * files/folders of a specified path upto a specified max depth and registers them for a WatchService to generate
         * events for the specified watch event kinds.
         * @param basePath      The path to walk.
         * @param maxDepth      The maximum depth to walk.
         * @param eventsToWatch Events to register with the watch service.
         */
        public WatchServiceRegisteringFileVisitor(Path basePath, int maxDepth, WatchEvent.Kind<?> ... eventsToWatch) {
            this.basePath = basePath;
            this.eventsToWatch = eventsToWatch;
            this.maxDepth = maxDepth;
        }

        /**
         * Called when a file is visited.
         * @param childPath Path of the visited file.
         * @param attrs     Attributes of the visited file.
         * @return The result of the visit, indicates the next traversal action.
         */
        @Override
        public FileVisitResult visitFile(Path childPath, BasicFileAttributes attrs) {
            if(Files.isDirectory(childPath)) {
                int newMaxDepth = getNewMaxDepth(childPath);
                registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(childPath, newMaxDepth, eventsToWatch);
                successfulDirectoryVisitsCount++;
            }
            else {
                successfulFileVisitsCount++;
            }
            return FileVisitResult.CONTINUE;
        }

        /**
         * Called when a file visit fails.
         * @param childPath     Path of the file whose visit failed.
         * @param ioException   Exception thrown during file visit failure.
         * @return The result of the visit, indicates the next traversal action.
         */
        @Override
        public FileVisitResult visitFileFailed(Path childPath, IOException ioException) {
            failedVisitsCount++;
            return FileVisitResult.SKIP_SUBTREE;
        }

        /**
         * Gets the max depth for the specified path relative to the original max depth of the base path.
         * @param childPath Path for which the max depth needs to be computed.
         * @return New max depth of the input child path.
         */
        private int getNewMaxDepth(Path childPath){
            Path relativizedChildPath = basePath.relativize(childPath);
            int newMaxDepth = maxDepth;
            if(!relativizedChildPath.toString().equals("")) {
                newMaxDepth -= relativizedChildPath.getNameCount();
            }
            return newMaxDepth;
        }

        /**
         * Called after a directory is visited.
         * @param childPath     Path of the visited file.
         * @param ioException   Exception thrown during directory visit failure.
         * @return The result of the visit, indicates the next traversal action.
         */
        @Override
        public FileVisitResult postVisitDirectory(Path childPath, IOException ioException)
                    throws IOException {
            super.postVisitDirectory(childPath, ioException);

            int newMaxDepth = getNewMaxDepth(childPath);
            registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(childPath, newMaxDepth, eventsToWatch);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Returns the number of successful file visits so far by this visitor.
         * @return Number of successful file visits.
         */
        public long getSuccessfulFileVisitsCount() {
            return successfulFileVisitsCount;
        }

        /**
         * Returns the number of successful directory visits so far by this visitor.
         * @return Number of successful directory visits.
         */
        public long getSuccessfulDirectoryVisitsCount() {
            return successfulDirectoryVisitsCount;
        }

        /**
         * Returns the number of failed file visits so far by this visitor.
         * @return Number of failed file visits.
         */
        public long getFailedVisitsCount() {
            return failedVisitsCount;
        }

    }

    static class WatchServiceRegistrationInfo {

        private final Path path;
        private final WatchService watchService;
        private final int maxDepth;
        private final WatchEvent.Kind<?>[] eventsToWatch;

        /**
         * This class abstracts all the information needed to track watch service registrations.
         * @param path          Registered path.
         * @param watchService  Watch service with which the path has been registered.
         * @param maxDepth      Max directory depth to monitor for events.
         * @param eventsToWatch Events registered with the watch service.
         */
        public WatchServiceRegistrationInfo(Path path, WatchService watchService, int maxDepth,  WatchEvent.Kind<?> ... eventsToWatch){
            this.path = path;
            this.watchService = watchService;
            this.maxDepth = maxDepth;
            this.eventsToWatch = eventsToWatch;
        }

        /**
         * Returns the registered path.
         * @return Registered path.
         */
        @SuppressWarnings("unused")
        public Path getPath() {
            return path;
        }

        /**
         * Returns the watch service with which the path has been registered.
         * @return Watch service with which the path has been registered.
         */
        public WatchService getWatchService() {
            return watchService;
        }

        /**
         * The max directory depth to monitor for events.
         * @return Max directory depth to monitor for events.
         */
        public int getMaxDepth() {
            return maxDepth;
        }

        /**
         * The events registered with the watch service.
         * @return Events registered with the watch service.
         */
        @SuppressWarnings("unused")
        public WatchEvent.Kind<?>[] getEventsToWatch() {
            return eventsToWatch;
        }
    }

    private static final Logger logger = LogManager.getLogger(FileUtility.class);
    private static final ConcurrentHashMap<String, WatchServiceRegistrationInfo> pathToWatchServiceRegistrationInfoMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Queue<WatchEvent<?>>> watchServiceEventsMap = new ConcurrentHashMap<>();
    private static Thread backgroundWatchServiceRegisteringThread;

    /**
     * Creates a specific number of folders with the specified name and prefix/suffix
     *
     * @param path     The path where the folders have to be created
     * @param name     The base name of the folders to be created
     * @param isPrefix If true append the number to the prefix of the folder name, else append it as the suffix.
     * @param count    The number of folders to be created
     */
    public static void createFolders(Path path, String name, boolean isPrefix, int count) {
        IntStream.range(1, count + 1)
                .parallel()
                .forEach(x -> {
                            String folderName = isPrefix ? Integer.toString(x).concat(name) : name.concat(Integer.toString(x));
                            Path folderPath = path.resolve(folderName);
                            try {
                                Files.createDirectories(folderPath);
                            } catch (IOException e) {
                                logger.error(e);
                            }
                        }
                );
    }

    /**
     * Sets permissions to a specified file/folder and all its contents if needed
     *
     * @param path           The path of the folder/file whose permission needs to be changed
     * @param permissions    The new permission to set to the folder/file specified
     * @param setRecursively If true and a folder is specified, it sets the specified permissions to all the contents
     *                       in it, recursively.
     *                       If false and a folder is specified, It only sets the specified permission to the folder
     *                       specified and not any of its contents.
     *                       If a file is specified, irrespective, this parameter doesn't have any effect on it.
     */
    public static void setPermissions(Path path, Set<PosixFilePermission> permissions, boolean setRecursively) {
        try {
            if (setRecursively) {
                Files.walk(path)
                        .parallel()
                        .forEach(node -> {
                            try {
                                Files.setPosixFilePermissions(node, permissions);
                            } catch (IOException e) {
                                logger.error(e);
                            }
                        });
            }
            else
                Files.setPosixFilePermissions(path, permissions);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * Lists the immediate folder contents/file that match the specified pattern
     *
     * @param path          The path to find file name matches in.
     * @param patternString The pattern to match the file name with.
     * @return List of Path objects of the files/folders whose name matches the specified pattern.
     */
    public static List<Path> listMatches(Path path, String patternString) {
        return getContentMatches(path, patternString, false);
    }

    /**
     * Lists the recursive folder contents/file that match the specified pattern
     *
     * @param path          The path to recursively find file name matches in.
     * @param patternString The pattern to match the file name with.
     * @return List of Path objects of the files/folder contents whose name matches the specified pattern.
     */
    public static List<Path> treeMatches(Path path, String patternString) {
        return getContentMatches(path, patternString, true);
    }

    /**
     * Lists the recursive/immediate folder contents/file that match the specified pattern
     *
     * @param path          The path to recursively find file name matches in.
     * @param patternString The pattern to match the file name with.
     * @param searchTree    If a directory is specified and this parameter is true, it searches recursively, else it only
     *                      searches the immediate contents of the folder. This parameter is not applicable if a file path
     *                      is specified.
     * @return List of Path objects of the files/folder contents whose name matches the specified pattern.
     */
    private static List<Path> getContentMatches(Path path, String patternString, boolean searchTree) {
        List<Path> matchList = new ArrayList<>();
        Stream<Path> contents = null;
        try {
            contents = searchTree ? Files.walk(path) : Files.list(path);
        } catch (IOException e) {
            logger.error(e);
        }
        if (Files.isDirectory(path)) {
            matchList.addAll(
                    contents.parallel()
                            .filter(content -> RegexUtility.matches(patternString, 0, content.getFileName().toString()))
                            .collect(Collectors.toList())
            );
        } else if (RegexUtility.matches(patternString, 0, path.getFileName().toString()))
            matchList.add(path);
        return matchList;
    }

    /**
     * Deletes the specified folder/file
     *
     * @param path The folder/file to delete
     */
    public static void deleteRecursively(Path path) {
        try {
            if (!Files.isDirectory(path)) {
                Files.delete(path);
            } else {
                Stream<Path> dirTree = Files.list(path);
                dirTree.forEach(walkPath -> {
                    try {
                        if (Files.isDirectory(walkPath)) {
                            FileUtility.deleteRecursively(walkPath);
                        } else {
                            Files.delete(walkPath);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                dirTree.close();
                Files.delete(path);

            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * Deletes contents specified by the path input if the name matches the provided pattern
     *
     * @param path           The path to perform the matched deletion in.
     * @param pattern        The pattern to match the file/folder name with.
     * @param setRecursively If a directory is specified and this parameter is true, it matches recursively and deletes,
     *                       else it checks the specified folder/file name and deletes it if it matches the specified
     *                       pattern. This parameter is not applicable if a file path is specified.
     */
    public static void deleteIfMatches(Path path, String pattern, boolean setRecursively) {
        if (RegexUtility.matches(pattern, 0, path.getFileName().toString()))
            FileUtility.deleteRecursively(path);
        else
            FileUtility.treeMatches(path, pattern)
                    .stream()
                    .forEach(FileUtility::deleteRecursively);
    }

    /**
     * Changes the permission of the contents specified by the path input if the name matches the provided pattern
     *
     * @param path           The path to perform the matched permission change in.
     * @param permissions    The permissions to set the matched folders/files with
     * @param pattern        The pattern to match the file/folder name with.
     * @param setRecursively If a directory is specified and this parameter is true, it matches recursively and sets the
     *                       specified permissions, else it checks the specified folder/file name and sets the
     *                       permissions if it matches the specified pattern. This parameter is not applicable if a file
     *                       path is specified.
     */
    public static void setPermissionsIfMatches(Path path, Set<PosixFilePermission> permissions, String pattern, boolean setRecursively) {
        FileUtility.listMatches(path, pattern)
                .parallelStream()
                .forEach(content -> FileUtility.setPermissions(content, permissions, setRecursively));
    }

    /**
     * Sets the attribute of the contents specified by the path input if the name matches the provided pattern
     *
     * @param path           The path to set the attribute in.
     * @param attribute      The attribute to set the matched folders/files with
     * @param pattern        The pattern to match the file/folder name with.
     * @param setRecursively If a directory is specified and this parameter is true, it matches recursively and sets the
     *                       specified attribute, else it checks the specified folder/file name and sets the
     *                       attribute if it matches the specified pattern. This parameter is not applicable if a file
     *                       path is specified.
     * @throws IOException Thrown by Files.walk()
     */
    public static void setAttributeIfMatches(Path path, FileAttribute<?> attribute, String pattern, boolean setRecursively) throws IOException {
        Stream<Path> contents = setRecursively ? Files.walk(path) : Arrays.asList(path).stream();
        contents.parallel()
                .forEach(content -> {
                    try {
                        Files.setAttribute(content, attribute.name(), attribute.value());
                    } catch (IOException e) {
                        logger.error(e);
                    }
                });
    }

    /**
     * Checks if one path is a sub path of the other
     * @param parentPath        Path to check for sub paths
     * @param potentialSubPath  Path to check if it's a sub path of the parentPath path
     * @return true if potentialSubPath is a sub path of parentPath
     */
    public static boolean isSubPath(Path parentPath, Path potentialSubPath) {
        // Input validation
        Objects.requireNonNull(parentPath);
        Objects.requireNonNull(potentialSubPath);
        if(!Files.isDirectory(parentPath)) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.parentPathNotADirectory",
                    parentPath
                )
            );
        }

        parentPath = parentPath.toAbsolutePath();
        potentialSubPath = potentialSubPath.toAbsolutePath();

        if(parentPath.equals(potentialSubPath)) {
            return true;
        }

        String parentPathString = parentPath.toString() + File.separator;
        String potentialSubPathString = potentialSubPath.toString();

        return potentialSubPathString.startsWith(parentPathString);
    }

    /**
     * Get the closest existing parent folder for the input path.
     * @param path  Input path.
     * @return The closest existing parent folder for the input path.
     */
    public static Path getClosestExistingParent(Path path){
        // Input validation
        Objects.requireNonNull(path);

        Path tempPath = path.toAbsolutePath();
        while (tempPath != null && !Files.exists(tempPath)) {
            tempPath = tempPath.getParent();
        }
        return tempPath;
    }

    /**
     * Registers a specified currentPath for a specific set of events with WatchService.
     * @param currentPath       Path to register and generate events.
     * @param maxDepth          Maximum depth to register WatchService.
     * @param eventTypesToWatch Type of events for which events have to be generated.
     */
    private static void registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(Path currentPath, int maxDepth, WatchEvent.Kind<?> ... eventTypesToWatch) {
        Path tempPath = currentPath.toAbsolutePath();
        if(!pathToWatchServiceRegistrationInfoMap.containsKey(tempPath.toString()) && Files.isDirectory(tempPath)) {
            try {
                WatchService subPathWatchService = FileSystems.getDefault().newWatchService();
                WatchServiceRegistrationInfo watchServiceRegistrationInfo = new WatchServiceRegistrationInfo(tempPath, subPathWatchService, maxDepth, eventTypesToWatch);
                pathToWatchServiceRegistrationInfoMap.put(tempPath.toString(), watchServiceRegistrationInfo);
                currentPath.register(watchServiceRegistrationInfo.getWatchService(), eventTypesToWatch);
                logger.debug(
                    I18NUtility.getFormattedString(
                        "utilities.FileUtility.registeredWatchServiceMessage",
                        tempPath.toString()
                    )
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            logger.debug(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.skippedWatchServiceRegistrationMessage",
                    tempPath,
                    pathToWatchServiceRegistrationInfoMap.containsKey(tempPath.toString()),
                    Files.isDirectory(tempPath)
                )
            );
        }
    }

    /**
     * Registers a specified directory path to a WatchService to keep track of specific type of events within that path
     *
     * Limitations:
     * - Actual event timestamp is not available
     * - Large maxDepth can cause OOM and other performance issues
     *
     * @param path          Directory path to register with the WatchService
     * @param maxDepth      Maximum children depth from specified path to register and trigger events
     * @param consumer      The callback to invoke when a desired event is triggered.
     * @param eventsToWatch Type of events to register for trigger
     * @throws IOException Thrown if registering a WatchService or walking through a path  or modifying it fails.
     */
    public static void registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(
            Path path,
            int maxDepth,
            BiConsumer<Path, WatchEvent<?>> consumer,
            WatchEvent.Kind<?> ... eventsToWatch
    ) throws IOException {
        // Input validation
        Objects.requireNonNull(path);
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(eventsToWatch);
        if(!Files.exists(path)) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.pathDoesNotExist",
                    path
                )
            );
        }
        if(!Files.isDirectory(path)) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.pathNotADirectory",
                    path
                )
            );
        }
        if(maxDepth < 0) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.negativeMaxDepth",
                    maxDepth
                )
            );
        }
        if(eventsToWatch.length == 0) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.noEventsToWatch"
                )
            );
        }
        if(eventsToWatch.length > 4) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.moreThanMaxEventsToWatch",
                    Arrays.toString(eventsToWatch)
                )
            );
        }

        Path tempPath = path.toAbsolutePath();
        logger.info(
            I18NUtility.getFormattedString(
                "utilities.FileUtility.registeredWatchServiceMessage",
                tempPath
            )
        );

        // Register all children recursively
        WatchServiceRegisteringFileVisitor watchServiceRegisteringFileVisitor
                = new WatchServiceRegisteringFileVisitor(tempPath, maxDepth, eventsToWatch);
        Files.walkFileTree(
            tempPath,
            new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
            maxDepth,
            watchServiceRegisteringFileVisitor
        );

        logger.info(
            I18NUtility.getFormattedString(
                "utilities.FileUtility.successfulFolderVisits",
                watchServiceRegisteringFileVisitor.getSuccessfulDirectoryVisitsCount()
            )
        );
        logger.info(
            I18NUtility.getFormattedString(
                "utilities.FileUtility.successfulFileVisits",
                watchServiceRegisteringFileVisitor.getSuccessfulFileVisitsCount()
            )
        );
        logger.info(
            I18NUtility.getFormattedString(
                "utilities.FileUtility.failedVisits",
                watchServiceRegisteringFileVisitor.getFailedVisitsCount()
            )
        );

        // Invoke bg thread only once, not repeatedly when the function is called multiple times
        if(backgroundWatchServiceRegisteringThread == null) {
            backgroundWatchServiceRegisteringThread = new Thread(() -> {
                boolean threadSwitch = Boolean.parseBoolean(
                    PropertyUtility.getProperty("utilities.FileUtility.bgthread.watchservice.switch")
                );
                while (threadSwitch) {
                    HashMap<String, WatchServiceRegistrationInfo> pathToWatchServiceRegistrationInfoMapCopy = new HashMap<>(pathToWatchServiceRegistrationInfoMap);

                    // Iterate through all registered watch services and check if any events are generated
                    pathToWatchServiceRegistrationInfoMapCopy.keySet()
                        .stream()
                        .sorted()
                        .forEach(subPath -> {
                            WatchService subPathWatchService = pathToWatchServiceRegistrationInfoMapCopy
                                    .get(subPath)
                                    .getWatchService();
                            watchServiceEventsMap.putIfAbsent(subPath, new LinkedBlockingQueue<>());
                            Queue<WatchEvent<?>> watchServiceEventsQueue = watchServiceEventsMap.get(subPath);
                            try {
                                WatchKey watchKey = subPathWatchService.poll();

                                if (watchKey != null) {
                                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                                    /* Iterate through all watch events and add another watch service for folders
                                    created within the maxDepth level. */
                                    for (WatchEvent<?> event : watchEvents) {
                                        watchServiceEventsQueue.add(event);

                                        Path changePath = Paths.get(subPath)
                                                .resolve((Path) event.context());
                                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {

                                            if(Files.isDirectory(changePath.toAbsolutePath())) {
                                                Path basePath = changePath.toAbsolutePath();
                                                @SuppressWarnings("UnusedAssignment")
                                                int maxDepthForBasePath = -1;

                                                while (basePath != null && !pathToWatchServiceRegistrationInfoMapCopy.containsKey(basePath.toString())) {
                                                    basePath = basePath.getParent();
                                                }
                                                if (basePath != null) {
                                                    maxDepthForBasePath = pathToWatchServiceRegistrationInfoMapCopy.get(basePath.toString())
                                                            .getMaxDepth();

                                                    if (basePath.relativize(changePath).getNameCount() <= maxDepthForBasePath) {
                                                        registerWatchServiceIfPathIsADirectoryAndIsNotAlreadyRegistered(changePath, maxDepthForBasePath - 1, consumer,
                                                                eventsToWatch);
                                                    } else {
                                                        logger.debug(
                                                            I18NUtility.getFormattedString(
                                                                "utilities.FileUtility.skippingRegistrationForPathBeyondMaxDepth",
                                                                changePath,
                                                                maxDepth
                                                            )
                                                        );
                                                    }
                                                }
                                            }
                                            else {
                                                logger.debug(
                                                    I18NUtility.getFormattedString(
                                                        "utilities.FileUtility.skippingRegistrationAsPathIsADirectory",
                                                        changePath
                                                    )
                                                );
                                            }
                                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                            /* If a directory is deleted, deregister it if it has previously been
                                            registered to a watch service */
                                            if (Files.isDirectory(changePath)) {
                                                deRegisterWatchServiceForDirectory(changePath);
                                            }
                                        }
                                        consumer.accept(changePath, event);
                                        watchServiceEventsQueue.remove();
                                    }
                                    watchKey.reset();
                                }
                            }
                            catch (ClosedWatchServiceException | IOException e) {
                                logger.warn(e);
                            }
                        });
                    threadSwitch = Boolean.parseBoolean(
                            PropertyUtility.getProperty("utilities.FileUtility.bgthread.watchservice.switch")
                    );
                }
            });
            backgroundWatchServiceRegisteringThread.start();
        }
    }

    /**
     * Remove the WatchService registered for the specified path
     * @param path  Path whose WatchService registration needs to be removed
     */
    public static void deRegisterWatchServiceForDirectory(Path path) {
        // Input validation
        Objects.requireNonNull(path);
        if(!Files.isDirectory(path)) {
            throw new IllegalArgumentException(
                I18NUtility.getFormattedString(
                    "utilities.FileUtility.pathNotADirectory",
                    path
                )
            );
        }

        Set<String> registeredPaths = new HashSet<>(pathToWatchServiceRegistrationInfoMap.keySet());

        registeredPaths.stream()
                .map(Paths::get)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .filter(registeredPath -> isSubPath(path, Paths.get(registeredPath)))
                .forEach(registeredPath -> {
                    logger.debug(
                        I18NUtility.getFormattedString(
                            "utilities.FileUtility.deRegisteringPathMessage",
                            registeredPath
                        )
                    );

                    // Remove watch service
                    WatchService watchServiceToRemove = pathToWatchServiceRegistrationInfoMap.remove(registeredPath)
                            .getWatchService();

                    // Close watch service
                    try {
                        watchServiceToRemove.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                });
    }

    /**
     * Clears all unprocessed WatchService events generated until now.
     */
    public static void clearAllWatchServiceEvents() {
        watchServiceEventsMap.clear();
    }
}
