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

    public static boolean isDoc(String ext) {
        DocTypeEnum[] values = DocTypeEnum.values();
        for (DocTypeEnum value : values) {
            if (value.docType().equals(ext)) {
                return true;
            }
        }

        return false;
    }

}
