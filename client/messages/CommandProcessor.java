package client.messages;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.commands.AdminCommand;
import client.messages.commands.CommandExecute;
import client.messages.commands.CommandObject;
import client.messages.commands.DonatorCommand;
import client.messages.commands.GMCommand;
import client.messages.commands.InternCommand;
import client.messages.commands.PlayerCommand;
import client.messages.commands.SLFCGGameCommand;
import client.messages.commands.SuperDonatorCommand;
import client.messages.commands.SuperGMCommand;
import constants.ServerConstants;
import database.DatabaseConnection;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import tools.FileoutputUtil;

public class CommandProcessor {
   private static final HashMap<String, CommandObject> commands = new HashMap();
   private static final HashMap<Integer, ArrayList<String>> commandList = new HashMap();

   private static void sendDisplayMessage(MapleClient c, String msg, ServerConstants.CommandType type) {
      if (c.getPlayer() != null) {
         switch(type) {
         case NORMAL:
            c.getPlayer().dropMessage(6, msg);
            break;
         case TRADE:
            c.getPlayer().dropMessage(-2, "오류 : " + msg);
         }

      }
   }

   public static void dropHelp(MapleClient c) {
      c.getPlayer().dropMessage(5, "명령어 리스트 : ");

      for(int i = 0; i <= c.getPlayer().getGMLevel(); ++i) {
         if (commandList.containsKey(i)) {
            Iterator var2 = ((ArrayList)commandList.get(i)).iterator();

            while(var2.hasNext()) {
               String s = (String)var2.next();
               c.getPlayer().dropMessage(6, s);
            }
         }
      }

   }

   public static boolean processCommand(MapleClient c, String line, ServerConstants.CommandType type) {
      String[] splitted;
      CommandObject co;
      if (line.charAt(0) == ServerConstants.PlayerGMRank.NORMAL.getCommandPrefix() || c.getPlayer().getGMLevel() > ServerConstants.PlayerGMRank.NORMAL.getLevel() && line.charAt(0) == ServerConstants.PlayerGMRank.DONATOR.getCommandPrefix()) {
         splitted = line.split(" ");
         splitted[0] = splitted[0].toLowerCase();
         co = (CommandObject)commands.get(splitted[0]);
         if (co != null && co.getType() == type) {
            if (c.getPlayer().getMapId() == 121212121) {
               sendDisplayMessage(c, "현재 맵에서는 명령어 사용이 불가능합니다.", type);
               return true;
            } else {
               try {
                  co.execute(c, splitted);
               } catch (Exception var9) {
               }

               return true;
            }
         } else {
            sendDisplayMessage(c, "현재 입력한 플레이어 명령어가 존재하지 않습니다.", type);
            return true;
         }
      } else if (c.getPlayer().getGMLevel() > ServerConstants.PlayerGMRank.NORMAL.getLevel() && (line.charAt(0) == ServerConstants.PlayerGMRank.SUPERGM.getCommandPrefix() || line.charAt(0) == ServerConstants.PlayerGMRank.INTERN.getCommandPrefix() || line.charAt(0) == ServerConstants.PlayerGMRank.GM.getCommandPrefix() || line.charAt(0) == ServerConstants.PlayerGMRank.ADMIN.getCommandPrefix())) {
         splitted = line.split(" ");
         splitted[0] = splitted[0].toLowerCase();
         co = (CommandObject)commands.get(splitted[0]);
         if (co == null) {
            if (splitted[0].equals(line.charAt(0) + "help")) {
               dropHelp(c);
               return true;
            } else {
               sendDisplayMessage(c, "현재 입력한 관리자 명령어가 존재하지 않습니다.", type);
               return true;
            }
         } else {
            if (c.getPlayer().getGMLevel() >= co.getReqGMLevel()) {
               int ret = 0;

               try {
                  ret = co.execute(c, splitted);
               } catch (ArrayIndexOutOfBoundsException var7) {
                  sendDisplayMessage(c, "The command was not used properly: " + var7, type);
               } catch (Exception var8) {
                  FileoutputUtil.outputFileError("Log_Command_Except.rtf", var8);
               }

               if (ret > 0 && c.getPlayer() != null) {
                  if (c.getPlayer().isGM()) {
                     logCommandToDB(c.getPlayer(), line, "gmlog");
                  } else {
                     logCommandToDB(c.getPlayer(), line, "internlog");
                  }
               }
            } else {
               sendDisplayMessage(c, "해당 명령어를 사용하는데 권한레벨이 충분하지 않습니다.", type);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static void logCommandToDB(MapleCharacter player, String command, String table) {
      PreparedStatement ps = null;
      Connection con = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO " + table + " (cid, command, mapid) VALUES (?, ?, ?)");
         ps.setInt(1, player.getId());
         ps.setString(2, command);
         ps.setInt(3, player.getMap().getId());
         ps.executeUpdate();
      } catch (SQLException var14) {
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var14);
         var14.printStackTrace();
      } finally {
         try {
            ps.close();
            con.close();
         } catch (SQLException var13) {
         }

      }

   }

   static {
      Class[] array = new Class[]{PlayerCommand.class, InternCommand.class, GMCommand.class, AdminCommand.class, DonatorCommand.class, SuperDonatorCommand.class, SuperGMCommand.class, SLFCGGameCommand.class};
      Class[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Class clasz = var2[var4];

         try {
            ServerConstants.PlayerGMRank rankNeeded = (ServerConstants.PlayerGMRank)clasz.getMethod("getPlayerLevelRequired").invoke((Object)null, (Object[])null);
            Class<?>[] a = clasz.getDeclaredClasses();
            ArrayList<String> cL = new ArrayList();
            Class[] var9 = a;
            int var10 = a.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               Class c = var9[var11];

               try {
                  if (!Modifier.isAbstract(c.getModifiers()) && !c.isSynthetic()) {
                     Object o = c.newInstance();

                     boolean enabled;
                     try {
                        enabled = c.getDeclaredField("enabled").getBoolean(c.getDeclaredField("enabled"));
                     } catch (NoSuchFieldException var16) {
                        enabled = true;
                     }

                     if (o instanceof CommandExecute && enabled) {
                        char var10001 = rankNeeded.getCommandPrefix();
                        cL.add(var10001 + c.getSimpleName().toLowerCase());
                        commands.put(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase(), new CommandObject((CommandExecute)o, rankNeeded.getLevel()));
                        if (rankNeeded.getCommandPrefix() != ServerConstants.PlayerGMRank.GM.getCommandPrefix() && rankNeeded.getCommandPrefix() != ServerConstants.PlayerGMRank.NORMAL.getCommandPrefix()) {
                           commands.put("!" + c.getSimpleName().toLowerCase(), new CommandObject((CommandExecute)o, ServerConstants.PlayerGMRank.GM.getLevel()));
                        }
                     }
                  }
               } catch (Exception var17) {
                  var17.printStackTrace();
                  FileoutputUtil.outputFileError("Log_Script_Except.rtf", var17);
               }
            }

            Collections.sort(cL);
            commandList.put(rankNeeded.getLevel(), cL);
         } catch (Exception var18) {
            var18.printStackTrace();
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var18);
         }
      }

   }
}
