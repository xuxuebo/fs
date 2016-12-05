(function ($) {
    $(function () {
        var business = {};
        var uploader = null;

        function uploadCompleted(file) {
            console.log(file);
            $("#" + file.id + " .percentage").text("上传完毕");
            $(".itemStop").hide();
            $(".itemUpload").hide();
            $(".itemDel").hide();
        }

        WebUploader.Uploader.register({
            "before-send-file": "beforeSendFile",
            "before-send": "beforeSend",
            "after-send-file": "afterSendFile"
        }, {
            beforeSendFile: function (file) {
                //秒传验证
                var task = new $.Deferred();
                (new WebUploader.Uploader())
                    .md5File(file, 0, 10 * 1024 * 1024).progress(function (percentage) {
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
                                console.log(data);
                                if (data.status == "FAILED") {
                                    //FAILED表示参数错误或者程序执行错误，不需要上传文件
                                    task.reject();
                                    uploader.skipFile(file);
                                    alert(data.processMsg);
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
                        console.log(data);
                        if (data.status == "FAILED") {
                            //FAILED表示参数错误，不需要上传分片，结束文件上传
                            task.reject();
                            //uploader.skipFile(file);
                            alert(data.processMsg);
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
                $.ajax({
                    type: "POST",
                    url: business.uploadFileUrl,
                    data: $.extend(true, {"resumeType": "chunksMerge"}, business),
                    cache: false,
                    dataType: "json"
                }).then(function (data, textStatus, jqXHR) {
                        console.log(data);
                        if (data.status == "FAILED") {
                            //FAILED表示参数错误(包括实际分片总数和前台传来的分片总数不一致)或者程序执行错误，上传失败
                            task.reject();
                            alert(data.processMsg);
                        } else if (data.status == "SUCCESS"
                            || data.status == "PROCESSING") {
                            //SUCCESS表示分片已合并完成并且正确处理
                            task.resolve();
                            file.data = data;
                            uploadCompleted(file);
                            if (business.uploadCompleted) {
                                business.uploadCompleted(data);
                            }
                        } else {
                            //表示文件正在合并或者合并失败
                            task.resolve();
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
            wu.formData = function () {
                return $.extend(true, {resumeType: "chunkUpload"}, business);
            };

            uploader = WebUploader.create(wu);

            uploader.on("fileQueued", function (file) {
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

            uploader.on('uploadSuccess', function (file) {
                //$("#" + file.id + " .percentage").text('已上传');
            });

            uploader.on('uploadError', function (file) {
                // $("#" + file.id + " .percentage").text('上传出错');
            });

            uploader.on('uploadComplete', function (file) {
                //$("#" + file.id + " .percentage").fadeOut();
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
                $("#bar").css("width", percentage * 800 + "px");
                $("#" + file.id + " .percentage").text(percentage * 100 + "%");
                $("#" + file.id + " .percent").text(parseInt(percentage * 10000) / 100.0 + "%");
            });

            return uploader;
        };
    });
})(jQuery);