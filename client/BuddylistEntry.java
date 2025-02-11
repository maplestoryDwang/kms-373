package client;

public class BuddylistEntry {
   private String name = "";
   private String repName = "";
   private String group = "";
   private String memo = "";
   private int accountId;
   private int cid;
   private int channel = -1;
   private int level;
   private int job;
   private boolean visible;

   public BuddylistEntry(String name, String repName, int accountId, int characterId, String group, int channel, boolean visible, int level, int job, String memo) {
      this.setName(name);
      this.repName = repName;
      this.setAccountId(accountId);
      this.setCharacterId(characterId);
      this.group = group;
      this.channel = channel;
      this.visible = visible;
      this.setLevel(level);
      this.setJob(job);
      this.memo = memo;
   }

   public int getChannel() {
      return this.channel;
   }

   public void setChannel(int channel) {
      this.channel = channel;
   }

   public boolean isOnline() {
      return this.channel >= 0;
   }

   public void setOffline() {
      this.channel = -1;
   }

   public String getName() {
      return this.name;
   }

   public int getCharacterId() {
      return this.cid;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public String getGroupName() {
      return this.group;
   }

   public void setGroupName(String groupName) {
      this.group = groupName;
   }

   public String getMemo() {
      if (this.memo == null) {
         this.memo = "";
      }

      return this.memo;
   }

   public void setMemo(String m) {
      this.memo = m;
   }

   public int getLevel() {
      return this.level;
   }

   public int getJob() {
      return this.job;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.getCharacterId();
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         BuddylistEntry other = (BuddylistEntry)obj;
         return this.getCharacterId() == other.getCharacterId();
      }
   }

   public int getAccountId() {
      return this.accountId;
   }

   public void setAccountId(int accountId) {
      this.accountId = accountId;
   }

   public String getRepName() {
      return this.repName;
   }

   public void setRepName(String repName) {
      this.repName = repName;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setJob(int job) {
      this.job = job;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public void setCharacterId(int cid) {
      this.cid = cid;
   }
}
