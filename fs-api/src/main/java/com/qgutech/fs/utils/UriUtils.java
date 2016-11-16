package com.qgutech.fs.utils;


public interface UriUtils {
    /**
     * fs上传文件时的uri
     */
    String UPLOAD_URI = "/fs/file/uploadFile";

    /**
     * fs获取文件时的uri
     */
    String GET_FILE_URI = "/fs/file/getFile/";

    /**
     * fs获取文件时的uri
     */
    String DOWNLOAD_FILE_URI = "/fs/file/downloadFile/";

    /**
     * fs截图时的uri
     */
    String CUT_IMAGE_URI = "/fs/file/cutImage";

    /**
     * fs重新处理文件时的uri
     */
    String REPROCESS_FILE_URI = "/fs/file/reprocessFile";

}
