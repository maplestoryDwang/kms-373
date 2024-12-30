package client;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import tools.packet.CWvsContext;

public class BuddyList implements Serializable {
   private static final long serialVersionUID = 1413738569L;
   private final Map<Integer, BuddylistEntry> buddies = new LinkedHashMap();
   private byte capacity;
   private boolean changed = false;
   private Deque<CharacterNameAndId> pendingRequests = new LinkedList();

   public BuddyList(byte capacity) {
      this.capacity = capacity;
   }

   public boolean contains(int accId) {
      return this.buddies.containsKey(accId);
   }

   public boolean containsVisible(int accId) {
      BuddylistEntry ble = (BuddylistEntry)this.buddies.get(accId);
      return ble == null ? false : ble.isVisible();
   }

   public byte getCapacity() {
      return this.capacity;
   }

   public void setCapacity(byte capacity) {
      this.capacity = capacity;
   }

   public BuddylistEntry get(int accId) {
      return (BuddylistEntry)this.buddies.get(accId);
   }

   public BuddylistEntry get(String characterName) {
      String lowerCaseName = characterName.toLowerCase();
      Iterator var3 = this.buddies.values().iterator();

      BuddylistEntry ble;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         ble = (BuddylistEntry)var3.next();
      } while(!ble.getName().toLowerCase().equals(lowerCaseName));

      return ble;
   }

   public void put(BuddylistEntry entry) {
      this.buddies.put(entry.getAccountId(), entry);
   }

   public void remove(int accId) {
      this.buddies.remove(accId);
      this.changed = true;
   }

   public Collection<BuddylistEntry> getBuddies() {
      return this.buddies.values();
   }

   public boolean isFull() {
      return this.buddies.size() >= this.capacity;
   }

   public int[] getBuddyIds() {
      int[] buddyIds = new int[this.buddies.size()];
      int i = 0;

      BuddylistEntry ble;
      for(Iterator var3 = this.buddies.values().iterator(); var3.hasNext(); buddyIds[i++] = ble.getAccountId()) {
         ble = (BuddylistEntry)var3.next();
      }

      return buddyIds;
   }

   public void loadFromTransfer(Map<CharacterNameAndId, Boolean> data) {
      Iterator var2 = data.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<CharacterNameAndId, Boolean> qs = (Entry)var2.next();
         CharacterNameAndId buddyid = (CharacterNameAndId)qs.getKey();
         boolean pair = (Boolean)qs.getValue();
         if (!pair) {
            this.getPendingRequests().push(buddyid);
         } else {
            this.put(new BuddylistEntry(buddyid.getName(), buddyid.getRepName(), buddyid.getAccId(), buddyid.getId(), buddyid.getGroupName(), -1, true, buddyid.getLevel(), buddyid.getJob(), buddyid.getMemo()));
         }
      }

   }

   public void loadFromDb(int accId) throws SQLException {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT b.buddyaccid, b.pending, c.name as buddyname, c.id as buddyid, c.job as buddyjob, c.level as buddylevel, b.repname, b.groupname, b.memo FROM buddies as b, characters as c WHERE c.accountid = b.buddyaccid AND b.accid = ?");
         ps.setInt(1, accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            int buddyid = rs.getInt("buddyaccid");
            String buddyname = rs.getString("buddyname");
            if (rs.getInt("pending") == 1) {
               this.getPendingRequests().push(new CharacterNameAndId(rs.getInt("buddyid"), buddyid, buddyname, rs.getString("repname"), rs.getInt("buddylevel"), rs.getInt("buddyjob"), rs.getString("groupname"), rs.getString("memo")));
            } else {
               this.put(new BuddylistEntry(buddyname, rs.getString("repname"), buddyid, rs.getInt("buddyid"), rs.getString("groupname"), -1, true, rs.getInt("buddylevel"), rs.getInt("buddyjob"), rs.getString("memo")));
            }
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("DELETE FROM buddies WHERE pending = 1 AND accid = ?");
         ps.setInt(1, accId);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (Exception var10) {
      } finally {
         if (con != null) {
            con.close();
         }

         if (ps != null) {
            ps.close();
         }

         if (rs != null) {
            rs.close();
         }

      }

   }

   public void addBuddyRequest(MapleClient c, int accid, int cidFrom, String nameFrom, String repName, int channelFrom, int levelFrom, int jobFrom, String groupName, String memo) {
      this.put(new BuddylistEntry(nameFrom, repName, accid, cidFrom, groupName, channelFrom, false, levelFrom, jobFrom, memo));
      if (this.getPendingRequests().isEmpty()) {
         c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.requestBuddylistAdd(cidFrom, accid, nameFrom, levelFrom, jobFrom, c, groupName, memo));
      } else {
         this.getPendingRequests().push(new CharacterNameAndId(cidFrom, accid, nameFrom, repName, levelFrom, jobFrom, groupName, memo));
      }

   }

   public void setChanged(boolean v) {
      this.changed = v;
   }

   public boolean changed() {
      return this.changed;
   }

   public CharacterNameAndId pollPendingRequest() {
      return (CharacterNameAndId)this.getPendingRequests().pollLast();
   }

   public Deque<CharacterNameAndId> getPendingRequests() {
      return this.pendingRequests;
   }

   public void setPendingRequests(Deque<CharacterNameAndId> pendingRequests) {
      this.pendingRequests = pendingRequests;
   }

   public static enum BuddyAddResult {
      BUDDYLIST_FULL,
      ALREADY_ON_LIST,
      OK;
   }

   public static enum BuddyOperation {
      ADDED,
      DELETED;
   }
}
