package io.galya.files.model;

import io.galya.files.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static io.galya.files.util.FileSystemUtils.*;

public class FileInfo {

    private final String name;
    private final Path path;
    private final long sizeInBytes;
    private String hashCode = null;

    public FileInfo(Path path) throws CompareFoldersAppException {
        try {
            if (isFile(path)) {
                this.name = getFileName(path);
                this.path = path;
                this.sizeInBytes = getFileSize(path);
            } else {
                throw new IllegalArgumentException("The specified path does not point to a file: " + path.toString());
            }
        } catch (IllegalArgumentException | IOException e) {
            String exceptionText = String.format("%s cannot be initialized with file path: %s",
                    this.getClass().getName(), path.toString());
            throw new CompareFoldersAppException(exceptionText);
        }
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    /**
     * Returns the File size in bytes.
     */
    public long getSize() {
        return sizeInBytes;
    }

    public String getHashCode() throws NoSuchAlgorithmException, CompareFoldersAppException {
        if (hashCode == null) {
            hashCode = FileSystemUtils.generateHashCode(path);
        }
        return hashCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("Object of type ");
        sBuilder.append(this.getClass().getName());
        sBuilder.append("; name: ");
        sBuilder.append(getName());
        sBuilder.append("; file path: ");
        sBuilder.append(getPath());
        sBuilder.append("; file size: ");
        sBuilder.append(getSize());
        return sBuilder.toString();
    }
}
