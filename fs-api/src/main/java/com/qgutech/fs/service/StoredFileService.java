package com.qgutech.fs.service;


import com.qgutech.fs.domain.StoredFile;

import java.util.List;

public interface StoredFileService {

    StoredFile get(String storedFileId);

    List<StoredFile> listByIds(List<String> storedFileIds);

    String save(StoredFile storedFile);
}
