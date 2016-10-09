package com.qgutech.fs.convert;


import java.io.File;

public interface Converter {

    File convert(String inputFilePath, String targetFileDirPath) throws Exception;

}
