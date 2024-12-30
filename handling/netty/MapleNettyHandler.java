package handling.netty;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory;
import constants.GameConstants;
import constants.ServerConstants;
import constants.ServerType;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import handling.auction.handler.AuctionHandler;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.handler.AllianceHandler;
import handling.channel.handler.BuddyListHandler;
import handling.channel.handler.ChatHandler;
import handling.channel.handler.DueyHandler;
import handling.channel.handler.FishingHandler;
import handling.channel.handler.GuildHandler;
import handling.channel.handler.HyperHandler;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.InventoryHandler;
import handling.channel.handler.ItemMakerHandler;
import handling.channel.handler.MarriageHandler;
import handling.channel.handler.MatrixHandler;
import handling.channel.handler.MobHandler;
import handling.channel.handler.NPCHandler;
import handling.channel.handler.OrbHandler;
import handling.channel.handler.PartyHandler;
import handling.channel.handler.PetHandler;
import handling.channel.handler.PlayerHandler;
import handling.channel.handler.PlayerInteractionHandler;
import handling.channel.handler.PlayersHandler;
import handling.channel.handler.StatsHandling;
import handling.channel.handler.SummonHandler;
import handling.channel.handler.UnionHandler;
import handling.channel.handler.UserInterfaceHandler;
import handling.farm.handler.FarmHandler;
import handling.login.LoginServer;
import handling.login.handler.CharLoginHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.Randomizer;
import server.enchant.EquipmentEnchant;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.quest.party.MapleNettPyramid;
import tools.FileoutputUtil;
import tools.MapleAESOFB;
import tools.Pair;
import tools.StringUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;

public class MapleNettyHandler extends SimpleChannelInboundHandler<LittleEndianAccessor> {
   private final ServerType serverType;
   private final int channel;
   private final List<String> BlockedIP = new ArrayList();
   private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap();

   public MapleNettyHandler(ServerType serverType, int channel) {
      this.serverType = serverType;
      this.channel = channel;
   }

   public void channelActive(ChannelHandlerContext ctx) {
      String address = ctx.channel().remoteAddress().toString().split(":")[0];
      if (this.BlockedIP.contains(address)) {
         ctx.close();
      } else {
         Pair<Long, Byte> track = (Pair)this.tracker.get(address);
         byte count;
         if (track == null) {
            count = 1;
         } else {
            count = (Byte)track.right;
            long difference = System.currentTimeMillis() - (Long)track.left;
            if (difference < 2000L) {
               ++count;
            } else if (difference > 20000L) {
               count = 1;
            }

            if (count >= 10) {
               this.BlockedIP.add(address);
               this.tracker.remove(address);
               ctx.close();
               return;
            }
         }

         this.tracker.put(address, new Pair(System.currentTimeMillis(), count));
         boolean check = true;
         if (address.contains("219.250.30.201") || address.contains("1.248.193.246") || address.contains("36.39.235.217") || address.contains("103.226.79.71")) {
            check = false;
         }

         byte[] serverRecv = new byte[]{-70, 104, 84, 99};
         byte[] serverSend = new byte[]{-56, 70, -42, 118};
         MapleClient client = new MapleClient(ctx.channel(), new MapleAESOFB(serverSend, (short)-1150, this.serverType == ServerType.CHANNEL || this.serverType == ServerType.CASHSHOP || this.serverType == ServerType.AUCTION, true), new MapleAESOFB(serverRecv, (short)1149, this.serverType == ServerType.CHANNEL || this.serverType == ServerType.CASHSHOP || this.serverType == ServerType.AUCTION));
         client.setChannel(this.channel);
         ctx.writeAndFlush(LoginPacket.initializeConnection((short)1149, serverSend, serverRecv, !this.serverType.equals(ServerType.LOGIN)));
         if (this.serverType == ServerType.LOGIN) {
            LoginServer.Channels.put(address, ctx.channel());
         }

         ctx.channel().attr(MapleClient.CLIENTKEY).set(client);
      }
   }

   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      MapleClient client = (MapleClient)ctx.channel().attr(MapleClient.CLIENTKEY).get();
      if (this.serverType == ServerType.LOGIN) {
         LoginServer.Channels.remove(client.getSessionIPAddress());
      }

      if (client != null) {
         client.disconnect(true, false);
         System.out.println(client.getSessionIPAddress() + " disconnected.");
      }

