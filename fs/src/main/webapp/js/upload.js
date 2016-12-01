(function ($) {
    $(function () {
        WebUploader.create({
            swf: '/fs/flash/Uploader.swf',
            server: '/fs/file/uploadFile',
            pick: {id: "#picker", multiple: false},
            formData: {"corpCode": "ladeng.com", appCode: "km", "businessId": "1111"}
        }).on('fileQueued', function (file) {
                console.log("=======")
            });
    });
})(jQuery);


/*        // 当有文件被添加进队列的时候
 uploader.on('fileQueued', function (file) {
 $list.append('<div id="' + file.id + '" class="item">' +
 '<h4 class="info">' + file.name + '</h4>' +
 '<p class="state">等待上传...</p>' +
 '</div>');
 });

 // 文件上传过程中创建进度条实时显示。
 uploader.on('uploadProgress', function (file, percentage) {
 var $li = $('#' + file.id),
 $percent = $li.find('.progress .progress-bar');

 // 避免重复创建
 if (!$percent.length) {
 $percent = $('<div class="progress progress-striped active">' +
 '<div class="progress-bar" role="progressbar" style="width: 0%">' +
 '</div>' +
 '</div>').appendTo($li).find('.progress-bar');
 }

 $li.find('p.state').text('上传中');
 $percent.css('width', percentage * 100 + '%');
 });

 uploader.on('uploadSuccess', function (file) {
 $('#' + file.id).find('p.state').text('已上传');
 });

 uploader.on('uploadError', function (file) {
 $('#' + file.id).find('p.state').text('上传出错');
 });

 uploader.on('uploadComplete', function (file) {
 $('#' + file.id).find('.progress').fadeOut();
 });

 WebUploader.Uploader.register({
 "before-send-file": "beforeSendFile",  // 整个文件上传前
 "before-send": "beforeSend",           // 每个分片上传前
 "after-send-file": "afterSendFile"     // 分片上传完毕
 }, {
 beforeSendFile: function (file) {
 var task = new $.Deferred();
 var start = new Date().getTime();

 //拿到上传文件的唯一名称，用于断点续传
 uniqueFileName = md5(file.name + file.size);

 $.ajax({
 type: "POST",
 url: check_url,   // 后台url地址
 data: {
 type: "init",
 uniqueFileName: uniqueFileName
 },
 cache: false,
 async: false,  // 同步
 timeout: 1000,
 dataType: "json"
 }).then(function (data, textStatus, jqXHR) {
 if (data.complete) { //若存在，这返回失败给WebUploader，表明该文件不需要上传
 task.reject();
 // 业务逻辑...

 } else {
 task.resolve();
 }
 }, function (jqXHR, textStatus, errorThrown) { //任何形式的验证失败，都触发重新上传
 task.resolve();
 });

 return $.when(task);
 }, beforeSend: function (block) {
 //分片验证是否已传过，用于断点续传
 var task = new $.Deferred();
 $.ajax({
 type: "POST",
 url: check_url,
 data: {
 type: "block",
 chunk: block.chunk,
 size: block.end - block.start
 },
 cache: false,
 async: false,  // 同步
 timeout: 1000,
 dataType: "json"
 }).then(function (data, textStatus, jqXHR) {
 if (data.is_exists) { //若存在，返回失败给WebUploader，表明该分块不需要上传
 task.reject();
 } else {
 task.resolve();
 }
 }, function (jqXHR, textStatus, errorThrown) { //任何形式的验证失败，都触发重新上传
 task.resolve();
 });
 return $.when(task);
 }, afterSendFile: function (file) {
 var chunksTotal = Math.ceil(file.size / chunkSize);
 if (chunksTotal > 1) {
 //合并请求
 var task = new $.Deferred();
 $.ajax({
 type: "POST",
 url: check_url,
 data: {
 type: "merge",
 name: file.name,
 chunks: chunksTotal,
 size: file.size
 },
 cache: false,
 async: false,  // 同步
 dataType: "json"
 }).then(function (data, textStatus, jqXHR) {
 // 业务逻辑...

 }, function (jqXHR, textStatus, errorThrown) {
 uploader.trigger('uploadError');
 task.reject();
 });
 return $.when(task);
 }
 }
 });*/