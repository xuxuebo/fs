package com.qgutech.fs.utils;


public interface FsConstants {
    String PDF_SUFFIX = ".pdf";
    String PDF_PREFIX = "pdfResult_";
    String DEFAULT_AUDIO_TYPE = "mp3";
    String DEFAULT_VIDEO_SUFFIX = ".m3u8";
    String DEFAULT_VIDEO_TYPE = "m3u8";
    String DEFAULT_IMAGE_SUFFIX = ".png";
    String DEFAULT_IMAGE_TYPE = "png";
    String FILE_DIR_SRC = "src";
    String FILE_DIR_GEN = "gen";
    String FILE_DIR_TMP = "tmp";
    String FILE_DIR_IMPT = "impt";
    String FILE_DIR_EXPT = "expt";
    String FILE_DIR_UNZIP = "unzip";
    String FILE_DIR_IMG = "img";
    String PATH_SEPARATOR = "/";
    String POINT = ".";
    String VERTICAL_LINE_REGEX = "\\|";
    String VERTICAL_LINE = "|";
    String HTTP_COLON = "://";
    String VIDEO_COVER = "vc.png";
    String ORIGIN_IMAGE_NAME = "o.png";
    String FIRST = "1";
    String DEFAULT_AUDIO_NAME = "a.mp3";
    String ZIP_INDEX_FILE = "index.html";
    String UNDERLINE = "_";
    String DEFAULT_CORP_CODE = "default";

    String RESPONSE_RESULT_ERROR = "error";
    String RESPONSE_RESULT_PARAM_ILLEGAL = "param_illegal";
    String RESPONSE_RESULT_TIME_OUT = "time_out";
    String RESPONSE_RESULT_SERVER_NOT_EXIST = "server_not_exist";
    String RESPONSE_RESULT_SIGN_ERROR = "sign_error";
    String RESPONSE_RESULT_FS_FILE_NOT_EXIST = "fs_file_not_exist";

    String COMPRESS_FILE_SUFFIX_ZIP = "zip";
    String COMPRESS_FILE_SUFFIX_7Z = "7z";
    String COMPRESS_FILE_SUFFIX_RAR = "rar";

    String DECOMPRESS = "decompress";
    String FFMPEG = "ffmpeg";

    String RESPONSE_FORMAT_JSON = "json";
    String RESPONSE_FORMAT_HTML = "html";
    String RESPONSE_FORMAT_XML = "xml";
    String RESPONSE_FORMAT_JSONP = "jsonp";

    String FILE_URL_GET_FILE = "/file/getFile/";
    String FILE_URL_DOWNLOAD_FILE = "/file/downloadFile/";

    /**
     * 断点续传处理类型：验证md5，判断文件是否已经上传过
     */
    String RESUME_TYPE_MD5_CHECK = "md5Check";

    /**
     * 断点续传处理类型：验证分片是否存在
     */
    String RESUME_TYPE_CHUNK_CHECK = "chunkCheck";

    /**
     * 断点续传处理类型：分片合并
     */
    String RESUME_TYPE_CHUNKS_MERGE = "chunksMerge";

    /**
     * 断点续传处理类型：分片上传
     */
    String RESUME_TYPE_CHUNK_UPLOAD = "chunkUpload";
    /**
     * 临时文件的后缀
     */
    String TMM_FILE_SUFFIX = ".tmp";

    String IMAGE_NAME_WIN_WORD_EXE = "WINWORD.EXE";
    String IMAGE_NAME_POWER_PNT_EXE = "POWERPNT.EXE";
    String IMAGE_NAME_EXCEL_EXE = "EXCEL.EXE";
}
