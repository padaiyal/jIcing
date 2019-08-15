package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utilities.I18NUtility;

import java.util.MissingResourceException;

class I18nUtilityTest {

    @Test
    void testGetStringWithValidKey() {
        Assertions.assertEquals("Hello All!!!", I18NUtility.getString("com.sample.message"));
    }

    @Test
    void testGetStringWithInvalidKey() {
        boolean missingResourceExceptionThrown = false;
        try {
            I18NUtility.getString("com.sample.message.invalid");
        }
        catch(MissingResourceException e) {
            missingResourceExceptionThrown = true;
        }
        finally {
            Assertions.assertTrue(missingResourceExceptionThrown);
        }
    }

    @Test
    void testAddValidResourceBundle() {
        I18NUtility.addResourceBundle("Test1_Resource_Bundle");
        String value = I18NUtility.getString("test1.message");
        Assertions.assertEquals("test message 1", value);
    }

    @Test
    void testAddMissingResourceBundle() {
        boolean missingResourceExceptionThrown = false;
        try {
            I18NUtility.addResourceBundle("Test1_Resource_Bundle_invalid");
        }
        catch (MissingResourceException e) {
            missingResourceExceptionThrown = true;
        }
        Assertions.assertTrue(missingResourceExceptionThrown);
    }

    @Test
    void testRemoveResourceBundle() {
        I18NUtility.addResourceBundle("Test2_Resource_Bundle");
        I18NUtility.getString("test2.message");
        I18NUtility.removeResourceBundle("Test2_Resource_Bundle");
        boolean missingResourceExceptionThrown = false;
        try {
            I18NUtility.getString("test2.message");
        }
        catch (MissingResourceException e) {
            missingResourceExceptionThrown = true;
        }
        Assertions.assertTrue(missingResourceExceptionThrown);
    }

    @Test
    void testRemoveMissingResourceBundle() {
        boolean missingResourceExceptionThrown = false;
        try {
            I18NUtility.removeResourceBundle("Test1_Resource_Bundle_invalid");
        }
        catch (MissingResourceException e) {
            missingResourceExceptionThrown = true;
        }
        Assertions.assertTrue(missingResourceExceptionThrown);
    }
}
