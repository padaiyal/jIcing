import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utilities.PropertyUtility;

import java.io.FileNotFoundException;
import java.util.MissingResourceException;

class PropertyUtilityTest {

    @Test
    void testGetStringWithValidProperty() {
        Assertions.assertEquals("TEST PROPERTY", PropertyUtility.getProperty("test.prop"));
    }

    @Test
    void testGetStringWithInvalidProperty() {
        boolean missingResourceExceptionThrown = false;
        try {
            PropertyUtility.getProperty("test_invalid");
        }
        catch(MissingResourceException e) {
            missingResourceExceptionThrown = true;
        }
        finally {
            Assertions.assertTrue(missingResourceExceptionThrown);
        }
    }

    @Test
    void testAddValidPropertyFile() throws FileNotFoundException {
        PropertyUtility.addPropertyFile("test.properties");
        String value = PropertyUtility.getProperty("test1");
        Assertions.assertEquals("TEST1", value);
    }

    @Test
    void testAddMissingPropertyFile() {
        boolean fileNotFoundExceptionThrown = false;
        try {
            PropertyUtility.addPropertyFile("test_invalid.properties");
        }
        catch (FileNotFoundException e) {
            fileNotFoundExceptionThrown = true;
        }
        Assertions.assertTrue(fileNotFoundExceptionThrown);
    }

    @Test
    void testRemovePropertyFile() throws FileNotFoundException {
        PropertyUtility.addPropertyFile("test.properties");
        PropertyUtility.getProperty("test2");
        PropertyUtility.removePropertyFile("test.properties");
        boolean fileNotFoundExceptionThrown = false;
        try {
            PropertyUtility.getProperty("test2");
        }
        catch (MissingResourceException e) {
            fileNotFoundExceptionThrown = true;
        }
        Assertions.assertTrue(fileNotFoundExceptionThrown);
    }

    @Test
    void testRemoveMissingPropertyFile() {
        boolean fileNotFoundExceptionThrown = false;
        try {
            PropertyUtility.removePropertyFile("test_invalid.properties");
        }
        catch (FileNotFoundException e) {
            fileNotFoundExceptionThrown = true;
        }
        Assertions.assertTrue(fileNotFoundExceptionThrown);
    }
}

