package com.qgutech.fs.service;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.service.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import static com.qgutech.fs.domain.FsFile.*;

@Service("fsFileService")
public class FsFileServiceImpl extends BaseServiceImpl<FsFile> implements FsFileService {

}
