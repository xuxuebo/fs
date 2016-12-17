package com.qgutech.fs.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePropertyEditor extends PropertyEditorSupport {

    private static final String[] DATE_PATTERNS = new String[]{"yyyy-MM-dd",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyyMMdd",
            "yyyyMMdd HH:mm:ss", "yyyyMMdd HH:mm:ss.S",
            "yyyyMMdd HH:mm:ss.SSS", "yyyyMMdd HH:mm", "yyyyMMdd HH"};

    public DatePropertyEditor() {

    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)) {
            setValue(null);
            return;
        }

        try {
            Date date = DateUtils.parseDate(text, DATE_PATTERNS);
            setValue(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("can't convert text[" + text
                    + "] to date because of format not support!", e);
        }
    }

    public String getAsText() {
        if (getValue() == null) {
            return null;
        }

        Date date = (Date) getValue();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

}