      ctx.channel().attr(MapleClient.CLIENTKEY).set((Object)null);
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
   }

   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
         IdleStateEvent var3 = (IdleStateEvent)evt;
      }

   }

   protected void channelRead0(ChannelHandlerContext ctx, LittleEndianAccessor slea) throws Exception {
      MapleClient c = (MapleClient)ctx.channel().attr(MapleClient.CLIENTKEY).get();
      int header_num = slea.readShort();
      if (c.mEncryptedOpcode.containsKey(header_num)) {
         header_num = (Integer)c.mEncryptedOpcode.get(header_num);
      }

      boolean show = true;
      switch(header_num) {
      case 341:
      case 352:
      case 356:
      case 612:
         show = false;
      default:
         if (show && ServerConstants.DEBUG_RECEIVE && header_num != RecvPacketOpcode.HACKSHIELD.getValue() && header_num != RecvPacketOpcode.MOVE_LIFE.getValue() && header_num != RecvPacketOpcode.NPC_ACTION.getValue() && header_num != RecvPacketOpcode.MOVE_PLAYER.getValue() && header_num != RecvPacketOpcode.SESSION_CHECK.getValue() && header_num != RecvPacketOpcode.SHOW_ICBM.getValue() && header_num != RecvPacketOpcode.TAKE_DAMAGE.getValue() && header_num != RecvPacketOpcode.QUEST_ACTION.getValue() && header_num != RecvPacketOpcode.AUTO_AGGRO.getValue() && header_num != RecvPacketOpcode.HYPER_R.getValue()) {
            System.out.println("[" + RecvPacketOpcode.getOpcodeName(header_num) + "] " + header_num + " : " + slea.toString());
         }

         RecvPacketOpcode[] var6 = RecvPacketOpcode.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            RecvPacketOpcode recv = var6[var8];
            if (recv.getValue() == header_num) {
               try {
                  handlePacket(recv, slea, c, this.serverType);
               } catch (Exception var11) {
                  var11.printStackTrace();
               }

               return;
            }
         }

      }
   }

   public static final void handlePacket(RecvPacketOpcode header, LittleEndianAccessor slea, MapleClient c, ServerType serverType) throws Exception {
      int rand;
      int type2;
      Item item;
      short type;
      short badPacketSize;
      switch(header) {
      case PONG:
         c.pongReceived();
         break;
      case HACKSHIELD:
         if (slea.readByte() == 2) {
            CharLoginHandler.HackShield(slea, c);
         }
         break;
      case CLIENT_HELLO:
         slea.readByte();
         type = slea.readShort();
         badPacketSize = slea.readShort();
         if (type != 1149 && badPacketSize != 1) {
            System.err.println("ERROR : " + c.getSessionIPAddress());
         }
         break;
      case CHECK_HOTFIX:
         c.getSession().writeAndFlush(LoginPacket.getHotfix());
         break;
      case LOAD_WZ_DATA:
         CharLoginHandler.checkLoadWzData(slea, c);
         break;
      case SESSION_CHECK:
         CharLoginHandler.SessionCheck(slea, c);
         break;
      case RESET_SECOND_PW:
         CharLoginHandler.ResetSecondPW(slea, c);
         break;
      case OTP_SETTING:
         CharLoginHandler.OTPSetting(slea, c);
         break;
      case INPUT_OTP:
         CharLoginHandler.InputOTP(slea, c);
         break;
      case LOGIN_PASSWORD:
         CharLoginHandler.login(slea, c);
         break;
      case SELECT_CHANNEL_LIST:
         slea.skip(1);
         CharLoginHandler.SelectChannelList(c, slea.readInt());
         break;
      case LEAVING_WORLD:
         CharLoginHandler.ServerListRequest(c, true);
         break;
      case LOGIN_REQUEST:
         try {
            CharLoginHandler.getLoginRequest(slea, c);
         } catch (Exception var17) {
            var17.printStackTrace();
         }
         break;
      case CHARLIST_REQUEST:
         try {
            CharLoginHandler.CharlistRequest(slea, c);
         } catch (Exception var16) {
            var16.printStackTrace();
         }
         break;
      case CHECK_CHAR_NAME:
         CharLoginHandler.CheckCharName(slea.readMapleAsciiString(), c);
         break;
      case CHAR_NAME_CHANGE:
         CharLoginHandler.CheckCharNameChange(slea, c);
         break;
      case MARRIAGE_ITEM:
         MarriageHandler.UseItem(slea, c, c.getPlayer());
         break;
      case NAME_CHANGER:
      case NAME_CHANGER_SPW:
         PlayerHandler.NameChanger(header == RecvPacketOpcode.NAME_CHANGER_SPW, slea, c);
         break;
      case CREATE_CHAR:
         CharLoginHandler.CreateChar(slea, c);
         break;
      case CREATE_ULTIMATE:
         CharLoginHandler.CreateUltimate(slea, c);
         break;
      case DELETE_CHAR:
         CharLoginHandler.DeleteChar(slea, c);
         break;
      case CHAR_SELECT:
         CharLoginHandler.Character_WithoutSecondPassword(slea, c);
         break;
      case LOGIN_WITH_CREATE_CHAR:
         CharLoginHandler.LoginWithCreateCharacter(slea, c);
         break;
      case ONLY_REG_SECOND_PASSWORD:
         CharLoginHandler.onlyRegisterSecondPassword(slea, c);
         break;
      case AUTH_LOGIN_WITH_SPW:
         CharLoginHandler.checkSecondPassword(slea, c);
         break;
      case NEW_PASSWORD_CHECK:
         CharLoginHandler.NewPassWordCheck(c);
         break;
      case PACKET_ERROR:
         if (slea.available() >= 6L) {
            type = slea.readShort();
            slea.skip(4);
            if (type == 2) {
               c.getPlayer().saveToDB(true, true);
            }

            badPacketSize = slea.readShort();
            slea.skip(4);
            int pHeader = slea.readShort();
            String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
            pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
            String op = SendPacketOpcode.getOpcodeName(pHeader);
            String from = "";
            if (c.getPlayer() != null) {
               String var10000 = c.getPlayer().getName();
               from = "Chr: " + var10000 + " LVL(" + c.getPlayer().getLevel() + ") job: " + c.getPlayer().getJob() + " MapID: " + c.getPlayer().getMapId();
            }

            String Recv = from + "\r\nSendOP(-38): " + op + " [" + pHeaderStr + "] (" + (badPacketSize - 4) + ")\r\n" + slea.toString(false) + "\r\n\r\n";
            System.out.println(Recv);
            FileoutputUtil.log("ClientErrorPacket.txt", Recv);
         }
         break;
      case CHANGE_CHANNEL:
      case CHANGE_ROOM_CHANNEL:
         InterServerHandler.ChangeChannel(slea, c, c.getPlayer(), header == RecvPacketOpcode.CHANGE_ROOM_CHANNEL);
         break;
      case PLAYER_LOGGEDIN:
         slea.skip(4);
         type2 = slea.readInt();
         if (serverType.equals(ServerType.CASHSHOP)) {
            CashShopOperation.EnterCS(type2, c);
         } else {
            InterServerHandler.Loggedin(type2, c);
         }
         break;
      case ENTER_CASH_SHOP:
         boolean isNpc = ServerConstants.csNpc > 0;
         InterServerHandler.EnterCS(c, c.getPlayer(), isNpc);
         break;
      case ENTER_AUCTION:
         AuctionHandler.EnterAuction(c.getPlayer(), c);
         break;
      case ENTER_FARM:
         FarmHandler.enterFarm(c.getPlayer(), c);
         break;
      case MOVE_PLAYER:
         PlayerHandler.MovePlayer(slea, c, c.getPlayer());
         break;
      case CHAR_INFO_REQUEST:
         try {
            slea.readInt();
            PlayerHandler.CharInfoRequest(slea.readInt(), c, c.getPlayer());
         } catch (Exception var15) {
            var15.printStackTrace();
         }
         break;
      case CLOSE_RANGE_ATTACK:
      case SPOTLIGHT_ATTACK:
         PlayerHandler.closeRangeAttack(slea, c, c.getPlayer(), header == RecvPacketOpcode.SPOTLIGHT_ATTACK);
         break;
      case RANGED_ATTACK:
         PlayerHandler.rangedAttack(slea, c, c.getPlayer());
         break;
      case MAGIC_ATTACK:
         PlayerHandler.MagicDamage(slea, c, c.getPlayer(), false, false);
         break;
      case BUFF_ATTACK:
         PlayerHandler.BuffAttack(slea, c, c.getPlayer());
         break;
      case SPECIAL_MOVE:
         PlayerHandler.SpecialMove(slea, c, c.getPlayer());
         break;
      case POSION_LEGION:
         PlayerHandler.PoisonLegion(slea, c);
         break;
      case CHILLING_ATTACK:
         PlayerHandler.MagicDamage(slea, c, c.getPlayer(), true, false);
         break;
      case ORBITAL_ATTACK:
         PlayerHandler.MagicDamage(slea, c, c.getPlayer(), false, true);
         break;
      case INCREASE_DURATION:
         PlayerHandler.IncreaseDuration(c.getPlayer(), slea.readInt());
         break;
      case SHOW_SOULEFFECT_R:
         c.getPlayer().getMap().broadcastMessage(CField.showSoulEffect(c.getPlayer(), slea.readByte()));
         break;
      case SOUL_EFFECT_RECIVE:
         type2 = slea.readInt();
         rand = slea.readInt();
         c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.showSoulEffect(c.getPlayer(), (byte)type2, rand), false);
         break;
      case PSYCHIC_GRAB_PREPARATION:
         PlayerHandler.PsychicGrabPreparation(slea, c, false);
         break;
      case PSYCHIC_GRAB:
         PlayerHandler.PsychicGrabPreparation(slea, c, true);
         break;
      case ULTIMATE_MATERIAL:
         c.getSession().writeAndFlush(CWvsContext.PsychicUltimateDamager(slea.readInt(), c.getPlayer()));
         break;
      case DOJANG_HANDLER:
         PlayerHandler.DojangHandler(slea, c);
         break;
      case RELEASE_ROCK:
         CWvsContext.CancelPsychicGrep(slea, c);
         break;
      case PSYCHIC_ATTACK_R:
         PlayerHandler.CreateKinesisPsychicArea(slea, c);
         break;
      case PSYCHIC_DAMAGE_R:
         c.getSession().writeAndFlush(CWvsContext.PsychicDamage(slea, c));
         break;
      case CANCEL_PSYCHIC_GRAB_R:
         slea.skip(8);
         CWvsContext.CancelPsychicGrep(slea, c);
         break;
      case PSYCHIC_ULTIMATE_R:
         PlayerHandler.psychicUltimateRecv(slea, c);
         break;
      case KINESIS_GROUND_R:
         PlayerHandler.KinesisGround(c, slea);
         break;
      case DOT_ATTACK:
         PlayerHandler.closeRangeAttack(slea, c, c.getPlayer(), true);
         break;
      case TOUCH_MIST:
         PlayerHandler.touchMist(slea, c);
         break;
      case SPECIAL_STAT:
         ItemMakerHandler.getSpecialStat(slea, c);
         break;
      case CRAFT_DONE:
         ItemMakerHandler.CraftComplete(slea, c, c.getPlayer());
         break;
      case CRAFT_MAKE:
         ItemMakerHandler.CraftMake(slea, c, c.getPlayer());
         break;
      case CRAFT_EFFECT:
         ItemMakerHandler.CraftEffect(slea, c, c.getPlayer());
         break;
      case START_HARVEST:
         ItemMakerHandler.StartHarvest(slea, c, c.getPlayer());
         break;
      case STOP_HARVEST:
         ItemMakerHandler.StopHarvest(slea, c, c.getPlayer());
         break;
      case MAKE_EXTRACTOR:
         ItemMakerHandler.MakeExtractor(slea, c, c.getPlayer());
         break;
      case USE_BAG:
         ItemMakerHandler.UseBag(slea, c, c.getPlayer());
         break;
      case USE_RECIPE:
         ItemMakerHandler.UseRecipe(slea, c, c.getPlayer());
         break;
      case MOVE_ANDROID:
         PlayerHandler.MoveAndroid(slea, c, c.getPlayer());
         break;
      case MOVE_HAKU:
         PlayerHandler.MoveHaku(slea, c, c.getPlayer());
         break;
      case FACE_EXPRESSION:
         PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
         break;
      case FACE_ANDROID:
         PlayerHandler.ChangeAndroidEmotion(slea.readInt(), c.getPlayer());
         break;
      case TAKE_DAMAGE:
         PlayerHandler.TakeDamage(slea, c, c.getPlayer());
      case BLACK_MAGE_BALL_RECV:
      case ALLOW_PARTY_INVITE:
      case COODINATION_RESULT:
      case PlatformerEnter:
      case LOTUS_AIR_ATTACK:
      case BATTLEGROUND_SELECT_AVATER:
         break;
      case HEAL_OVER_TIME:
         PlayerHandler.Heal(slea, c.getPlayer());
         break;
      case CANCEL_BUFF:
         PlayerHandler.CancelBuffHandler(slea, c.getPlayer());
         break;
      case MECH_CANCEL:
         PlayerHandler.CancelMech(slea, c.getPlayer());
         break;
      case CANCEL_ITEM_EFFECT:
         if (slea.available() >= 4L) {
            PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
         }
         break;
      case USE_TITLE:
         PlayerHandler.UseTitle(slea, c, c.getPlayer());
         break;
      case USE_CHAIR:
         slea.readInt();
         PlayerHandler.UseChair(slea.readInt(), c, c.getPlayer(), slea);
         break;
      case CANCEL_CHAIR:
         PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
         break;
      case USE_ITEMEFFECT:
         PlayerHandler.UseItemEffect(slea.readInt(), c, c.getPlayer());
         break;
      case SKILL_EFFECT:
         PlayerHandler.SkillEffect(slea, c.getPlayer());
         break;
      case MESO_DROP:
         slea.readInt();
         PlayerHandler.DropMeso(slea.readInt(), c.getPlayer());
         break;
      case CHANGE_KEYMAP:
         PlayerHandler.ChangeKeymap(slea, c.getPlayer());
         break;
      case PET_BUFF:
         PetHandler.ChangePetBuff(slea, c.getPlayer());
         break;
      case CHANGE_MAP:
         if (serverType.equals(ServerType.CASHSHOP)) {
            CashShopOperation.LeaveCS(slea, c, c.getPlayer());
         } else {
            PlayerHandler.ChangeMap(slea, c, c.getPlayer());
         }
         break;
      case CHANGE_MAP_SPECIAL:
         slea.skip(1);
         PlayerHandler.ChangeMapSpecial(slea.readMapleAsciiString(), c, c.getPlayer());
         break;
      case USE_INNER_PORTAL:
         slea.skip(1);
         PlayerHandler.InnerPortal(slea, c, c.getPlayer());
         break;
      case TROCK_ADD_MAP:
         PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
         break;
      case AranCombo:
         type2 = slea.readInt();
         PlayerHandler.AranCombo(c, c.getPlayer(), type2);
         break;
      case LOSE_AranCombo:
         PlayerHandler.LossAranCombo(c, c.getPlayer(), 1);
         break;
      case BOSS_MATCHING:
         PlayerHandler.BossMatching(slea, c.getPlayer());
         break;
      case BOSS_WARP:
         PlayerHandler.BossWarp(slea, c.getPlayer());
         break;
      case SKILL_MACRO:
         PlayerHandler.ChangeSkillMacro(slea, c.getPlayer());
         break;
      case GIVE_FAME:
         PlayersHandler.GiveFame(slea, c, c.getPlayer());
         break;
      case NOTE_ACTION:
         PlayersHandler.Note(slea, c.getPlayer());
         break;
      case USE_DOOR:
         PlayersHandler.UseDoor(slea, c.getPlayer());
         break;
      case USE_RANDOM_DOOR:
         PlayersHandler.UseRandomDoor(slea, c.getPlayer());
         break;
      case USE_MECH_DOOR:
         PlayersHandler.UseMechDoor(slea, c.getPlayer());
         break;
      case ANDROID_EAR:
         PlayerHandler.AndroidEar(c, slea);
         break;
      case DAMAGE_REACTOR:
         PlayersHandler.HitReactor(slea, c);
         break;
      case CLICK_REACTOR:
      case TOUCH_REACTOR:
         PlayersHandler.TouchReactor(slea, c);
         break;
      case SPACE_REACTOR:
         PlayersHandler.SpaceReactor(slea, c);
         break;
      case CLOSE_CHALKBOARD:
         c.getPlayer().setChalkboard((String)null);
         break;
      case ITEM_SORT:
         InventoryHandler.ItemSort(slea, c);
         break;
      case ITEM_GATHER:
         InventoryHandler.ItemGather(slea, c);
         break;
      case ITEM_MOVE:
         InventoryHandler.ItemMove(slea, c);
         break;
      case MOVE_BAG:
         InventoryHandler.MoveBag(slea, c);
         break;
      case SWITCH_BAG:
         InventoryHandler.SwitchBag(slea, c);
         break;
      case ITEM_MAKER:
         ItemMakerHandler.ItemMaker(slea, c);
         break;
      case ITEM_PICKUP:
         InventoryHandler.Pickup_Player(slea, c, c.getPlayer());
         break;
      case USE_CASH_ITEM:
         InventoryHandler.UseCashItem(slea, c);
         break;
      case RUNE_TOUCH:
         PlayersHandler.TouchRune(slea, c.getPlayer());
         break;
      case RUNE_USE:
         PlayersHandler.UseRune(slea, c.getPlayer());
         break;
      case CONTENTS_GUIDE:
         PlayerHandler.GuideWarp(slea, c.getPlayer());
         break;
      case MANNEQUIN:
         PlayerHandler.useMannequin(slea, c.getPlayer());
         break;
      case HASTE_BOX:
         PlayerHandler.openHasteBox(slea, c.getPlayer());
         break;
      case USE_ADI_CUBE:
      case USE_CUBE:
         slea.skip(4);
         InventoryHandler.UseCube(slea, c);
         break;
      case USE_ITEM:
         InventoryHandler.UseItem(slea, c, c.getPlayer());
         break;
      case USE_MAGNIFY_GLASS:
         InventoryHandler.UseMagnify(slea, c);
         break;
      case USE_STAMP:
         InventoryHandler.UseStamp(slea, c);
         break;
      case USE_EDITIONAL_STAMP:
         InventoryHandler.UseEditionalStamp(slea, c);
         break;
      case USE_CHOOSE_CUBE:
         InventoryHandler.UseChooseCube(slea, c);
         break;
      case USE_CHOOSE_ABILITY:
         PlayerHandler.UseChooseAbility(slea, c);
         break;
      case USE_SCRIPTED_NPC_ITEM:
         slea.skip(4);
      case USE_CONSUME_ITEM:
         InventoryHandler.UseScriptedNPCItem(slea, c, c.getPlayer());
         break;
      case USE_RETURN_SCROLL:
         InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
         break;
      case WARP_GUILD_MAP:
         PlayerHandler.warpGuildMap(slea, c.getPlayer());
         break;
      case BLACK_MAGE_RECV:
         PlayerHandler.BlackMageRecv(slea, c);
         break;
      case USE_PET_LOOT:
         InventoryHandler.UsePetLoot(slea, c);
         break;
      case VICIOUS_HAMMER_RES:
         InventoryHandler.UseGoldenHammer(slea, c);
         break;
      case VICIOUS_HAMMER_RESULT:
         c.getSession().writeAndFlush(CSPacket.ViciousHammer(false, !c.getPlayer().vh));
         break;
      case USE_SILVER_KARMA:
         InventoryHandler.useSilverKarma(slea, c.getPlayer());
         break;
      case USE_FLAG_SCROLL:
      case USE_POTENTIAL_SCROLL:
      case USE_UPGRADE_SCROLL:
      case USE_EQUIP_SCROLL:
      case USE_REBIRTH_SCROLL:
      case USE_BLACK_REBIRTH_SCROLL:
         slea.readInt();
         type = slea.readShort();
         if (header == RecvPacketOpcode.USE_UPGRADE_SCROLL) {
            slea.readShort();
         }

         InventoryHandler.UseUpgradeScroll(header, type, slea.readShort(), slea.readByte(), c, c.getPlayer());
         break;
      case USE_EDITIONAL_SCROLL:
         InventoryHandler.UseEditionalScroll(slea, c);
         break;
      case USE_SUMMON_BAG:
         InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
         break;
      case USE_SKILL_BOOK:
         slea.readInt();
         InventoryHandler.UseSkillBook((short)((byte)slea.readShort()), slea.readInt(), c, c.getPlayer());
         break;
      case USE_CATCH_ITEM:
         InventoryHandler.UseCatchItem(slea, c, c.getPlayer());
         break;
      case USE_MOUNT_FOOD:
         InventoryHandler.UseMountFood(slea, c, c.getPlayer());
         break;
      case USE_SOUL_ENCHANTER:
         InventoryHandler.UseSoulEnchanter(slea, c, c.getPlayer());
         break;
      case USE_SOUL_SCROLL:
         InventoryHandler.UseSoulScroll(slea, c, c.getPlayer());
         break;
      case REWARD_ITEM:
         InventoryHandler.UseRewardItem((short)((byte)slea.readShort()), slea.readInt(), c, c.getPlayer());
         break;
      case HYPNOTIZE_DMG:
         MobHandler.HypnotizeDmg(slea, c.getPlayer());
         break;
      case SPIRIT_HIT:
         MobHandler.SpiritHit(slea, c.getPlayer());
         break;
      case ORGEL_HIT:
         MobHandler.OrgelHit(slea, c.getPlayer());
         break;
      case MOB_NODE:
         MobHandler.MobNode(slea, c.getPlayer());
         break;
      case BIND_LIFE:
         MobHandler.BindMonster(slea, c);
         break;
      case MEDAL_DISPLAY:
         PlayerHandler.UpdateMedalDisplay(slea, c.getPlayer());
         break;
      case TITLE_DISPLAY:
         PlayerHandler.UpdateTitleDisplay(slea, c.getPlayer());
         break;
      case MOVE_LIFE:
         MobHandler.MoveMonster(slea, c, c.getPlayer());
         break;
      case AUTO_AGGRO:
         MobHandler.AutoAggro(slea.readInt(), c.getPlayer());
         break;
      case FRIENDLY_DAMAGE:
         MobHandler.FriendlyDamage(slea, c.getPlayer());
         break;
      case DAMAGE_SKIN:
         PlayerHandler.UpdateDamageSkin(slea, c, c.getPlayer());
         break;
      case MIRROR_DUNGEON:
         PlayerHandler.MirrorDungeon(slea, c);
         break;
      case MONSTER_BOMB:
         MobHandler.MonsterBomb(slea.readInt(), c.getPlayer());
         break;
      case ENTER_DUNGEN:
         PlayerHandler.EnterDungen(slea, c);
         break;
      case MOB_BOMB:
         MobHandler.MobBomb(slea, c.getPlayer());
         break;
      case NPC_SHOP:
         NPCHandler.NPCShop(slea, c, c.getPlayer());
         break;
      case NPC_TALK:
         NPCHandler.NPCTalk(slea, c, c.getPlayer());
         break;
      case NPC_TALK_MORE:
         NPCHandler.NPCMoreTalk(slea, c);
         break;
      case NPC_ACTION:
         NPCHandler.NPCAnimation(slea, c);
         break;
      case QUEST_ACTION:
         NPCHandler.QuestAction(slea, c, c.getPlayer());
         break;
      case STORAGE:
         NPCHandler.Storage(slea, c, c.getPlayer());
         break;
      case GENERAL_CHAT_ITEM:
      case GENERAL_CHAT:
         try {
            if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
               slea.readInt();
               ChatHandler.GeneralChat(slea.readMapleAsciiString(), slea.readByte(), c, c.getPlayer(), slea, header);
               c.getPlayer().forMatrix();
            }
         } catch (Exception var14) {
            var14.printStackTrace();
         }
         break;
      case PARTYCHATITEM:
      case PARTYCHAT:
         ChatHandler.Others(slea, c, c.getPlayer(), header);
         break;
      case WHISPERITEM:
      case WHISPER:
         ChatHandler.Whisper_Find(slea, c, header);
         break;
      case MESSENGER:
         ChatHandler.Messenger(slea, c);
         break;
      case AUTO_ASSIGN_AP:
         StatsHandling.AutoAssignAP(slea, c, c.getPlayer());
         break;
      case DISTRIBUTE_AP:
         StatsHandling.DistributeAP(slea, c, c.getPlayer());
         break;
      case DISTRIBUTE_SP:
         slea.readInt();
         StatsHandling.DistributeSP(slea.readInt(), slea.readInt(), c, c.getPlayer());
         break;
      case ADD_HYPERSKILL:
         HyperHandler.HyperStatHandler(slea, c);
         break;
      case ADD_HYPERSTAT:
         slea.readInt();
         HyperHandler.HyperStatHandler(slea, c);
         break;
      case RESET_HYPERSKILL:
         HyperHandler.ResetHyperSkill(c);
         break;
      case RESET_HYPERSTAT:
         slea.readInt();
         HyperHandler.ResetHyperStatHandler(slea, c);
         break;
      case HYPERSTAT_PRESETS:
         HyperHandler.HyperStatPresets(slea, c);
         break;
      case PLAYER_INTERACTION:
         PlayerInteractionHandler.PlayerInteraction(slea, c, c.getPlayer());
         break;
      case GUILD_OPERATION:
         GuildHandler.Guild(slea, c);
         break;
      case DENY_GUILD_REQUEST:
         slea.skip(1);
         GuildHandler.DenyGuildRequest(slea.readMapleAsciiString(), c);
         break;
      case GUILD_REGISTER_REQUEST:
         GuildHandler.GuildJoinRequest(slea, c.getPlayer());
         break;
      case GUILD_REGISTER_CANCEL:
         GuildHandler.GuildCancelRequest(slea, c, c.getPlayer());
         break;
      case GUILD_REGISTER_ACCEPT:
         GuildHandler.GuildRegisterAccept(slea, c.getPlayer());
         break;
      case GUILD_REGISTER_DENY:
         GuildHandler.GuildJoinDeny(slea, c.getPlayer());
         break;
      case REQUEST_GUILD:
         GuildHandler.GuildRequest(slea.readInt(), c.getPlayer());
         break;
      case GUILD_RANKING_REQUEST:
         GuildHandler.guildRankingRequest(slea.readByte(), c);
         break;
      case CANCEL_GUILD_REQUEST:
         GuildHandler.cancelGuildRequest(c, c.getPlayer());
         break;
      case GUILD_OPTION:
         GuildHandler.SendGuild(slea, c);
         break;
      case ALLIANCE_OPERATION:
         AllianceHandler.HandleAlliance(slea, c, false);
         break;
      case DENY_ALLIANCE_REQUEST:
         AllianceHandler.HandleAlliance(slea, c, true);
         break;
      case PARTY_OPERATION:
         PartyHandler.PartyOperation(slea, c);
         break;
      case DENY_PARTY_REQUEST:
         PartyHandler.DenyPartyRequest(slea, c);
         break;
      case BUDDYLIST_MODIFY:
         BuddyListHandler.BuddyOperation(slea, c);
         break;
      case CYGNUS_SUMMON:
         UserInterfaceHandler.CygnusSummon_NPCRequest(c);
         break;
      case SHIP_OBJECT:
         UserInterfaceHandler.ShipObjectRequest(slea.readInt(), c);
         break;
      case BUY_CS_ITEM:
         CashShopOperation.BuyCashItem(slea, c, c.getPlayer());
         break;
      case COUPON_CODE:
         slea.skip(1);
         CashShopOperation.CouponCode(slea.readMapleAsciiString(), c);
         break;
      case CS_CHARGE:
         CashShopOperation.csCharge(c);
         break;
      case CS_UPDATE:
         CashShopOperation.CSUpdate(c);
         break;
      case CS_GIFT:
         CashShopOperation.csGift(slea, c);
         break;
      case MVP_SPECIAL_PACK:
         CashShopOperation.mvpSpecialPack(slea.readInt(), c);
         break;
      case MVP_GIFT_PACK:
         CashShopOperation.mvpGiftPack(c);
         break;
      case DAMAGE_SUMMON:
         SummonHandler.DamageSummon(slea, c.getPlayer());
         break;
      case MOVE_SUMMON:
         SummonHandler.MoveSummon(slea, c.getPlayer());
         break;
      case SUMMON_ATTACK:
         SummonHandler.SummonAttack(slea, c, c.getPlayer());
         break;
      case MOVE_DRAGON:
         SummonHandler.MoveDragon(slea, c.getPlayer());
         break;
      case SUB_SUMMON:
         SummonHandler.SubSummon(slea, c.getPlayer());
         break;
      case REMOVE_SUMMON:
      case REMOVE_SUMMON2:
         SummonHandler.RemoveSummon(slea, c);
         break;
      case SPAWN_PET:
         PetHandler.SpawnPet(slea, c, c.getPlayer());
         break;
      case MOVE_PET:
         PetHandler.MovePet(slea, c.getPlayer());
         break;
      case PET_CHAT:
         if (slea.available() >= 12L) {
            type2 = slea.readInt();
            slea.readInt();
            PetHandler.PetChat(type2, slea.readShort(), slea.readMapleAsciiString(), c.getPlayer());
         }
         break;
      case PET_COMMAND:
         item = null;
         MaplePet pet = c.getPlayer().getPet((long)((byte)slea.readInt()));
         slea.readByte();
         if (pet == null) {
            return;
         }

         PetHandler.PetCommand(pet, PetDataFactory.getPetCommand(pet.getPetItemId(), slea.readByte()), c, c.getPlayer());
         break;
      case PET_FOOD:
         PetHandler.PetFood(slea, c, c.getPlayer());
         break;
      case PET_LOOT:
         InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
         break;
      case PET_AUTO_POT:
         PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
         break;
      case PET_EXCEPTION_LIST:
         PetHandler.petExceptionList(slea, c, c.getPlayer());
         break;
      case DUEY_ACTION:
         DueyHandler.DueyOperation(slea, c);
         break;
      case REPAIR:
         NPCHandler.repair(slea, c);
         break;
      case REPAIR_ALL:
         NPCHandler.repairAll(c);
         break;
      case USE_ITEM_QUEST:
         NPCHandler.UseItemQuest(slea, c);
         break;
      case AUCTION_RESULT:
         AuctionHandler.Handle(slea, c);
         break;
      case AUCTION_EXIT:
         AuctionHandler.LeaveAuction(c, c.getPlayer());
         break;
      case UPDATE_QUEST:
         NPCHandler.UpdateQuest(slea, c);
         break;
      case MEMORY_CHOICE_R:
         PlayerHandler.MemoryChoice(slea, c);
         break;
      case FOLLOW_REQUEST:
         PlayersHandler.FollowRequest(slea, c);
         break;
      case FOLLOW_CANCEL:
         PlayersHandler.followCancel(slea, c);
         break;
      case AUTO_FOLLOW_REPLY:
      case FOLLOW_REPLY:
         PlayersHandler.FollowReply(slea, c);
         break;
      case USE_KAISER_COLOR:
         InventoryHandler.UseKaiserColorChange(slea, c);
         break;
      case USE_NAME_CHANGE:
         InventoryHandler.UseNameChangeCoupon(slea, c);
         break;
      case RING_ACTION:
         PlayersHandler.RingAction(slea, c);
         break;
      case WEDDING_PRESENT:
         PlayersHandler.WeddingPresent(slea, c);
         break;
      case USE_TELE_ROCK:
         InventoryHandler.TeleRock(slea, c);
         break;
      case REPORT:
         PlayersHandler.Report(slea, c);
         break;
      case MAPLE_EXIT:
      case GAME_EXIT:
         try {
            InterServerHandler.getGameQuitRequest(header, slea, c);
         } catch (Exception var13) {
            var13.printStackTrace();
         }
         break;
      case PQ_REWARD:
         InventoryHandler.SelectPQReward(slea, c);
         break;
      case INNER_CHANGE:
         try {
            PlayerHandler.ChangeInner(slea, c);
         } catch (Exception var12) {
            var12.printStackTrace();
         }
         break;
      case ABSORB_REGEN:
         PlayerHandler.absorbingRegen(slea, c);
         break;
      case CAIN_STACK_SKILL:
         PlayerHandler.Vmatrixstackbuff(c, false, slea);
         break;
      case ZERO_SCROLL_UI:
         PlayerHandler.ZeroScrollUI(slea.readInt(), c);
         break;
      case ZERO_SCROLL_LUCKY:
         PlayerHandler.ZeroScrollLucky(slea, c);
         break;
      case ZERO_SCROLL:
         PlayerHandler.ZeroScroll(slea, c);
         break;
      case ZERO_SCROLL_START:
         PlayerHandler.ZeroScrollStart(header, slea, c);
         break;
      case ZERO_WEAPON_INFO:
         PlayerHandler.ZeroWeaponInfo(slea, c);
         break;
      case ZERO_WEAPON_UPGRADE:
         PlayerHandler.ZeroWeaponLevelUp(slea, c);
         break;
      case ZERO_TAG:
         PlayerHandler.ZeroTag(slea, c);
         break;
      case ZERO_TAG_REMOVE:
         PlayerHandler.ZeroTagRemove(c);
         break;
      case SUB_ACTIVE_SKILL:
         PlayerHandler.subActiveSkills(slea, c);
         break;
      case ZERO_CLOTHES:
         PlayerHandler.ZeroClothes(slea, c);
         break;
      case WILL_OF_SWORD_COMBO:
         PlayerHandler.absorbingSword(slea, c.getPlayer());
         break;
      case FIELD_ATTACK_OBJ_ATTACK:
         PlayerHandler.FieldAttackObjAttack(slea, c.getPlayer());
         break;
      case FIELD_ATTACK_OBJ_ACTION:
         PlayerHandler.FieldAttackObjAction(slea, c.getPlayer());
         break;
      case ORBITAL_FLAME:
         PlayerHandler.OrbitalFlame(slea, c);
         break;
      case VIEW_SKILLS:
         PlayersHandler.viewSkills(slea, c);
         break;
      case SKILL_SWIPE:
         PlayersHandler.StealSkill(slea, c);
         break;
      case CHOOSE_SKILL:
         PlayersHandler.ChooseSkill(slea, c);
         break;
      case VOYD_PRESSURE:
         PlayerHandler.VoydPressure(slea, c.getPlayer());
         break;
      case EQUIPMENT_ENCHANT:
         EquipmentEnchant.handleEnchant(slea, c);
         break;
      case DRESS_UP:
         PlayerHandler.DressUpRequest(c.getPlayer(), slea);
         break;
      case UNLOCK_TRINITY:
         PlayerHandler.unlockTrinity(c);
         break;
      case DRESSUP_TIME:
         PlayerHandler.DressUpTime(slea, c);
         break;
      case HYPER_R:
         HyperHandler.getHyperSkill(slea, c);
         break;
      case UPDATE_CORE:
         MatrixHandler.updateCore(slea, c);
         break;
      case MATRIX_SKILL:
         PlayerHandler.MatrixSkill(slea, c);
         break;
      case MEGA_SMASHER:
         PlayerHandler.megaSmasherRequest(slea, c);
         break;
      case SHADOW_SERVENT_EXTEND:
         PlayerHandler.ShadowServentExtend(slea, c);
         break;
      case JOKER_R:
         PlayerHandler.joker(c);
         break;
      case MINICONNONBALL:
         PlayerHandler.CannonBall(slea, c);
         break;
      case SELECT_DICE:
         PlayerHandler.selectDice(slea, c);
         break;
      case SYMBOL_LEVELUP:
      case AUT_SYMBOL_LEVELUP:
         type2 = slea.readInt();
         if (type2 == 1) {
            PlayerHandler.UpdateSymbol(slea, c, type2);
         } else if (type2 == 2) {
            PlayerHandler.SymbolMultiExp(slea, c);
         } else {
            PlayerHandler.SymbolExp(slea, c);
         }
         break;
      case UNLINK_SKILL:
         PlayerHandler.UnlinkSkill(slea.readInt(), c);
         break;
      case LINK_SKILL:
         PlayerHandler.LinkSkill(slea.readInt(), slea.readInt(), slea.readInt(), c);
         break;
      case ARK_GAUGE:
         PlayerHandler.arkGauge(slea.readInt(), c.getPlayer());
         break;
      case RESPAWN:
         PlayerHandler.Respawn(c);
         break;
      case SOUL_MATCH:
         PlayerHandler.SoulMatch(slea, c);
         break;
      case DAILY_GIFT:
         PlayerHandler.DailyGift(c);
         break;
      case HAMMER_OF_TODD:
         InventoryHandler.Todd(slea, c);
         break;
      case NPC_OF_TODD:
         NPCScriptManager.getInstance().start(c, 9900000, "todd");
         break;
      case OPEN_UNION:
         UnionHandler.openUnion(c);
         break;
      case PRAY:
         PlayerHandler.activePrayBuff(c);
         break;
      case RESOTRE:
         PlayerHandler.activeRestoreBuff(c);
         break;
      case LUCID_STATE_CHANGE:
         MobHandler.lucidStateChange(c.getPlayer());
         break;
      case INHUMAN_SPEED:
         PlayerHandler.InhumanSpeed(slea, c);
         break;
      case SET_UNION:
         UnionHandler.setUnion(slea, c);
         break;
      case SET_UNION_FREESET:
         UnionHandler.setUnionPriset(slea, c);
         break;
      case UPDATE_JAGUAR:
         PlayerHandler.UpdateJaguar(slea, c);
         break;
      case AURA_WEAPON:
         PlayerHandler.auraWeapon(slea, c);
         break;
      case REMOVE_MIST:
         PlayerHandler.removeMist(slea, c);
         break;
      case PEACE_MAKER_1:
         PlayerHandler.PeaceMaker(slea, c);
         break;
      case PEACE_MAKER_2:
         PlayerHandler.PeaceMaker2(slea, c);
         break;
      case DEMON_FRENZY:
         PlayerHandler.DemonFrenzy(c);
         break;
      case KEYDOWN_MANAGEMENT:
         PlayerHandler.keydownSkillManagement(slea, c);
         break;
      case SKILL_SUB_EFFECT:
         PlayerHandler.subSkillEffect(slea, c.getPlayer());
         break;
      case CANCEL_SUB_EFFECT:
         PlayerHandler.cancelSubEffect(slea, c.getPlayer());
         break;
      case CHANGE_SUB_EFFECT:
         PlayerHandler.changeSubEffect(slea, c.getPlayer());
         break;
      case SHOW_ICBM:
         PlayerHandler.showICBM(slea, c.getPlayer());
         break;
      case ARK_LINK:
         PlayerHandler.LinkofArk(slea, c.getPlayer());
         break;
      case FLOW_OF_FIGHT:
         PlayerHandler.FlowOfFight(c.getPlayer());
         break;
      case UserTowerChairSetting:
         slea.skip(4);
         PlayerHandler.TowerChair(slea, c);
         break;
      case BlockGameRes:
         PlayerHandler.HandleBlockGameRes(slea, c);
         break;
      case ExitBlockGame:
         PlayerHandler.ExitBlockGame(slea, c);
         break;
      case ClickBingoCell:
         slea.skip(8);
         PlayerHandler.HandleCellClick(slea.readInt(), c);
         break;
      case ClickBingo:
         PlayerHandler.HandleBingoClick(c);
         break;
      case DREAM_BREAKER_SKILL:
         slea.skip(4);
         PlayerHandler.HandleDreamBreakerSkill(c, slea.readInt());
         break;
      case SPECIAL_GAME_EXIT:
         PlayerHandler.ExitSpecialGame(c);
         break;
      case HDetectiveGameInput:
         PlayerHandler.HandleHundredDetectiveGame(slea, c);
         break;
      case BALANCE_EXIT:
         MapleNPC npc = MapleLifeFactory.getNPC(1540445);
         if (npc != null && !npc.getName().equals("MISSINGNO")) {
            npc.setPosition(c.getPlayer().getPosition());
            npc.setCy(c.getPlayer().getPosition().y);
            npc.setRx0(c.getPlayer().getPosition().x + 50);
            npc.setRx1(c.getPlayer().getPosition().x - 50);
            npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
            npc.setCustom(true);
            c.getPlayer().getMap().addMapObject(npc);
            c.getPlayer().getMap().broadcastMessage(CField.NPCPacket.spawnNPC(npc, true));
         }
         break;
      case UserClientResolutionResult:
         PlayerHandler.HandleResolution(slea, c);
         break;
      case CHARACTER_ORDER:
         c.order(slea);
         break;
      case EXIT_PLATFORMER:
         PlayerHandler.HandlePlatformerExit(slea, c);
         break;
      case REPLACE_SUMMON:
         SummonHandler.replaceSummon(slea, c);
         break;
      case ICBM:
         PlayerHandler.ICBM(slea, c);
         break;
      case DIMENTION_SWORD:
         PlayerHandler.DimentionSword(slea, c);
         break;
      case SPECIAL_SUMMON:
         SummonHandler.specialSummon(slea, c);
         break;
      case SPECIAL_SUMMON_5TH:
         SummonHandler.specialSummon5th(slea, c);
         break;
      case EFFECT_SUMMON:
         SummonHandler.effectSummon(slea, c);
         break;
      case CANCEL_EFFECT_SUMMON:
         SummonHandler.cancelEffectSummon(slea, c);
         break;
      case AFTER_CANCEL:
         PlayerHandler.cancelAfter(slea, c);
         break;
      case AUTO_SKILL:
      case AUTO_SKILL2:
         PlayerHandler.autoSkill(slea, c);
         break;
      case RESPAWN_LUCID:
         PlayerHandler.RespawnLucid(slea, c);
         break;
      case RAINBOW_RUSH_START:
         PlayerHandler.RainBowRushStart(c);
         break;
      case RAINBOW_RUSH_TIMER:
         PlayerHandler.RainBowRushTimer(slea, c);
         break;
      case RAINBOW_RUSH_DEAD:
         if (!c.getPlayer().isNoDeadRush()) {
            PlayerHandler.RainBowRushDead(c);
         }
         break;
      case RAINBOW_RUSH_RETURN_MAP:
         PlayerHandler.RainBowRushReturnMap(c);
         break;
      case RAINBOW_RUSH_TIMER_SECOND:
         PlayerHandler.RainBowRushTimer(slea, c);
         break;
      case POISON_NOVA:
         PlayerHandler.PoisonNova(slea, c);
         break;
      case USE_MOON_GAUGE:
         PlayerHandler.useMoonGauge(c);
         break;
      case WILL_MOON:
         PlayerHandler.wiilMoon(slea, c);
         break;
      case WILL_SPIDER_TOUCH:
         PlayerHandler.touchSpider(slea, c);
         break;
      case SKILL_TO_Crystal:
         PlayerHandler.SkillToCrystal(slea, c);
         break;
      case RETURN_RESULT:
         InventoryHandler.returnScrollResult(slea, c);
         break;
      case MINIGAME_OPERATION:
         PlayerInteractionHandler.minigameOperation(slea, c);
         break;
      case BUFF_FREEZER:
         PlayerHandler.buffFreezer(slea, c);
         break;
      case QUICK_SLOT:
         PlayerHandler.quickSlot(slea, c);
         break;
      case CHECK_CORE_SECONDPW:
         PlayerHandler.checkCoreSecondpw(slea, c);
         break;
      case INVITE_CHAIR:
         PlayerHandler.inviteChair(slea, c);
         break;
      case RESULT_CHAIR:
         PlayerHandler.resultChair(slea, c);
         break;
      case BLOOD_FIST:
         PlayerHandler.bloodFist(slea, c);
         break;
      case YOYO_STACK:
      case STACK_BUFF:
      case CHARGE_SKILL:
         PlayerHandler.Vmatrixstackbuff(c, false, slea);
         break;
      case UPDATE_MIST:
         try {
            PlayerHandler.updateMist(slea, c);
         } catch (Exception var11) {
            var11.printStackTrace();
         }
         break;
      case NETT_PYRAMID_CHECK:
         type2 = slea.readInt();
         MapleNettPyramid mnp = c.getPlayer().getNettPyramid();
         if (mnp == null) {
            System.out.println("MapleNettPyramid = null");
         } else if (type2 == 1) {
            int sid = slea.readInt();
            mnp.useSkill(c.getPlayer(), sid);
         } else if (type2 == 3) {
            mnp.check();
         }
         break;
      case FISHING:
         FishingHandler.fishing(slea, c);
         break;
      case FISHING_END:
         FishingHandler.fishingEnd(c);
         break;
      case ADD_BULLET:
         PlayerHandler.BlesterSkill(slea, c, false);
         break;
      case REVOLVING_BUNKER_CANCEL:
         if (c.getPlayer().getBuffedValue(37121004)) {
            c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(37121004));
         }
         break;
      case MAGUNM_BLOW:
         PlayerHandler.Magunmblow(slea, c);
         break;
      case ARCANE_CATALYST:
         InventoryHandler.ArcaneCatalyst(slea, c);
         break;
      case ARCANE_CATALYST2:
         InventoryHandler.ArcaneCatalyst2(slea, c);
         break;
      case ARCANE_CATALYST3:
         InventoryHandler.ArcaneCatalyst3(slea, c);
         break;
      case ARCANE_CATALYST4:
         InventoryHandler.ArcaneCatalyst4(slea, c);
         break;
      case RETURN_SYNTHESIZING:
         InventoryHandler.ReturnSynthesizing(slea, c);
         break;
      case DEMIAN_BIND:
         MobHandler.demianBind(slea, c);
         break;
      case DEMIAN_ATTACKED:
         MobHandler.demianAttacked(slea, c);
         break;
      case STIGMA_INCINERATE_USE:
         MobHandler.useStigmaIncinerate(slea, c);
         break;
      case STONE_ATTACKED:
         MobHandler.stoneAttacked(slea, c);
         break;
      case SPOTLIGHT_BUFF:
         PlayerHandler.spotlightBuff(slea, c);
         break;
      case BLESS_5TH:
         PlayerHandler.bless5th(slea, c);
         break;
      case UNION_FREESET:
         UnionHandler.unionFreeset(slea, c);
         break;
      case BLACK_HAND:
         MobHandler.jinHillahBlackHand(slea, c);
         break;
      case TOUCH_ALTER:
         MobHandler.touchAlter(slea, c);
         break;
      case QUICK_MOVE:
         NPCHandler.quickMove(slea, c);
         break;
      case UNK_JINHILLIA:
         MobHandler.unkJinHillia(slea, c);
         break;
      case DIMENTION_MIRROR:
         NPCHandler.dimentionMirror(slea, c);
         break;
      case USE_BLACK_REBIRTH_RESULT:
         InventoryHandler.blackRebirthResult(slea, c);
         break;
      case QUICK_PASS:
         PlayerHandler.quickPass(slea, c);
         break;
      case BATTLE_STATISTICS:
         PlayerHandler.battleStatistics(slea, c);
         break;
      case EVENTUI_RESULT:
         PlayerHandler.eventUIResult(slea, c);
         break;
      case MOBSKILL_DELAY:
         MobHandler.mobSkillDelay(slea, c);
         break;
      case REMOVE_SECOND_ATOM:
         PlayerHandler.removeSecondAtom(slea, c);
         break;
      case INFO_SECOND_ATOM:
         PlayerHandler.InfoSecondAtom(slea, c);
         break;
      case ROPE_CONNECT:
         PlayerHandler.ropeConnect(slea, c);
         break;
      case AURA_PARTY_BUFF:
         PlayersHandler.auraPartyBuff(slea, c);
         break;
      case EXIT_FARM:
         FarmHandler.leaveFarm(c, c.getPlayer());
         break;
      case UPDATE_FARM_IMG:
         FarmHandler.updateFarmImg(slea, c);
         break;
      case FPS_SHOOT_REQUEST:
         PlayerHandler.fpsShootRequest(slea, c);
         break;
      case ZERO_LUCKY_SCROLL:
         PlayerHandler.ZeroLuckyScroll(c, slea);
         break;
      case COURTSHIP_COMMAND:
         PlayerHandler.courtshipCommand(slea, c);
         break;
      case FORCE_INFO:
         PlayerHandler.forceInfo(slea, c);
         break;
      case VSKILL_SPECIAL:
         PlayerHandler.vSkillSpecial(slea, c);
         break;
      case SPAWN_ORB:
         OrbHandler.spawnOrb(slea, c);
         break;
      case MOVE_ORB:
         OrbHandler.moveOrb(slea, c);
         break;
      case REMOVE_ORB:
         OrbHandler.removeOrb(slea, c);
         break;
      case REVENANT:
         PlayerHandler.Revenant(slea, c);
         break;
      case REVENANT_DAMAGE:
         PlayerHandler.Revenantend(slea, c);
         break;
      case SILHOUETTE_MIRAGE:
         PlayerHandler.SilhouEtteMirage(c.getPlayer(), slea);
         break;
      case MECH_CARRIER:
         SummonHandler.mechCarrier(slea, c);
         break;
      case PHOTON_RAY:
         PlayerHandler.photonRay(slea, c);
         break;
      case CRYSTAL_GATE:
         PlayerHandler.crystalGate(slea, c);
         break;
      case CANCEL_BUFF_FORCE:
         PlayerHandler.cancelBuffForce(c);
         break;
      case COMMAND_LOCK2:
      case COMMAND_LOCK:
      case EVENT_UI_SKILL:
         PlayerHandler.CommandLockAction2(slea, c);
         break;
      case CHANGE_DRAGON_ATTACK_IMG:
         PlayerHandler.ChangeDragonImg(c.getPlayer(), slea);
         break;
      case ATTACK_DRAGON_IMG:
         PlayerHandler.AttackDragonImg(c.getPlayer(), slea);
         break;
      case PHANTOM_SHRUOD:
         PlayerHandler.PhantomShroud(slea, c);
         break;
      case LIFT_BREAK:
         PlayerHandler.LiftBreak(c.getPlayer(), slea);
         break;
      case HARMONY_LINK:
         PlayerHandler.HarmonyLink(slea, c);
         break;
      case FORCEATOM_EFFECT:
         PlayerHandler.ForceAtomEffect(slea, c);
         break;
      case COLOR_CARD_COMMAND:
         PlayersHandler.ColorCardHandler(slea, c);
         break;
      case CONTENTS_WAITING:
         PlayersHandler.ContentsWaiting(slea, c);
         break;
      case MAPLE_YUT_HANDLER:
         PlayersHandler.MapleYutHandler(slea, c);
         break;
      case MESSENGER_SEARCH:
         ChatHandler.Messengerserch(slea, c);
         break;
      case SELECT_REINCARNATION:
         PlayerHandler.SelectReincarnation(slea, c);
         break;
      case SELECT_HOLY_UNITY:
         PlayerHandler.SelectHolyUnity(slea, c);
         break;
      case PYRET_BLESS:
         PlayerHandler.PyretBless(slea, c);
         break;
      case ZERO_SHOCK_WAVE:
         PlayerHandler.ZeroShockWave(slea, c);
         break;
      case REISSUE_MEDAL:
         PlayerHandler.MedalReissuance(c, slea);
         break;
      case PSYCHIC_OVER:
         c.getPlayer().givePPoint((byte)1);
         break;
      case ROLLING_GRADE:
         PlayerHandler.PinkBeanRollingGrade(c, slea);
         break;
      case POISON_REGION:
         PlayerHandler.handlePoisonRegion(slea, c);
         break;
      case DEBUFF_OBJ_HIT:
         PlayerHandler.DebuffObjHit(c, slea);
         break;
      case SPAWN_MIST_MONSTER:
         MobHandler.spawnMistArea(slea, c);
         break;
      case MOVE_MONSTER_SPAWN_MIST:
         MobHandler.SpawnMoveMobMist(slea, c.getPlayer());
         break;
      case PAPULATUS_BOMB:
      case PAPULATUS_BOMB_DAMAGE:
         MobHandler.PapulaTusBomb(slea, c, header == RecvPacketOpcode.PAPULATUS_BOMB_DAMAGE ? 1 : 0);
         break;
      case PAPULATUS_PINCERS:
         MobHandler.PapulaTusPincers(slea, c);
         break;
      case PAPULATUS_PINCERS_RESET:
         MobHandler.PapulaTusPincersreset(slea, c);
         break;
      case BLOOD_QUEEN_BREATH:
         MobHandler.BloodyQueenBress(slea, c);
         break;
      case TOUCH_MONSTER:
         MobHandler.BloodyQueenMirror(slea, c);
         break;
      case VANVAN_TIMEOVER:
         MobHandler.VanVanTimeOver(slea, c);
         break;
      case SPAWN_BELLUM_MIST:
         MobHandler.SpawnBellumMist(slea, c);
         break;
      case REMOVE_OBSTACLE:
         MobHandler.RemoveObstacle(slea, c);
         break;
      case REMOVE_ENERGYSPHERE:
         MobHandler.RemoveEnergtSphere(slea, c);
         break;
      case DEMIAN_SWORD_HANDLE:
         MobHandler.DemianSwordHandle(slea, c);
         break;
      case NOTE_HANDLE:
         PlayerHandler.NoteHandle(c, slea);
         break;
      case NOTE_HANDLER:
         PlayerHandler.NoteHandler(c, slea);
         break;
      case MAPLE_CABINET:
         PlayerHandler.MapleCabiNet(c, slea);
         break;
      case EXP_POCKET:
         PlayerHandler.ExpPocket(c, slea);
         break;
      case CHAT_EMOTICON:
         PlayerHandler.ChatEmoticon(slea, c);
         break;
      case CREATE_CORE:
         c.send(CWvsContext.CreateCore(1));
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         NPCScriptManager.getInstance().dispose(c);
         c.removeClickedNPC();
         NPCScriptManager.getInstance().start(c, 2007, "Create_Core");
         break;
      case USE_AP_RESET_SCROLL:
         slea.skip(4);
         type = slea.readShort();
         rand = slea.readInt();
         c.getPlayer().resetStats(4, 4, 4, 4);
         c.getPlayer().dropMessage(5, "AP 초기화가 완료 되었습니다.");
         c.send(CWvsContext.enableActions(c.getPlayer()));
         MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(rand), type, (short)1, false);
         break;
      case USE_ITEM_LOCK:
         slea.skip(8);
         item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slea.readShort());
         if (item != null) {
            Equip eq = (Equip)item;
            String set = "아이템";
            if ((eq.getEquipmentType() & 131072) == 131072) {
               eq.setEquipmentType(eq.getEquipmentType() - 131072);
               set = set + "의 잠금이 해제";
            } else {
               eq.setEquipmentType(eq.getEquipmentType() | 131072);
               set = set + "에 잠금이 설정";
            }

            set = set + "되었습니다.";
            c.getPlayer().dropMessage(5, set);
            c.send(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, eq));
            c.send(CWvsContext.enableActions(c.getPlayer()));
         }
         break;
      case HIT_ERDA_SPECTRUM:
         PlayerHandler.HitErdaSpectrum(c, slea);
         break;
      case ACT_ERDA_SPECTRUM:
         PlayerHandler.ActErdaSpectrum(c, slea);
         break;
      case BALL_ERDA_SPECTRUM:
         PlayerHandler.BallErdaSpectrum(c, slea);
         break;
      case AFTER_CANCEL2:
         PlayerHandler.AfterCancel2(c, slea);
         break;
      case SP_PORTAL_USE:
         PlayerHandler.SpPortalUse(c, slea);
         break;
      case START_QUEST:
         type2 = slea.readInt();
         rand = slea.readInt();
         int types = slea.readByte();
         slea.skip(4);
         c.getPlayer().setKeyValue(type2, "start", types.makeConcatWithConstants<invokedynamic>(types));
         c.getPlayer().setKeyValue(type2, "NpcSpeech", rand + types);
         NPCScriptManager.getInstance().startQuest(c, rand, type2);
         break;
      case FREE_CHANGE_JOB:
         PlayerHandler.JobChange(slea, c, c.getPlayer());
         break;
      case ITEMMAKER_COOLDOWN:
         c.send(CField.ItemMakerCooldown(slea.readInt(), 0));
         break;
      case SUDDEN_MISSION_CLEAR:
         int[][] itemlist = new int[][]{{2049153, 1}, {5062010, 10}, {5062500, 20}, {5062503, 10}, {4310012, 35}, {2049751, 1}, {2049752, 1}, {2048759, 5}, {2048757, 5}, {2048766, 3}, {5069000, 1}, {5069001, 1}, {5064000, 1}, {5064100, 1}, {2431480, 1}, {5064400, 1}, {5064400, 1}, {2433019, 1}, {2049153, 1}, {5062010, 20}, {5062500, 20}, {5062503, 10}, {4310012, 35}, {2049751, 1}, {2049752, 1}, {2048759, 5}, {2048757, 5}, {2048766, 3}, {5069000, 1}, {5069001, 1}, {5064000, 1}, {5064100, 1}, {2431480, 1}, {5064400, 1}, {5064400, 1}, {2433019, 1}, {2049153, 1}, {5062010, 20}, {5062500, 20}, {5062503, 10}, {4310012, 35}, {2049751, 1}, {2049752, 1}, {2048759, 5}, {2048757, 5}, {2048766, 3}, {5069000, 1}, {5069001, 1}, {5064000, 1}, {5064100, 1}, {2430452, 1}, {5064400, 1}, {5064400, 1}, {2433019, 1}, {4310005, 1}, {2431421, 1}, {5062009, 15}, {5068300, 1}};
         if (c.getPlayer().getKeyValue(51351, "queststat") == 3L) {
            if (c.getQuestStatus(100825) == 2) {
               c.getPlayer().setKeyValue(100835, "suddenMK", "1");
               c.getPlayer().setKeyValue(100835, "state", "1");
            }

            c.send(CWvsContext.updateSuddenQuest((int)c.getPlayer().getKeyValue(51351, "midquestid"), false, 0L, "count=0;Quest=0;day=0;state=0;"));
            c.getPlayer().removeKeyValue(51351);
            rand = Randomizer.rand(0, itemlist.length - 1);
            c.getPlayer().gainCabinetItemPlayer(itemlist[rand][0], itemlist[rand][1], 1, "돌발미션 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
            c.getPlayer().dropMessage(5, "돌발미션 보상이 <메이플 보관함>에 지급 되었습니다.");
            c.getPlayer().getClient().getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(c.getPlayer(), 15, true));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showNormalEffect(c.getPlayer(), 15, false), false);
         }
         break;
      case TYOONKITCHEN_COOK_SUC:
         PlayersHandler.MapleTyoonKitchenSuc(slea, c);
         break;
      case TYOONKITCHEN_COOK_MAKE:
         PlayersHandler.MapleTyoonKitchenMake(slea, c);
         break;
      case LARA_POINT:
         PlayerHandler.LaraPoint(c, slea);
         break;
      case USE_SECOND_ATOM:
         PlayerHandler.UseSecondAtom(c, slea);
         break;
      case USE_CIRCULATOR:
      case USE_CHAOS_CIRCULATOR:
         InventoryHandler.UseCirculator(slea, c);
         break;
      case Lotus:
         PlayerHandler.Lotus(slea, c);
         break;
      case Lotus2:
         PlayerHandler.Lotus2(slea, c);
         break;
      case TANGYOON_COOKING:
         int salt = slea.readByte();
         if (salt == 0) {
            PlayerHandler.TangyoonCooking(slea, c);
         } else {
            PlayerHandler.TangyoonSalt(slea, c);
         }
         break;
      case mix_hair:
         PlayerHandler.믹스헤어(slea, c);
         break;
      default:
         System.out.println("[UNHANDLED] Recv [" + header.toString() + "] found");
      }

   }
}
