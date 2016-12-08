必须要求：1、安装Microsoft Office 2010及其以上 ; 2、安装.NET framework 4.0
基本用法：OfficeToPDF.exe inputFile(输入文件全路径) outputFile(输处文件全路径)
参考地址：http://officetopdf.codeplex.com/documentation
支持类型：Word (.doc, .dot,  .docx, .dotx, .docm, .dotm, .rtf, .wpd)
        Excel (.xls, .xlsx, .xlsm, .xlsb, .xlt, .xltx, .xltm, .csv)
        Powerpoint (.ppt, .pptx, .pptm, .pps, .ppsx, .ppsm, .pot, .potx, .potm)
        Visio (.vsd, .vsdx, .vsdm, .svg) [Requires >= Visio 2013 for .svg, .vsdx and .vsdm support]
        Publisher (.pub)
        Outlook (.msg, .vcf, .ics)
        Project (.mpp) [Requires Project >= 2010 for .mpp support]
        OpenOffice (.odt, .odp, .ods)
错误码：
0 - Success
1 - Failure
2 - Unknown Error
4 - File protected by password
8 - Invalid arguments
16 - Unable to open the source file
32 - Unsupported file format
64 - Source file not found
128 - Output directory not found