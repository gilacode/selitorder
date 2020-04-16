package com.avicenna.util;

import com.avicenna.nav.NavMgr;
import com.google.inject.Inject;

public class DateTimeStatic {

    @Inject
    static DateTimeUtil dateTimeUtil;

    public static DateTimeUtil getDateTimeUtil() {
        return dateTimeUtil;
    }
}
