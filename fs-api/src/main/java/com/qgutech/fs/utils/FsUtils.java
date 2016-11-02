package com.qgutech.fs.utils;


import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FsUtils {

    public static String formatDateToYYMM(Date date) {
        Assert.notNull(date, "Date is null!");
        return new SimpleDateFormat("yyMM").format(date);
    }

}
