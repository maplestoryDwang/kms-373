package client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import tools.Pair;

public enum SecondaryStat implements Serializable {
   IndiePad(0),
   IndieMad(1),
   IndiePdd(2),
   IndieHp(3),
   IndieHpR(4),
   IndieMp(5),
   IndieMpR(6),
   IndieAcc(7),
   IndieEva(8),
   IndieJump(9),
   IndieSpeed(10),
   IndieAllStat(11),
   IndieAllStatR(12),
   IndieDodgeCriticalTime(13),
   IndieExp(14),
   IndieBooster(15),
   IndieFixedDamageR(16),
   PyramidStunBuff(17),
   PyramidFrozenBuff(18),
   PyramidFireBuff(19),
   PyramidBonusDamageBuff(20),
   IndieRelaxEXP(21),
   IndieStr(22),
   IndieDex(23),
   IndieInt(24),
   IndieLuk(25),
   IndieDamR(26),
   IndieScriptBuff(27),
   IndieMaxDamageR(28),
   IndieAsrR(29),
   IndieTerR(30),
   IndieCr(31),
   IndiePddR(32),
   IndieCD(33),
   IndieBDR(34),
   IndieStatR(35),
   IndieStance(36),
   IndieIgnoreMobPdpR(37),
   IndieEmpty(38),
   IndiePadR(39),
   IndieMadR(40),
   IndieEvaR(41),
   IndieDrainHP(42),
   IndiePmdR(43),
   IndieForceJump(44),
   IndieForceSpeed(45),
   IndieDamageReduce(46),
   IndieSummon(47),
   IndieReduceCooltime(48),
   IndieNotDamaged(49),
   IndieJointAttack(50),
   IndieKeyDownMoving(51),
   IndieUnkIllium(52),
   IndieEvasion(53),
   IndieShotDamage(54),
   IndieSuperStance(55),
   IndieGrandCross(56),
   IndieDamReduceR(57),
   IndieWickening1(58),
   IndieWickening2(59),
   IndieWickening3(30),
   IndieWickening4(61),
   IndieFloating(62),
   IndieUnk1(63),
   IndieUnk2(64),
   IndieDarkness(65),
   IndieBlockSkill(66),
   IndieBarrier(68),
   IndieNDR(70),
   Indie_STAT_COUNT(72),
   Pad(73),
   Pdd(74),
   Mad(75),
   Acc(76),
   Eva(77),
   Craft(78),
   Speed(81),
   Jump(82),
   MagicGaurd(83),
   DarkSight(84),
   Booster(85),
   PowerGaurd(86),
   MaxHP(87),
   MaxMP(88),
   Invincible(89),
   SoulArrow(90),
   Stun(91),
   Poison(92),
   Seal(93),
   Darkness(94),
   ComboCounter(95),
   BlessedHammer(96),
   BlessedHammer2(97),
   SnowCharge(98),
   HolySymbol(99),
   MesoUp(100),
   ShadowPartner(101),
   Steal(102),
   PickPocket(103),
   Murderous(104),
   Thaw(105),
   Weakness(106),
   Curse(107),
   Slow(108),
   Morph(109),
   Recovery(110),
   BasicStatUp(111),
   Stance(112),
   SharpEyes(113),
   ManaReflection(114),
   Attract(115),
   NoBulletConsume(116),
   Infinity(117),
   AdvancedBless(118),
   Illusion(119),
   Blind(120),
   Concentration(121),
   BanMap(122),
   MaxLevelBuff(123),
   MesoUpByItem(124),
   WealthOfUnion(125),
   RuneOfGreed(126),
   Ghost(127),
   Barrier(128),
   ReverseInput(129),
   ItemUpByItem(130),
   RespectPImmune(131),
   RespectMImmune(132),
   DefenseAtt(133),
   DefenseState(134),
   DojangBerserk(135),
   DojangInvincible(136),
   DojangShield(137),
   SoulMasterFinal(138),
   WindBreakerFinal(139),
   ElementalReset(140),
   HideAttack(141),
   EventRate(142),
   AranCombo(143),
   AuraRecovery(144),
   UnkBuffStat1(145),
   BodyPressure(146),
   RepeatEffect(147),
   ExpBuffRate(148),
   StopPortion(149),
   StopMotion(150),
   Fear(151),
   HiddenPieceOn(152),
   MagicShield(153),
   SoulStone(154),
   Flying(156),
   Frozen(157),
   AssistCharge(158),
   Enrage(159),
   DrawBack(160),
   NotDamaged(161),
   FinalCut(162),
   HowlingParty(163),
   BeastFormDamage(164),
   Dance(165),
   EnhancedMaxHp(166),
   EnhancedMaxMp(167),
   EnhancedPad(168),
   EnhancedMad(169),
   EnhancedPdd(170),
   PerfectArmor(171),
   UnkBuffStat2(172),
   IncreaseJabelinDam(173),
   PinkbeanMinibeenMove(174),
   Sneak(175),
   Mechanic(176),
   BeastFormMaxHP(177),
   DiceRoll(178),
   BlessingArmor(179),
   DamR(180),
   TeleportMastery(181),
   CombatOrders(182),
   Beholder(183),
   DispelItemOption(184),
   Inflation(185),
   OnixDivineProtection(186),
   Web(187),
   Bless(188),
   TimeBomb(189),
   DisOrder(190),
   Thread(191),
   Team(192),
   Explosion(193),
   BuffLimit(194),
   STR(195),
   INT(196),
   DEX(197),
   LUK(198),
   DispelByField(199),
   DarkTornado(200),
   PVPDamage(201),
   PvPScoreBonus(202),
   PvPInvincible(203),
   PvPRaceEffect(204),
   WeaknessMdamage(205),
   Frozen2(206),
   PvPDamageSkill(207),
   AmplifyDamage(208),
   Shock(209),
   InfinityForce(210),
   IncMaxHP(211),
   IncMaxMP(212),
   HolyMagicShell(213),
   KeyDownTimeIgnore(214),
   ArcaneAim(215),
   MasterMagicOn(216),
   Asr(217),
   Ter(218),
   DamAbsorbShield(219),
   DevilishPower(220),
   Roulette(221),
   SpiritLink(222),
   AsrRByItem(223),
   Event(224),
   CriticalIncrease(225),
   DropItemRate(226),
   DropRate(227),
   ItemInvincible(228),
   Awake(229),
   ItemCritical(230),
   ItemEvade(231),
   Event2(232),
   DrainHp(233),
   IncDefenseR(234),
   IncTerR(235),
   IncAsrR(236),
   DeathMark(237),
   Infiltrate(238),
   Lapidification(239),
   VenomSnake(240),
   CarnivalAttack(241),
   CarnivalDefence(242),
   CarnivalExp(243),
   SlowAttack(244),
   PyramidEffect(245),
   UnkBuffStat3(246),
   KeyDownMoving(247),
   IgnoreTargetDEF(248),
   UNK_249(249),
   ReviveOnce(250),
   Invisible(251),
   EnrageCr(252),
   EnrageCrDamMin(253),
   Judgement(254),
   DojangLuckyBonus(255),
   PainMark(256),
   Magnet(257),
   MagnetArea(258),
   GuidedArrow(259),
   UnkBuffStat4(260),
   BlessMark(261),
   BonusAttack(262),
   UnkBuffStat5(263),
   FlowOfFight(264),
   ShadowMomentum(265),
   GrandCrossSize(266),
   LuckOfUnion(267),
   PinkBeanFighting(268),
   VampDeath(270),
   BlessingArmorIncPad(271),
   KeyDownAreaMoving(272),
   Larkness(273),
   StackBuff(274),
   AntiMagicShell(275),
   LifeTidal(276),
   HitCriDamR(277),
   SmashStack(278),
   RoburstArmor(279),
   ReshuffleSwitch(280),
   SpecialAction(281),
   VampDeathSummon(282),
   StopForceAtominfo(283),
   SoulGazeCriDamR(284),
   Affinity(285),
   PowerTransferGauge(286),
   AffinitySlug(287),
   Trinity(288),
   IncMaxDamage(289),
   BossShield(290),
   MobZoneState(291),
   GiveMeHeal(292),
   TouchMe(293),
   Contagion(294),
   ComboUnlimited(295),
   SoulExalt(296),
   IgnorePCounter(297),
   IgnoreAllCounter(298),
   IgnorePImmune(299),
   IgnoreAllImmune(300),
   UnkBuffStat6(301),
   FireAura(302),
   VengeanceOfAngel(303),
   HeavensDoor(304),
   Preparation(305),
   BullsEye(306),
   IncEffectHPPotion(307),
   IncEffectMPPotion(308),
   BleedingToxin(309),
   IgnoreMobDamR(310),
   Asura(311),
   MegaSmasher(312),
   FlipTheCoin(313),
   UnityOfPower(314),
   Stimulate(315),
   ReturnTeleport(316),
   DropRIncrease(317),
   IgnoreMobPdpR(318),
   BdR(319),
   CapDebuff(320),
   Exceed(321),
   DiabloicRecovery(322),
   FinalAttackProp(323),
   ExceedOverload(324),
   OverloadCount(325),
   Buckshot(326),
   FireBomb(327),
   HalfstatByDebuff(328),
   SurplusSupply(329),
   SetBaseDamage(330),
   EvaR(331),
   NewFlying(332),
   AmaranthGenerator(333),
   OnCapsule(334),
   CygnusElementSkill(335),
   StrikerHyperElectric(336),
   EventPointAbsorb(337),
   EventAssemble(338),
   StormBringer(339),
   AccR(340),
   DexR(341),
   Translucence(342),
   PoseType(343),
   CosmicForge(344),
   ElementSoul(345),
   CosmicOrb(346),
   GlimmeringTime(347),
   SolunaTime(348),
   WindWalk(349),
   SoulMP(350),
   FullSoulMP(351),
   SoulSkillDamageUp(352),
   ElementalCharge(353),
   Listonation(354),
   CrossOverChain(355),
   ChargeBuff(356),
   ReincarnationFull(357),
   Reincarnation(358),
   ReincarnationAccept(359),
   ChillingStep(360),
   DotBasedBuff(361),
   BlessingAnsanble(362),
   ComboCostInc(363),
   NaviFlying(364),
   QuiverCatridge(365),
   AdvancedQuiver(366),
   ImmuneBarrier(367),
   ArmorPiercing(368),
   CardinalMark(369),
   QuickDraw(370),
   BowMasterConcentration(371),
   TimeFastABuff(372),
   TimeFastBBuff(373),
   GatherDropR(374),
   AimBox2D(375),
   TrueSniping(376),
   DebuffTolerance(377),
   UnkBuffStat8(378),
   DotHealHPPerSecond(379),
   DotHealMPPerSecond(380),
   SpiritGuard(381),
   PreReviveOnce(382),
   SetBaseDamageByBuff(383),
   LimitMP(384),
   ReflectDamR(385),
   ComboTempest(386),
   MHPCutR(387),
   MMPCutR(388),
   SelfWeakness(389),
   ElementDarkness(390),
   FlareTrick(391),
   Ember(392),
   Dominion(393),
   SiphonVitality(394),
   DarknessAscension(395),
   BossWaitingLinesBuff(396),
   DamageReduce(397),
   ShadowServant(398),
   ShadowIllusion(399),
   KnockBack(400),
   IgnisRore(401),
   ComplusionSlant(402),
   JaguarSummoned(403),
   JaguarCount(404),
   SSFShootingAttack(405),
   DevilCry(406),
   ShieldAttack(407),
   DarkLighting(408),
   AttackCountX(409),
   BMageDeath(410),
   BombTime(411),
   NoDebuff(412),
   BattlePvP_Mike_Shield(413),
   BattlePvP_Mike_Bugle(414),
   AegisSystem(415),
   SoulSeekerExpert(416),
   HiddenPossession(417),
   ShadowBatt(418),
   MarkofNightLord(419),
   WizardIgnite(420),
   FireBarrier(421),
   ChangeFoxMan(422),
   HolyUnity(423),
   DemonFrenzy(424),
   ShadowSpear(425),
   DemonDamageAbsorbShield(426),
   Ellision(427),
   QuiverFullBurst(428),
   LuminousPerfusion(429),
   WildGrenadier(430),
   GrandCross(432),
   BattlePvP_Helena_Mark(433),
   BattlePvP_Helena_WindSpirit(434),
   BattlePvP_LangE_Protection(435),
   BattlePvP_LeeMalNyun_ScaleUp(436),
   BattlePvP_Revive(437),
   PinkbeanAttackBuff(438),
   PinkbeanRelax(439),
   PinkbeanRollingGrade(440),
   PinkbeanYoYoStack(442),
   RandAreaAttack(443),
   NextAttackEnhance(444),
   BeyondNextAttackProb(445),
   AranCombotempastOption(446),
   NautilusFinalAttack(447),
   ViperTimeLeap(448),
   RoyalGuardState(449),
   RoyalGuardPrepare(450),
   MichaelSoulLink(451),
   MichaelProtectofLight(452),
   TryflingWarm(453),
   AddRange(454),
   KinesisPsychicPoint(455),
   KinesisPsychicOver(456),
   KinesisIncMastery(457),
   KinesisPsychicEnergeShield(458),
   BladeStance(459),
   DebuffActiveHp(460),
   DebuffIncHp(461),
   MortalBlow(462),
   SoulResonance(463),
   Fever(464),
   SikSin(465),
   TeleportMasteryRange(466),
   FixCooltime(467),
   IncMobRateDummy(468),
   AdrenalinBoost(469),
   AranDrain(470),
   AranBoostEndHunt(471),
   HiddenHyperLinkMaximization(472),
   RWCylinder(473),
   RWCombination(474),
   RWUnk(475),
   RwMagnumBlow(476),
   RwBarrier(477),
   RWBarrierHeal(478),
   RWMaximizeCannon(479),
   RWOverHeat(480),
   UsingScouter(481),
   RWMovingEvar(482),
   Stigma(483),
   InstallMaha(484),
   CooldownHeavensDoor(485),
   CooldownRune(486),
   PinPointRocket(487),
   Transform(488),
   EnergyBurst(489),
   Striker1st(490),
   BulletParty(491),
   SelectDice(492),
   Pray(493),
   ChainArtsFury(494),
   DamageDecreaseWithHP(495),
   PinkbeanYoYoAttackStack(496),
   AuraWeapon(497),
   OverloadMana(498),
   RhoAias(499),
   PsychicTornado(500),
   SpreadThrow(501),
   HowlingGale(502),
   VMatrixStackBuff(503),
   MiniCannonBall(504),
   ShadowAssult(505),
   MultipleOption(506),
   UnkBuffStat15(507),
   BlitzShield(508),
   SplitArrow(509),
   FreudsProtection(510),
   Overload(511),
   Spotlight(512),
   KawoongDebuff(513),
   WeaponVariety(514),
   GloryWing(515),
   ShadowerDebuff(516),
   OverDrive(517),
   Etherealform(518),
   ReadyToDie(519),
   Oblivion(520),
   CriticalReinForce(521),
   CurseOfCreation(522),
   CurseOfDestruction(523),
   BlackMageDebuff(524),
   BodyOfSteal(525),
   PapulCuss(526),
   PapulBomb(527),
   HarmonyLink(528),
   FastCharge(529),
   UnkBuffStat20(530),
   CrystalBattery(531),
   Deus(532),
   UnkBuffStat21(533),
   BattlePvP_Rude_Stack(534),
   UnkBuffStat23(535),
   UnkBuffStat24(536),
   UnkBuffStat25(537),
   SpectorGauge(538),
   SpectorTransForm(539),
   PlainBuff(540),
   ScarletBuff(541),
   GustBuff(542),
   AbyssBuff(543),
   ComingDeath(544),
   FightJazz(545),
   ChargeSpellAmplification(546),
   InfinitySpell(547),
   MagicCircuitFullDrive(548),
   LinkOfArk(549),
   MemoryOfSource(550),
   UnkBuffStat26(551),
   WillPoison(552),
   UnkBuffStat27(553),
   UnkBuffStat28(554),
   CooltimeHolyMagicShell(555),
   Striker3rd(556),
   ComboInstict(557),
   ResonateUltimatum(558),
   WindWall(559),
   UnkBuffStat29(560),
   SwordOfSoulLight(561),
   MarkOfPhantomStack(562),
   MarkOfPhantomDebuff(563),
   UnkBuffStat30(565),
   UnkBuffStat31(566),
   UnkBuffStat32(567),
   UnkBuffStat33(568),
   UnkBuffStat34(569),
   EventSpecialSkill(570),
   PmdReduce(571),
   ForbidOpPotion(572),
   ForbidEquipChange(573),
   YalBuff(573),
   IonBuff(574),
   UnkBuffStat35(575),
   DefUp(576),
   Protective(577),
   BloodFist(578),
   BattlePvP_Wonky_ChargeA(579),
   UNK_580(580),
   BattlePvP_Wonky_Charge(582),
   BattlePvP_Wonky_Awesome(581),
   UnkBuffStat42(582),
   UnkBuffStat43(583),
   UnkBuffStat44(584),
   UNK_585(585),
   UNK_586(586),
   Bless5th(588),
   Bless5th2(589),
   PinkBeanMatroCyca(590),
   UnkBuffStat46(591),
   UnkBuffStat47(592),
   UnkBuffStat48(593),
   UnkBuffStat49(594),
   UnkBuffStat50(595),
   PapyrusOfLuck(596),
   HoyoungThirdProperty(597),
   TidalForce(598),
   Alterego(599),
   AltergoReinforce(600),
   ButterflyDream(601),
   Sungi(602),
   SageWrathOfGods(603),
   EmpiricalKnowledge(604),
   UnkBuffStat52(605),
   UnkBuffStat53(606),
   Graffiti(607),
   DreamDowon(608),
   WillofSwordStrike(609),
   WillofSword(610),
   AdelGauge(611),
   Creation(612),
   Dike(613),
   Wonder(614),
   Restore(615),
   Novility(616),
   AdelResonance(617),
   RuneOfPure(618),
   RuneOfTransition(619),
   DuskDarkness(620),
   YellowAura(621),
   DrainAura(622),
   BlueAura(623),
   DarkAura(624),
   DebuffAura(625),
   UnionAura(626),
   IceAura(627),
   KnightsAura(628),
   ZeroAuraStr(629),
   IncarnationAura(630),
   AdventOfGods(631),
   Revenant(632),
   RevenantDamage(633),
   SilhouetteMirage(634),
   BlizzardTempest(635),
   PhotonRay(636),
   AbyssalLightning(637),
   Striker4th(638),
   RoyalKnights(639),
   SalamanderMischief(640),
   LawOfGravity(641),
   RepeatingCrossbowCatridge(642),
   CrystalGate(643),
   ThrowBlasting(644),
   SageElementalClone(645),
   DarknessAura(646),
   WeaponVarietyFinale(647),
   LiberationOrb(648),
   LiberationOrbActive(649),
   EgoWeapon(650),
   RelikUnboundDischarge(651),
   MoraleBoost(652),
   AfterImageShock(653),
   Malice(654),
   Possession(655),
   DeathBlessing(656),
   ThanatosDescent(657),
   RemainIncense(658),
   GripOfAgony(659),
   DragonPang(660),
   SerenDebuffs(661),
   SerenDebuff(662),
   SerenDebuffUnk(663),
   PriorPryperation(664),
   AdrenalinBoostActive(671),
   UNK_672(672),
   YetiAnger(673),
   YetiAngerMode(674),
   YetiSpicy(675),
   YetiFriendsPePe(676),
   PinkBeanMagicShow(677),
   용맥_읽기(679),
   산의씨앗(680),
   산_무등(681),
   흡수_강(682),
   흡수_바람(683),
   흡수_해(684),
   자유로운용맥(685),
   Lotus(687),
   NatureFriend(688),
   SeaSerpent(691),
   SerpentStone(692),
   SerpentScrew(693),
   Cosmos(694),
   UNK_696(696),
   UNK_698(698),
   HolyWater(699),
   Triumph(700),
   FlashMirage(701),
   HolyBlood(702),
   OrbitalExplosion(703),
   PhoenixDrive(704),
   UNK_705(705),
   UNK_706(706),
   ElementalKnight(708),
   EquilibriumLiberation(709),
   SummonChakri(710),
   VoidBurst(712),
   EnergyCharged(714),
   DashJump(715),
   DashSpeed(716),
   RideVehicle(717),
   PartyBooster(718),
   GuidedBullet(719),
   Undead(720),
   RideVehicleExpire(721),
   RelikGauge(722),
   Grave(723),
   CountPlus1(724);

