一、上传图片抽取坐标
url：
http://文件服务器域名或者ip/fs/file/uploadFile（跨域url使用/fs/file/uploadFile?actualHost=文件服务器域名或者ip
，当前的域名对应的机器要配置反向代理）
必填项：
1、extractPoint表示是否抽取坐标点，true表示是，不填或者false表示不是。
选填项：
1、cell表示每个小格子大小，默认20。
2、w表示logo前的宽度，默认1200。
3、h表示logo墙的高度，默认为650。
结果获取（json）：
status表示状态，SUCCESS表示正确，FAILED表示失败，
当结果正确时，points表示坐标集合，当失败时，processMsg表示失败原因。
上传组件可以参考：
http://192.168.0.35/fs/demo2.html
fs-parent/fs/src/main/webapp/pages/demo2.html


二、文字抽取坐标
url：
http://文件服务器域名或者ip/fs/file/uploadFile（跨域url使用/fs/file/uploadFile?actualHost=文件服务器域名或者ip
，当前的域名对应的机器要配置反向代理）
必填项：
1、extractPoint表示是否抽取坐标点，true表示是，不填或者false表示不是。
2、text文字内容，如清谷科技。
选填项：
1、cell表示每个小格子大小，默认20。
2、w表示logo前的宽度，默认1200。
3、h表示logo墙的高度，默认为650。
4、family表示字体名称，如黑体，宋体，楷体等，当前计算机支持的字体，默认为楷体。
5、style字体样式， 0表示一般，1粗体，2斜体，默认为0。
结果获取（json）：
status表示状态，SUCCESS表示正确，FAILED表示失败，
当结果正确时，points表示坐标集合，当失败时，processMsg表示失败原因。

三、上传文件
url：
http://文件服务器域名或者ip/fs/file/uploadFile（跨域url使用/fs/file/uploadFile?actualHost=文件服务器域名或者ip
，当前的域名对应的机器要配置反向代理）
如果支持断点续传，上传组件可以参考：
http://192.168.0.35/fs/demo2.html
fs-parent/fs/src/main/webapp/pages/demo2.html
必填项：
1、appCode表示应用编号。
2、corpCode表示公司编号。
3、processor表示文件的处理类型，参考路径
fs-parent/fs-api/src/main/java/com/qgutech/fs/domain/ProcessorTypeEnum.java。
4、businessId表业关联的业务ID，如课程id。
5、session表示人员的登录session，如果当前文件服务器验证session，为必填项，如果当前文件服务器不验证session，可以不填写。
选填项：
1、businessCode表业关联的业务编号，如课程编号。
2、businessDir，指定业务目录，指定时可以将一些指定业务的关联的文件放到该目录下，如果课程可以单独放入课程目录下
3、responseFormat表示返回结果格式，支持json，xml，jsonp，html。
结果获取：
status表示状态，SUCCESS表示正确处理完成，FAILED表示处理失败，PROCESSING表示正在异步处理在，
当失败时，processMsg表示失败原因，id表示文件在文件系统中的主键，fileUrl表示文件的url。json格式如下：
{
"appCode":"km",
"businessId":"1483595358915",
"corpCode":"ladeng.com",
"fileSize":"19934",
"fileUrl":"http://file.qgutech.com/fs/file/getFile/stt/bd52e6ce04cb7cd105a1406c538fc25d_1483596864532/ladeng.com/km/src/doc/1701/1483595358915/ff808081596a1cac01596d439c280004.docx",
"id":"ff808081596a1cac01596d439c280004",
"processor":"DOC",
"responseFormat":"json",
"serverCode":"0000",
"status":"PROCESSING",
"storedFileName":"Redis试卷.docx",
"suffix":"docx"
}

