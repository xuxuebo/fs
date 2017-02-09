#文档服务器搭建手册

##一、部署Nginx+Lua
###下载相关软件
1. [下载Nginx](http://nginx.org/)(手册使用的版本为1.9.13，位置：fs-parent/工具/nginx-1.9.13.tar.gz);
2. [下载Lua](http://www.lua.org/)(手册使用的版本为5.1.5，位置：fs-parent/工具/lua-5.1.5.tar.gz);
3. [下载LuaJIT](http://luajit.org/)(手册使用的版本为2.0.4，位置：fs-parent/工具/LuaJIT-2.0.4.tar.gz);
4. [下载Nginx的echo模块](https://github.com/openresty/echo-nginx-module/tags)(手册使用的版本为echo-nginx-module-0.59,位置：fs-parent/工具/v0.59.tar.gz);
5. [下载Nginx的lua模块](https://github.com/openresty/lua-nginx-module/tags)(手册使用的版本为lua-nginx-module-0.10.5，位置：fs-parent/工具/v0.10.5.tar.gz);
6. [下载Nginx的开发工具模块](https://github.com/simpl/ngx_devel_kit/)(手册使用的版本为ngx_devel_kit-0.3.0，位置：fs-parent/工具/v0.3.0.tar.gz);
7. [下载Nginx的redis模块](https://github.com/openresty/redis2-nginx-module)(手册使用的版本为redis2-nginx-module-0.13，位置：fs-parent/工具/v0.13.tar.gz);
8. [下载Nginx的misc模块](https://github.com/openresty/set-misc-nginx-module)(手册使用的版本为set-misc-nginx-module-0.30，位置：fs-parent/工具/set-misc-nginx-module-0.30.tar.gz);

**注意：需要其他的nginx模块请自行去[gitHub](https://github.com/)上下载。**
###安装Lua
1. 解压lua-5.1.5.tar.gz(tar -zvxf lua-5.1.5.tar.gz)；
2. 进入lua源码文件夹(cd lua-5.1.5)；
3. 编译并且安装(make &amp;&amp; make install)。
###安装LuaJIT
1. 解压LuaJIT-2.0.4.tar.gz(tar -zvxf LuaJIT-2.0.4.tar.gz)；
2. 进入LuaJIT源码文件夹(cd LuaJIT-2.0.4)；
3. 编译并且安装(make &amp;&amp; make install)；
4. 如果是64位系统，还需要将/usr/local/lib/libluajit-5.1.so.2建立软连接到/lib64/libluajit-5.1.so.2
(ln -s  /usr/local/lib/libluajit-5.1.so.2.0.2 /lib64/libluajit-5.1.so.2)；
5. 设置lua的环境变量。<br>
export LUAJIT_LIB=/usr/local/lib<br>
export LUAJIT_INC=/usr/local/include/luajit-2.0
###安装Nginx
1. 解压nginx-1.9.13.tar.gz(tar -zvxf nginx-1.9.13.tar.gz)；
2.  进入nginx源码文件夹(cd nginx-1.9.13)；
3. 建立文件夹3rdModule，用于存放第三方模块的源码(mkdir 3rdModule)；
4. 将需要的nginx第三方模块解压到文件夹3rdModule中；
5. 配置nginx的安装配置项(根据需求来选择)；<br>
./configure  --prefix=/web/nginx_lua/(nignx的安装目录) --with-pcre --with-http_stub_status_module --with-http_mp4_module --with-http_ssl_module --with-http_realip_module --with-http_addition_module --with-http_sub_module --with-http_dav_module --with-http_flv_module --with-http_gzip_static_module --with-http_random_index_module --with-http_secure_link_module --with-http_degradation_module --with-http_stub_status_module --with-file-aio --with-ipv6 --with-poll_module --with-select_module --add-module=./3rdModule/echo-nginx-module-0.59 --add-module=./3rdModule/lua-nginx-module-0.10.5 --add-module=./3rdModule/ngx_devel_kit-0.3.0 --add-module=./3rdModule/redis2-nginx-module-0.13 --add-module=./3rdModule/set-misc-nginx-module-0.30/
6. 编译并且安装(make -j2 &amp;&amp; make install)。