   private static final long serialVersionUID = 0L;
   private int buffstat;
   private int first;
   private boolean stacked = false;
   private int disease;
   private int flag;
   private int x;
   private int y;

   private SecondaryStat(int flag) {
      this.buffstat = 1 << 31 - flag % 32;
      this.setFirst(31 - (byte)((int)Math.floor((double)(flag / 32))));
      this.setStacked(this.name().startsWith("Indie") || this.name().startsWith("Pyramid"));
      this.setFlag(flag);
   }

   private SecondaryStat(int flag, int disease) {
      this.buffstat = 1 << 31 - flag % 32;
      this.setFirst(31 - (byte)((int)Math.floor((double)(flag / 32))));
      this.setStacked(this.name().startsWith("Indie") || this.name().startsWith("Pyramid"));
      this.setFlag(flag);
      this.disease = disease;
   }

   private SecondaryStat(int flag, int first, int disease) {
      this.buffstat = 1 << 31 - flag % 32;
      this.setFirst(first);
      this.setFlag(flag);
      this.disease = disease;
   }

   public final int getPosition() {
      return this.getFirst();
   }

   public final int getPosition(boolean stacked) {
      if (!stacked) {
         return this.getFirst();
      } else {
         switch(this.getFirst()) {
         case 0:
            return 16;
         case 1:
            return 15;
         case 2:
            return 14;
         case 3:
            return 13;
         case 4:
            return 12;
         case 5:
            return 11;
         case 6:
            return 10;
         case 7:
            return 9;
         case 8:
            return 8;
         case 9:
            return 7;
         case 10:
            return 6;
         case 11:
            return 5;
         case 12:
            return 4;
         case 13:
            return 3;
         case 14:
            return 2;
         case 15:
            return 1;
         case 16:
            return 0;
         default:
            return 0;
         }
      }
   }

