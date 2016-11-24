package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.SignLevelEnum;
import com.qgutech.fs.processor.Processor;
import com.qgutech.fs.processor.ProcessorFactory;
import com.qgutech.fs.utils.*;
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
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.JedisCommands;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
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

        try {
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            fsFile = processor.submit(fsFile);
        } catch (Exception e) {
            fsFile.setProcessMsg(e.getMessage());
            LOG.error("Exception occurred when processing upload file[" + fsFile + "]!", e);
        }

        responseResult(fsFile, request, response);
    }

    private void responseResult(FsFile fsFile, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=utf-8");
        String responseFormat = fsFile.getResponseFormat();
        if (FsConstants.RESPONSE_FORMAT_HTML.equals(responseFormat)) {
            response.getWriter().write(fsFile.toHtml(getDomain(request)));
        } else if (FsConstants.RESPONSE_FORMAT_XML.equals(responseFormat)) {
            response.getWriter().write(fsFile.toXml());
        } else {
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
            LOG.error("Param[id:" + fsFileId + ",session:" + session + ",x:" + x + ",y:" + y
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
    public String reprocessFile(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/asyncProcess")
    public String asyncProcess(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/backUploadFile")
    public void backUploadFile(FsFile fsFile) throws Exception {
        MultipartFile file = fsFile.getFile();
        if (file == null || file.getInputStream() == null) {
            LOG.error("FsFile's file is null or it's inputStream is null!");
            return;
        }

        if (StringUtils.isEmpty(fsFile.getCorpCode())
                || StringUtils.isEmpty(fsFile.getAppCode())
                || fsFile.getProcessor() == null
                || fsFile.getCreateTime() == null
                || StringUtils.isEmpty(fsFile.getId())) {
            LOG.error("Param[corpCode:" + fsFile.getCorpCode() + ",appCode:" + fsFile.getAppCode()
                    + ",processor:" + fsFile.getProcessor() + ",createTime:" + fsFile.getCreateTime()
                    + ",id:" + fsFile.getId() + "] is null or empty!");
            return;
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        String tmpDirPath = FsPathUtils.getImportTmpDirPath();
        try {
            File tmpDirFile = new File(tmpDirPath);
            if (!tmpDirFile.exists() && !tmpDirFile.mkdirs()) {
                return;
            }

            File tmpFile = new File(tmpDirFile, file.getOriginalFilename());
            if (!tmpFile.exists() && !tmpFile.createNewFile()) {
                return;
            }

            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            String genFilePath = processor.getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                return;
            }

            outputStream = new FileOutputStream(tmpFile);
            inputStream = file.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            FsUtils.decompress(tmpFile.getAbsolutePath(), genFilePath);
        } finally {
            FsUtils.deleteFile(tmpDirPath);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }
    }
}
