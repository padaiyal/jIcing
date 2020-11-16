package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum OS {
    WINDOWS,
    LINUX,
    MAC_OS_X,
    UNKNOWN;

    private static Logger logger = LogManager.getLogger(OS.class);

    /**
     * Detects the OS of the machine in which this method is called.
     * @return The detected OS. If it's unable to detect, it will return UNKNOWN.
     */
    public static OS getOs() {
        String osString = System.getProperty("os.name")
                .toUpperCase();
        logger.debug("OS string: " + osString);
        OS detectedOS;
        if(osString.contains("LINUX")) {
            detectedOS = LINUX;
        }
        else if(osString.contains("WINDOWS")) {
            detectedOS = WINDOWS;
        }
        else if(osString.contains("MAC OS X")) {
            detectedOS = MAC_OS_X;
        }
        else {
            detectedOS = UNKNOWN;
        }
        logger.info("OS detected: " + detectedOS);
        return detectedOS;
    }
}
