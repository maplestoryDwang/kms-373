package handling.world;

import client.MapleCoolDownValueHolder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBuffStorage implements Serializable {
   private static final Map<Integer, List<PlayerBuffValueHolder>> buffs = new ConcurrentHashMap();
   private static final Map<Integer, List<MapleCoolDownValueHolder>> coolDowns = new ConcurrentHashMap();

   public static final void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
      buffs.put(chrid, toStore);
   }

   public static final void addCooldownsToStorage(int chrid, List<MapleCoolDownValueHolder> toStore) {
      coolDowns.put(chrid, toStore);
   }

   public static final List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
      return (List)buffs.remove(chrid);
   }

   public static final List<MapleCoolDownValueHolder> getCooldownsFromStorage(int chrid) {
      return (List)coolDowns.remove(chrid);
   }
}