四、剪切文件
url：
http://文件服务器域名或者ip/fs/file/cutImage（跨域url使用/fs/file/cutImage?actualHost=文件服务器域名或者ip
，当前的域名对应的机器要配置反向代理）
必填项：
1、id表示文件在文件服务器中的编号。
2、x表示剪切图片的起始点的x轴。
3、y表示剪切图片的起始点的y轴。
4、w表示剪切图片的宽度。
5、h表示剪切图片的高度。
6、session表示人员的登录session，如果当前文件服务器验证session，为必填项，如果当前文件服务器不验证session，可以不填写。
选填项：
1、responseFormat表示返回结果格式，支持json，xml，jsonp，html。
结果获取：
status表示状态，SUCCESS表示正确，FAILED表示失败，
当结果正确时，storedFileName表示剪切图片的名称，fileUrl表示剪切的图片url。当失败时，processMsg表示失败原因。
json格式如下：
正确结果：
{
"fileUrl":"http://file.qgutech.com/fs/file/getFile/stt/524dfbe6866d164719fad1b0e2098615_1483666701579/ladeng.com/km/gen/img/1701/ff808081596a1cac0159716c283d000c/100_100_300_300.png",
"id":"ff808081596a1cac0159716c283d000c",
"processMsg":"Cutting image successfully!",
"responseFormat":"json",
"status":"SUCCESS",
"storedFileName":"100_100_300_300.png"
}
错误结果：
{
"id":"ff808081596a1cac0159716c283d0001",
"processMsg":"File not exist!",
"responseFormat":"json",
"status":"FAILED"
}

五、再次处理
url：
http://文件服务器域名或者ip/fs/file/reprocessFile（跨域url使用/fs/file/reprocessFile?actualHost=文件服务器域名或者ip
，当前的域名对应的机器要配置反向代理）
必填项：
1、id表示文件在文件服务器中的编号。
选填项：
1、responseFormat表示返回结果格式，支持json，xml，jsonp，html。
结果获取：
status表示状态，SUCCESS表示正确处理完成，FAILED表示处理失败，PROCESSING表示正在异步处理在，
当失败时，processMsg表示失败原因。json格式如下：
{
"appCode":"km",
"businessId":"1483667500032",
"corpCode":"ladeng.com",
"fileSize":"19934",
"id":"ff808081596a1cac015971798487000d",
"processor":"DOC",
"responseFormat":"json",
"serverCode":"0000",
"status":"PROCESSING",
"storedFileName":"Redis试卷.docx",
"subFileCount":"6",
"suffix":"docx"
}

六、文件服务器配置说明
一个文件服务器为一台服务器（单独的一台物理机），可以包含多个fs服务。（包含一个nginx）

改变文件服务器的校验级别：
文件服务器的校验级别参考fs-parent/fs-api/src/main/java/com/qgutech/fs/domain/SignLevelEnum.java
1、修改文件服务器在t_fs_server表中记录的字段sign_level；
2、修改文件服务器所有fs服务的配置文件env.properties的配置项fs.signLevel；
3、修改文件服务器的nginx的配置参数set $signLevel 校验级别。

改变文件服务器的秘钥：
1、修改文件服务器在t_fs_server表中记录的字段secret；
2、修改文件服务器所有fs服务的配置文件env.properties的配置项fs.serverSecret；
3、修改文件服务器的nginx的配置参数set $secret 秘钥。

改变文档服务器的域名（或者ip）：
1、修改文件服务器在t_fs_server表中记录的字段host；
2、修改文件服务器所有fs服务的配置文件env.properties的配置项fs.serverHost；
3、修改文件服务器的nginx的配置参数set $serverHost 域名。

改变文件url的过期时间：
1、修改文件服务器所有fs服务的配置文件env.properties的配置项fs.urlExpireTime，单位为毫秒；
2、修改文件服务器的nginx的配置参数set $urlExpireTime 时间;（单位为分钟）

整个文件服务器系统由存储位置是否一致（还可以根据文件的校验级别）划分为若干子文件系统，每个子文件系统的编号均不一致。
档服务器的编号：
1、一个文件服务器系统包含一个默认的子文件系统，默认为0000（也就是dedault公司）；
2、内部文档服务器的文件不存储到编号为0000的分布式文件系统中，编号不可以为0000；

