package utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyUtility {

    private static Set<String> propertyFileNames = Collections.synchronizedSet(new HashSet<>());
    private static Set<Properties> propertyFiles = Collections.synchronizedSet(new HashSet<>());

    static {
        propertyFileNames.add("src/main/resources/icing.properties");
        buildPropertyFileSet();
    }

    /**
     * Build the Properties objects from the currently specified property file names in the propertyFileNames set
     */
    private static void buildPropertyFileSet() {
        propertyFiles.clear();
        propertyFiles.addAll(
                propertyFileNames.stream()
                        .map(propertyFileName -> {
                            Properties propertyFile = new Properties();
                            try {
                                propertyFile.load(Files.newInputStream(Paths.get(String.valueOf(propertyFileName))));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return propertyFile;
                        })
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Retrieves the value corresponding to the property name specified.
     * If multiple property files contain the same key, the first property
     * file (Decided by the order in which the property file is added) in
     * which the property is present is used.
     *
     * @param property The property to retrieve the value for
     * @return The value corresponding to the property specified
     */
    public synchronized static String getProperty(String property) {
        Optional<String> value = propertyFiles.stream()
                .map(propertyFile -> {
                    String tempValue;
                    try {
                        tempValue = propertyFile.getProperty(property);
                    } catch (MissingResourceException e) {
                        tempValue = null;
                    }
                    return tempValue;
                })
                .filter(Objects::nonNull)
                .findFirst();
        if (value.isPresent()) {
            return value.get();
        } else {
            throw new MissingResourceException(
                    String.format("Can't find property - %s", property),
                    I18NUtility.class.getName(),
                    property
            );
        }
    }

    /**
     * Add a specified property file
     *
     * @param propertyFileName Property file to add
     */
    public synchronized static void addPropertyFile(String propertyFileName) throws FileNotFoundException {
        if (Files.exists(Paths.get(propertyFileName))) {
            propertyFileNames.add(propertyFileName);
            buildPropertyFileSet();
        } else {
            throw new FileNotFoundException(String.format("%s property file not found", propertyFileName));
        }
    }

    /**
     * Remove a specified property file
     *
     * @param propertyFileName Property file to remove
     */
    public synchronized static void removePropertyFile(String propertyFileName) throws FileNotFoundException {
        boolean propertyFilePresent = propertyFileNames.remove(propertyFileName);
        if (!propertyFilePresent) {
            throw new FileNotFoundException(String.format("%s property file not found", propertyFileName));
        }
        buildPropertyFileSet();
    }
}
