package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.SignLevelEnum;
import com.qgutech.fs.processor.Processor;
import com.qgutech.fs.processor.ProcessorFactory;
import com.qgutech.fs.utils.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.JedisCommands;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@DependsOn({"propertiesUtils", "fsRedis"})
@RequestMapping("/file/*")
public class FileController {

    private static final Log LOG = LogFactory.getLog(FileController.class);
    private static final URLCodec URL_CODEC = new URLCodec();
    private static final String originPattern = "/(.+)/(.+)/src/.+/\\d{4}/\\w+/(\\w+)\\.\\w+";
    private static final String genPattern = "/(.+)/(.+)/gen/\\w+/\\d{4}/(\\w+)/.+";

    @Resource
    private ProcessorFactory processorFactory;

    @RequestMapping("/uploadFile")
    public void uploadFile(FsFile fsFile, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        if (!PropertiesUtils.isUpload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!breakpointResume(fsFile)) {
            responseResult(fsFile, request, response);
            return;
        }

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        try {
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            fsFile = processor.submit(fsFile);
        } catch (Exception e) {
            fsFile.setProcessMsg(e.getMessage());
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            LOG.error("Exception occurred when processing upload file[" + fsFile + "]!", e);
        }

        responseResult(fsFile, request, response);
    }

    private boolean breakpointResume(FsFile fsFile) {
        String resumeType = fsFile.getResumeType();
        if (FsConstants.RESUME_TYPE_MD5_CHECK.equals(resumeType)) {
            return checkMd5(fsFile);
        }

        if (FsConstants.RESUME_TYPE_CHUNK_CHECK.equals(resumeType)) {
            return checkChunk(fsFile);
        }

        if (FsConstants.RESUME_TYPE_CHUNKS_MERGE.equals(resumeType)) {
            return mergeChunks(fsFile);
        }

        return !FsConstants.RESUME_TYPE_CHUNK_UPLOAD.equals(resumeType) || uploadChunk(fsFile);
    }

