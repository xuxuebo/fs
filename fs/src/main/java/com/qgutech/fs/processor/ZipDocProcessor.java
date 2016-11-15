package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;

public class ZipDocProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        if (PropertiesUtils.isDocConvert()) {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_ZIP_DOC_QUEUE_LIST);
            String fsFileId = fsFile.getId();
            //当重复提交时，防止处理音频重复
            //commonJedis.lrem(RedisKey.FS_ZIP_DOC_QUEUE_LIST, 0, fsFileId);
            commonJedis.lpush(RedisKey.FS_ZIP_DOC_QUEUE_LIST, fsFileId);
            commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
        } else {
            String backUrl = PropertiesUtils.getHttpProtocol() + FsConstants.HTTP_COLON
                    + PropertiesUtils.getServerHost() + PropertiesUtils.getBackUri();
            fsFile.setBackUrl(backUrl);
            HttpUtils.doPost(PropertiesUtils.getAsyncUrl(), fsFile.toMap()
                    , fsFile.getTmpFilePath(), fsFile.getStoredFileName());
        }
    }


    @Override
    public void afterProcess(FsFile fsFile) throws Exception {
        String backUrl = fsFile.getBackUrl();
        if (StringUtils.isNotEmpty(backUrl)) {
            String genFilePath = getGenFilePath(fsFile);
            File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
            File compressFile = new File(parentFile, FsUtils.generateUUID()
                    + FsConstants.POINT + FsConstants.COMPRESS_FILE_SUFFIX_ZIP);
            FsUtils.compress(genFilePath, compressFile.getAbsolutePath());
            HttpUtils.doPost(backUrl, fsFile.toMap(), compressFile.getAbsolutePath(), null);//todo 容错
        }

        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        updateFsFile(fsFile);
    }
}
