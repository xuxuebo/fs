package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.SignLevelEnum;
import com.qgutech.fs.processor.Processor;
import com.qgutech.fs.processor.ProcessorFactory;
import com.qgutech.fs.utils.CustomDomainUtil;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.MimeUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Controller
@DependsOn({"propertiesUtils"})
@RequestMapping("/file/*")
public class FileController {

    private static final Log LOG = LogFactory.getLog(FileController.class);

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

        String requestURI = request.getRequestURI();
        String signLevel = PropertiesUtils.getSignLevel();
        String validSegment = FsConstants.FILE_URL_COMMON
                + signLevel + FsConstants.PATH_SEPARATOR;
        int position = requestURI.indexOf(validSegment);
        if (position < 0 || position > (requestURI.length() - validSegment.length())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (SignLevelEnum.nn.name().equals(signLevel)) {
            File file = new File(PropertiesUtils.getFileStoreDir()
                    , requestURI.substring(position + validSegment.length()));
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

    public static void main(String[] args) {
        long length = new File("C:\\Users\\Administrator\\Desktop\\test\\3.png").length();
        System.out.println(length);
    }
}
