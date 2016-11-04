package com.qgutech.fs.utils;

import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

/**
 * 这个类用于帮助开发人员快速的构建hql语句和传递查询参数
 *
 * @author ELF@TEAM
 * @since 2016年2月25日 10:41:32
 */
public class HqlBuilder {

    private static final String BLANK = " ";
    private static final String EMPTY = "";

    private String sqlSplit;
    private StringBuilder builder;
    private List<Object> parameterList;
    private Map<String, Object> parameterMap;
    private Integer firstRecordIndex;
    private Integer maxRecordNum;

    public HqlBuilder() {
        super();
        init();
    }

    public HqlBuilder(String sqlSlice) {
        this();
        append(sqlSlice);
    }

    /**
     * 初始化当前的Hql Builder.
     */
    protected void init() {
        this.sqlSplit = BLANK;
        this.builder = new StringBuilder();
        this.parameterList = new ArrayList<Object>();
        this.parameterMap = new HashMap<String, Object>();
    }

    /**
     * 清理当前的Hql Builder,清空所有已经存在的数据.
     */
    public void clear() {
        init();
    }

    /**
     * 往Hql Builder里面增加新的Hql对象。
     *
     * @param sqlSlice sql片段
     * @return 当前HqlBuilder
     */
    public HqlBuilder append(String sqlSlice) {
        if (sqlSlice == null) {
            return this;
        }

        this.builder.append(sqlSplit);
        this.builder.append(sqlSlice);
        return this;
    }

    /**
     * 往Hql Builder里面增加Hql Slice,同时添加对应的参数.<br>
     * 注意这个参数是直接往参数List里面添加的.
     *
     * @param sqlSlice   新的Hql片段.
     * @param parameters 需要添加的参数数组.
     * @return 当前对象.
     */
    public HqlBuilder append(String sqlSlice, Object... parameters) {
        this.append(sqlSlice);
        this.addParameterList(Arrays.asList(parameters));
        return this;
    }

    /**
     * 往Hql Builder里面增加新的Hql片段,同时它不会添加与之前sql片段的分隔。
     *
     * @param sqlSlice sql片段
     * @return 当前HqlBuilder
     */
    public HqlBuilder escapeAppend(String sqlSlice) {
        if (sqlSlice == null) {
            return this;
        }

        this.builder.append(sqlSlice);
        return this;
    }

    /**
     * 去掉当前Builder中最后的一个字符，如果当前串中没有任何字符。则返回自己。
     *
     * @return 当前HqlBuilder
     */
    public HqlBuilder removeLastCharacter() {
        int length = this.builder.length();
        if (length <= 0) {
            return this;
        }

        this.builder.deleteCharAt(length - 1);
        return this;
    }

    /**
     * 去掉当前Builder中最后的一个字符，如果当前串中没有任何字符。<br>
     * 则返回自己。 如果已经存在的字符的个数少于要删除的字符个数，则删除全部字符。
     *
     * @return 当前HqlBuilder
     */
    public HqlBuilder removeLastCharacters(int n) {
        int length = this.builder.length();
        if (length == 0) {
            return this;
        }

        int start = n > length ? 0 : length - n;
        this.builder.delete(start, length);
        return this;
    }

    /**
     * 向当前的Sql起始部分插入一个串。
     *
     * @param slice 要插入的Sql片段。
     * @return 当前HqlBuilder
     */
    public HqlBuilder insertStartSlice(String slice) {
        if (slice == null || slice.isEmpty()) {
            return this;
        }

        this.builder.insert(0, sqlSplit + slice);
        return this;
    }

    /**
     * 首先检查当前Sql的末尾部分，如果末尾部分和oldSlice相同，<br>
     * newSlice替换oldSlice.如果末尾部分和oldSlice不相同，<br>
     * 这把当前的newSilice追加到当前SqlSlice的末尾。
     *
     * @param newSlice 要添加的SQL Slice。
     * @param oldSlice 可能已存在的Hql Slice
     * @return 当前HqlBuilder
     */
    public HqlBuilder replaceOrAddLastSlice(String newSlice, String oldSlice) {
        if (oldSlice == null || oldSlice.isEmpty()) {
            append(newSlice);
            return this;
        }

        int sqlLength = this.builder.length();
        int oldSliceLength = oldSlice.length();
        if (oldSliceLength > sqlLength) {
            append(newSlice);
            return this;
        }

        int lastIndexOf = this.builder.lastIndexOf(oldSlice);
        if (lastIndexOf == (sqlLength - oldSliceLength)) {
            this.builder.replace(lastIndexOf, sqlLength, newSlice);
        } else {
            append(newSlice);
        }

        return this;
    }

