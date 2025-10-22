package org.hrprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path dataDirectory;
    private final Path persistentEmployeesPath;

    public FileStorageService() {
        String currentDir = System.getProperty("user.dir");
        this.dataDirectory = Paths.get(currentDir, "data", "input");
        this.persistentEmployeesPath = dataDirectory.resolve("HR_RO_SMARTDISPO_WS.xlsx");

        logger.info("Data directory: {}", dataDirectory.toAbsolutePath());
        logger.info("Persistent employees file path: {}", persistentEmployeesPath.toAbsolutePath());
    }

    public Path getPersistentEmployeesFile() {
        return persistentEmployeesPath;
    }

    public boolean persistentEmployeesFileExists() {
        boolean exists = Files.exists(persistentEmployeesPath);
        return exists;
    }

    public void updatePersistentEmployeesFile(MultipartFile newFile) throws IOException {
        logger.info("Updating persistent employees file with: {}", newFile.getOriginalFilename());

        try {
            Files.createDirectories(dataDirectory);
            logger.info("Created directories: {}", dataDirectory.toAbsolutePath());

            if (Files.exists(persistentEmployeesPath)) {
                Path backupPath = dataDirectory.resolve("HR_RO_SMARTDISPO_WS.xlsx.backup");
                Files.copy(persistentEmployeesPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Created backup: {}", backupPath.toAbsolutePath());
            }

            newFile.transferTo(persistentEmployeesPath.toFile());
            logger.info("Successfully updated persistent employees file: {}", persistentEmployeesPath.toAbsolutePath());

        } catch (IOException e) {
            logger.error("Error updating persistent employees file", e);
            throw e;
        }
    }

    public File getPersistentEmployeesAsFile() {
        return persistentEmployeesPath.toFile();
    }
}
