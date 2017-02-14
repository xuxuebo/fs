# 文档服务器搭建手册

## 一、部署Nginx+Lua
### 下载相关软件
1. [下载Nginx](http://nginx.org/)(手册使用的版本为1.9.13，位置：fs-parent/deploy/nginx/nginx-1.9.13.tar.gz);
2. [下载Lua](http://www.lua.org/)(手册使用的版本为5.1.5，位置：fs-parent/deploy/nginx/lua-5.1.5.tar.gz);
3. [下载LuaJIT](http://luajit.org/)(手册使用的版本为2.0.4，位置：fs-parent/deploy/nginx/LuaJIT-2.0.4.tar.gz);
4. [下载Nginx的echo模块](https://github.com/openresty/echo-nginx-module/tags)(手册使用的版本为echo-nginx-module-0.59,位置：fs-parent/deploy/nginx/v0.59.tar.gz);
5. [下载Nginx的lua模块](https://github.com/openresty/lua-nginx-module/tags)(手册使用的版本为lua-nginx-module-0.10.5，位置：fs-parent/deploy/nginx/v0.10.5.tar.gz);
6. [下载Nginx的开发工具模块](https://github.com/simpl/ngx_devel_kit/)(手册使用的版本为ngx_devel_kit-0.3.0，位置：fs-parent/deploy/nginx/v0.3.0.tar.gz);
7. [下载Nginx的redis模块](https://github.com/openresty/redis2-nginx-module)(手册使用的版本为redis2-nginx-module-0.13，位置：fs-parent/deploy/nginx/v0.13.tar.gz);
8. [下载Nginx的misc模块](https://github.com/openresty/set-misc-nginx-module)(手册使用的版本为set-misc-nginx-module-0.30，位置：fs-parent/deploy/nginx/set-misc-nginx-module-0.30.tar.gz)。

**注意：需要其他的nginx模块请自行去[gitHub](https://github.com/)上下载。**
### 安装Lua
1. 解压lua-5.1.5.tar.gz(tar -zvxf lua-5.1.5.tar.gz)；
2. 进入lua源码文件夹(cd lua-5.1.5)；
3. 编译并且安装(make &amp;&amp; make install)。
<br>

### 安装LuaJIT
1. 解压LuaJIT-2.0.4.tar.gz(tar -zvxf LuaJIT-2.0.4.tar.gz)；
2. 进入LuaJIT源码文件夹(cd LuaJIT-2.0.4)；
3. 编译并且安装(make &amp;&amp; make install)；
4. 如果是64位系统，还需要将/usr/local/lib/libluajit-5.1.so.2建立软连接到/lib64/libluajit-5.1.so.2
(ln -s  /usr/local/lib/libluajit-5.1.so.2.0.2 /lib64/libluajit-5.1.so.2)；
5. 设置lua的环境变量。<br>
export LUAJIT_LIB=/usr/local/lib<br>
export LUAJIT_INC=/usr/local/include/luajit-2.0
<br>

### 安装Nginx
1. 解压nginx-1.9.13.tar.gz(tar -zvxf nginx-1.9.13.tar.gz)；
2.  进入nginx源码文件夹(cd nginx-1.9.13)；
3. 建立文件夹3rdModule，用于存放第三方模块的源码(mkdir 3rdModule)；
4. 将需要的nginx第三方模块解压到文件夹3rdModule中；
5. 配置nginx的安装配置项(根据需求来选择)；<br>
./configure  --prefix=/web/nginx_lua/(nignx的安装目录) --with-pcre --with-http_stub_status_module --with-http_mp4_module --with-http_ssl_module --with-http_realip_module --with-http_addition_module --with-http_sub_module --with-http_dav_module --with-http_flv_module --with-http_gzip_static_module --with-http_random_index_module --with-http_secure_link_module --with-http_degradation_module --with-http_stub_status_module --with-file-aio --with-ipv6 --with-poll_module --with-select_module --add-module=./3rdModule/echo-nginx-module-0.59 --add-module=./3rdModule/lua-nginx-module-0.10.5 --add-module=./3rdModule/ngx_devel_kit-0.3.0 --add-module=./3rdModule/redis2-nginx-module-0.13 --add-module=./3rdModule/set-misc-nginx-module-0.30/
6. 编译并且安装(make -j2 &amp;&amp; make install)。
<br>

### 配置Nginx
1. 在nginx的http模块加入配置 lua_shared_dict store 50m;
2. 在nginx的server(fs所在的server)模块加入以下配置项：<br>
        set $enableValidateSwitch 是否开启文件校验，默认为true;<br>
        set $serverHost 当前文件服务器的域名(或者ip)，可以包含端口;<br>
        set $signLevel 当前文件服务器的校验级别;<br>
        set $excludeCorpCodes 所有权限都不校验权限的公司列表，默认为'';<br>
        set $excludeResources 不校验权限的资源ID列表,默认为'';<br>
        set $secret 文档服务器的秘钥;<br>
        set $excludeSecretCorpCodes 不校验秘钥权限的公司列表，默认为'';<br>
        set $excludeTimeCorpCodes 不校验时间权限的公司列表，默认为'';<br>
        set $excludeSessionCorpCodes 不校验session权限的公司列表，默认为'';<br>
        set $urlExpireTime url的实效时间（单位为分钟），默认1440(24*60);<br>
        set $checkSidUrl 用于检验session是否正确的url，有环境决定;<br>
        set $sessionValidCacheTime session验证结果缓存时间（单位为秒），默认180;<br>
        set $sessionSignSecret 验证session的签名秘钥，默认为sf;<br>
        set $ffmpeg ffmpeg命令的绝对路径;<br>
        set $fsRepo 文件存储目录，参考配置项fs.fileStoreDir,**最后的分隔符要带**;<br>
 **如果需要图片自动剪切和压缩功能，nginx的启动用户必须和$fsRepo的权限用户一致。**
3. 在nginx的server(fs所在的server)模块加入以下location：<br>


                 location /fs-service/ {
                        proxy_pass http://fs-service;
                        proxy_set_header X-FORWARDED-FOR $proxy_add_x_forwarded_for;
                        proxy_set_header Host $host;
                        proxy_set_header X-Real-IP $remote_addr;
                }


                location /fs/ {
                       default_type 'text/html';
                       access_by_lua_file conf/validateFile.lua;
                       add_header Access-Control-Allow-Origin *;
                       #nginx跟后端服务器连接超时时间(代理连接超时)
                       proxy_connect_timeout 1h;
                       #设置读取代理服务器的响应时长
                       proxy_read_timeout 1h;
                       #后端服务器数据回传时间(代理发送超时)
                       proxy_send_timeout 1h;
                       #客户端请求内容最大值
                       client_max_body_size 2000m;
                       proxy_pass http://fs;
                       proxy_set_header Host $host;

                 }

                #/..的数量由root（一般是nginx的html目录）决定。
                set $prefix '/../../..';
                #repository为文件服务器存放文件的目录
                set $repository $prefix$fsRepo;
                location ~ ^/fs/file/getFile/nn/(.*/\d+_\d+[\d_]*(\.)+png)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;

                        set $file_path $repository$1;
                        if (-f $file_path) {
                              rewrite ^(.*)$ $file_path break;
                        }

                        if (!-f $file_path) {
                              set $imagePath $fsRepo$1;
                              access_by_lua_file conf/image.lua;
                              rewrite ^(.*)$ $file_path break;
                              #proxy_pass http://fs;
                        }
                }
                
                location ~ ^/fs/file/getFile/nn/[^/]+/([^/]+/\w+/src.*/img/\d+/.+)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;
                        set $file_path $repository$1;
                        if (-f $file_path) {
                               rewrite ^(.*)$ $file_path break;
                        }
                                
                        if (!-f $file_path) {
                               set $imagePath $fsRepo$1;
                               access_by_lua_file conf/originImage.lua;
                               rewrite ^(.*)$ $file_path break;
                               #proxy_pass http://fs;
                        }
                }

                 location ~ ^/fs/file/getFile/nn/(.*/[^/]*(\.)+[^/]*)$ {
                       open_file_cache off;
                       if_modified_since off;
                       add_header Cache-Control no-cache;
                       access_by_lua_file conf/validateFile.lua;
                       #文件服务器存放文件的目录
                       root $fsRepo;
                       rewrite ^/fs/file/getFile/nn/(.*/[^/]*(\.)+[^/]*)$ $1 break;
                 }

                location ~ ^/fs/file/downloadFile/nn/(.*/[^/]*(\.)+[^/]*)$ {
                       open_file_cache off;
                       if_modified_since off;
                       add_header Cache-Control no-cache;
                       access_by_lua_file conf/validateFile.lua;
                       #文件服务器存放文件的目录
                       root $fsRepo;
                       rewrite ^/fs/file/downloadFile/nn/(.*/[^/]*(\.)+[^/]*)$ $1 break;
                }


                location ~ ^/fs/file/getFile/\w+/[^/]+/(.*/\d+_\d+[\d_]*(\.)+png)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;
                        set $file_path $repository$1;
                        if (-f $file_path) {
                              rewrite ^(.*)$ $file_path break;
                        }

                        if (!-f $file_path) {
                              set $imagePath $fsRepo$1;
                              access_by_lua_file conf/image.lua;
                              rewrite ^(.*)$ $file_path break;
                              #proxy_pass http://fs;
                        }
                }
                
                location ~ ^/fs/file/getFile/\w+/[^/]+/([^/]+/\w+/src.*/img/\d+/.+)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;
                        set $file_path $repository$1;
                        if (-f $file_path) {
                               rewrite ^(.*)$ $file_path break;
                        }
                
                        if (!-f $file_path) {
                               set $imagePath $fsRepo$1;
                               access_by_lua_file conf/originImage.lua;
                               rewrite ^(.*)$ $file_path break;
                               #proxy_pass http://fs;
                        }
                }

                location ~ ^/fs/file/getFile/\w+/[^/]+/(.*/[^/]*(\.)+[^/]*)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;
                        #文件服务器存放文件的目录
                        root $fsRepo;
                        rewrite ^/fs/file/getFile/\w+/[^/]+/(.*/[^/]*(\.)+[^/]*)$ $1 break;
                }

                location ~ ^/fs/file/downloadFile/\w+/[^/]+/(.*/[^/]*(\.)+[^/]*)$ {
                        open_file_cache off;
                        if_modified_since off;
                        add_header Cache-Control no-cache;
                        access_by_lua_file conf/validateFile.lua;
                        #文件服务器存放文件的目录
                        root $fsRepo;
                        rewrite ^/fs/file/downloadFile/\w+/[^/]+/(.*/[^/]*(\.)+[^/]*)$ $1 break;
                }
4. 在nginx的http模块加入以下upstream：<br>

upstream  fs  {<br>
                &nbsp;&nbsp;&nbsp;&nbsp;server   fs的tomcat1(包含ip和端口);<br>
                &nbsp;&nbsp;&nbsp;&nbsp;server   fs的tomcat2(包含ip和端口);<br>
        }<br>

upstream  fs-service  {<br>
                &nbsp;&nbsp;&nbsp;&nbsp;server   fs-service的tomcat(包含ip和端口);<br>
        }<br>
        
5. 将文件权限验证脚本validateFile.lua放到nginx的conf目录下，文件路径fs-parent/fs/src/main/resources/validateFile.lua
6. 将图片剪切和压缩脚本image.lua放到nginx的conf目录下，文件路径fs-parent/fs/src/main/resources/image.lua
7. 将权限验证脚本validateFile.lua依赖的lua第三方插件store.lua，shim.lua，json.lua，http_headers.lua，http.lua，cjson.so放到/opt/lualib/目录下面(脚本文件在目录fs-parent/deploy/lua下);

## 二、部署ffmpeg(Linux)<br>

**注意：只有有图片压缩，图片剪切，视频转换，音频转换的文件服务器才需要安装ffmpeg。**<br>

参考资料：<br>
[Linux下ffmpeg的完整安装](http://www.cnblogs.com/wanghetao/p/3386311.html)<br>
[CentOS6.2安装ffmpeg](http://www.lenky.info/archives/2013/10/2349)<br>
[error while loading shared libraries: xxx.so.x"错误的原因和解决办法](http://blog.chinaunix.net/uid-26212859-id-3256667.html)<br>
### 下载相关软件
1. [下载ffmpeg](http://ffmpeg.org/)(手册使用的版本为3.0.2，位置：fs-parent/deploy/ffmpeg/ffmpeg-3.0.2.tar.bz2);

**注意：需要其他的音频和视频转码器请参考fs-parent/deploy/ffmpeg/目录下面的，没有的请自行下载。**
### 安装ffmpeg
1. 解压ffmpeg-3.0.2.tar.bz2(tar -jvxf ffmpeg-3.0.2.tar.bz2)；
2. 进入ffmpeg源码文件夹(cd ffmpeg-3.0.2)；
3. ./configure --prefix=/opt/ffmpeg --enable-libmp3lame --enable-libvorbis --enable-gpl --enable-version3 --enable-nonfree --enable-pthreads --enable-libfaac --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libx264 --enable-libxvid --enable-postproc --enable-ffserver --enable-ffplay<br>
ffmpeg模块选择参考./configure --help<br>
如果安装过程出现错误，请参考参考资料，下载相应的音频或者视频转码器并安装，而后再执行此步骤；
4. 编译并且安装(make &amp;&amp; make install)。
<br>

## 三、部署文件转化服务器
1. 安装Microsoft Office 2010及其以上;
2. 安装.NET framework 4.0；
3. 安装JDK1.6+，如果JDK为64位的，请将fs-parent/deploy/JACOB/jacob-1.14.3-x64.dll放在JDK的bin目录下
，如果JDK为32位的，请将fs-parent/deploy/JACOB/jacob-1.14.3-x86.dll放在JDK的bin目录下；
4. 配置好tomcat的配置项（端口，jdk，启动位置等），将fs的war包放置webapps中，配置好env.properties文件；
5. 参考env的配置项fs.officeTrustDir，设置office（word，ppt，excel）的信任目录
             （如果不设置信任目录，可能有一些受保护的office文件jacob不能打开）;
6. 根据env的配置项fs.doc2Pdf.convertToolPath，将fs-parent/deploy/DocToPDF/OfficeToPDF.exe放置到配置目录下；
7. 参考env的配置项fs.pdfToImage.convertToolPath,将fs-parent/deploy/PDFToImage/Release.rar解压后，放置到配置目录下；
8. 下载windows版的ffmpeg，解压后将ffmpeg的bin目录配置到classpath下（参考：fs-parent/deploy/ffmpeg/ffmpeg-20170208-3aae1ef-win64-static.zip）；
9. 启动tomcat，运行文件转化服务。

## 四、部署文件服务器
1. 创建fs的数据库，执行fs.sql脚本，脚本位置fs-parent/fs/src/main/resources/fs.sql(如果数据库不存在)；
2. 部署fs服务(可以部署多个tomcat)；
3. 将当前文件服务器的插入表t_fs_server中（字段参考fs的配置文件env.properties）；
4. 部署fs-service服务(可以部署多个tomcat)；

## 五、部署ceph分布式文件服务器

**对于简单需求可以不用部署**<br>
参考资料：<br>
[ceph官网](http://ceph.com/)<br>
[ceph中文社区](http://ceph.org.cn/)<br>