    /**
     * 用于删除字符串的最后一个字符
     *
     * @param str 需要处理的字符串
     * @return 返回去除后的builder
     */
    public HqlBuilder removeLastSlice(String str) {
        if (str == null || str.isEmpty()) {
            return this;
        }

        int length = this.builder.length();
        int lastIndexOf = this.builder.lastIndexOf(str);
        if (lastIndexOf != -1 && lastIndexOf == (length - str.length())) {
            this.builder.delete(lastIndexOf, length);
        }

        return this;
    }

    public HqlBuilder enableSplit() {
        this.sqlSplit = BLANK;
        return this;
    }

    public HqlBuilder disableSplit() {
        this.sqlSplit = EMPTY;
        return this;
    }

    public String getSql() {
        return this.builder.toString();
    }

    public String getSqlSplit() {
        return this.sqlSplit;
    }

    public HqlBuilder append(HqlBuilder hqlBuilder) {
        if (hqlBuilder == null) {
            return this;
        }

        if (hqlBuilder == this) {
            return this;
        }

        this.append(hqlBuilder.getSql());
        this.addParameterMap(hqlBuilder.getParameterMap());
        this.addParameterList(hqlBuilder.getParameterList());
        return this;
    }

    /**
     * 用当前的sql片段替换当前builder中的最后一个字符。
     *
     * @param slice sql片段。
     * @return 当前HqlBuilder
     */
    public HqlBuilder replaceLastCharacter(String slice) {
        if (slice == null) {
            return this;
        }

        int length = this.builder.length();
        if (length == 0) {
            return this;
        }

        this.builder.replace(length - 1, length, slice);
        return this;
    }

    /**
     * 拼写SQL语句 添加参数
     *
     * @param placeholder 参数名
     * @param value       参数值
     * @return 当前SQL
     */
    public HqlBuilder addParameter(String placeholder, Object value) {
        if (value instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) value;
            value = new Timestamp(date.getTime());
        }
        parameterMap.put(placeholder, value);
        return this;
    }

    public HqlBuilder addParameterMap(Map<String, Object> parameters) {
        Set<Entry<String, Object>> entrySet = parameters.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                value = new Timestamp(date.getTime());
            }
            parameterMap.put(key, value);
        }
        return this;
    }

    public HqlBuilder setParameterMap(Map<String, Object> parameters) {
        if (parameters == null) {
            parameterMap = new HashMap<String, Object>();
        } else {
            Set<Entry<String, Object>> entrySet = parameters.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof java.util.Date) {
                    java.util.Date date = (java.util.Date) value;
                    value = new Timestamp(date.getTime());
                }
                parameterMap.put(key, value);
            }
        }
        return this;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public List<Object> getParameterList() {
        return parameterList;
    }

    public HqlBuilder setParameters(List<Object> parameters) {
        if (parameters == null) {
            this.parameterList = new ArrayList<Object>();
        } else {
            for (Object parameter : parameters) {
                if (parameter instanceof java.util.Date) {
                    java.util.Date date = (java.util.Date) parameter;
                    parameter = new Timestamp(date.getTime());
                }
                this.parameterList.add(parameter);
            }
        }
        return this;
    }

    public HqlBuilder addParameter(Object parameter) {
        if (parameter instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) parameter;
            parameter = new Timestamp(date.getTime());
        }
        this.parameterList.add(parameter);
        return this;
    }

    public HqlBuilder addParameters(Object... parameters) {
        List<Object> parameterList = Arrays.asList(parameters);
        for (Object parameter : parameterList) {
            if (parameter instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) parameter;
                parameter = new Timestamp(date.getTime());
            }
            this.parameterList.add(parameter);
        }
        return this;
    }

    public HqlBuilder addParameterList(List<Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return this;
        }

        for (Object parameter : parameters) {
            if (parameter instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) parameter;
                parameter = new Timestamp(date.getTime());
            }
            this.parameterList.add(parameter);
        }
        return this;
    }

    @Override
    public String toString() {
        return this.getSql();
    }

    public Integer getFirstRecordIndex() {
        return firstRecordIndex;
    }

    public void setFirstRecordIndex(Integer firstRecordIndex) {
        this.firstRecordIndex = firstRecordIndex;
    }

    public Integer getMaxRecordNum() {
        return maxRecordNum;
    }

    public void setMaxRecordNum(Integer maxRecordNum) {
        this.maxRecordNum = maxRecordNum;
    }


}
