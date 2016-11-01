package com.qgutech.fs.domain;


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
