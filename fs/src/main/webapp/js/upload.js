(function ($) {
    $(function () {
        var business = {};
        var uploader = null;

        function uploadCompleted(file) {
            uploader.reset();
            $("#bar").css("width", "800px");
            $("#bar").css("background", "#5EC4EA");
            $("#" + file.id + " .percent").text("100%");
            $("#" + file.id + " .percentage").text("上传完毕");
            $(".itemStop").hide();
            $(".itemUpload").hide();
            $(".itemDel").hide();
        }

        function uploadFailed(file) {
            uploader.reset();
            $("#bar").css("width", "800px");
            $("#" + file.id + " .percent").text("上传失败");
            $("#bar").css("background", "red");
            $("#" + file.id + " .percentage").text("上传失败");
            $(".itemStop").hide();
            $(".itemUpload").hide();
            $(".itemDel").hide();
        }


        function prepareUpload(file) {
            $("#bar").css("width", "800px");
            $("#" + file.id + " .percent").text("上传准备中。。。");
            $("#bar").css("background", "green");
            $("#" + file.id + " .percentage").text("上传准备中。。。");
        }

        function processUpload(file) {
            $("#" + file.id + " .percent").text("处理中。。。");
            $("#" + file.id + " .percentage").text("处理中。。。");
        }

        WebUploader.Uploader.register({
            "before-send-file": "beforeSendFile",
            "before-send": "beforeSend",
            "after-send-file": "afterSendFile"
        }, {
            beforeSendFile: function (file) {
                prepareUpload(file);
                //秒传验证
                var task = new $.Deferred();
                (new WebUploader.Uploader())
                    .md5File(file, 0, 10 * 1024 * 1024)
                    .progress(function (percentage) {
                        //console.log(percentage);
                    }).then(function (val) {
                        business.md5 = val;
                        business.chunks = Math.ceil(file.size / business.chunkSize);
                        business.storedFileName = file.name;
                        business.fileSize = file.size;
                        business.suffix = file.ext;

                        $.ajax({
                            type: "POST",
                            url: business.uploadFileUrl,
                            data: $.extend(true, {resumeType: "md5Check"}, business),
                            cache: false,
                            dataType: "json"
                        }).then(function (data, textStatus, jqXHR) {
                                //console.log(data);
                                if (data.status == "FAILED") {
                                    //FAILED表示参数错误或者程序执行错误，不需要上传文件
                                    task.reject();
                                    uploader.skipFile(file);
                                    uploadFailed(file);
                                    console.log("Uploading File Failed:" + data.processMsg);
                                } else if (data.status == "SUCCESS"
                                    || data.status == "PROCESSING") {
                                    //表示文件已存在并且处理正确或者处理中，不需要上传文件
                                    task.reject();
                                    uploader.skipFile(file);
                                    file.data = data;
                                    uploadCompleted(file);
                                    if (business.uploadCompleted) {
                                        business.uploadCompleted(data);
                                    }
                                } else {
                                    //表示文件不存在，需要上传文件
                                    task.resolve();
                                }
                            }, function (jqXHR, textStatus, errorThrown) {
                                //任何形式的验证失败，都触发重新上传
                                task.resolve();
                            });
                    });

                return $.when(task);
            }, beforeSend: function (block) {
                //分片验证是否已传过，用于断点续传
                var task = new $.Deferred();
                var chunkCheck = {
                    resumeType: "chunkCheck",
                    chunk: block.chunk,
                    blockSize: (block.end - block.start)
                };

                $.ajax({
                    type: "POST",
                    url: business.uploadFileUrl,
                    data: $.extend(true, chunkCheck, business),
                    cache: false,
                    dataType: "json"
                }).then(function (data, textStatus, jqXHR) {
                        //console.log(data);
                        if (data.status == "FAILED") {
                            //FAILED表示参数错误，不需要上传分片，结束文件上传
                            task.reject();
                            console.log("Uploading Chunk Failed:" + data.processMsg);
                        } else if (data.status == "SUCCESS") {
                            //SUCCESS表示分片已存在，不需要上传分片
                            task.reject();
                        } else {
                            //表示分片不存在，需要上传分片
                            task.resolve();
                        }
                    }, function (jqXHR, textStatus, errorThrown) {
                        //任何形式的验证失败，都触发重新上传
                        task.resolve();
                    });

                return $.when(task);
            }, afterSendFile: function (file) {
                //合并请求
                var task = new $.Deferred();
                processUpload(file);
                $.ajax({
                    type: "POST",
                    url: business.uploadFileUrl,
                    data: $.extend(true, {"resumeType": "chunksMerge"}, business),
                    cache: false,
                    dataType: "json"
                }).then(function (data, textStatus, jqXHR) {
                        //console.log(data);
                        if (data.status == "FAILED") {
                            //FAILED表示参数错误(包括实际分片总数和前台传来的分片总数不一致)或者程序执行错误，上传文件失败
                            task.reject();
                            uploadFailed(file);
                            console.log("Merging Chunk Failed:" + data.processMsg);
                        } else if (data.status == "SUCCESS"
                            || data.status == "PROCESSING") {
                            //SUCCESS表示分片已合并完成并且正确处理或者正在处理中。
                            task.resolve();
                            file.data = data;
                            uploadCompleted(file);
                            if (business.uploadCompleted) {
                                business.uploadCompleted(data);
                            }
                        } else {
                            //表示文件正在合并或者合并失败
                            task.resolve();
                            console.log("文件正在合并或者合并失败:" + data.processMsg);
                        }

                    }, function (jqXHR, textStatus, errorThrown) {
                        task.reject();
                    });

                return $.when(task);
            }
        });

        window.uploadFile = function (wu, param) {
            business = $.extend(true, business, param);
            business.uploadFileUrl = wu.server;
            business.chunkSize = wu.chunkSize || 5000 * 1024;
            business.responseFormat = param.responseFormat || "json";
            var processor = business.processor;
            if (processor == "VID") {
                wu.accept = {
                    title: "不支持的视频类型",
                    extensions: 'wmv,flv,mp4,rmvb,mkv,mov,avi,m4v,asf'
                };
            } else if (processor == "AUD") {
                wu.accept = {
                    title: "不支持的音频类型",
                    extensions: 'mp3,ape'
                };
            } else if (processor == "IMG") {
                wu.accept = {
                    title: "不支持的图片类型",
                    mimeTypes: "image/bmp,image/png,image/gif,image/jpeg,image/tiff",
                    extensions: 'bmp,png,gif,jpg,jpeg,tif'
                };
            } else if (processor == "DOC") {
                wu.accept = {
                    title: "不支持的文档类型",
                    mimeTypes: "application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/vnd.ms-excel,pplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/pdf,text/plain",
                    extensions: 'doc,docx,ppt,pptx,xls,xlsx,pdf,txt'
                };
            } else if (processor != "FILE") {
                wu.accept = {
                    title: "不支持的压缩类型",
                    //mimeTypes: "application/zip,application/x-rar-compressed,application/application/x-7z-compressed",
                    extensions: 'zip,rar,7z'
                };
            }

            wu.formData = function () {
                return $.extend(true, {resumeType: "chunkUpload"}, business);
            };
            uploader = WebUploader.create(wu);

            uploader.on("fileQueued", function (file) {
                $("#theList").html('');
                $("#theList").append('<li id="' + file.id + '">'
                    + '<img /><span>' + file.name
                    + '</span><span class="itemUpload">上传</span><span class="itemStop">'
                    + '暂停</span><span class="itemDel">删除</span>'
                    + '<div class="percentage"></div>'
                    + '<div class="progressBar"><div id="bar"><span class="percent"></span></div></div>'
                    + '</li>');
                var $img = $("#" + file.id).find("img");
                uploader.makeThumb(file, function (error, src) {
                    if (error) {
                        $img.replaceWith("<span>不能预览</span>");
                    }

                    $img.attr("src", src);
                });
            });

            uploader.on('beforeFileQueued', function (file) {
                if (this && this.options && this.options.accept
                    && this.options.accept instanceof Array
                    && this.options.accept[0]) {
                    var accept = this.options.accept[0];
                    if (accept.extensions) {
                        if (accept.extensions.indexOf(file.ext.toLowerCase()) < 0) {
                            alert(accept.title);
                        }
                    }
                }
            });

            uploader.on('uploadError', function (file) {

            });

            uploader.on('uploadComplete', function (file) {

            });

            $("#theList").on("click", ".itemUpload", function () {
                uploader.upload();
                //"上传"-->"暂停"
                $(this).hide();
                $(".itemStop").show();
            });

            $("#theList").on("click", ".itemStop", function () {
                uploader.stop(true);
                //"暂停"-->"上传"
                $(this).hide();
                $(".itemUpload").show();
            });

            $("#theList").on("click", ".itemDel", function () {
                uploader.removeFile($(this).parent().attr("id"));	//从上传文件列表中删除
                $(this).parent().remove();	//从上传列表dom中删除
            });

            uploader.on("uploadProgress", function (file, percentage) {
                $("#bar").css("background", "#5EC4EA");
                $("#bar").css("width", percentage * 800 + "px");
                $("#" + file.id + " .percentage").text(percentage * 100 + "%");
                $("#" + file.id + " .percent").text(parseInt(percentage * 10000) / 100.0 + "%");
            });

            return uploader;
        };
    });
})(jQuery);