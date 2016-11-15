package com.qgutech.fs.processor;


import com.qgutech.fs.convert.Converter;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.PropertiesUtils;

import java.io.File;

public class DocProcessor extends AbstractProcessor {

    protected Converter docToPdfConverter;
    protected Converter pdfToImageConverter;

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateDoc(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        if (PropertiesUtils.isDocConvert()) {

        } else {

        }


        //todo
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        //todo
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        File pdfFile = docToPdfConverter.convert(tmpFilePath, parentFile.getAbsolutePath());
        File imageFile = new File(parentFile, FsConstants.FILE_DIR_IMG);
        imageFile = pdfToImageConverter.convert(pdfFile.getAbsolutePath(), imageFile.getAbsolutePath());

        afterProcess(fsFile);
    }

    public Converter getDocToPdfConverter() {
        return docToPdfConverter;
    }

    public void setDocToPdfConverter(Converter docToPdfConverter) {
        this.docToPdfConverter = docToPdfConverter;
    }

    public Converter getPdfToImageConverter() {
        return pdfToImageConverter;
    }

    public void setPdfToImageConverter(Converter pdfToImageConverter) {
        this.pdfToImageConverter = pdfToImageConverter;
    }
}
