package client.inventory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class PetDataFactory {
   private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Item.wz"));
   private static Map<Pair<Integer, Integer>, PetCommand> petCommands = new HashMap();
   private static Map<Integer, Integer> petHunger = new HashMap();
   private static Map<Integer, Integer> petWonderGrade = new HashMap();

   public static final PetCommand getPetCommand(int petId, int skillId) {
      PetCommand ret = (PetCommand)petCommands.get(new Pair(petId, skillId));
      if (ret != null) {
         return ret;
      } else {
         MapleData skillData = dataRoot.getData("Pet/" + petId + ".img");
         int prob = 0;
         int inc = 0;
         if (skillData != null) {
            prob = MapleDataTool.getInt("interact/" + skillId + "/prob", skillData, 0);
            inc = MapleDataTool.getInt("interact/" + skillId + "/inc", skillData, 0);
         }

         ret = new PetCommand(petId, skillId, prob, inc);
         petCommands.put(new Pair(petId, skillId), ret);
         return ret;
      }
   }

   public static final int getWonderGrade(int petId) {
      Integer ret = (Integer)petWonderGrade.get(petId);
      if (ret != null) {
         return ret;
      } else {
         MapleData wonderData = dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/wonderGrade");
         ret = MapleDataTool.getInt(wonderData, -1);
         petHunger.put(petId, ret);
         return ret;
      }
   }

   public static final int getHunger(int petId) {
      Integer ret = (Integer)petHunger.get(petId);
      if (ret != null) {
         return ret;
      } else {
         MapleData hungerData = dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry");
         ret = MapleDataTool.getInt(hungerData, 1);
         petHunger.put(petId, ret);
         return ret;
      }
   }
}
