package utilities;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 ZIP,
 ZIPX,
 RAR,
 TAR,
 7Z,
 JAR,
 ISO.
 XZ,
 GZ,
 BZ2
 */
public class ArchiveUtility {

    private static final Logger logger = LogManager.getLogger(ArchiveUtility.class);

    public enum ArchiveFileFormat {
        ZIP(".ZIP"),
        ZIP_ENCRYPTED(".ZIP"),
        ZIPX(".ZIPX"),
        RAR(".RAR"),
        TAR(".TAR"),
        SEVEN_ZIP(".SEVEN_ZIP"),
        JAR(".JAR"),
        ISO(".ISO"),
        XZ(".XZ"),
        GZ(".GZ"),
        BZ2(".BZ2");

        private String fileExtension;
        ArchiveFileFormat(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public boolean isArchiveFileFormat(Path path) {
            return Files.exists(path)
                    && path.toString()
                    .toUpperCase()
                    .endsWith(fileExtension);
        }
    }

    /**
     *Extracts the specified archive to the specified destination.
     * @param archiveFilePath Path of the archive.
     * @param destinationPath Destination path to extract the archive to.
     */
    public static void extractArchive(Path archiveFilePath, Path destinationPath) throws IOException {
        Objects.requireNonNull(archiveFilePath);
        Objects.requireNonNull(destinationPath);
        if(Files.notExists(archiveFilePath)) {
            // TODO: Add I18N string
            new FileNotFoundException("Archive not found - " + archiveFilePath);
        }

        ArchiveFileFormat archiveFileFormat = getArchiveFormat(archiveFilePath);

        switch(archiveFileFormat) {
            case ZIP:
                byte[] buffer = new byte[1024];
                try(InputStream inputStream = Files.newInputStream(archiveFilePath)) {
                    try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        while (zipEntry != null) {
                            Path filePath = destinationPath.resolve(zipEntry.getName());
                            Files.createFile(filePath);
                            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                                int bytesRead;
                                while ((bytesRead = zipInputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            }
                            zipEntry = zipInputStream.getNextEntry();
                        }
                        zipInputStream.closeEntry();
                    }
                }
                break;
            default:
                // TODO: Define custom exception. Add I18N String.
                throw new IOException("Unsupported archive file format - " + archiveFileFormat);
        }
    }

    /**
     *Creates an archive of the specified path and type in the specified destination path.
     * @param sourcePath Path to archive.
     * @param destinationPath Path to create the archive in.
     * @param archiveFileFormat Type of archive to be created.
     */
    public static void createArchive(Path sourcePath, Path destinationPath, ArchiveFileFormat archiveFileFormat, String password) throws IOException {
        Objects.requireNonNull(sourcePath);
        Objects.requireNonNull(destinationPath);
        Objects.requireNonNull(archiveFileFormat);

        switch(archiveFileFormat) {
            case ZIP:
                createZipArchive(sourcePath, destinationPath, password);
                break;
            default:
                // TODO: Define custom exception. Add I18N String.
                throw new IOException("Unsupported archive file format - " + archiveFileFormat);
        }
    }

    private static void createZipArchive(Path sourcePath, Path destinationPath, String password) throws IOException {
        if(password == null) {
            try (OutputStream outputStream = Files.newOutputStream(destinationPath)) {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                    logger.info(sourcePath);
                    Files.walk(sourcePath)
                            .forEach(path -> {
                                if (Files.isDirectory(path)) {
                                    try {
                                        logger.info(path);
                                        logger.info(sourcePath.getParent().relativize(path).normalize().toString());
                                        zipOutputStream.putNextEntry(new ZipEntry(sourcePath.getParent().relativize(path).normalize().toString() + File.separator));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    try {
                                        ZipEntry zipEntry = new ZipEntry(sourcePath.getParent().relativize(path).normalize().toString());
                                        zipOutputStream.putNextEntry(zipEntry);
                                        byte[] buffer = new byte[1024];
                                        int bytesRead;
                                        try (InputStream inputStream = Files.newInputStream(path)) {
                                            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                                                zipOutputStream.write(buffer, 0, bytesRead);
                                            }
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                }
            }
        }
        else {
            // Referred https://github.com/srikanth-lingala/zip4j
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            ZipFile zipFile = new ZipFile(destinationPath.toString(), password.toCharArray());
            zipFile.addFolder(sourcePath.toFile(), zipParameters);
        }
    }

    /**
     *Probes and tries to identify the type of archive file.
     * @param sourcePath Path of the Archive file to probe type of.
     * @return Type of Archive.
     */
    public static ArchiveFileFormat getArchiveFormat(Path sourcePath) {
        ArchiveFileFormat result = null;
        for(ArchiveFileFormat archiveFileFormat: ArchiveFileFormat.values()) {
            if(archiveFileFormat.isArchiveFileFormat(sourcePath)) {
                result = archiveFileFormat;
                break;
            }
        }
        return result;
    }
}

