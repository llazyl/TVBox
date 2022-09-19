package com.github.tvbox.osc.ui.tv.widget;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Epginfo {

    public Date startdateTime;
    public Date enddateTime;
    public int datestart;
    public int dateend;
    public String title;
    public String start;
    public String end;

    public Epginfo(String str, String str1, String str2) {

        title = str;
        start = str1;
        end = str2;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startdateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(simpleDateFormat.format(new Date()) + " " + str1 + ":00", new ParsePosition(0));
        enddateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(simpleDateFormat.format(new Date()) + " " + str2 + ":00", new ParsePosition(0));
        datestart = Integer.parseInt(start.replace(":", ""));
        dateend = Integer.parseInt(end.replace(":", ""));
    }
}