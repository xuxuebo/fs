package com.qgutech.fs.utils;


public interface RedisKey {
    String FS_QUEUE_NAME_LIST = "fs_queue_name_list";
    String FS_AUDIO_QUEUE_LIST = "fs_audio_queue_list";
    String FS_ZIP_AUDIO_QUEUE_LIST = "fs_zip_audio_queue_list";
    String FS_VIDEO_QUEUE_LIST = "fs_video_queue_list";
    String FS_ZIP_VIDEO_QUEUE_LIST = "fs_zip_video_queue_list";
    String FS_ZIP_IMAGE_QUEUE_LIST = "fs_zip_image_queue_list";
    String FS_DOC_QUEUE_LIST = "fs_doc_queue_list";
    String FS_ZIP_DOC_QUEUE_LIST = "fs_zip_doc_queue_list";
    String FS_DOING_LIST_SUFFIX = "_doing";
    String FS_DOING_LIST_LOCK_SUFFIX = "_lock";
    String FS_FILE_CONTENT_PREFIX = "fs_file_content_prefix_";
    String FS_CHECK_SESSION_RESULT = "fs_check_session_result_";
    String FS_CHUNK_MERGE_LOCK_ = "fs_chunk_merge_lock_";
    String FS_WINDOWS_PID_ = "fs_windows_pid_";
    String FS_REPEAT_EXECUTE_CNT_ = "fs_repeat_execute_cnt_";
    String FS_REPEAT_EXECUTE_QUEUE_NAME = "fs_repeat_execute_queue_name";
    String FS_REPEAT_EXECUTE_LOCK = "fs_repeat_execute_lock";
}
