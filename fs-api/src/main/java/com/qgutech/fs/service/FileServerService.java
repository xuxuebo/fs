package com.qgutech.fs.service;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;

import java.util.List;
import java.util.Map;

public interface FileServerService {

    /**
     * 根据文件在文件系统中的主键获取源文件访问url（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 源文件访问url
     */
    String getOriginFileUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取源文件访问url列表（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 源文件访问url列表
     */
    Map<String, String> getBatchOriginFileUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取源文件下载url（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 源文件下载url
     */
    String getOriginDownloadUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取源文件下载url列表（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 源文件下载url列表
     */
    Map<String, String> getBatchOriginDownloadUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取清晰度（由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）
     * 和视频url映射的列表。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频清晰度和视频url的映射。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频清晰度和视频url的映射，
     * 第一个视频清晰度和视频url的映射对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 视频清晰度和视频url的映射列表。
     */
    List<Map<String, String>> getVideoUrls(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键获取视频清晰度（
     * 由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）和视频url的映射。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则为视频清晰度和视频url的映射。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频清晰度和视频url的映射。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 清晰度和视频url的映射
     */
    Map<String, String> getVideoTypeUrlMap(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表批量获取清晰度（由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）
     * 和视频url映射的列表。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频清晰度和视频url映射的列表，列表如下：
     * <ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频清晰度和视频url映射。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频清晰度和视频url映射，
     * 第一个视频清晰度和视频url映射对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 批量返回清晰度和视频url映射的列表。
     */
    Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获视频封面路径的列表。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频的封面路径。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频的封面路径，
     * 第一个视频封面路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 视频封面路径的列表。
     */
    List<String> getVideoCoverUrls(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键获视频封面路径。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则为视频的封面路径。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频封面路径。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 视频封面路径。
     */
    String getVideoCoverUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和视频封面路径的映射。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频封面路径如下：
     * <ul>
     * <li>如果为单个视频，则为视频的封面路径。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频封面路径。</li>
     * </ul></b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和视频封面路径的映射
     */
    Map<String, String> getBatchVideoCoverUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和视频封面路径列表的映射。
     * <br/>
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频封面路径的列表，列表如下：
     * <ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频的封面路径。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频的封面路径，
     * 第一个视频封面路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和视频封面路径列表的映射，key为文件主键，value为视频封面路径的列表
     */
    Map<String, List<String>> getBatchVideoCoverUrlsMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获音频的url路径。
     * <br/>
     * <b>此方法适用于单个音频文件以及音频包：<ul>
     * <li>如果为单个音频，则为音频的url路径。</li>
     * <li>如果为音频包，则为<span style='color:red;'>第一个</span>音频的url路径。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 音频的url路径
     */
    String getAudioUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键获音频url路径的列表。
     * <br/>
     * <b>此方法适用于单个音频文件以及音频包：<ul>
     * <li>如果为单个音频，则列表的长度为1，每一个列表元素为一个音频的url路径。</li>
     * <li>如果为音频包，则列表的长度为音频包中的音频个数，每一个列表元素为一个音频的url路径，
     * 第一个音频url路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 音频url路径的列表
     */
    List<String> getAudioUrls(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和音频url路径的映射。
     * <br/>
     * <b>此方法适用于单个音频文件以及音频包,key为文件主键，value为音频的url路径如下：
     * <ul>
     * <li>如果为单个音频，则为音频的url路径。</li>
     * <li>如果为音频包，则为<span style='color:red;'>第一个</span>音频的url路径。</li>
     * </ul></b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和音频url路径的映射
     */
    Map<String, String> getBatchAudioUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和音频url路径列表的映射。
     * <br/>
     * <b>此方法适用于单个音频文件以及音频包,key为文件主键，value为音频url路径的列表，列表如下：
     * <ul>
     * <li>如果为单个音频，则列表的长度为1，每一个列表元素为一个音频的url路径。</li>
     * <li>如果为音频包，则列表的长度为音频包中的音频个数，每一个列表元素为一个音频的url路径，
     * 第一个音频的url路径对应第一个列表元素。</li>
     * </ul>
     * </b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和音频url路径列表的映射，key为文件主键，value为音频url路径的列表
     */
    Map<String, List<String>> getBatchAudioUrlsMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取压缩文件解压后的index.html页面的url路径。
     * 此方法只适合于压缩类型{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIP}
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 压缩文件解压后的index.html页面的url路径
     */
    String getZipUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和压缩文件解压后的index.html页面url路径的映射。
     * 此方法只适合于压缩类型{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIP}
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和压缩文件解压后的index.html页面url路径的映射
     */
    Map<String, String> getBatchZipUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取图片的url路径。
     * 图片的url路径具体参见{@link FileServerService#getBatchImageUrlMap(java.util.List, java.lang.String)}
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 图片url路径
     */
    String getImageUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和图片url路径的映射。
     * key为文件主键，value为图片url路径，此方法适用于以下几类文件。
     * <ul>
     * <li>如果为单个文档{@link com.qgutech.fs.domain.ProcessorTypeEnum#DOC}
     * ，则图片url为文档转化为图片集后的第一张图片的url路径。</li>
     * <li>如果为文档包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZDOC}
     * ，则图片url路径为文档包的第一个文档转化为图片集后的第一张图片的url路径。</li>
     * <li>如果为单个图片{@link com.qgutech.fs.domain.ProcessorTypeEnum#IMG}
     * ，则文件url路径为图片转化为默认类型图片（png）的url路径。</li>
     * <li>如果为图片包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIMG}
     * ，则文件url路径为图片包中的第一张图片转化为默认类型图片（png）的url路径。</li>
     * <ul/>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和图片url路径的映射，key为文件主键，value为图片url路径
     */
    Map<String, String> getBatchImageUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取文件得url路径。
     * 文件得url路径具体参见{@link FileServerService#getBatchFileUrlMap(java.util.List, java.lang.String)}
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @param session  当前用户的登录session，不可为空。
     * @return 文件url路径
     */
    String getFileUrl(String fsFileId, String session);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和文件url路径的映射。
     * <br/>
     * <b>此方法适用于所有类型的文件,key为文件主键，value为文件url路径，如下：
     * <ul>
     * <li>如果为单个文档{@link com.qgutech.fs.domain.ProcessorTypeEnum#DOC}
     * ，则文件url路径为文档转化为图片集后的第一张图片的url路径。</li>
     * <li>如果为文档包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZDOC}
     * ，则文件url路径为文档包的第一个文档转化为图片集后的第一张图片的url路径。</li>
     * <li>如果为单个图片{@link com.qgutech.fs.domain.ProcessorTypeEnum#IMG}
     * ，则文件url路径为图片转化为默认类型图片的url路径。</li>
     * <li>如果为图片包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIMG}
     * ，则文件url路径为图片包中的第一张图片转化为默认类型图片的url路径。</li>
     * <li>如果为单个视频{@link com.qgutech.fs.domain.ProcessorTypeEnum#VID}
     * ，则文件url路径为视频处理后的原画质视频的url路径。</li>
     * <li>如果为视频包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZVID}
     * ，则文件url路径为视频包中第一个视频处理后（转为m3u8）的原画质视频的url路径。</li>
     * <li>如果为单个音频{@link com.qgutech.fs.domain.ProcessorTypeEnum#AUD}
     * ，则文件url路径为音频处理后（转为mp3）的url路径。</li>
     * <li>如果为音频包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZAUD}
     * ，则文件url路径为音频包中第一个音频处理后（转为mp3）的url路径。</li>
     * <li>如果为压缩包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIP}
     * ，则文件url路径为压缩包解压后的index.html的url路径。</li>
     * <li>如果为上传类（不处理）{@link com.qgutech.fs.domain.ProcessorTypeEnum#FILE}
     * ，则文件url路径为源文件的url路径。</li>
     * </ul>
     * </b>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @param session      当前用户的登录session，不可为空。
     * @return 文件主键和文件url路径的映射，key为文件主键，value为文件url路径
     */
    Map<String, String> getBatchFileUrlMap(List<String> fsFileIdList, String session);

    /**
     * 根据文件在文件系统中的主键获取文件的子文件数量。
     * 具体参见{@link FileServerService#getBatchSubFileCountMap(java.util.List)}
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @return 文件的子文件数量
     */
    Long getSubFileCount(String fsFileId);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和文件的子文件数量的映射。
     * <ul>
     * <li>如果文件为单个视频{@link com.qgutech.fs.domain.ProcessorTypeEnum#VID}
     * ，单个音频{@link com.qgutech.fs.domain.ProcessorTypeEnum#AUD}
     * ，单张图片{@link com.qgutech.fs.domain.ProcessorTypeEnum#IMG}
     * ，压缩包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIP}
     * ，不做处理的文件{@link com.qgutech.fs.domain.ProcessorTypeEnum#FILE}时，子文件数量为0。</li>
     * <li>如果文件为单个文档{@link com.qgutech.fs.domain.ProcessorTypeEnum#DOC}时
     * ，子文件数量为文档的页数。</li>
     * <li>如果文件为文档包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZDOC}
     * ，图片包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZIMG}
     * ，视频包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZVID}
     * ，音频包{@link com.qgutech.fs.domain.ProcessorTypeEnum#ZAUD}时
     * ，子文件数量为压缩包中的文档数。</li>
     * </ul>
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @return 文件主键和文件的子文件数量的映射，key为文件主键，value为文件的子文件数量
     */
    Map<String, Long> getBatchSubFileCountMap(List<String> fsFileIdList);

    /**
     * 根据文件在文件系统中的主键获取文档包的每个文档的页数列表。
     * 比如，文档包有三个文档，第一个文档100页，第二个33页，第三个67页。
     * 则列表中的第一个元素为100，列表中的第二个元素为33，列表中的第三个元素为67。
     * 此方法只适用于文档包。
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @return 文档包的每个文档的页数列表
     */
    List<Long> getSubFileCounts(String fsFileId);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和文档包的每个文档的页数列表的映射。
     * 比如，文档包有三个文档，第一个文档100页，第二个33页，第三个67页。
     * 则列表中的第一个元素为100，列表中的第二个元素为33，列表中的第三个元素为67。
     * 此方法只适用于文档包。
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @return 文件主键和文档包的每个文档的页数列表的映射，key为文件主键，value为文档包的每个文档的页数列表
     */
    Map<String, List<Long>> getBatchSubFileCountsMap(List<String> fsFileIdList);

    /**
     * 根据文件主键获取文件详情
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @return 文件详情
     */
    FsFile getFsFile(String fsFileId);

    /**
     * 根据文件主键列表获取文件详情的列表
     *
     * @param fsFileIdList 文件在文件系统中的主键列表，不可为空。
     * @return 文件详情的列表
     */
    List<FsFile> getFsFiles(List<String> fsFileIdList);

    /**
     * 根据公司编号获取公司可以上传文件的文档服务器（包含default公司的）列表。
     *
     * @param corpCode 公司编号，不可为空。
     * @return 公司可以上传文件的文档服务器（包含default公司的）列表
     */
    List<FsServer> getUploadFsServerList(String corpCode);

    /**
     * 根据文件主键获取可以下载该文件的文档服务器列表。
     *
     * @param fsFileId 文件在文件系统中的主键，不可为空。
     * @return 可以下载该文件的文档服务器列表
     */
    List<FsServer> getDownloadFsServerList(String fsFileId);

    /**
     * 根据公司编号和文档服务器编号获取可以下载的文档服务器列表。
     *
     * @param corpCode   公司编号，不可为空。
     * @param serverCode 文档服务器编号，不可为空。
     * @return 可以下载的文档服务器列表
     */
    List<FsServer> getDownloadFsServerListByServerCode(String corpCode, String serverCode);
}
