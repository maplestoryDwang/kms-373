package tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CurrentTime1 {
   public static String getCurrentTime() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
      String time = simpleTimeFormat.format(calz.getTime());
      return time;
   }

   public static String getCurrentTime(long times) {
      SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
      String time = simpleTimeFormat.format(times);
      return time;
   }

   public static String getAllCurrentTime() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      String time = simpleTimeFormat.format(calz.getTime());
      return time;
   }

   public static int 년() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getYear();
      return day;
   }

   public static int 월() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getMonth();
      return day;
   }

   public static int 일() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getDate();
      return day;
   }

   public static int 요일() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getDay();
      return day;
   }

   public static int 시() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getHours();
      return day;
   }

   public static int 분() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getMinutes();
      return day;
   }

   public static int 초() {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      int day = calz.getTime().getSeconds();
      return day;
   }

   public static String getAllCurrentTime(long times) {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      String time = simpleTimeFormat.format(times);
      return time;
   }

   public static String getAllCurrentTime1(long times) {
      Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
      SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("MM월dd일");
      String time = simpleTimeFormat.format(times);
      return time;
   }

   public static int getLeftTimeFromMinute(int minute) {
      Calendar d = Calendar.getInstance(TimeZone.getTimeZone("KST"));
      int min = d.get(12);
      int sec = d.get(13);
      int secs = min * 60 + sec;
      int leftsecs = minute * 60 - secs % (minute * 60);
      return leftsecs;
   }
}