   public final int getValue() {
      return this.getBuffstat();
   }

   public final boolean canStack() {
      return this.isStacked();
   }

   public int getDisease() {
      return this.disease;
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public static final SecondaryStat getByFlag(int flag) {
      SecondaryStat[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SecondaryStat d = var1[var3];
         if (d.getFlag() == flag) {
            return d;
         }
      }

      return null;
   }

   public static final SecondaryStat getBySkill(int skill) {
      SecondaryStat[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SecondaryStat d = var1[var3];
         if (d.getDisease() == skill) {
            return d;
         }
      }

      return null;
   }

   public static final List<SecondaryStat> getUnkBuffStats() {
      List<SecondaryStat> stats = new ArrayList();
      SecondaryStat[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SecondaryStat d = var1[var3];
         if (d.name().startsWith("UnkBuff")) {
            stats.add(d);
         }
      }

      return stats;
   }

   public static final SecondaryStat getRandom() {
      SecondaryStat dis = null;

      while(true) {
         SecondaryStat[] values = values();
         int length = values.length;

         for(int i = 0; i < length; ++i) {
            dis = values[i];
            if (Randomizer.nextInt(values().length) == 0) {
               return dis;
            }
         }
      }
   }

   public static boolean isEncode4Byte(Map<SecondaryStat, Pair<Integer, Integer>> statups) {
      SecondaryStat[] array = new SecondaryStat[]{CarnivalDefence, SpiritLink, DojangLuckyBonus, SoulGazeCriDamR, PowerTransferGauge, ReturnTeleport, ShadowPartner, SetBaseDamage, QuiverCatridge, ImmuneBarrier, NaviFlying, Dance, DotHealHPPerSecond, SetBaseDamageByBuff, MagnetArea, MegaSmasher, RwBarrier, VampDeath, RideVehicle, RideVehicleExpire, Protective, BlitzShield, UnkBuffStat2, HolyUnity, BattlePvP_Rude_Stack};
      SecondaryStat[] var3 = array;
      int var4 = array.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         SecondaryStat stat = var3[var5];
         if (statups.containsKey(stat)) {
            return true;
         }
      }

      return false;
   }

