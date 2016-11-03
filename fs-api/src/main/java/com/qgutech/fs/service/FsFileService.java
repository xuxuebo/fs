package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsFile;

import java.util.List;

public interface FsFileService {

    FsFile get(String storedFileId);

    List<FsFile> listByIds(List<String> storedFileIds);

    String save(FsFile fsFile);
}
