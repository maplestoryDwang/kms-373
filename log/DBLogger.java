package log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import tools.FileoutputUtil;

public class DBLogger {
   private static final DBLogger instance = new DBLogger();
   private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private static final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");

   public static DBLogger getInstance() {
      return instance;
   }

   private String escape(String input) {
      return input.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
   }

   public static String CurrentReadable_Time() {
      return sdf.format(Calendar.getInstance().getTime());
   }

   public void logChat(LogType.Chat type, int cid, String charname, String message, String etc) {
      String var10000 = FileoutputUtil.채팅로그;
      String var10001 = CurrentReadable_Time();
      FileoutputUtil.log(var10000, "[" + var10001 + "] 캐릭터ID : " + cid + "  /  닉네임 : " + this.escape(charname) + "  /  메세지 : " + this.escape(message) + "  /  맵 : " + this.escape(etc));
   }
}