    //FAILED表示参数错误或者程序执行错误，不需要上传文件
    //SUCCESS表示文件已存在并且处理正确，不需要上传文件
    //为空表示文件不存在，需要上传文件
    private boolean checkMd5(FsFile fsFile) {
        String md5 = fsFile.getMd5();
        if (StringUtils.isEmpty(md5)) {
            LOG.error("Md5 check is failed because of param[md5:" + md5 + "] error!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("File md5 is necessary!");
            return false;
        }

        File md5File = new File(PropertiesUtils.getMd5FileDir(), md5);
        if (md5File.exists()) {//表示已经上传完成并且完成合并
            fsFile.setTmpFilePath(md5File.getAbsolutePath());
            return true;
        }

        return false;
    }

    //FAILED表示参数错误，不需要上传分片，结束文件上传
    //SUCCESS表示分片已存在，不需要上传分片
    //为空表示分片不存在，需要上传分片
    private boolean checkChunk(FsFile fsFile) {
        String md5 = fsFile.getMd5();
        Long chunk = fsFile.getChunk();
        Long chunkSize = fsFile.getChunkSize();
        Long blockSize = fsFile.getBlockSize();
        if (StringUtils.isEmpty(md5) || chunk == null
                || chunk < 0 || chunkSize == null || chunkSize < 0
                || blockSize == null || blockSize < 0) {
            LOG.error("Chunk check is failed because of param[md5:"
                    + md5 + ",chunk:" + chunk + ",chunkSize:" + chunkSize
                    + ",blockSize:" + blockSize + "] error!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        File md5File = new File(PropertiesUtils.getMd5FileDir(), md5);
        if (md5File.exists()) {//表示已经上传完成并且完成合并
            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
            return false;
        }

        String chunkFileDir = PropertiesUtils.getChunkFileDir();
        File chunkFile = new File(chunkFileDir, DigestUtils.md5Hex(md5 + chunkSize)
                + File.separator + chunk);
        if (chunkFile.exists() && chunkFile.length() == blockSize) {
            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        }

        return false;
    }

    //FAILED表示参数错误(包括实际分片总数和前台传来的分片总数不一致)或者程序执行错误，上传失败
    //SUCCESS表示分片已合并完成并且正确处理
    //为空表示文件正在合并或者合并失败
    private boolean mergeChunks(FsFile fsFile) {
        String md5 = fsFile.getMd5();
        Long chunkSize = fsFile.getChunkSize();
        Long chunks = fsFile.getChunks();
        if (StringUtils.isEmpty(md5) || chunkSize == null || chunkSize < 0
                || chunks == null || chunks < 0) {
            LOG.error("Chunks merge is failed because of param[md5:"
                    + md5 + "chunks:" + chunks + "chunkSize:" + chunkSize + "] is error!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        if (chunks == 1) {
            File md5File = new File(PropertiesUtils.getMd5FileDir(), md5);
            if (md5File.exists()) {
                fsFile.setTmpFilePath(md5File.getAbsolutePath());
                return true;
            }

            LOG.error("File[" + md5File.getAbsolutePath() + ",md5:" + md5 + "] not exist!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        File chunkDir = new File(PropertiesUtils.getChunkFileDir()
                , DigestUtils.md5Hex(md5 + chunkSize));
        int chunkNum = getChunkNum(chunkDir.getAbsolutePath());
        if (chunks == chunkNum) {
            String key = RedisKey.FS_CHUNK_MERGE_LOCK_ + chunkDir.getName();
            boolean lock = getLock(key, 5 * 60 * 1000);
            if (!lock) {
                fsFile.setProcessMsg("File merging!");
                return false;
            }

            FileChannel outChannel = null;
            try {
                List<File> files = Arrays.asList(getChunks(chunkDir.getAbsolutePath()));
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Integer.valueOf(f1.getName()) - Integer.valueOf(f2.getName());
                    }
                });

                String md5FileDir = PropertiesUtils.getMd5FileDir();
                File md5Dir = new File(md5FileDir);
                if (!md5Dir.exists() && !md5Dir.mkdirs() && !md5Dir.exists()) {
                    fsFile.setProcessMsg("File Merging failed!");
                    return false;
                }

                File md5TmpFile = new File(md5FileDir, md5 + FsConstants.TMM_FILE_SUFFIX);
                FsUtils.deleteFile(md5TmpFile);
                if (!md5TmpFile.createNewFile()) {
                    fsFile.setProcessMsg("File Merging failed!");
                    return false;
                }

                outChannel = new FileOutputStream(md5TmpFile).getChannel();
                FileChannel inChannel = null;
                for (File file : files) {
                    try {
                        inChannel = new FileInputStream(file).getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                    } finally {
                        closeChannel(inChannel);
                    }
                }

                closeChannel(outChannel);
                File md5File = new File(md5FileDir, md5);
                if (!md5TmpFile.renameTo(md5File)) {
                    fsFile.setProcessMsg("File Merging failed!");
                    return false;
                }

                fsFile.setTmpFilePath(md5File.getAbsolutePath());
                FsUtils.deleteFile(chunkDir);
                return true;
            } catch (Exception e) {
                LOG.error("Exception occurred when merging file["
                        + chunkDir.getAbsolutePath() + "]!", e);
                fsFile.setProcessMsg("File Merging failed!");
                return false;
            } finally {
                closeChannel(outChannel);
                FsRedis.getCommonJedis().expire(key, 0);
            }
        }

        File md5File = new File(PropertiesUtils.getMd5FileDir(), md5);
        if (md5File.exists()) {
            fsFile.setTmpFilePath(md5File.getAbsolutePath());
            return true;
        }

        LOG.error("The param total chunks[" + chunks
                + "] is not equal the actual chunks[" + chunkNum + "]!");
        fsFile.setStatus(ProcessStatusEnum.FAILED);
        fsFile.setProcessMsg("Param Error!");
        return false;
    }

    //FAILED表示参数错误或者程序执行错误，上传失败
    //SUCCESS表示单分片文件上传并处理完成
    //为空表示分片上传成功
    private boolean uploadChunk(FsFile fsFile) {
        MultipartFile file = fsFile.getFile();
        Long chunks = fsFile.getChunks();
        String md5 = fsFile.getMd5();

        Long chunkSize = fsFile.getChunkSize();
        if (file == null || file.isEmpty() || chunks == null || chunks < 0
                || StringUtils.isEmpty(md5) || chunkSize == null || chunkSize < 0) {
            LOG.error("Upload chunk is failed because of param[md5:"
                    + md5 + ",chunks:" + chunks + ",chunkSize:" + chunkSize + "] error!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        if (chunks == 1) {
            File md5File = new File(PropertiesUtils.getMd5FileDir(), md5);
            File md5DirFile = md5File.getParentFile();
            if (!md5DirFile.exists() && !md5DirFile.mkdirs() && !md5DirFile.exists()) {
                LOG.error("Creating md5 file directory[" + md5DirFile.getAbsolutePath() + "] failed!");
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                fsFile.setProcessMsg("Upload Error!");
                return false;
            }

            if (!md5File.exists() && !createNewFile(md5File, file) && !md5File.exists()) {
                LOG.error("Creating md5 file[" + md5File.getAbsolutePath() + "] failed!");
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                fsFile.setProcessMsg("Upload Error!");
                return false;
            }

            //fsFile.setTmpFilePath(md5File.getAbsolutePath());
            return false;
        }

        Long chunk = fsFile.getChunk();
        if (chunk == null || chunk < 0) {
            LOG.error("Upload chunk is failed because of param[chunk:" + chunk + "] error!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        String chunkFileDir = PropertiesUtils.getChunkFileDir();
        File chunkFile = new File(chunkFileDir, DigestUtils.md5Hex(md5 + chunkSize)
                + File.separator + chunk);
        File parentFile = chunkFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs() && !parentFile.exists()) {
            LOG.error("Creating chunk file directory[" + parentFile.getAbsolutePath() + "] failed!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Upload Error!");
        } else {
            if (!chunkFile.exists() && !createNewFile(chunkFile, file) && !chunkFile.exists()) {
                LOG.error("Creating chunk file[" + chunkFile.getAbsolutePath() + "] failed!");
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                fsFile.setProcessMsg("Upload Error!");
            }
        }

        return false;
    }

    private boolean createNewFile(File file, MultipartFile multipartFile) {
        try {
            if (file.createNewFile()) {
                multipartFile.transferTo(file);
                return true;
            }

            return false;
        } catch (Exception e) {
            LOG.error("Creating file[" + file.getAbsolutePath() + "] failed!", e);
            return false;
        }
    }

    private void closeChannel(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                //np
            }
        }
    }

    public boolean getLock(String key, long lockExpireTime) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        Long lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
        if (lockFlag != null && lockFlag == 1l) {
            commonJedis.expire(key, (int) (lockExpireTime / 1000));
            return true;
        }

        String millSeconds = commonJedis.get(key);
        if (millSeconds == null) {
            lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
            if (lockFlag != null && lockFlag == 1l) {
                commonJedis.expire(key, (int) (lockExpireTime / 1000));
                return true;
            } else {
                return false;
            }
        }

        long currentMillSeconds = System.currentTimeMillis();
        if (currentMillSeconds - Long.valueOf(millSeconds) >= lockExpireTime) {
            String redisLockTime = commonJedis.getSet(key, currentMillSeconds + "");
            if (redisLockTime == null || redisLockTime.equals(millSeconds)) {
                commonJedis.expire(key, (int) (lockExpireTime / 1000));
                return true;
            }
        }

        return false;
    }

    private int getChunkNum(String chunkDir) {
        File[] chunks = getChunks(chunkDir);
        return chunks == null ? 0 : chunks.length;
    }

    private File[] getChunks(String chunkDir) {
        File dirFile = new File(chunkDir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return new File[0];
        }

        return dirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });
    }

    private void responseResult(FsFile fsFile, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=utf-8");
        String responseFormat = fsFile.getResponseFormat();
        if (FsConstants.RESPONSE_FORMAT_HTML.equals(responseFormat)) {
            response.getWriter().write(fsFile.toHtml(getDomain(request)));
        } else if (FsConstants.RESPONSE_FORMAT_XML.equals(responseFormat)) {
            response.getWriter().write(fsFile.toXml());
        } else if (FsConstants.RESPONSE_FORMAT_JSONP.equals(responseFormat)) {
            response.getWriter().write(fsFile.toJsonp());
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
            response.getWriter().write(fsFile.toJson());
        }
    }


    private String getDomain(HttpServletRequest request) {
        if (!PropertiesUtils.isCanOutputDocumentDomain()) {
            return null;
        }

        String domain = toDomain(request.getHeader("Referer"));
        if (StringUtils.isEmpty(domain)) {
            domain = CustomDomainUtil.getBaseDomain(request.getServerName());
        }

        return domain;
    }

    private String toDomain(String referer) {
        if (StringUtils.isEmpty(referer)) {
            return null;
        }

        int position = referer.indexOf(FsConstants.HTTP_COLON);
        if (position < 0) {
            return null;
        }

        int start = position + FsConstants.HTTP_COLON.length();
        int end = referer.indexOf(FsConstants.PATH_SEPARATOR, start);
        if (end < 0) {
            return null;
        }

        String serverName = referer.substring(start, end);
        if (StringUtils.isEmpty(serverName)) {
            return null;
        }

        if (!serverName.contains(":")) {
            return CustomDomainUtil.getBaseDomain(serverName);
        }

        return null;
    }

    @RequestMapping("/getFile/**")
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        getFile(request, response, false);
    }


    @RequestMapping("/downloadFile/**")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        getFile(request, response, true);
    }

    private void getFile(HttpServletRequest request, HttpServletResponse response
            , boolean download) throws Exception {
        if (!PropertiesUtils.isDownload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestURI = URL_CODEC.decode(request.getRequestURI());
        FsFile fsFile = new FsFile();
        if (!checkAuth(download, requestURI, fsFile, request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String signLevel = PropertiesUtils.getSignLevel();
        String checkSeg = (download ? FsConstants.FILE_URL_DOWNLOAD_FILE : FsConstants.FILE_URL_GET_FILE)
                + signLevel + FsConstants.PATH_SEPARATOR;
        int pos = requestURI.indexOf(checkSeg) + checkSeg.length();
        if (!SignLevelEnum.nn.name().equals(signLevel)) {
            pos = requestURI.indexOf(FsConstants.PATH_SEPARATOR, pos);
        }

        String path = requestURI.substring(pos);
        File file = new File(PropertiesUtils.getFileStoreDir(), path);
        String extension = FilenameUtils.getExtension(file.getName());
        if (!file.exists() || StringUtils.isEmpty(extension)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(MimeUtils.getContentTypeByExt(extension));
        response.setContentLength((int) file.length());
        if (download) {
            String srcFile = FsConstants.PATH_SEPARATOR + FsConstants.FILE_DIR_SRC
                    + FsConstants.PATH_SEPARATOR;
            String filename = file.getName();
            if (path.contains(srcFile)) {
                FsFile originFsFile = HttpUtils.getFsFile(fsFile.getId());
                if (originFsFile == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                filename = originFsFile.getStoredFileName();
            }

            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private boolean checkAuth(boolean download, String requestURI, FsFile fsFile
            , HttpServletRequest request) {
        String signLevel = PropertiesUtils.getSignLevel();
        String checkSeg = (download ? FsConstants.FILE_URL_DOWNLOAD_FILE : FsConstants.FILE_URL_GET_FILE)
                + signLevel + FsConstants.PATH_SEPARATOR;
        int pos = requestURI.indexOf(checkSeg);
        if (pos < 0 || (pos + checkSeg.length()) >= requestURI.length()) {
            return false;
        }

        pos += checkSeg.length();
        if (SignLevelEnum.nn.name().equals(signLevel)) {
            return true;
        }

        int end = requestURI.indexOf(FsConstants.PATH_SEPARATOR, pos);
        if (end < 0 || end >= requestURI.length()) {
            return false;
        }

        String auth = requestURI.substring(pos, end);
        String path = requestURI.substring(end);
        Pattern compile = path.contains(FsConstants.PATH_SEPARATOR
                + FsConstants.FILE_DIR_SRC + FsConstants.PATH_SEPARATOR)
                ? Pattern.compile(originPattern)
                : Pattern.compile(genPattern);
        Matcher matcher = compile.matcher(path);
        if (!matcher.find()) {
            return false;
        }

        fsFile.setCorpCode(matcher.group(1));
        fsFile.setAppCode(matcher.group(2));
        fsFile.setId(matcher.group(3));
        long timestamp;
        String session = null;
        SignLevelEnum signLevelEnum = SignLevelEnum.valueOf(signLevel);
        switch (signLevelEnum) {
            case nn:
                return true;
            case st:
                break;
            case stt:
                pos = auth.indexOf(FsConstants.UNDERLINE);
                if (pos < 0) {
                    return false;
                }

                timestamp = Long.parseLong(auth.substring(pos + 1));
                if (System.currentTimeMillis() - timestamp >= PropertiesUtils.getUrlExpireTime()) {
                    return false;
                }

                fsFile.setTimestamp(timestamp);
                break;
            case sn:
                session = auth;
                return checkSession(session, HttpUtils.getRealRemoteAddress(request));
            case sts:
                String[] split = auth.split(FsConstants.UNDERLINE);
                if (split.length < 3) {
                    return false;
                }

                timestamp = Long.parseLong(split[1]);
                if (System.currentTimeMillis() - timestamp >= PropertiesUtils.getUrlExpireTime()) {
                    return false;
                }

                fsFile.setTimestamp(timestamp);
                session = split[2];
                if (!checkSession(session, HttpUtils.getRealRemoteAddress(request))) {
                    return false;
                }

                break;
            default:
                return false;
        }

        FsServer fsServer = new FsServer();
        fsServer.setSignLevel(signLevelEnum);
        fsServer.setSecret(PropertiesUtils.getServerSecret());
        fsServer.setHost(PropertiesUtils.getServerHost());
        String sign = Signer.sign(fsServer, fsFile, session);
        String paramSign = signLevel + FsConstants.PATH_SEPARATOR + auth;

        return paramSign.equals(sign);
    }

    private boolean checkSession(String session, String remoteAddress) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        String key = RedisKey.FS_CHECK_SESSION_RESULT + session
                + FsConstants.UNDERLINE + remoteAddress;
        String checkResult = commonJedis.get(key);
        if (StringUtils.isNotEmpty(checkResult)) {
            return BooleanUtils.toBoolean(checkResult);
        }

        String sign = Signer.md5(session + FsConstants.VERTICAL_LINE + remoteAddress
                + FsConstants.VERTICAL_LINE + PropertiesUtils.getCheckSessionSecret());
        Map<String, String> paramMap = new HashMap<String, String>(2);
        paramMap.put("sid", session);
        paramMap.put("sign", sign);
        boolean checkSessionResult = false;
        try {
            String result = HttpUtils.doPost(PropertiesUtils.getCheckSessionUrl(), paramMap);
            checkSessionResult = PropertiesUtils.getCheckSessionCorrectResult().equals(result);
            commonJedis.setex(key, PropertiesUtils.getCheckSessionCacheTime()
                    , Boolean.toString(checkSessionResult));
        } catch (Exception e) {
            LOG.error("Exception occurred when checking session[sid:" + session + "] by request[url:" +
                    PropertiesUtils.getCheckSessionUrl() + ",sid:" + session + ",sign:" + sign
                    + ",checkSessionSecret:" + PropertiesUtils.getCheckSessionSecret() + "]!", e);
        }

        return checkSessionResult;
    }

    @RequestMapping("/cutImage")
    public void cutImage(FsFile fsFile, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        if (!PropertiesUtils.isUpload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String fsFileId = fsFile.getId();
        String session = fsFile.getSession();
        Integer x = fsFile.getX();
        Integer y = fsFile.getY();
        Integer w = fsFile.getW();
        Integer h = fsFile.getH();
        if (StringUtils.isEmpty(fsFileId) || StringUtils.isEmpty(session)
                || x == null || y == null || w == null || h == null) {
            LOG.error("One of the param[id:" + fsFileId + ",session:" + session + ",x:" + x + ",y:" + y
                    + ",w:" + w + ",h:" + h + "] is null or empty when cutting image!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param is illegally!");
            responseResult(fsFile, request, response);
            return;
        }

        FsFile dbFsFile = HttpUtils.getFsFile(fsFileId);
        if (dbFsFile == null) {
            LOG.error("File[id:" + fsFileId + ",session:" + session + ",x:" + x + ",y:" + y
                    + ",w:" + w + ",h:" + h + "] not exist when cutting image!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("File not exist!");
            responseResult(fsFile, request, response);
            return;
        }

        String imagePath = PathUtils.getImagePath(dbFsFile);
        if (StringUtils.isEmpty(imagePath)) {
            LOG.error("File[id:" + fsFileId + ",session:" + session + ",x:" + x + ",y:" + y
                    + ",w:" + w + ",h:" + h + "] is not an image when cutting image!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("File not an image!");
            responseResult(fsFile, request, response);
            return;
        }

        File originImageFile = new File(PropertiesUtils.getFileStoreDir(), imagePath);
        if (!originImageFile.exists()) {
            LOG.error("Image[id:" + fsFileId + ",session:" + session + ",x:" + x + ",y:" + y
                    + ",w:" + w + ",h:" + h + ",imagePath:" + imagePath + "] not exist when cutting image!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Image not exist!");
            responseResult(fsFile, request, response);
            return;
        }

        String imageName = x + FsConstants.UNDERLINE + y + FsConstants.UNDERLINE
                + w + FsConstants.UNDERLINE + h + FsConstants.DEFAULT_IMAGE_SUFFIX;
        fsFile.setStoredFileName(imageName);
        File imageFile = new File(originImageFile.getParent(), imageName);
        if (!imageFile.exists()) {
            String[] commands = {FsConstants.FFMPEG, "-i"
                    , originImageFile.getAbsolutePath(), "-vf"
                    , "crop=" + w + ":" + h + ":" + x + ":" + y, "-y"
                    , imageFile.getAbsolutePath()};
            LOG.info("Start executing command[" + FsUtils.toString(commands) + "]!");
            FsUtils.executeCommand(commands);
            LOG.info("End executing command[" + FsUtils.toString(commands) + "]!");
        }

        dbFsFile.setSession(session);
        fsFile.setFileUrl(FsPathUtils.getImageUrl(dbFsFile, imageName));
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        fsFile.setProcessMsg("Cutting image successfully!");
        responseResult(fsFile, request, response);
    }

    @RequestMapping("/reprocessFile")
    public void reprocessFile(String id, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        if (StringUtils.isEmpty(id)) {
            LOG.error("Param[id:" + id + "] is illegally!");
            FsFile fsFile = new FsFile();
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param is illegally!");
            responseResult(fsFile, request, response);
            return;
        }

        FsFile fsFile = HttpUtils.getFsFile(id);
        if (fsFile == null) {
            LOG.error("FsFile[id:" + id + "] not exist!");
            fsFile = new FsFile();
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("File not exist!");
            responseResult(fsFile, request, response);
            return;
        }

        try {
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            processor.submitToReprocess(fsFile);
        } catch (Exception e) {
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg(e.getMessage());
            LOG.error("Exception occurred when reprocessing the fsFile[" + fsFile + "]!", e);
        }

        responseResult(fsFile, request, response);
    }

    @RequestMapping("/asyncProcess")
    public void asyncProcess(FsFile fsFile) throws Exception {//todo sign
        Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
        processor.submit(fsFile);
    }

    @RequestMapping("/backUploadFile")
    public void backUploadFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        MultipartFile file = fsFile.getFile();
        if (file == null || file.getInputStream() == null) {
            LOG.error("FsFile's file is null or it's inputStream is null!");
            response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
            return;
        }

        String sign = fsFile.getSign();
        if (StringUtils.isEmpty(fsFile.getCorpCode())
                || StringUtils.isEmpty(fsFile.getAppCode())
                || fsFile.getProcessor() == null
                || fsFile.getCreateTime() == null
                || StringUtils.isEmpty(fsFile.getId())
                || StringUtils.isEmpty(sign)) {
            LOG.error("One of the param[corpCode:" + fsFile.getCorpCode() + ",appCode:" + fsFile.getAppCode()
                    + ",processor:" + fsFile.getProcessor() + ",createTime:" + fsFile.getCreateTime()
                    + ",id:" + fsFile.getId() + ",sign:" + sign + "] is null or empty!");
            response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
            return;
        }

        String newSign = Signer.sign(fsFile);
        if (!sign.equals(newSign)) {
            LOG.error("The generating sign[" + sign + "] is not equal the param sign[" + sign + "]!");
            response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
            return;
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        String tmpDirPath = FsPathUtils.getImportTmpDirPath();
        try {
            File tmpDirFile = new File(tmpDirPath);
            if (!tmpDirFile.exists() && !tmpDirFile.mkdirs()) {
                LOG.error("Creating directory[" + tmpDirPath
                        + "] failed when back uploading the fsFile[" + fsFile + "]!");
                response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            File tmpFile = new File(tmpDirFile, file.getOriginalFilename());
            if (!tmpFile.exists() && !tmpFile.createNewFile()) {
                LOG.error("Creating file[" + tmpFile.getAbsolutePath()
                        + "] failed when back uploading the fsFile[" + fsFile + "]!");
                response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            String genFilePath = processor.getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                LOG.error("Creating directory[" + genFilePath
                        + "] failed when back uploading the fsFile[" + fsFile + "]!");
                response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            outputStream = new FileOutputStream(tmpFile);
            inputStream = file.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            FsUtils.decompress(tmpFile.getAbsolutePath(), genFilePath);
        } catch (Exception e) {
            LOG.error("Exception occurred when back uploading the fsFile[" + fsFile + "]!", e);
            response.getWriter().write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            FsUtils.deleteFile(tmpDirPath);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }
    }
}