   public boolean isSpecialBuff() {
      switch(this) {
      case EnergyCharged:
      case DashSpeed:
      case DashJump:
      case RideVehicle:
      case PartyBooster:
      case GuidedBullet:
      case Undead:
      case RideVehicleExpire:
      case RelikGauge:
      case Grave:
         return true;
      default:
         return false;
      }
   }

   public int getFlag() {
      return this.flag;
   }

   public void setFlag(int flag) {
      this.flag = flag;
   }

   public boolean isItemEffect() {
      switch(this) {
      case DropItemRate:
      case ItemUpByItem:
      case MesoUpByItem:
      case ExpBuffRate:
      case WealthOfUnion:
      case LuckOfUnion:
         return true;
      default:
         return false;
      }
   }

   public boolean SpectorEffect() {
      switch(this) {
      case SpectorGauge:
      case SpectorTransForm:
      case PlainBuff:
      case ScarletBuff:
      case GustBuff:
      case AbyssBuff:
         return true;
      default:
         return false;
      }
   }

   public int getBuffstat() {
      return this.buffstat;
   }

   public void setBuffstat(int buffstat) {
      this.buffstat = buffstat;
   }

   public int getFirst() {
      return this.first;
   }

   public void setFirst(int first) {
      this.first = first;
   }

   public boolean isStacked() {
      return this.stacked;
   }

   public void setStacked(boolean stacked) {
      this.stacked = stacked;
   }
}
