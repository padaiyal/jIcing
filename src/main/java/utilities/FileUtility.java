package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileUtility {

    private static Logger logger = LogManager.getLogger(FileUtility.class);

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
            if (setRecursively)
                Files.walk(path)
                        .parallel()
                        .forEach(node -> {
                            try {
                                Files.setPosixFilePermissions(node, permissions);
                            } catch (IOException e) {
                                logger.error(e);
                            }
                        });
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
}
