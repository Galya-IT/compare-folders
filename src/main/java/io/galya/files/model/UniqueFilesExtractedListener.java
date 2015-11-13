package io.galya.files.model;

import io.galya.files.model.FileInfoWrapper;

import java.util.Collection;

/**
 * Created by Galya on 13/11/15.
 */
public interface UniqueFilesExtractedListener {

    void onUniqueFilesExtracted(Collection<FileInfoWrapper> uniqueFiles);

}
