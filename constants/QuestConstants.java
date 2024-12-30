package constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import tools.Pair;

public class QuestConstants {
   public static Map<Integer, List<Pair<Integer, Integer>>> subQuestCheck = new HashMap();
   public static List<Integer> blockQuest = new ArrayList();

   static {
      File[] var0 = (new File("resources\\bin\\Quest.wz\\Check_sub")).listFiles();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         File f = var0[var2];

         try {
            FileInputStream setting = new FileInputStream(f);
            int qid = Integer.parseInt(f.getName().replaceAll(".info", ""));
            Properties setting_ = new Properties();
            setting_.load(setting);
            setting.close();
            String[] mobs = setting_.getProperty("mobs").split(",");
            List<Pair<Integer, Integer>> mobs_int = new ArrayList();
            String[] var9 = mobs;
            int var10 = mobs.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               String m = var9[var11];
               String[] sp = m.split("=");
               mobs_int.add(new Pair(Integer.parseInt(sp[0]), Integer.parseInt(sp[1])));
            }

            subQuestCheck.put(qid, mobs_int);
         } catch (FileNotFoundException var14) {
            var14.printStackTrace();
         } catch (IOException var15) {
            var15.printStackTrace();
         }
      }

   }
}
