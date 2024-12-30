package server.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class QuestCompleteStatus {
   public static List<Integer> completeQuests = new ArrayList();

   public static int getInt(String a) {
      try {
         return Integer.parseInt(a);
      } catch (Exception var2) {
         return 0;
      }
   }

   public static void run() {
      MapleDataProvider npc = MapleDataProviderFactory.getDataProvider(new File("wz/Npc.wz"));
      MapleDataDirectoryEntry root = npc.getRoot();
      Iterator var2 = root.getFiles().iterator();

      label71:
      while(true) {
         MapleDataFileEntry topDir;
         int id;
         do {
            if (!var2.hasNext()) {
               MapleData questInfo = MapleDataProviderFactory.getDataProvider(new File("wz/Quest.wz")).getData("QuestInfo.img");
               Iterator var11 = questInfo.iterator();

               while(true) {
                  int id;
                  int autoStart;
                  int selfStart;
                  int dailyAlarm;
                  do {
                     do {
                        do {
                           if (!var11.hasNext()) {
                              return;
                           }

                           MapleData data = (MapleData)var11.next();
                           autoStart = MapleDataTool.getInt("autoStart", data, 0);
                           selfStart = MapleDataTool.getInt("selfStart", data, 0);
                           dailyAlarm = MapleDataTool.getInt("dailyAlarm", data, 0);
                           id = MapleDataTool.getInt("blocked", data, 0);
                           id = getInt(data.getName());
                        } while(id > 0);
                     } while(id == 0);
                  } while(autoStart <= 0 && selfStart <= 0 && dailyAlarm <= 0);

                  if (!completeQuests.contains(id)) {
                  }
               }
            }

            topDir = (MapleDataFileEntry)var2.next();
         } while(topDir.getName().length() > 20);

         Iterator var4 = npc.getData(topDir.getName()).iterator();

         while(true) {
            MapleData data;
            do {
               if (!var4.hasNext()) {
                  continue label71;
               }

               data = (MapleData)var4.next();
            } while(!data.getName().contains("condition"));

            Iterator var6 = data.iterator();

            while(var6.hasNext()) {
               MapleData questData = (MapleData)var6.next();
               id = getInt(questData.getName());
               if (id != 0 && !completeQuests.contains(id)) {
               }
            }
         }
      }
   }
}
