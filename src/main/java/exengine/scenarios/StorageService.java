package exengine.scenarios;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {
    private static final String rootLocation= "scenario_logs";
    private boolean initialized=false;

    /**
     * stores a file in the root file path
     * @param file the MultipartFile to store
     * @return true, if the storage was successful
     * @author aabert
     */
    public boolean store(MultipartFile file, String filename) {
        if (!initialized) this.init();
        if (file.isEmpty()) return false;
        Path folderPath = Paths.get(rootLocation);
        Path destinationFile = folderPath.resolve(
                Paths.get(filename)).normalize().toAbsolutePath();
        new File(folderPath.normalize().toAbsolutePath().toString()).mkdirs();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Loads the filepath as a String
     * @param filename the name of the file
     * @return the filepath to the uploaded file
     * @author aabert
     */
    public static String getFilePath(String filename) {
        if (filename==null) return null;
        Path folderPath = Paths.get(rootLocation);
        Path file = folderPath.resolve(filename);
        return file.toAbsolutePath().toString();
    }

    /**
     * Initializes the storage
     */
    public void init() {
        try {
            Files.createDirectories(Paths.get(rootLocation));
            initialized=true;
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
}
