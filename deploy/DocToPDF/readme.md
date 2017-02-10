## 必须要求
1. 安装Microsoft Office 2010及其以上；
2. 安装.NET framework 4.0。

## 基本用法
OfficeToPDF.exe inputFile(输入文件全路径) outputFile(输处文件全路径)

## 支持类型
1. Word (.doc, .dot,  .docx, .dotx, .docm, .dotm, .rtf, .wpd);
2. Excel (.xls, .xlsx, .xlsm, .xlsb, .xlt, .xltx, .xltm, .csv);
3. Powerpoint (.ppt, .pptx, .pptm, .pps, .ppsx, .ppsm, .pot, .potx, .potm);
4. Visio (.vsd, .vsdx, .vsdm, .svg) [**Requires >= Visio 2013 for .svg, .vsdx and .vsdm support**];
5. Publisher (.pub);
6. Outlook (.msg, .vcf, .ics);
7. Project (.mpp) [**Requires Project >= 2010 for .mpp support**];
8. OpenOffice (.odt, .odp, .ods)。

## 错误码
1. 0 - Success
2. 1 - Failure
3. 2 - Unknown Error
4. 4 - File protected by password
5. 8 - Invalid arguments
6. 16 - Unable to open the source file
7. 32 - Unsupported file format
8. 64 - Source file not found
9. 128 - Output directory not found

[参考地址](http://officetopdf.codeplex.com/documentation)