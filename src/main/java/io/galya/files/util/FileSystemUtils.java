package io.galya.files.util;

import io.galya.files.model.CompareFoldersAppException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSystemUtils {

    public static boolean isFile(Path path) {
        return Files.exists(path) && !Files.isDirectory(path);
    }

    public static String getFileName(Path path) throws IllegalArgumentException {
        String fileName = null;

        if (isFile(path)) {
            File file = new File(path.toString());
            fileName = file.getName();
        } else {
            throw new IllegalArgumentException("The specified path does not point to a file: " + path.toString());
        }

        return fileName;
    }

    public static String getFileName(String pathString) throws CompareFoldersAppException {
        Path path = FileSystems.getDefault().getPath(pathString);
        return getFileName(path);
    }

    public static long getFileSize(Path path) throws IllegalArgumentException, IOException {
        if (isFile(path)) {
            long sizeInBytes = (long) Files.getAttribute(path, "basic:size", LinkOption.NOFOLLOW_LINKS);
            return sizeInBytes;
        } else {
            throw new IllegalArgumentException("Path provided does not point to a file: " + path.toString());
        }
    }

    public static String generateHashCode(Path path) throws CompareFoldersAppException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        try (SeekableByteChannel channel = Files.newByteChannel(path)) {
            final int BYTES_ALLOCATED = 1024 * 8;
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_ALLOCATED);

            int bytesRead;

            while ((bytesRead = channel.read(buffer)) > 0) {
                md.update(buffer.array(), 0, bytesRead);
                buffer.clear();
            }

            channel.close();
        } catch (IOException e) {
            throw new CompareFoldersAppException("File cannot be read: " + e.getCause());
        }

        byte[] md5 = md.digest();
        BigInteger bi = new BigInteger(1, md5);

        return bi.toString(16);
    }

    public static boolean areNestedDirectories(File file1, File file2) {
        boolean isAnyNestedDirectory = false;

        if (file1.toPath().startsWith(file2.toPath())
                || file2.toPath().startsWith(file1.toPath())) {
            isAnyNestedDirectory = true;
        }

        return isAnyNestedDirectory;
    }
}
