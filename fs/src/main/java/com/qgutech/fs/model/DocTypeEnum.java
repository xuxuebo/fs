package com.qgutech.fs.model;


public enum DocTypeEnum {
    DOC,
    DOCX,
    PPT,
    PPTX,
    XLS,
    XLSX,
    TXT,
    PDF;

    public String docType() {
        return name().toLowerCase();
    }

}
