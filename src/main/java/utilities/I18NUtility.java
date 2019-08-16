package utilities;

import java.util.*;
import java.util.stream.Collectors;

public class I18NUtility {

    private static String language = PropertyUtility.getProperty("common.language");
    private static String region = PropertyUtility.getProperty("common.region");
    private static Locale locale = new Locale(language, region);
    private static Set<String> resourceBundleNames = Collections.synchronizedSet(new HashSet<>());
    private static Set<ResourceBundle> resourceBundles = Collections.synchronizedSet(new HashSet<>());

    static {
        String defaultResourceBundleName = PropertyUtility.getProperty("utilities.I18NUtility.resourcebundle.default");
        resourceBundleNames.add(defaultResourceBundleName);
        buildResourceBundlesSet();
    }

    /**
     * Build the ResourceBundle objects from the currently specified resource bundle names in the resourceBundleNames set
     */
    private static void buildResourceBundlesSet() {
        resourceBundles.clear();
        resourceBundles.addAll(
                resourceBundleNames.stream()
                        .map(resourceBundleName -> ResourceBundle.getBundle(resourceBundleName, locale))
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Retrieves the value corresponding to the key specified.
     * If multiple resource bundles contain the same key, the first resource
     * bundle (Decided by the order in which the resource bundle is added) in
     * which the key is present is used.
     * @param key The key to retrieve the value for
     * @return The value corresponding to the key specified
     */
    public static String getString(String key) {
        Optional<String> value = resourceBundles.stream()
            .map(resourceBundle -> {
                String tempValue;
                try {
                    tempValue = resourceBundle.getString(key);
                }
                catch (MissingResourceException e) {
                    tempValue = null;
                }
                return tempValue;
            })
            .filter(Objects::nonNull)
            .findFirst();
        if(value.isPresent()) {
            return value.get();
        }
        else {
            throw new MissingResourceException(
                String.format("Can't find resource for bundle %s", key),
                I18NUtility.class.getName(),
                key
            );
        }
    }

    /**
     * Add a specified resource bundle
     * @param resourceBundleName Resource bundle to add
     */
    public synchronized static void addResourceBundle(String resourceBundleName) {
        resourceBundleNames.add(resourceBundleName);
        try {
            buildResourceBundlesSet();
        }
        catch (MissingResourceException e) {
            resourceBundleNames.remove(resourceBundleName);
            throw e;
        }
    }

    /**
     * Remove a specified resource bundle
     * @param resourceBundleName Resource bundle to remove
     */
    public synchronized static void removeResourceBundle(String resourceBundleName) {
        boolean resourceBundlePresent = resourceBundleNames.remove(resourceBundleName);
        if(!resourceBundlePresent) {
            throw new MissingResourceException(
                    String.format("Can't find resource bundle %s", resourceBundleName),
                    I18NUtility.class.getName(),
                    resourceBundleName
            );
        }
        buildResourceBundlesSet();
    }
}
