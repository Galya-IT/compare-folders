package io.galya.files.model;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.galya.files.util.FileSystemUtils.*;

public class FileInfoWrapper implements Comparable<FileInfoWrapper> {
    private String hashCode = null;
    private Collection<FileInfo> files = new HashSet<FileInfo>();

    public FileInfoWrapper(Path path) throws CompareFoldersAppException {
        try {
            addPath(path);
        } catch (Exception e) {
            String exceptionText = String.format("Path %s cannot be added.%s %s", path.toString(),
                    System.getProperty("line.separator"), e.getMessage());
            throw new CompareFoldersAppException(exceptionText);
        }
    }

    public FileInfoWrapper(FileInfo file) throws CompareFoldersAppException {
        files.add(file);
    }
    
    public String getName() {
        return getRandomFile().getName();
    }

    /**
     * Returns the File size in bytes.
     */
    public long getSize() {
        return getRandomFile().getSize();
    }

    public int getCount() {
        return files.size();
    }
    
    public String getHashCode() throws NoSuchAlgorithmException, CompareFoldersAppException {
        return getRandomFile().getHashCode();
    }
    
    public Collection<Path> getPaths() {
        Set<Path> paths = new HashSet<>();
        
        if (!files.isEmpty()) {
            for (FileInfo file : files) {
                paths.add(file.getPath());
            }
        }
        
        return paths;
    }
    
    public void addPath(Path path) throws CompareFoldersAppException, NoSuchAlgorithmException,
            IllegalArgumentException, IOException {
        
        if (!isFile(path)) {
            throw new CompareFoldersAppException("Not a file: " + path.toString());
        }

        if (!files.isEmpty()) {
            boolean isTheSameFile = checkIfTheSameFile(path);

            if (!isTheSameFile) {
                throw new CompareFoldersAppException("Not the same file: " + path.toString());
            }
        }

        FileInfo file = new FileInfo(path);
        files.add(file);
    }

    public void addFile(FileInfo file) throws NoSuchAlgorithmException, CompareFoldersAppException {
        String currentWrapperFilesHashCode = getHashCode();
        String newFileHashCode = file.getHashCode();
        
        if (currentWrapperFilesHashCode.equals(newFileHashCode)) {
            files.add(file);
        } else {
            throw new CompareFoldersAppException("File is not the same as the collection of files in wrapper: " + file.getPath().toString());
        }
    }
    
    private FileInfo getRandomFile() {
        FileInfo randomFile = null;

        for (FileInfo file : files) {
            randomFile = file;
            break;
        }

        return randomFile;
    }
    
    private boolean checkIfTheSameFile(Path path) throws NoSuchAlgorithmException, CompareFoldersAppException,
            IllegalArgumentException, IOException {
        boolean isTheSameName = false, isTheSameSize = false, isTheSameContent = false;

        isTheSameName = getName().equals(getFileName(path));

        if (isTheSameName) {
            isTheSameSize = (getSize() == getFileSize(path));

            if (isTheSameSize) {
                if (hashCode == null) {
                    hashCode = generateHashCode(getRandomFile().getPath());
                }

                String hashCodePathToAdd = generateHashCode(path);
                if (hashCode.equals(hashCodePathToAdd)) {
                    isTheSameContent = true;
                }
            }
        }

        boolean isTheSameFile = isTheSameName && isTheSameSize && isTheSameContent;
        return isTheSameFile;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        final String TAB = "      ";
        final String NEW_LINE = System.getProperty("line.separator");

        Collection<Path> pathsCollection = getPaths();
        SortedSet<Path> sortedSetOfPaths = new TreeSet<Path>(new Comparator<Path>() {
            @Override
            public int compare(Path path1, Path path2) {
                String path1String = path1.toString();
                String path2String = path2.toString();
                return path1String.compareTo(path2String);
            }
        });
        sortedSetOfPaths.addAll(pathsCollection);

        String firstLineString = String.format("[%d] %s (%,d bytes):", pathsCollection.size(), getName(), getSize());
        stringBuilder.append(firstLineString);

        for (Path path : sortedSetOfPaths) {
            stringBuilder.append(NEW_LINE);
            String lineForPathString = TAB + path.toString();
            stringBuilder.append(lineForPathString);
        }

        return stringBuilder.toString();
    }

    /**
     * Descending sorting based on number of paths pointing to the same file.
     */
    @Override
    public int compareTo(FileInfoWrapper fileWrapper2) {
        int numberOfPathsFile1 = this.getCount();
        int numberOfPathsFile2 = fileWrapper2.getCount();

        if (numberOfPathsFile1 < numberOfPathsFile2) {
            return 1;
        } else {
            return -1;
        }
    }
}
