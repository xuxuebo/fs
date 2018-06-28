package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.SignLevelEnum;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FsPathUtils extends PathUtils {

    public static String getImportTmpDirPath() {
        return getImportTmpDirPath(FsUtils.generateUUID());
    }

    public static String getImportTmpDirPath(String filename) {
        return PropertiesUtils.getFileStoreDir() + FsConstants.FILE_DIR_TMP
                + File.separator + FsConstants.FILE_DIR_IMPT
                + File.separator + FsUtils.formatDateToYYMM(new Date())
                + File.separator + filename;
    }

    public static String getExportTmpDirPath() {
        return PropertiesUtils.getFileStoreDir() + FsConstants.FILE_DIR_TMP
                + File.separator + FsConstants.FILE_DIR_EXPT
                + File.separator + FsUtils.formatDateToYYMM(new Date())
                + File.separator + FsUtils.generateUUID();
    }

    public static FsServer getFsServer() {
        FsServer fsServer = new FsServer();
        fsServer.setSignLevel(SignLevelEnum.valueOf(PropertiesUtils.getSignLevel()));
        fsServer.setHost(PropertiesUtils.getServerHost());
        fsServer.setSecret(PropertiesUtils.getServerSecret());
        fsServer.setServerCode(PropertiesUtils.getServerCode());

        return fsServer;
    }

    public static String getOriginFileUrl(FsFile fsFile) {
        return getOriginFileUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), fsFile.getSession());
    }

    public static String getImageUrl(FsFile fsFile) {
        return getImageUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), fsFile.getSession());
    }

    public static String getImageUrl(FsFile fsFile, String subFilePath) {
        String imageUrl = getImageUrl(fsFile);
        if (StringUtils.isEmpty(imageUrl)) {
            return null;
        }

        int index = imageUrl.lastIndexOf(FsConstants.PATH_SEPARATOR);
        if (index < 0) {
            return imageUrl;
        }

        if (subFilePath.startsWith(FsConstants.PATH_SEPARATOR)) {
            return imageUrl.substring(0, index) + subFilePath;
        } else {
            return imageUrl.substring(0, index + 1) + subFilePath;
        }
    }

    /**
     *
     * @param pathUrl
     * @return
     */
    public static List<String> absolutePath(List<String> pathUrl,String corpCode){
        List<String> paths = new ArrayList<String>();
        String fileStoreDir = "/web/fs/fs/";
        for(String s : pathUrl){
            if(s.indexOf(corpCode)>-1){
                s = fileStoreDir + s.substring(s.indexOf(corpCode),s.length());
                s.replace("\\\\","/");
                paths.add(s);
            }
        }
        return paths;
    }

    /**
     *
     * @param pathUrl
     * @param corpCode
     * @param fileName
     * @return
     */
    public static String compressFile(List<String> pathUrl,String corpCode,String fileName){
        String dateStr = FsUtils.formatDateToYYMM(new Date());
        String path = "/web/fs/fs/"+corpCode+"/km/src/file/zip/"+dateStr+"/";
        String zipPath = "/web/fs/fs/"+corpCode+"/km/src/file/zip/"+dateStr+"/"+fileName+".zip";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        //压缩文件
        zipFiles(pathUrl,zipPath);

        return zipPath;
    }

    public static void zipFiles(List<String> srcFileUrls, String zipFileUrl){
        File zipFile = new File(zipFileUrl);
        List<File> srcFile = new ArrayList<File>(srcFileUrls.size());
        File file ;
        for(String path : srcFileUrls){
            file = new File(path);
            srcFile.add(file);
        }
        byte[] buf=new byte[1024];
        try {
            //ZipOutputStream类：完成文件或文件夹的压缩
            if(!zipFile.exists()){

                zipFile.createNewFile();

            }
            ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zipFile));
            for(int i=0;i<srcFile.size();i++){
                FileInputStream in=new FileInputStream(srcFile.get(i));
                out.putNextEntry(new ZipEntry(srcFile.get(i).getName()));
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
