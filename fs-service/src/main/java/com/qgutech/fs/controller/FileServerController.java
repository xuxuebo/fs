package com.qgutech.fs.controller;

import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.FsFileService;
import com.qgutech.fs.service.FsServerService;
import com.qgutech.fs.utils.*;
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

    @RequestMapping("/remoteMethod")
    public void handleRemoteRequest(RemoteRequest remoteRequest
            , HttpServletResponse response) throws Exception {
        RemoteResponse remoteResponse;
        try {
            remoteResponse = remoteRequest.invoke();
        } catch (Throwable e) {
            remoteResponse = new RemoteResponse();
            remoteResponse.setExceptionOccurs(true);
            remoteResponse.setContent(e);
        }

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(gson.toJson(remoteResponse));
    }

    public static void main(String[] args) {
        Audio audio = new Audio();
        audio.setDuration("111");
        audio.setBitRate(10);
        System.out.println(gson.toJson(audio));
    }

    @RequestMapping("/getFile")
    public void getFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            if (!validate(fsFile, writer, true)) {
                return;
            }

            FsFile file = fsFileService.get(fsFile.getId());
            if (file == null) {
                return;
            }

            writer.write(gson.toJson(file));
        } catch (Exception e) {
            LOG.error("Exception occurred  when getting fsFile by condition[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/deleteFile")
    public void deleteFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            if (!validate(fsFile, writer, true)) {
                return;
            }

            fsFileService.delete(fsFile.getId());
        } catch (Exception e) {
            LOG.error("Exception occurred  when deleting fsFile by condition[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/saveFile")
    public void saveFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            if (!validate(fsFile, writer, false)) {
                return;
            }

            String fsFileId = fsFileService.save(fsFile);
            writer.write(fsFileId);
        } catch (Exception e) {
            LOG.error("Exception occurred when saving fsFile[" + fsFile + "]!", e);
            writer.write(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @RequestMapping("/updateFile")
    public void updateFile(FsFile fsFile, HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        try {
            if (!validate(fsFile, writer, true)) {
                return;
            }

            String fsFileId = fsFile.getId();
            FsFile dbFsFile = fsFileService.get(fsFileId);
            if (dbFsFile == null) {
                LOG.error("FsFile[id:" + fsFileId + "] not exist when updating fsFile!");
                writer.print(FsConstants.RESPONSE_RESULT_FS_FILE_NOT_EXIST);
                return;
            }

            dbFsFile.merge(fsFile);
            fsFileService.update(dbFsFile);
        } catch (Exception e) {
            LOG.error("Exception occurred  when updating fsFile[" + fsFile + "]!", e);
            writer.print(FsConstants.RESPONSE_RESULT_ERROR);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private boolean validate(FsFile fsFile, PrintWriter writer, boolean checkFsFileId) {
        String sign = fsFile.getSign();
        String serverHost = fsFile.getServerHost();
        Long timestamp = fsFile.getTimestamp();
        String fsFileId = fsFile.getId();
        String serverCode = fsFile.getServerCode();
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(serverHost)
                || timestamp == null || (checkFsFileId && StringUtils.isEmpty(fsFileId))
                || StringUtils.isEmpty(serverCode)) {
            LOG.error("One of the param[sign:" + sign + ",serverHost:" + serverHost + ",timestamp:"
                    + timestamp + (checkFsFileId ? (",id:" + fsFileId) : StringUtils.EMPTY)
                    + ",serverCode:" + serverCode + "] is null or empty!");
            writer.write(FsConstants.RESPONSE_RESULT_PARAM_ILLEGAL);
            return false;
        }

        long timeMillis = System.currentTimeMillis();
        long maxWaitForRequest = PropertiesUtils.getMaxWaitForRequest();
        if (timeMillis - timestamp >= maxWaitForRequest) {
            LOG.error("The request timeout because of  the current timestamp[" + timeMillis
                    + "] subtract param timestamp[" + timestamp
                    + "] bigger than maxWaitTime[" + maxWaitForRequest + "]!");
            writer.write(FsConstants.RESPONSE_RESULT_TIME_OUT);
            return false;
        }

        FsServer fsServer = fsServerService.getFsServerByServerHostAndServerCode(serverHost, serverCode);
        if (fsServer == null) {
            LOG.error("Can not find a fs server which  host is "
                    + serverHost + " and serverCode is " + serverCode + " !");
            writer.write(FsConstants.RESPONSE_RESULT_SERVER_NOT_EXIST);
            return false;
        }

        String secret = fsServer.getSecret();
        String newSign = checkFsFileId ? Signer.sign(fsFileId, serverHost, serverCode, secret, timestamp)
                : Signer.sign(serverHost, serverCode, secret, timestamp);
        if (!sign.equals(newSign)) {
            LOG.error("The generating sign[sign:" + newSign
                    + (checkFsFileId ? (",id:" + fsFileId) : StringUtils.EMPTY)
                    + ",serverHost:" + serverHost + ",timestamp:" + timestamp
                    + ",secret:" + secret + "] is not equal the param sign[sign:" + sign + "]!");
            writer.write(FsConstants.RESPONSE_RESULT_SIGN_ERROR);
            return false;
        }

        return true;
    }
}
