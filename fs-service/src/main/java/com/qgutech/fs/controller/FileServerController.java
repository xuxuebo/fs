package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.FsFileService;
import com.qgutech.fs.service.FsServerService;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.PropertiesUtils;
import com.qgutech.fs.utils.Signer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/fileServer/*")
public class FileServerController {

    private static final Log LOG = LogFactory.getLog(FileServerController.class);

    @Resource
    private FsFileService fsFileService;
    @Resource
    private FsServerService fsServerService;

    @RequestMapping("/saveFile")
    public void saveFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            String sign = fsFile.getSign();
            String serverHost = fsFile.getServerHost();
            Long timestamp = fsFile.getTimestamp();
            String corpCode = fsFile.getCorpCode();
            String serverCode = fsFile.getServerCode();
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(serverHost)
                    || timestamp == null || StringUtils.isEmpty(corpCode)
                    || StringUtils.isEmpty(serverCode)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            long timeMillis = System.currentTimeMillis();
            if (timeMillis - timestamp >= PropertiesUtils.getMaxWaitForRequest()) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            List<FsServer> fsServers = fsServerService.getUploadFsServerList(corpCode);
            if (CollectionUtils.isEmpty(fsServers)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsServer fsServer = null;
            for (FsServer server : fsServers) {
                if (serverHost.equals(server.getHost())
                        && serverCode.equals(server.getServerCode())) {
                    fsServer = server;
                    break;
                }
            }

            if (fsServer == null) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(serverHost, fsServer.getSecret(), timestamp);
            if (sign.equals(newSign)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String fsFileId = fsFileService.save(fsFile);
            writer.print(fsFileId);
        } catch (Exception e) {
            LOG.error("Exception occurred  when save fsFile[" + fsFile + "]!", e);
            writer.print(FsConstants.RESPONSE_RESULT_ERROR);
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
            if (StringUtils.isEmpty(fsFileId) || StringUtils.isEmpty(sign)
                    || StringUtils.isEmpty(serverHost) || timestamp == null) {
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
            List<FsServer> fsServers = fsServerService.getUploadFsServerList(dbFsFile.getCorpCode());
            if (CollectionUtils.isEmpty(fsServers)) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            FsServer fsServer = null;
            for (FsServer fs : fsServers) {
                if (serverHost.equals(fs.getHost())
                        && dbFsFile.getServerCode().equals(fs.getServerCode())) {
                    fsServer = fs;
                    break;
                }
            }

            if (fsServer == null) {
                writer.print(FsConstants.RESPONSE_RESULT_ERROR);
                return;
            }

            String newSign = Signer.sign(fsFileId, serverHost, fsServer.getSecret(), timestamp);
            if (sign.equals(newSign)) {
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
