package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
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
import redis.clients.jedis.JedisCommands;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
        if (!PropertiesUtils.isDownload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestURI = URL_CODEC.decode(request.getRequestURI());
        if (!checkAuth(requestURI, request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String signLevel = PropertiesUtils.getSignLevel();
        String checkSeg = FsConstants.FILE_URL_COMMON + signLevel + FsConstants.PATH_SEPARATOR;
        int pos = requestURI.indexOf(checkSeg) + checkSeg.length();
        if (!SignLevelEnum.nn.name().equals(signLevel)) {
            pos = requestURI.indexOf(FsConstants.PATH_SEPARATOR, pos);
        }

        File file = new File(PropertiesUtils.getFileStoreDir(), requestURI.substring(pos));
        String extension = FilenameUtils.getExtension(file.getName());
        if (!file.exists() || StringUtils.isEmpty(extension)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(MimeUtils.getContentTypeByExt(extension));
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            response.setContentLength((int) file.length());
            IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private boolean checkAuth(String requestURI, HttpServletRequest request) {
        String signLevel = PropertiesUtils.getSignLevel();
        String checkSeg = FsConstants.FILE_URL_COMMON + signLevel + FsConstants.PATH_SEPARATOR;
        int pos = requestURI.indexOf(checkSeg) + checkSeg.length();
        if (pos < 0 || pos >= requestURI.length()) {
            return false;
        }

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

        FsFile fsFile = new FsFile();
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

    @RequestMapping("/downloadFile/*")
    public String downloadFile(FsFile fsFile, HttpServletResponse response) {
        if (!PropertiesUtils.isDownload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return null;
    }

    @RequestMapping("/cutImage")
    public String cutImage(FsFile fsFile, HttpServletResponse response) {
        if (!PropertiesUtils.isUpload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        String fsFileId = fsFile.getId();


        return null;
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
    public String backUploadFile(FsFile fsFile) {
        return null;
    }
}
