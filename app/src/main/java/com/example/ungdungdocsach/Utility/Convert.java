package com.example.ungdungdocsach.Utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Convert {
    public static final String formatTimeStamp(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(timestamp);
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate;
    }
}
