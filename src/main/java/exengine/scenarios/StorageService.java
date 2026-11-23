package exengine.scenarios;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {
    private final String rootLocation= "resources";
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
     * Loads the file as a resource. Returns null if the file was not found.
     * @param filename the name of the file
     * @return the resource (file) if found, null if not found or error occurs
     * @author aabert
     */
    public Resource loadFile(String filename) {
        if (!initialized) this.init();
        if (filename==null) return null;
        Path folderPath = Paths.get(rootLocation);
        Path file = folderPath.resolve(filename);
        try{
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            return null;
        }
        return null;
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
