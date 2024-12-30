package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseBackup {
   public static String dbpath;
   public static String dbuser;
   public static String dbpass;
   public static String dbname;
   public static String encoding;
   public static int savetime;
   public static int removetime;

   public static void main(String[] args) {
      try {
         Properties props = new Properties();
         FileReader fr = null;
         fr = new FileReader("Properties/database.properties");
         props.load(fr);
         dbpath = props.getProperty("query.path");
         dbuser = props.getProperty("query.user");
         dbpass = props.getProperty("query.password");
         dbname = props.getProperty("query.schema");
         encoding = props.getProperty("encoding");
         savetime = Integer.parseInt(props.getProperty("query.savetime"));
         removetime = Integer.parseInt(props.getProperty("query.removetime"));
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
            public void run() {
               DatabaseBackup.save();
               DatabaseBackup.delete();
            }
         }, 1000L, (long)savetime);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public static void save() {
      try {
         Runtime runtime = Runtime.getRuntime();
         String date = (new SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분 ss초")).format(new Date());
         File backupFile = new File("sql/backup_" + date + ".sql");
         FileWriter fw = new FileWriter(backupFile);
         Process child = runtime.exec(dbpath + " --user=" + dbuser + " --password=" + dbpass + " --default-character-set=" + encoding + " --lock-all-tables --opt " + dbname);
         InputStreamReader rs = new InputStreamReader(child.getInputStream());
         BufferedReader br = new BufferedReader(rs);

         String line;
         while((line = br.readLine()) != null) {
            fw.write(line + "\n");
         }

         fw.close();
         rs.close();
         br.close();
         System.out.println("[알림] " + date + " 데이터 베이스 저장이 완료되었습니다.");
      } catch (IOException var8) {
         var8.printStackTrace();
      }

   }

   public static void delete() {
      Calendar cal = Calendar.getInstance();
      long todayMil = cal.getTimeInMillis();
      Calendar fileCal = Calendar.getInstance();
      Date fileDate = null;
      File path = new File("sql/");
      File[] list = path.listFiles();

      for(int i = 0; i < list.length; ++i) {
         fileDate = new Date(list[i].lastModified());
         fileCal.setTime(fileDate);
         long diffMil = todayMil - fileCal.getTimeInMillis();
         if (diffMil > (long)removetime && list[i].exists()) {
            System.out.println(list[i].getName() + " 파일을 삭제하였습니다.");
            list[i].delete();
         }
      }

   }
}
