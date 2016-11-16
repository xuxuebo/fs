package com.qgutech.fs.controller;

import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.FsFileService;
import com.qgutech.fs.service.FsServerService;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.PropertiesUtils;
import com.qgutech.fs.utils.Signer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Controller
@RequestMapping("/fileServer/*")
public class FileServerController {

    private static final Log LOG = LogFactory.getLog(FileServerController.class);
    private static final Gson gson = new Gson();

    @Resource
    private FsFileService fsFileService;
    @Resource
    private FsServerService fsServerService;

    @RequestMapping("/getFile")
    public void getFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            String sign = fsFile.getSign();
            String serverHost = fsFile.getServerHost();
            Long timestamp = fsFile.getTimestamp();
            String fsFileId = fsFile.getId();
            String serverCode = fsFile.getServerCode();
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(serverHost)
                    || timestamp == null || StringUtils.isEmpty(fsFileId)
                    || StringUtils.isEmpty(serverCode)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            long timeMillis = System.currentTimeMillis();
            if (timeMillis - timestamp >= PropertiesUtils.getMaxWaitForRequest()) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsServer fsServer = fsServerService.getFsServerByServerHostAndServerCode(serverHost, serverCode);
            if (fsServer == null) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(fsFileId, serverHost, fsServer.getSecret(), timestamp);
            if (!sign.equals(newSign)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsFile file = fsFileService.get(fsFileId);
            if (file == null) {
                return;
            }

            writer.write(gson.toJson(file));
        } catch (Exception e) {
            LOG.error("Exception occurred  when save fsFile[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/deleteFile")
    public void deleteFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            String sign = fsFile.getSign();
            String serverHost = fsFile.getServerHost();
            Long timestamp = fsFile.getTimestamp();
            String fsFileId = fsFile.getId();
            String serverCode = fsFile.getServerCode();
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(serverHost)
                    || timestamp == null || StringUtils.isEmpty(fsFileId)
                    || StringUtils.isEmpty(serverCode)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            long timeMillis = System.currentTimeMillis();
            if (timeMillis - timestamp >= PropertiesUtils.getMaxWaitForRequest()) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsServer fsServer = fsServerService.getFsServerByServerHostAndServerCode(serverHost, serverCode);
            if (fsServer == null) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(fsFileId, serverHost, fsServer.getSecret(), timestamp);
            if (!sign.equals(newSign)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            fsFileService.delete(fsFileId);
        } catch (Exception e) {
            LOG.error("Exception occurred  when save fsFile[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/saveFile")
    public void saveFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            String sign = fsFile.getSign();
            String serverHost = fsFile.getServerHost();
            Long timestamp = fsFile.getTimestamp();
            String serverCode = fsFile.getServerCode();
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(serverHost)
                    || timestamp == null || StringUtils.isEmpty(serverCode)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            long timeMillis = System.currentTimeMillis();
            if (timeMillis - timestamp >= PropertiesUtils.getMaxWaitForRequest()) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsServer fsServer = fsServerService.getFsServerByServerHostAndServerCode(serverHost, serverCode);
            if (fsServer == null) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(serverHost, fsServer.getSecret(), timestamp);
            if (!sign.equals(newSign)) {
                writer.write(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String fsFileId = fsFileService.save(fsFile);
            writer.write(fsFileId);
        } catch (Exception e) {
            LOG.error("Exception occurred  when save fsFile[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/updateFile")
    public void updateFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            String fsFileId = fsFile.getId();
            String sign = fsFile.getSign();
            String serverHost = fsFile.getServerHost();
            Long timestamp = fsFile.getTimestamp();
            String serverCode = fsFile.getServerCode();
            if (StringUtils.isEmpty(fsFileId) || StringUtils.isEmpty(sign)
                    || StringUtils.isEmpty(serverHost) || timestamp == null
                    || StringUtils.isEmpty(serverCode)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsFile dbFsFile = fsFileService.get(fsFileId);
            if (dbFsFile == null) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            long timeMillis = System.currentTimeMillis();
            if (timeMillis - timestamp >= PropertiesUtils.getMaxWaitForRequest()) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            dbFsFile.merge(fsFile);
            FsServer fsServer = fsServerService.getFsServerByServerHostAndServerCode(serverHost, serverCode);
            if (fsServer == null) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(fsFileId, serverHost, fsServer.getSecret(), timestamp);
            if (!sign.equals(newSign)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            fsFileService.update(dbFsFile);
            writer.print(FsConstants.RESPONSE_RESULT_SUCCESS);
        } catch (Exception e) {
            LOG.error("Exception occurred  when update fsFile[" + fsFile + "]!", e);
            writer.print(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
