package io.galya.files.util;

import io.galya.files.model.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Galya on 13/11/15.
 */
public class UniqueFilesExtractor {

    private Collection<Path> paths;

    public UniqueFilesExtractor(Collection<Path> paths) {
        this.paths = paths;
    }

    public void extract(UniqueFilesExtractedListener listener) throws CompareFoldersAppException, NoSuchAlgorithmException, IOException {
        TraversingDirectoriesTask traverseDirectoriesTask = new TraversingDirectoriesTask(paths);
        traverseDirectoriesTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    Collection<FileInfoWrapper> uniqueFiles = traverseDirectoriesTask.getValue();
                    listener.onUniqueFilesExtracted(uniqueFiles);
                }
            }
        });
        new Thread(traverseDirectoriesTask).start();
    }

    private Set<FileInfoWrapper> combineUniqueFilesInWrapper(Map<String, List<FileInfo>> filesMappedByName) throws CompareFoldersAppException, NoSuchAlgorithmException {
        Set<FileInfoWrapper> filesWrapperSet = new HashSet<>();

        Iterator mapIterator = filesMappedByName.entrySet().iterator();

        while (mapIterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) mapIterator.next();

            List<FileInfo> filesSameNameList = (List<FileInfo>) mapEntry.getValue();
            int filesSameNameCount = filesSameNameList.size();

            if (filesSameNameCount == 1) {
                FileInfo file = filesSameNameList.get(0);
                FileInfoWrapper fileWrapper = new FileInfoWrapper(file);
                filesWrapperSet.add(fileWrapper);
            } else if (filesSameNameCount > 1) {
                Collection<FileInfoWrapper> currentNameWrappers = new HashSet<>();

                for (FileInfo file : filesSameNameList) {
                    if (currentNameWrappers.isEmpty()) {
                        FileInfoWrapper fileWrapper = new FileInfoWrapper(file);
                        currentNameWrappers.add(fileWrapper);
                    } else {
                        boolean isAddedToExistingWrapper = false;

                        for (FileInfoWrapper wrapper : currentNameWrappers) {
                            String currentWrapperFilesHashCode = wrapper.getHashCode();
                            String newFileHashCode = file.getHashCode();

                            if (currentWrapperFilesHashCode.equals(newFileHashCode)) {
                                wrapper.addFile(file);
                                isAddedToExistingWrapper = true;
                                break;
                            }
                        }

                        if (!isAddedToExistingWrapper) {
                            FileInfoWrapper fileWrapper = new FileInfoWrapper(file);
                            currentNameWrappers.add(fileWrapper);
                        }
                    }
                }

                filesWrapperSet.addAll(currentNameWrappers);
            }
        }

        return filesWrapperSet;
    }

    private class TraversingDirectoriesTask extends Task<Collection<FileInfoWrapper>> {
        private Collection<Path> paths;
        private Collection<FileInfo> files = new HashSet<>();

        public TraversingDirectoriesTask(Collection<Path> paths) throws IOException {
            this.paths = paths;
        }

        private Collection<FileInfo> listAllFiles(Collection<Path> directories) throws IOException {
            Set<FileInfo> filesCollection = new HashSet<>();

            for (Path directory : directories) {
                try {
                    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path absoluteFilePath, BasicFileAttributes attrs) throws IOException {
                            try {
                                FileInfo file = new FileInfo(absoluteFilePath);
                                filesCollection.add(file);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (Throwable t) {
                    System.err.println("Error occurred while traversing directory " + directory.toAbsolutePath());
                }
            }

            return filesCollection;
        }

        @Override
        protected Collection<FileInfoWrapper> call() throws Exception {
            Set<FileInfoWrapper> uniqueFileWrappers = new HashSet<>();

            try {
                files = listAllFiles(paths);
                Map<String, List<FileInfo>> filesMappedByName = files.stream().collect(Collectors.groupingBy(FileInfo::getName));
                try {
                    uniqueFileWrappers = combineUniqueFilesInWrapper(filesMappedByName);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return uniqueFileWrappers;
        }
    }
}
