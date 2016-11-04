package com.qgutech.fs.service;

import java.util.List;
import java.util.Map;

public interface FileServerService {

    /**
     * 根据文件在文件系统中的主键获取源文件url（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 源文件url
     */
    String getOriginFileUrl(String storedFileId);

    /**
     * 根据文件在文件系统中的主键列表获取源文件url列表（未作改动的文件）。
     * 此方法适用于所有类型的文件。
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 源文件url列表
     */
    Map<String, String> getBatchOriginFileUrlMap(List<String> storedFileIdList);

    /**
     * 根据文件在文件系统中的主键获取清晰度（由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）
     * 和视频url映射的列表。
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频清晰度和视频url的映射。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频清晰度和视频url的映射，
     * 第一个视频清晰度和视频url的映射对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 视频清晰度和视频url的映射列表。
     */
    List<Map<String, String>> getVideoUrls(String storedFileId);

    /**
     * 根据文件在文件系统中的主键获取视频清晰度（
     * 由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）和视频url的映射。
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则为视频清晰度和视频url的映射。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频清晰度和视频url的映射。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 清晰度和视频url的映射
     */
    Map<String, String> getVideoTypeUrlMap(String storedFileId);

    /**
     * 根据文件在文件系统中的主键列表批量获取清晰度（由码率和分辨率决定{@link com.qgutech.fs.domain.VideoTypeEnum}）
     * 和视频url映射的列表。
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频清晰度和视频url映射的列表，列表如下：
     * <ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频清晰度和视频url映射。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频清晰度和视频url映射，
     * 第一个视频清晰度和视频url映射对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 批量返回清晰度和视频url映射的列表。
     */
    Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(List<String> storedFileIdList);

    /**
     * 根据文件在文件系统中的主键获视频封面路径的列表。
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频的封面路径。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频的封面路径，
     * 第一个视频封面路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 视频封面路径的列表。
     */
    List<String> getVideoCoverUrls(String storedFileId);

    /**
     * 根据文件在文件系统中的主键获视频封面路径。
     * <b>此方法适用于单个视频文件以及视频包：<ul>
     * <li>如果为单个视频，则为视频的封面路径。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频封面路径。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 视频封面路径。
     */
    String getVideoCoverUrl(String storedFileId);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和视频封面路径的映射。
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频封面路径如下：
     * <ul>
     * <li>如果为单个视频，则为视频的封面路径。</li>
     * <li>如果为视频包，则为<span style='color:red;'>第一个</span>视频封面路径。</li>
     * </ul></b>
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 文件主键和视频封面路径的映射
     */
    Map<String, String> getBatchVideoCoverUrlMap(List<String> storedFileIdList);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和视频封面路径列表的映射。
     * <b>此方法适用于单个视频文件以及视频包,key为文件主键，value为视频封面路径的列表，列表如下：
     * <ul>
     * <li>如果为单个视频，则列表的长度为1，每一个列表元素为一个视频的封面路径。</li>
     * <li>如果为视频包，则列表的长度为视频包中的视频个数，每一个列表元素为一个视频的封面路径，
     * 第一个视频封面路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 文件主键和视频封面路径列表的映射，key为文件主键，value为视频封面路径的列表
     */
    Map<String, List<String>> getBatchVideoCoverUrlsMap(List<String> storedFileIdList);

    /**
     * 根据文件在文件系统中的主键获音频的url路径。
     * <b>此方法适用于单个音频文件以及音频包：<ul>
     * <li>如果为单个音频，则为音频的url路径。</li>
     * <li>如果为音频包，则为<span style='color:red;'>第一个</span>音频的url路径。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 音频的url路径
     */
    String getAudioUrl(String storedFileId);

    /**
     * 根据文件在文件系统中的主键获音频url路径的列表。
     * <b>此方法适用于单个音频文件以及音频包：<ul>
     * <li>如果为单个音频，则列表的长度为1，每一个列表元素为一个音频的url路径。</li>
     * <li>如果为音频包，则列表的长度为音频包中的音频个数，每一个列表元素为一个音频的url路径，
     * 第一个音频url路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileId 文件在文件系统中的主键
     * @return 音频url路径的列表
     */
    List<String> getAudioUrls(String storedFileId);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和音频url路径的映射。
     * <b>此方法适用于单个音频文件以及音频包,key为文件主键，value为音频的url路径如下：
     * <ul>
     * <li>如果为单个音频，则为音频的url路径。</li>
     * <li>如果为音频包，则为<span style='color:red;'>第一个</span>音频的url路径。</li>
     * </ul></b>
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 文件主键和音频url路径的映射
     */
    Map<String, String> getBatchAudioUrlMap(List<String> storedFileIdList);

    /**
     * 根据文件在文件系统中的主键列表获取文件主键和音频url路径列表的映射。
     * <b>此方法适用于单个音频文件以及音频包,key为文件主键，value为音频url路径的列表，列表如下：
     * <ul>
     * <li>如果为单个音频，则列表的长度为1，每一个列表元素为一个音频的url路径。</li>
     * <li>如果为音频包，则列表的长度为音频包中的音频个数，每一个列表元素为一个音频的url路径，
     * 第一个音频的url路径对应第一个列表元素。</li>
     * </ul></b>
     *
     * @param storedFileIdList 文件在文件系统中的主键列表
     * @return 文件主键和音频url路径列表的映射，key为文件主键，value为音频url路径的列表
     */
    Map<String, List<String>> getBatchAudioUrlsMap(List<String> storedFileIdList);

    String getFileUrl(String storedFileId);

    Map<String, String> getBatchFileUrlMap(List<String> storedFileIdList);

    Integer getSubFileCount(String storedFileId);

    List<Integer> getSubFileCountList(String storedFileId);

    Map<String, Integer> getSubFileCountMap(List<String> storedFileIdList);
}
