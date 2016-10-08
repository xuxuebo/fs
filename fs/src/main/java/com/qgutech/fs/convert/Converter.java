package com.qgutech.fs.convert;


import java.io.File;

public interface Converter {

    File convert(String inputFilePath, String targetFileDirPath
            , ResultProcess resultProcess) throws Exception;

}

interface ResultProcess {
    void processResult(String result);
}
