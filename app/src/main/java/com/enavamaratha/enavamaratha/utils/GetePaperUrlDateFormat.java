package com.enavamaratha.enavamaratha.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pooja Mantri on 3/1/18.
 */

public class GetePaperUrlDateFormat {


    public String ePaperPdfName(String ePaperdate) {


        try {

            // String dateStr = "21/02/2011";

            // Original Date Format
            DateFormat srcDf = new SimpleDateFormat("dd/MM/yyyy");

            // parse the date string into Date object
            Date date = srcDf.parse(ePaperdate);

            // Date Conversion Format
            DateFormat destDf = new SimpleDateFormat("dd_MM_yyyy");

            // format the date into another format
            ePaperdate = destDf.format(date);


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ePaperdate;
    }


    // For ePaper Pdf Url We required this format
    public String ePaperPdfUrl(String ePaperdate) {
        try {

            // String dateStr = "21/02/2011";

            // Original Date Format
            DateFormat srcDf = new SimpleDateFormat("dd/MM/yyyy");

            // parse the date string into Date object
            Date date = srcDf.parse(ePaperdate);

            // Date Conversion Format
            DateFormat destDf = new SimpleDateFormat("yyyy/MM/dd");

            // format the date into another format
            ePaperdate = destDf.format(date);


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ePaperdate;
    }
}
