package scripting;

import client.MapleCharacter;
import client.MapleClient;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import server.MaplePortal;
import tools.FileoutputUtil;

public class PortalScriptManager {
   private static final PortalScriptManager instance = new PortalScriptManager();
   private final Map<String, PortalScript> scripts = new HashMap();
   private static final ScriptEngineFactory sef = (new ScriptEngineManager()).getEngineByName("javascript").getFactory();

   public static final PortalScriptManager getInstance() {
      return instance;
   }

   private final PortalScript getPortalScript(String scriptName) {
      if (this.scripts.containsKey(scriptName)) {
         return (PortalScript)this.scripts.get(scriptName);
      } else {
         File scriptFile = new File("scripts/portal/" + scriptName + ".js");
         if (!scriptFile.exists()) {
            return null;
         } else {
            FileReader fr = null;
            ScriptEngine portal = sef.getScriptEngine();

            try {
               fr = new FileReader(scriptFile);
               CompiledScript compiled = ((Compilable)portal).compile(fr);
               compiled.eval();
            } catch (Exception var14) {
               System.err.println("Error executing Portalscript: " + scriptName + ":" + var14);
               FileoutputUtil.log("Log_Script_Except.rtf", "Error executing Portal script. (" + scriptName + ") " + var14);
            } finally {
               if (fr != null) {
                  try {
                     fr.close();
                  } catch (IOException var13) {
                     System.err.println("ERROR CLOSING" + var13);
                  }
               }

            }

            PortalScript script = (PortalScript)((Invocable)portal).getInterface(PortalScript.class);
            this.scripts.put(scriptName, script);
            return script;
         }
      }
   }

   public final void executePortalScript(MaplePortal portal, MapleClient c) {
      PortalScript script = this.getPortalScript(portal.getScriptName());
      if (script != null) {
         try {
            script.enter(new PortalPlayerInteraction(c, portal));
         } catch (Exception var5) {
            PrintStream var10000 = System.err;
            String var10001 = portal.getScriptName();
            var10000.println("Error entering Portalscript: " + var10001 + " : " + var5);
         }
      } else {
         MapleCharacter var6 = c.getPlayer();
         String var10002 = portal.getScriptName();
         var6.dropMessageGM(5, "script : " + var10002 + " / mapid : " + c.getPlayer().getMapId());
      }

   }

   public final void clearScripts() {
      this.scripts.clear();
   }
}
