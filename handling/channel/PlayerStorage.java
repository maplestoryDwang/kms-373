package handling.channel;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import handling.world.CharacterTransfer;
import handling.world.CheaterData;
import handling.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import server.Timer;

public class PlayerStorage {
   private final Map<String, MapleCharacter> nameToChar = new ConcurrentHashMap();
   private final Map<Integer, MapleCharacter> idToChar = new ConcurrentHashMap();
   private final Map<Integer, MapleClient> idToClient = new ConcurrentHashMap();
   private final Map<Integer, CharacterTransfer> PendingCharacter = new ConcurrentHashMap();

   public PlayerStorage() {
      Timer.PingTimer.getInstance().register(new PlayerStorage.PersistingTask(), 60000L);
   }

   public final Map<Integer, MapleCharacter> getAllCharacters() {
      return this.idToChar;
   }

   public final void registerPlayer(MapleCharacter chr) {
      this.nameToChar.put(chr.getName().toLowerCase(), chr);
      this.idToChar.put(chr.getId(), chr);
      this.idToClient.put(chr.getAccountID(), chr.getClient());
      World.Find.register(chr.getId(), chr.getAccountID(), chr.getName(), chr.getClient().getChannel());
   }

   public final void registerPendingPlayer(CharacterTransfer chr, int playerid) {
      this.PendingCharacter.put(playerid, chr);
   }

   public final void deregisterPlayer(MapleCharacter chr) {
      this.nameToChar.remove(chr.getName().toLowerCase());
      this.idToChar.remove(chr.getId());
      this.idToClient.remove(chr.getAccountID());
      World.Find.forceDeregister(chr.getId(), chr.getAccountID(), chr.getName());
   }

   public final void deregisterPlayer(int idz, int accIdz, String namez) {
      this.nameToChar.remove(namez.toLowerCase());
      this.idToChar.remove(idz);
      this.idToClient.remove(accIdz);
      World.Find.forceDeregister(idz, accIdz, namez);
   }

   public final int pendingCharacterSize() {
      return this.PendingCharacter.size();
   }

   public final void deregisterPendingPlayer(int charid) {
      this.PendingCharacter.remove(charid);
   }

   public final CharacterTransfer getPendingCharacter(int charid) {
      return (CharacterTransfer)this.PendingCharacter.remove(charid);
   }

   public final MapleCharacter getCharacterByName(String name) {
      return (MapleCharacter)this.nameToChar.get(name.toLowerCase());
   }

   public final MapleCharacter getCharacterById(int id) {
      return (MapleCharacter)this.idToChar.get(id);
   }

   public final MapleClient getClientById(int id) {
      return (MapleClient)this.idToClient.get(id);
   }

   public final int getConnectedClients() {
      return this.idToChar.size();
   }

   public final List<CheaterData> getCheaters() {
      List<CheaterData> cheaters = new ArrayList();
      return cheaters;
   }

   public final List<CheaterData> getReports() {
      List<CheaterData> cheaters = new ArrayList();
      return cheaters;
   }

   public final String getOnlinePlayers(boolean byGM) {
      StringBuilder sb = new StringBuilder();
      Iterator itr;
      if (byGM) {
         itr = this.nameToChar.values().iterator();

         while(itr.hasNext()) {
            sb.append(MapleCharacterUtil.makeMapleReadable(((MapleCharacter)itr.next()).getName()));
            sb.append(", ");
         }

         sb.insert(0, "동접 (" + this.nameToChar.size() + "명) [");
         sb.append("]");
      } else {
         itr = this.nameToChar.values().iterator();

         while(itr.hasNext()) {
            MapleCharacter chr = (MapleCharacter)itr.next();
            if (!chr.isGM()) {
               sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
               sb.append(", ");
            }
         }
      }

      return sb.toString();
   }

   public final List<String> getOnlinePlayers2(boolean byGM) {
      List<String> list = new ArrayList();
      if (byGM) {
         int i = 0;
         Iterator itr = this.nameToChar.values().iterator();

         while(itr.hasNext()) {
            ++i;
            list.add(((MapleCharacter)itr.next()).getName());
         }
      } else {
         Iterator itr = this.nameToChar.values().iterator();

         while(itr.hasNext()) {
            MapleCharacter chr = (MapleCharacter)itr.next();
            if (!chr.isGM()) {
               list.add(((MapleCharacter)itr.next()).getName());
            }
         }
      }

      return list;
   }

   public final void disconnectAll() {
      this.disconnectAll(false);
   }

   public final void disconnectAll(boolean checkGM) {
      Iterator itr = this.nameToChar.values().iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();

         try {
            chr.getClient().disconnect(true, false, true);
            chr.getClient().getSession().close();
            World.Find.forceDeregister(chr.getId(), chr.getAccountID(), chr.getName());
            System.out.println(chr.getName() + " 캐릭터를 셧다운 했습니다.");
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   public final void broadcastPacket(byte[] data) {
      Iterator itr = this.nameToChar.values().iterator();

      while(itr.hasNext()) {
         ((MapleCharacter)itr.next()).getClient().getSession().writeAndFlush(data);
      }

   }

   public final void broadcastSmegaPacket(byte[] data) {
      Iterator itr = this.nameToChar.values().iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();
         if (chr.getClient().isLoggedIn() && chr.getSmega()) {
            chr.getClient().getSession().writeAndFlush(data);
         }
      }

   }

   public final void broadcastGMPacket(byte[] data) {
      Iterator itr = this.nameToChar.values().iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();
         if (chr.getClient().isLoggedIn() && chr.isIntern()) {
            chr.getClient().getSession().writeAndFlush(data);
         }
      }

   }

   public class PersistingTask implements Runnable {
      public void run() {
         long currenttime = System.currentTimeMillis();
         Iterator<Entry<Integer, CharacterTransfer>> itr = PlayerStorage.this.PendingCharacter.entrySet().iterator();
         ArrayList removes = new ArrayList();

         while(itr.hasNext()) {
            Entry<Integer, CharacterTransfer> target = (Entry)itr.next();
            if (currenttime - ((CharacterTransfer)target.getValue()).TranferTime > 40000L) {
               removes.add(target);
            }
         }

         Iterator var7 = removes.iterator();

         while(var7.hasNext()) {
            Entry<Integer, CharacterTransfer> remove = (Entry)var7.next();
            PlayerStorage.this.PendingCharacter.remove(remove.getKey());
         }

      }
   }
}
