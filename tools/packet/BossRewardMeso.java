package tools.packet;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import tools.Triple;

public class BossRewardMeso {
   private static List<Triple<Integer, Integer, Integer>> lists = new ArrayList();
   private static List<Integer> list = new ArrayList();

   public static void Setting() {
      Load();
      lists.add(new Triple(8800022, 9210000, (Integer)list.get(0)));
      lists.add(new Triple(8800002, 9210001, (Integer)list.get(1)));
      lists.add(new Triple(8800102, 9210002, (Integer)list.get(2)));
      lists.add(new Triple(8880010, 9210003, (Integer)list.get(3)));
      lists.add(new Triple(8880002, 9210004, (Integer)list.get(4)));
      lists.add(new Triple(8880000, 9210005, (Integer)list.get(5)));
      lists.add(new Triple(8870000, 9210006, (Integer)list.get(6)));
      lists.add(new Triple(8870100, 9210007, (Integer)list.get(7)));
      lists.add(new Triple(8880200, 9210008, (Integer)list.get(8)));
      lists.add(new Triple(8900100, 9210009, (Integer)list.get(9)));
      lists.add(new Triple(8900000, 9210010, (Integer)list.get(10)));
      lists.add(new Triple(8910100, 9210011, (Integer)list.get(11)));
      lists.add(new Triple(8910000, 9210012, (Integer)list.get(12)));
      lists.add(new Triple(8920100, 9210013, (Integer)list.get(13)));
      lists.add(new Triple(8920000, 9210014, (Integer)list.get(14)));
      lists.add(new Triple(8930100, 9210015, (Integer)list.get(15)));
      lists.add(new Triple(8930000, 9210016, (Integer)list.get(16)));
      lists.add(new Triple(8840007, 9210017, (Integer)list.get(17)));
      lists.add(new Triple(8840000, 9210018, (Integer)list.get(18)));
      lists.add(new Triple(8840014, 9210019, (Integer)list.get(19)));
      lists.add(new Triple(8810214, 9210020, (Integer)list.get(20)));
      lists.add(new Triple(8810018, 9210021, (Integer)list.get(21)));
      lists.add(new Triple(8810122, 9210022, (Integer)list.get(22)));
      lists.add(new Triple(8860005, 9210023, (Integer)list.get(23)));
      lists.add(new Triple(8860000, 9210024, (Integer)list.get(24)));
      lists.add(new Triple(8820001, 9210025, (Integer)list.get(25)));
      lists.add(new Triple(8820212, 9210026, (Integer)list.get(26)));
      lists.add(new Triple(8850111, 9210027, (Integer)list.get(27)));
      lists.add(new Triple(8850011, 9210028, (Integer)list.get(28)));
      lists.add(new Triple(8950101, 9210029, (Integer)list.get(29)));
      lists.add(new Triple(8950001, 9210030, (Integer)list.get(30)));
      lists.add(new Triple(8880110, 9210031, (Integer)list.get(31)));
      lists.add(new Triple(8880100, 9210032, (Integer)list.get(32)));
      lists.add(new Triple(8880140, 9210033, (Integer)list.get(33)));
      lists.add(new Triple(8880141, 9210034, (Integer)list.get(34)));
      lists.add(new Triple(8500002, 9210035, (Integer)list.get(35)));
      lists.add(new Triple(8500012, 9210036, (Integer)list.get(36)));
      lists.add(new Triple(8500022, 9210037, (Integer)list.get(37)));
      lists.add(new Triple(8880340, 9210038, (Integer)list.get(38)));
      lists.add(new Triple(8880300, 9210039, (Integer)list.get(39)));
      lists.add(new Triple(8880410, 9210040, (Integer)list.get(40)));
      lists.add(new Triple(8880502, 9210041, (Integer)list.get(41)));
      lists.add(new Triple(8644650, 9210042, (Integer)list.get(42)));
      lists.add(new Triple(8645009, 9210043, (Integer)list.get(43)));
      lists.add(new Triple(8644655, 9210046, (Integer)list.get(44)));
      lists.add(new Triple(8645066, 9210047, (Integer)list.get(45)));
      lists.add(new Triple(8880142, 9210048, (Integer)list.get(46)));
      lists.add(new Triple(8880600, 9210050, (Integer)list.get(47)));
      lists.add(new Triple(8880711, 9210051, (Integer)list.get(48)));
      lists.add(new Triple(8880700, 9210052, (Integer)list.get(49)));
   }

   public static void Load() {
      try {
         FileInputStream setting = new FileInputStream("Properties/BossRewardMeso.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         lists.clear();
         list.clear();
         String[] mob = setting_.getProperty(toUni("meso")).replaceAll("\\}", "").replaceAll("\\{", "").replaceAll(" ", "").split(",");
         if (mob != null) {
            String[] var3 = mob;
            int var4 = mob.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String s = var3[var5];
               int meso = 0;
               if (s.length() > 0) {
                  meso = Integer.parseInt(s);
               }

               if (meso != 0) {
                  list.add(meso);
               }
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public static int RewardBossId(int bossid) {
      int Result = 0;
      switch(bossid) {
      case 9210009:
         Result = 8900103;
         break;
      case 9210010:
         Result = 8900003;
      case 9210011:
      case 9210012:
      case 9210015:
      case 9210016:
      case 9210017:
      case 9210018:
      case 9210019:
      case 9210020:
      case 9210021:
      case 9210022:
      case 9210023:
      case 9210024:
      case 9210025:
      case 9210027:
      case 9210028:
      case 9210035:
      case 9210036:
      case 9210037:
      case 9210042:
      case 9210043:
      case 9210044:
      case 9210045:
      case 9210046:
      case 9210047:
      case 9210049:
      default:
         break;
      case 9210013:
         Result = 8920106;
         break;
      case 9210014:
         Result = 8920006;
         break;
      case 9210026:
         Result = 8820101;
         break;
      case 9210029:
         Result = 8950102;
         break;
      case 9210030:
         Result = 8950002;
         break;
      case 9210031:
         Result = 8880111;
         break;
      case 9210032:
         Result = 8880101;
         break;
      case 9210033:
         Result = 8880167;
         break;
      case 9210034:
         Result = 8880177;
         break;
      case 9210038:
         Result = 8880342;
         break;
      case 9210039:
         Result = 8880302;
         break;
      case 9210040:
         Result = 8880405;
         break;
      case 9210041:
         Result = 8880518;
         break;
      case 9210048:
         Result = 8880156;
         break;
      case 9210050:
         Result = 8880614;
         break;
      case 9210051:
         Result = 8880725;
         break;
      case 9210052:
         Result = 8880726;
      }

      return Result;
   }

   protected static String toUni(String kor) throws UnsupportedEncodingException {
      return new String(kor.getBytes("KSC5601"), "8859_1");
   }

   public static List<Triple<Integer, Integer, Integer>> getLists() {
      return lists;
   }

   public static void setLists(List<Triple<Integer, Integer, Integer>> lists) {
      BossRewardMeso.lists = lists;
   }
}
