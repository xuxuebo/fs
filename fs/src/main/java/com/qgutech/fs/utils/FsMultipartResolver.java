package com.qgutech.fs.utils;


import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FsMultipartResolver implements MultipartResolver {

    private boolean resolveLazily = false;
    private int maxPostSize = 1024 * 1024 * 1024;

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request)
            throws MultipartException {
        Assert.notNull(request, "Request must not be null");
        if (this.resolveLazily) {
            return new DefaultMultipartHttpServletRequest(request) {
                @Override
                protected void initializeMultipart() {
                    MultipartParsingResult parsingResult = parseRequest(request);
                    setMultipartFiles(parsingResult.getMultipartFiles());
                    setMultipartParameters(parsingResult.getMultipartParameters());
                    setMultipartParameterContentTypes(parsingResult.getMultipartParameterContentTypes());
                }
            };
        } else {
            MultipartParsingResult parsingResult = parseRequest(request);
            return new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(),
                    parsingResult.getMultipartParameters(), parsingResult.getMultipartParameterContentTypes());
        }
    }

    protected MultipartParsingResult parseRequest(HttpServletRequest request) {
        try {
            String saveDirectory = FsPathUtils.getImportTmpDirPath();
            File saveDirFile = new File(saveDirectory);
            if (!saveDirFile.exists() && !saveDirFile.mkdirs() && !saveDirFile.exists()) {
                throw new IOException("Creating directory[" + saveDirectory + "] failed!");
            }

            MultipartRequest multipartRequest =
                    new MultipartRequest(request, saveDirectory, maxPostSize
                            , determineEncoding(request)
                            , new DefaultFileRenamePolicy());
            MultiValueMap<String, MultipartFile> multipartFiles =
                    new LinkedMultiValueMap<String, MultipartFile>();
            Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
            Map<String, String> multipartParameterContentTypes = new HashMap<String, String>();
            Enumeration fileNames = multipartRequest.getFileNames();
            while (fileNames.hasMoreElements()) {
                String fileName = (String) fileNames.nextElement();
                File file = multipartRequest.getFile(fileName);
                String originalFileName = multipartRequest.getOriginalFileName(fileName);
                String contentType = multipartRequest.getContentType(fileName);
                FsMultipartFile multipartFile = new FsMultipartFile(fileName
                        , file, originalFileName, contentType);
                multipartFiles.add(fileName, multipartFile);
            }

            Enumeration parameterNames = multipartRequest.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                String contentType = multipartRequest.getContentType(parameterName);
                multipartParameterContentTypes.put(parameterName, contentType);
                String[] parameterValues = multipartRequest.getParameterValues(parameterName);
                multipartParameters.put(parameterName, parameterValues);
            }

            return new MultipartParsingResult(multipartFiles, multipartParameters
                    , multipartParameterContentTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static class MultipartParsingResult {

        private final MultiValueMap<String, MultipartFile> multipartFiles;

        private final Map<String, String[]> multipartParameters;

        private final Map<String, String> multipartParameterContentTypes;

        public MultipartParsingResult(MultiValueMap<String, MultipartFile> mpFiles,
                                      Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {
            this.multipartFiles = mpFiles;
            this.multipartParameters = mpParams;
            this.multipartParameterContentTypes = mpParamContentTypes;
        }

        public MultiValueMap<String, MultipartFile> getMultipartFiles() {
            return this.multipartFiles;
        }

        public Map<String, String[]> getMultipartParameters() {
            return this.multipartParameters;
        }

        public Map<String, String> getMultipartParameterContentTypes() {
            return this.multipartParameterContentTypes;
        }
    }

    protected String determineEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }

        return encoding;
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        if (request == null) {
            return;
        }

        for (List<MultipartFile> files : request.getMultiFileMap().values()) {
            for (MultipartFile file : files) {
                if (file instanceof FsMultipartFile) {
                    FsMultipartFile fsMultipartFile = (FsMultipartFile) file;
                    fsMultipartFile.cleanup();
                }
            }
        }
    }

    public boolean isResolveLazily() {
        return resolveLazily;
    }

    public void setResolveLazily(boolean resolveLazily) {
        this.resolveLazily = resolveLazily;
    }

    public int getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(int maxPostSize) {
        this.maxPostSize = maxPostSize;
    }
}
