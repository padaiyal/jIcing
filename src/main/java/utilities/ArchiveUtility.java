package utilities;

import java.nio.file.Path;

/**
 * ZIP, ZIPX, RAR, TAR, 7Z, JAR, ISO
 * XZ, GZ, BZ2
 */
public class ArchiveUtility {

    public enum ArchiveFormat {
        ZIP, ZIPX, RAR, TAR, SEVEN_ZIP, JAR, ISO, XZ, GZ, BZ2;
    }

    /**
     *Extracts the specified archive to the specified destination.
     * @param archivePath Path of the archive.
     * @param destinationPath Destination path to extract the archive to.
     */
    public static void extractArchive(Path archivePath, Path destinationPath) {

    }

    /**
     *Creates an archive of the specified path and type in the specified destination path.
     * @param sourcePath Path to archive.
     * @param destinationPath Path to create the archive in.
     * @param archiveFormat Type of archive to be created.
     */
    public static void packArchive(Path sourcePath, Path destinationPath, ArchiveFormat archiveFormat) {

    }

    /**
     *Probes and tries to identify the type of archive file.
     * @param sourcePath Path of the Archive file to probe type of.
     * @return Type of Archive.
     */
    public String probeArchiveType(Path sourcePath) {
        return null;
    }
}

