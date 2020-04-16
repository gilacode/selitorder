package com.avicenna.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;

import java.util.Date;

@Singleton
public class DateTimeUtil {

    private static int count = 1;

    private final String dateFormat;
    private final String dateTimeFormat;
    private final String timeFormat;
    private final String reverseDateFormat;
    private final String reverseDateTimeFormat;
    private final String dayOfWeekFormat;
    private final String monthFormat;
    private final String yearFormat;

    @Inject DateTimeUtil(Config config) {
        dateFormat = config.getString("format.date");
        dateTimeFormat = config.getString("format.dateTime");
        timeFormat = config.getString("format.time");
        reverseDateFormat = config.getString("format.reverseDateFormat");
        reverseDateTimeFormat = config.getString("format.reverseDateTimeFormat");
        dayOfWeekFormat = config.getString("format.dayOfWeek");
        monthFormat = config.getString("format.month");
        yearFormat = config.getString("format.year");

        Logger.debug(this.getClass().getSimpleName() + " instantiated "+count+" time(s)");
        count++;
    }

    public Date toDate(String strDate) {
        if (strDate == null) {
            return null;
        }
        DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        LocalDate parsedDate = LocalDate.parse(strDate, format);
        return asDate(parsedDate);
    }

    public String getDate(Date date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        return format.print(asLocalDate(date));
    }

    public Date getDateToday() {
        return new LocalDate().toDate();
    }

    public String getDayOfWeek(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(dayOfWeekFormat);
        return format.print(asLocalDate(date));
    }

    public String getMonth(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(monthFormat);
        return format.print(asLocalDate(date));
    }

    public String getYear(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(yearFormat);
        return format.print(asLocalDate(date));
    }

    public Date toDateTime(String strDateTime) {
        DateTimeFormatter format = DateTimeFormat.forPattern(dateTimeFormat);
        LocalDateTime parsedDateTime = LocalDateTime.parse(strDateTime, format);
        return asDate(parsedDateTime);
    }

    public String getDateTime(Date dateTime) {
        DateTimeFormatter format = DateTimeFormat.forPattern(dateTimeFormat);
        return format.print(asLocalDateTime(dateTime));
    }

    public Integer toTime(String time) {
        if (time == null) {
           return null;
        }

        Integer separator = time.indexOf(":");
        if (separator < 0) {
            return null;
        }

        String[] times = time.split("\\:");
        if (times.length < 2) {
            return null;
        }

        Integer hour = Integer.parseInt(times[0]);
        Integer minute = Integer.parseInt(times[1]);
        Integer result = (hour * 60) + minute;
        return result;
    }

    public String getTime(Integer minutes) {
        if (minutes != null) {
            Integer hours = minutes / 60; //since both are ints, you get an int
            minutes = minutes % 60;
            return StringUtils.leftPad(hours.toString(), 2, "0") + ":"
                    + StringUtils.leftPad(minutes.toString(), 2, "0");
        } else {
            return null;
        }
    }

    public String getTime(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(timeFormat);
        return format.print(asLocalTime(date));
    }

    public Date toReverseDate(String strDate) {
        DateTimeFormatter format = DateTimeFormat.forPattern(reverseDateFormat);
        LocalDate parsedDate = LocalDate.parse(strDate, format);
        return asDate(parsedDate);
    }

    public String getReverseDate(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(reverseDateFormat);
        return format.print(asLocalDate(date));
    }

    public String getReverseDateTime(Date date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(reverseDateTimeFormat);
        return format.print(asLocalDate(date));
    }

    private Date asDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.toDate();
    }

    private Date asDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toDate();
    }

    private LocalDate asLocalDate(Date date) {
        return new LocalDate(date);
    }

    private LocalTime asLocalTime(Date date) {
        return new LocalTime(date);
    }

    private LocalDateTime asLocalDateTime(Date date) {
        return new LocalDateTime(date);
    }

    public String getFirstDateOfMonth() {
        return getDate(LocalDate.now().withDayOfMonth(1).toDate());
    }

    public String getLastDateOfMonth() {
        return getDate(LocalDate.now().withDayOfMonth(LocalDate.now().dayOfMonth().getMaximumValue()).toDate());
    }

    public String getAge(Date birthDate) {
        // Birth date
        LocalDate birthdate = new LocalDate(birthDate);
        // Today's date
        LocalDate now = new LocalDate();
        // Period
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        Integer year = period.getYears(), month = period.getMonths(), day = period.getDays();
        return year + " Year(s), " + month + " Month(s), " + day + " Day(s)";
    }

    public Integer getDurationYears(Date startDate, Date endDate) {

        Period period = calculatePeriod(startDate, endDate);

        Integer years = period.getYears();

        return years;
    }

    public Integer getDurationMonths(Date startDate, Date endDate) {
        Period period = calculatePeriod(startDate, endDate);

        Integer months = period.getMonths();

        return months;
    }

    public Integer getDurationDays(Date startDate, Date endDate) {

        Period period = calculatePeriod(startDate, endDate);

        Integer days = period.getDays();
        Integer weeks = period.getWeeks();

        days = days + (weeks * 7);

        return days;
    }

    public String getDurations(String startDate, String endDate) {

        Integer years = getDurationYears(toDate(startDate), toDate(endDate));
        Integer months = getDurationMonths(toDate(startDate), toDate(endDate));
        Integer days = getDurationDays(toDate(startDate), toDate(endDate));

        String durations = years + " Year(s) / " + months + " Month(s) / " + days + " Day(s)";

        return durations;
    }

    public String getDurationsToday(String startDate) {
        return getDurations(startDate, getDate(getDateToday()));
    }

    private Period calculatePeriod(Date startDate, Date endDate) {
        DateTime START_DT = (startDate == null) ? null : new DateTime(startDate);
        DateTime END_DT = (endDate == null) ? null : new DateTime(endDate);

        Period period = new Period(START_DT, END_DT);

        return period;
    }

    public Date setSOD(String strDate) {
        if (strDate == null) {
            return null;
        }
        DateTime dateTime = new DateTime(toDate(strDate));
        return dateTime.withTimeAtStartOfDay().toDate();
    }

    public Date setSOD(Date date) {
        if (date == null) {
            return null;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.withTimeAtStartOfDay().toDate();
    }

    public Date setEOD(String strDate) {
        if (strDate == null) {
            return null;
        }
        DateTime dateTime = new DateTime(toDate(strDate));
        return dateTime.plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
    }

    public Date setEOD(Date date) {
        if (date == null) {
            return null;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.plusHours(23).plusMinutes(59).plusSeconds(59).toDate();
    }
}
