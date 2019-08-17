package utilities;

public enum OS {
    WINDOWS,
    LINUX,
    UNKNOWN;

    public static OS getOs() {
        String os = System.getProperty("os.name").toUpperCase();
        System.out.println(os.contains("LINUX"));
        if(os.contains("LINUX")) {
            return LINUX;
        }
        else if(os.contains("WINDOWS")) {
            return WINDOWS;
        }
        else {
            return UNKNOWN;
        }
    }
}
