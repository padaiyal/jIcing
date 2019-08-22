import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utilities.StreamUtility;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

class StreamUtilityTest {

    @Test
    public void testConvertInputStreamToString() {
        String string = "ZXCVBNM!@#$%";
        String stringFromInputStream = StreamUtility.convertInputStreamToString(new ByteArrayInputStream(string.getBytes()));
        Assertions.assertEquals(string, stringFromInputStream);
    }

    @Test
    public void testConvertNullInputStreamToString() {
        boolean npeThrown = false;
        try {
            StreamUtility.convertInputStreamToString(null);
        }
        catch(NullPointerException e) {
            npeThrown = true;
        }
        Assertions.assertTrue(npeThrown);
    }
}
