package com.qgutech.fs.utils;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

public class ConvertUtils {
    public static final int WORD_TO_PDF_FORMAT = 17;
    public static final int XLS_TO_PDF_FORMAT = 0;
    private static final int PPT_TO_PDF_FORMAT = 32;
    public static final int PPT_TO_PNG_FORMAT = 17;

    public static void wordToPdf(String inputFile, String pdfFile) {
        ActiveXComponent app = null;
        Dispatch doc = null;
        try {
            ComThread.InitSTA();
            //打开word应用程序
            app = new ActiveXComponent("Word.Application");
            //设置word不可见
            app.setProperty("Visible", false);
            //获得word中所有打开的文档,返回Documents对象
            Dispatch docs = app.getProperty("Documents").toDispatch();
            //调用Documents对象中Open方法打开文档，并返回打开的文档对象Document
            doc = Dispatch.call(docs, "Open", inputFile, false, true).toDispatch();
            //调用Document对象的SaveAs方法，将文档保存为pdf格式
            Dispatch.call(doc, "ExportAsFixedFormat", pdfFile, WORD_TO_PDF_FORMAT);
        } finally {
            //关闭文档
            if (doc != null) {
                Dispatch.call(doc, "Close");
            }

            //关闭word应用程序
            if (app != null) {
                app.invoke("Quit");
            }

            ComThread.Release();
        }
    }


    public static void excelToPdf(String inputFile, String pdfFile) {
        ActiveXComponent app = null;
        Dispatch excel = null;
        try {
            ComThread.InitSTA();
            app = new ActiveXComponent("Excel.Application");
            app.setProperty("Visible", false);
            Dispatch excels = app.getProperty("Workbooks").toDispatch();
            excel = Dispatch.call(excels, "Open", inputFile, false, true).toDispatch();
            Dispatch.call(excel, "ExportAsFixedFormat", XLS_TO_PDF_FORMAT, pdfFile);
        } finally {
            if (excel != null) {
                Dispatch.call(excel, "Close");
            }

            if (app != null) {
                app.invoke("Quit");
            }

            ComThread.Release();
        }
    }

    public static void pptToPdf(String inputFile, String pdfFile) {
        ActiveXComponent app = null;
        Dispatch ppt = null;
        try {
            ComThread.InitSTA();
            app = new ActiveXComponent("PowerPoint.Application");
            //app.setProperty("Visible", false);
            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            ppt = Dispatch.call(ppts, "Open", inputFile, true, true, false).toDispatch();
            Dispatch.call(ppt, "SaveAs", pdfFile, PPT_TO_PDF_FORMAT);
        } finally {
            if (ppt != null) {
                Dispatch.call(ppt, "Close");
            }
            if (app != null) {
                app.invoke("Quit");
            }

            ComThread.Release();
        }
    }

    public static void pptToPng(String inputFile, String pngFile) {
        ActiveXComponent app = null;
        Dispatch ppt = null;
        try {
            ComThread.InitSTA();
            app = new ActiveXComponent("PowerPoint.Application");
            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            ppt = Dispatch.call(ppts, "Open", inputFile, true, true, false).toDispatch();
            Dispatch.call(ppt, "SaveAs", pngFile, PPT_TO_PNG_FORMAT);
        } finally {
            if (ppt != null) {
                Dispatch.call(ppt, "Close");
            }
            if (app != null) {
                app.invoke("Quit");
            }

            ComThread.Release();
        }
    }
}
