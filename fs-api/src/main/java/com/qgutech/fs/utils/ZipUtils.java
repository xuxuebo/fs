package com.qgutech.fs.utils;

/**
 * Created by Administrator on 2018/6/27.
 */

import com.qgutech.fs.domain.FsFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private ZipUtils(){
    }

    public static void doCompress(String srcFile, String zipFile) throws IOException {
        doCompress(new File(srcFile), new File(zipFile));
    }

    /**
     * 文件压缩
     * @param srcFile 目录或者单个文件
     * @param zipFile 压缩后的ZIP文件
     */
    public static void doCompress(File srcFile, File zipFile) throws IOException {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            doCompress(srcFile, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();//记得关闭资源
        }
    }

    public static void doCompress(String filelName, ZipOutputStream out) throws IOException{
        doCompress(new File(filelName), out);
    }

    public static void doCompress(File file, ZipOutputStream out) throws IOException{
        doCompress(file, out, "");
    }

    public static void doCompress(File inFile, ZipOutputStream out, String dir) throws IOException {
        if ( inFile.isDirectory() ) {
            File[] files = inFile.listFiles();
            if (files!=null && files.length>0) {
                for (File file : files) {
                    String name = inFile.getName();
                    if (!"".equals(dir)) {
                        name = dir + "/" + name;
                    }
                    ZipUtils.doCompress(file, out, name);
                }
            }
        } else {
            ZipUtils.doZip(inFile, out, dir);
        }
    }

    public static void doZip(File inFile, ZipOutputStream out, String dir) throws IOException {
        String entryName = null;
        if (!"".equals(dir)) {
            entryName = dir + "/" + inFile.getName();
        } else {
            entryName = inFile.getName();
        }
        ZipEntry entry = new ZipEntry(entryName);
        out.putNextEntry(entry);

        int len = 0 ;
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(inFile);
        while ((len = fis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            out.flush();
        }
        out.closeEntry();
        fis.close();
    }

    public static void zipFiles(List<String> srcfileUrls, String zipFileUrl){
        File zipFile = new File(zipFileUrl);
        List<File> srcFile = new ArrayList<File>(srcfileUrls.size());
        File file ;
        for(String path : srcfileUrls){
            file = new File(path.replace("\\\\","/"));
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
                FileInputStream in=new FileInputStream("//web//fs//fs//lbox//km//src//file//1806//1530006136328//402880a3643479ba01643b7e30460026.doc");
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

    public static void  zipFiles(String zipPath, String fileName, List<String> filePaths){
        //压缩文件初始设置
        String fileZip = fileName + ".zip"; // 拼接zip文件
        String filePath = zipPath + "\\" + fileZip;//之后用来生成zip文件

        List<File> fileList = new ArrayList<File>(filePaths.size());
        File file ;
        for(String path : filePaths){
            file = new File(path.replace("\\\\","/"));
            fileList.add(file);
        }

        // 创建临时压缩文件
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry ze = null;
            for (int i = 0; i < fileList.size(); i++) {//将所有需要下载的pdf文件都写入临时zip文件
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileList.get(i)));
                ze = new ZipEntry(fileList.get(i).getName());
                zos.putNextEntry(ze);
                int s = -1;
                while ((s = bis.read()) != -1) {
                    zos.write(s);
                }
                bis.close();
            }
            zos.flush();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}