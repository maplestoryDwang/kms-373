package scripting;

import client.MapleClient;
import handling.channel.ChannelServer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import tools.FileoutputUtil;

public class EventScriptManager extends AbstractScriptManager {
   private final Map<String, EventScriptManager.EventEntry> events = new LinkedHashMap();
   private static final AtomicInteger runningInstanceMapId = new AtomicInteger(0);

   public static final int getNewInstanceMapId() {
      return runningInstanceMapId.addAndGet(1);
   }

   public EventScriptManager(ChannelServer cserv, String[] scripts) {
      String[] var3 = scripts;
      int var4 = scripts.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String script = var3[var5];
         if (!script.equals("")) {
            Invocable iv = this.getInvocable("event/" + script + ".js", (MapleClient)null);
            if (iv != null) {
               this.getEvents().put(script, new EventScriptManager.EventEntry(script, iv, new EventManager(cserv, iv, script)));
            }
         }
      }

   }

   public final EventManager getEventManager(String event) {
      EventScriptManager.EventEntry entry = (EventScriptManager.EventEntry)this.getEvents().get(event);
      return entry == null ? null : entry.em;
   }

   public final void init() {
      Iterator var1 = this.getEvents().values().iterator();

      while(var1.hasNext()) {
         EventScriptManager.EventEntry entry = (EventScriptManager.EventEntry)var1.next();

         try {
            ((ScriptEngine)entry.iv).put("em", entry.em);
            entry.iv.invokeFunction("init", null);
         } catch (Exception var4) {
            System.out.println("Error initiating event: " + entry.script + ":" + var4);
            FileoutputUtil.log("Log_Script_Except.rtf", "Error initiating event: " + entry.script + ":" + var4);
         }
      }

   }

   public final void cancel() {
      Iterator var1 = this.getEvents().values().iterator();

      while(var1.hasNext()) {
         EventScriptManager.EventEntry entry = (EventScriptManager.EventEntry)var1.next();
         entry.em.cancel();
      }

   }

   public Map<String, EventScriptManager.EventEntry> getEvents() {
      return this.events;
   }

   private static class EventEntry {
      public String script;
      public Invocable iv;
      public EventManager em;

      public EventEntry(String script, Invocable iv, EventManager em) {
         this.script = script;
         this.iv = iv;
         this.em = em;
      }
   }
}
