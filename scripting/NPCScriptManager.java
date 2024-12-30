package scripting;

import client.MapleClient;
import java.io.PrintStream;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import server.life.MapleLifeFactory;
import tools.FileoutputUtil;
import tools.packet.CWvsContext;

public class NPCScriptManager extends AbstractScriptManager {
   private final Map<MapleClient, NPCConversationManager> cms = new WeakHashMap();
   private static final NPCScriptManager instance = new NPCScriptManager();

   public static final NPCScriptManager getInstance() {
      return instance;
   }

   public final void start(MapleClient c, int npc) {
      this.start(c, npc, (String)null);
   }

   public final void start(MapleClient c, String script) {
      this.start(c, 0, script);
   }

   public final boolean UseScript(MapleClient c, int quest) {
      Invocable iv = null;
      iv = this.getInvocable("quest/" + quest + ".js", c, true);
      return iv != null;
   }

   public final void startHairRoom(MapleClient c, int npc, String script, byte result, int slot, byte temp) {
      try {
         try {
            if (!this.cms.containsKey(c) && c.canClickNPC()) {
               Invocable iv;
               if (script == null) {
                  iv = this.getInvocable("npc/" + npc + ".js", c, true);
               } else {
                  iv = this.getInvocable("npc/" + script + ".js", c);
               }

               if (iv == null) {
                  iv = this.getInvocable("npc/notcoded.js", c, true);
                  if (iv == null) {
                     this.dispose(c);
                     return;
                  }
               }

               ScriptEngine scriptengine = (ScriptEngine)iv;
               NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte)-1, iv, script);
               this.cms.put(c, cm);
               scriptengine.put("cm", cm);
               c.getPlayer().setConversation(1);
               c.setClickedNPC();

               try {
                  iv.invokeFunction("start", result, slot, temp);
               } catch (NoSuchMethodException var15) {
               }
            }
         } catch (Exception var16) {
            System.err.println("Error executing NPC script, NPC ID : " + npc + "." + var16);
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing NPC script, NPC ID : " + npc + " / NPC SCRIPT : " + script + " / " + var16);
            this.dispose(c);
         }

      } finally {
         ;
      }
   }

   public final void start(MapleClient c, int npc, String script, String method) {
      try {
         try {
            if (!this.cms.containsKey(c) && c.canClickNPC()) {
               Invocable iv;
               if (script == null) {
                  iv = this.getInvocable("npc/" + npc + ".js", c, true);
               } else {
                  iv = this.getInvocable("npc/" + script + ".js", c);
               }

               if (iv == null) {
                  iv = this.getInvocable("npc/notcoded.js", c, true);
                  if (iv == null) {
                     this.dispose(c);
                     return;
                  }
               }

               ScriptEngine scriptengine = (ScriptEngine)iv;
               NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte)-1, iv, script);
               this.cms.put(c, cm);
               scriptengine.put("cm", cm);
               c.getPlayer().setConversation(1);
               c.setClickedNPC();

               try {
                  iv.invokeFunction(method);
               } catch (NoSuchMethodException var13) {
                  iv.invokeFunction("action", 1, 0, 0);
               }
            }
         } catch (Exception var14) {
            System.err.println("Error executing NPC script, NPC ID : " + npc + "." + var14);
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing NPC script, NPC ID : " + npc + " / NPC SCRIPT : " + script + " / " + var14);
            this.dispose(c);
         }

      } finally {
         ;
      }
   }

   public final void start(MapleClient c, int npc, String script) {
      try {
         try {
            if (!this.cms.containsKey(c) && c.canClickNPC()) {
               String var10000;
               int var10001;
               Invocable iv;
               if (script == null) {
                  iv = this.getInvocable("npc/" + npc + ".js", c, true);
                  var10000 = FileoutputUtil.엔피시대화로그;
                  var10001 = c.getAccID();
                  FileoutputUtil.log(var10000, "[엔피시오픈] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 " + MapleLifeFactory.getNPC(npc).getName() + "(" + npc + ")를 오픈");
                  c.getPlayer().dropMessageGM(6, "OpenNPC(" + npc + ")");
               } else {
                  iv = this.getInvocable("npc/" + script + ".js", c, true);
                  var10000 = FileoutputUtil.엔피시대화로그;
                  var10001 = c.getAccID();
                  FileoutputUtil.log(var10000, "[엔피시오픈] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 " + script + "를 오픈");
                  c.getPlayer().dropMessageGM(6, "OpenNPC(" + script + ")");
               }

               if (iv == null) {
                  iv = this.getInvocable("npc/notcoded.js", c, true);
                  if (iv == null) {
                     this.dispose(c);
                     return;
                  }
               }

               ScriptEngine scriptengine = (ScriptEngine)iv;
               NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte)-1, iv, script);
               this.cms.put(c, cm);
               scriptengine.put("cm", cm);
               c.getPlayer().setConversation(1);
               c.setClickedNPC();

               try {
                  iv.invokeFunction("start");
               } catch (NoSuchMethodException var12) {
                  iv.invokeFunction("action", 1, 0, 0);
               }
            }
         } catch (Exception var13) {
            System.err.println("Error executing NPC script, NPC ID : " + npc + "." + var13);
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing NPC script, NPC ID : " + npc + " / NPC SCRIPT : " + script + " / " + var13);
            this.dispose(c);
         }

      } finally {
         ;
      }
   }

   public final void startItem(MapleClient c, int npc, String script) {
      try {
         if (!this.cms.containsKey(c) && c.canClickNPC()) {
            Invocable iv = this.getInvocable("item/" + script + ".js", c);
            if (iv == null) {
               iv = this.getInvocable("item/notcoded.js", c);
               if (iv == null) {
                  this.dispose(c);
                  return;
               }
            }

            ScriptEngine scriptengine = (ScriptEngine)iv;
            NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte)-2, iv, script);
            this.cms.put(c, cm);
            scriptengine.put("cm", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();

            try {
               iv.invokeFunction("start");
            } catch (NoSuchMethodException var8) {
               iv.invokeFunction("action", 1, 0, 0);
            }
         }
      } catch (Exception var9) {
         System.err.println("Error executing Item NPC script, NPC ID : " + npc + "." + var9);
         FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Item NPC script, NPC ID : " + npc + "." + var9);
         this.dispose(c);
      }

   }

   public final void action(MapleClient c, byte mode, byte type, int selection) {
      if (mode != -1) {
         NPCConversationManager cm = (NPCConversationManager)this.cms.get(c);
         if (cm == null || cm.getLastMsg() > -1) {
            return;
         }

         try {
            if (cm.pendingDisposal) {
               this.dispose(c);
            } else {
               c.setClickedNPC();
               cm.getIv().invokeFunction("action", mode, type, selection);
            }
         } catch (Exception var7) {
            PrintStream var10000 = System.err;
            int var10001 = cm.getNpc();
            var10000.println("Error executing NPC script. NPC ID : " + var10001 + " / NPC SCRIPT : " + cm.getScript() + " : " + var7);
            this.dispose(c);
            var10001 = cm.getNpc();
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing NPC script, NPC ID : " + var10001 + "/" + cm.getScript() + " : " + var7);
         }
      }

   }

   public final void zeroaction(MapleClient c, byte mode, byte type, int selection1, int selection2) {
      if (mode != -1) {
         NPCConversationManager cm = (NPCConversationManager)this.cms.get(c);
         if (cm == null || cm.getLastMsg() > -1) {
            return;
         }

         try {
            if (cm.pendingDisposal) {
               this.dispose(c);
            } else {
               c.setClickedNPC();
               cm.getIv().invokeFunction("zeroaction", mode, type, selection1, selection2);
            }
         } catch (Exception var8) {
            PrintStream var10000 = System.err;
            int var10001 = cm.getNpc();
            var10000.println("Error executing NPC script. NPC ID : " + var10001 + ":" + var8);
            this.dispose(c);
            var10001 = cm.getNpc();
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing NPC script, NPC ID : " + var10001 + "." + var8);
         }
      }

   }

   public final void startQuest(MapleClient c, int npc, int quest) {
      try {
         if (quest == 100796 || quest == 100199) {
            c.removeClickedNPC();
            getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }

         if (!this.cms.containsKey(c)) {
            Invocable iv = this.getInvocable("quest/" + quest + ".js", c, true);
            if (iv == null) {
               this.dispose(c);
               return;
            }

            ScriptEngine scriptengine = (ScriptEngine)iv;
            NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte)0, iv, (String)null);
            this.cms.put(c, cm);
            scriptengine.put("qm", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            String var10000 = FileoutputUtil.엔피시대화로그;
            int var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[퀘스트오픈] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 퀘스트 : " + quest + "를 오픈");
            System.out.println("NPCID started: " + npc + " startquest " + quest);
            iv.invokeFunction("start", 1, 0, 0);
         }
      } catch (Exception var7) {
         System.err.println("Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + var7);
         FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + var7);
         this.dispose(c);
      }

   }

   public final void startQuest(MapleClient c, byte mode, byte type, int selection) {
      NPCConversationManager cm = (NPCConversationManager)this.cms.get(c);
      if (cm != null && cm.getLastMsg() <= -1) {
         try {
            if (cm.pendingDisposal) {
               this.dispose(c);
            } else {
               c.setClickedNPC();
               cm.getIv().invokeFunction("start", mode, type, selection);
            }
         } catch (Exception var7) {
            PrintStream var10000 = System.err;
            int var10001 = cm.getQuest();
            var10000.println("Error executing Quest script. (" + var10001 + ")...NPC: " + cm.getNpc() + ":" + var7);
            var10001 = cm.getQuest();
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Quest script. (" + var10001 + ")..NPCID: " + cm.getNpc() + ":" + var7);
            this.dispose(c);
         }

      }
   }

   public final void endQuest(MapleClient c, int npc, int quest, boolean customEnd) {
      try {
         if (!this.cms.containsKey(c) && c.canClickNPC()) {
            Invocable iv = this.getInvocable("quest/" + quest + ".js", c, true);
            if (iv == null) {
               this.dispose(c);
               return;
            }

            ScriptEngine scriptengine = (ScriptEngine)iv;
            NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte)1, iv, (String)null);
            this.cms.put(c, cm);
            scriptengine.put("qm", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            System.out.println("NPCID started: " + npc + " endquest " + quest);
            iv.invokeFunction("end", 1, 0, 0);
         }
      } catch (Exception var8) {
         System.err.println("Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + var8);
         FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Quest script. (" + quest + ")..NPCID: " + npc + ":" + var8);
         this.dispose(c);
      }

   }

   public final void endQuest(MapleClient c, byte mode, byte type, int selection) {
      NPCConversationManager cm = (NPCConversationManager)this.cms.get(c);
      if (cm != null && cm.getLastMsg() <= -1) {
         try {
            if (cm.pendingDisposal) {
               this.dispose(c);
            } else {
               c.setClickedNPC();
               cm.getIv().invokeFunction("end", mode, type, selection);
            }
         } catch (Exception var7) {
            PrintStream var10000 = System.err;
            int var10001 = cm.getQuest();
            var10000.println("Error executing Quest script. (" + var10001 + ")...NPC: " + cm.getNpc() + ":" + var7);
            var10001 = cm.getQuest();
            FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Quest script. (" + var10001 + ")..NPCID: " + cm.getNpc() + ":" + var7);
            this.dispose(c);
         }

      }
   }

   public final void dispose(MapleClient c) {
      NPCConversationManager npccm = (NPCConversationManager)this.cms.get(c);
      if (npccm != null) {
         this.cms.remove(c);
         if (npccm.getType() == -1) {
            c.removeScriptEngine("scripts/npc/notcoded.js");
            if (npccm.getScript() != null) {
               c.removeScriptEngine("scripts/npc/" + npccm.getScript() + ".js");
            } else {
               c.removeScriptEngine("scripts/npc/" + npccm.getNpc() + ".js");
            }
         } else if (npccm.getType() == -2) {
            c.removeScriptEngine("scripts/item/" + npccm.getScript() + ".js");
         } else {
            c.removeScriptEngine("scripts/quest/" + npccm.getQuest() + ".js");
         }
      }

      if (c.getPlayer() != null && c.getPlayer().getConversation() == 1) {
         c.getPlayer().setConversation(0);
      }

   }

   public final NPCConversationManager getCM(MapleClient c) {
      return (NPCConversationManager)this.cms.get(c);
   }

   public void scriptClear() {
      this.cms.clear();
   }
}
