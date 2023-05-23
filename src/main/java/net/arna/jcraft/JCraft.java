package net.arna.jcraft;

import net.arna.jcraft.command.ModCommandRegister;
import net.arna.jcraft.effects.ModStatusRegister;
import net.arna.jcraft.entity.*;
import net.arna.jcraft.item.ModItemRegister;
import net.arna.jcraft.registry.ModBlockRegister;
import net.arna.jcraft.registry.ModEntityRegister;
import net.arna.jcraft.registry.ModSoundRegister;
import net.arna.jcraft.spec.JCraftSpec;
import net.arna.jcraft.util.*;
import net.arna.jcraft.world.dimension.ModDimensionRegister;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.example.GeckoLibMod;
import software.bernie.geckolib3.GeckoLib;

import java.util.*;

import static net.arna.jcraft.entity.StandEntity.Stun;
import static net.arna.jcraft.util.JCraftUtils.activeTimestops;

//initialize your FUCKING variables, arna
//todo: add static IDs for stuff like stands and attacks to make checks for individual ones less expensive
//todo: add working out
public class JCraft implements ModInitializer {

	// Unchanging mod values
	public static final String MOD_ID = "jcraft";
	public static final int STAND_COUNT = 11;
	public static final int EVOLUTION_COUNT = 5;
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Identifier standControlChannel = new Identifier(MOD_ID, "scchannel");
	public static final Identifier serverFeedbackChannel = new Identifier(MOD_ID, "sfchannel");

    public static final DefaultParticleType COMBO_BREAK = FabricParticleTypes.simple();
	public static final DefaultParticleType COOLDOWN_CANCEL = FabricParticleTypes.simple();
	public static final DefaultParticleType HITSPARK_1 = FabricParticleTypes.simple();
	public static final DefaultParticleType HITSPARK_2 = FabricParticleTypes.simple();
	public static final DefaultParticleType KCPARTICLE = FabricParticleTypes.simple();

	private static final int SPEC_QUEUE_MOVESTUN_LIMIT = 11; // exclusive, 10 -> 0.5s window for queueing moves
	private static final int QUEUE_MOVESTUN_LIMIT = 7; // exclusive, 6 -> 0.3s window for queueing moves

	// Stand Cooldowns
	public static final String standLightCD = "SLightCD";
	public static final String standHeavyCD = "SHeavyCD";
	public static final String standBarrageCD = "SBarrageCD";
	public static final String standS1CD = "SS1CD";
	public static final String standS2CD = "SS2CD";
	public static final String standS3CD = "SS3CD";
	public static final String standUltCD = "SUltCD";

	// Spec Cooldowns
	public static final String heavyCD = "HeavyCD";
	public static final String barrageCD = "BarrageCD";
	public static final String s1CD = "S1CD";
	public static final String s2CD = "S2CD";
	public static final String s3CD = "S3CD";
	public static final String ultCD = "UltCD";

	// Universal Cooldowns
	public static final String standMMBCD = "M3CD";
	public static final String standCBCD = "CBCD";
	public static final String standCCCD = "CCCD";

	public static List<String> cooldowns = List.of(
			standLightCD, standHeavyCD, standBarrageCD, standUltCD, standS1CD, standS2CD, standS3CD,
			standMMBCD, standCBCD, standCCCD,
			heavyCD, barrageCD, ultCD, s1CD, s2CD, s3CD);

	// Gamerules
	public static GameRules.Key<GameRules.BooleanRule> SHOW_HITBOXES = GameRuleRegistry.register("showHitboxes", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
	public static GameRules.Key<GameRules.BooleanRule> KINGCRIMSON_TELEPORT_EFFECT = GameRuleRegistry.register("kingCrimsonTeleportEffect", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
	public static GameRules.Key<GameRules.BooleanRule> COMBO_COUNTER = GameRuleRegistry.register("comboCounter", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	public static GameRules.Key<GameRules.IntRule> CHANCE_MOB_SPAWNS_WITH_STAND = GameRuleRegistry.register("chanceMobSpawnsWithStand", GameRules.Category.MOBS, GameRuleFactory.createIntRule(5, 0,100));
	public static GameRules.Key<GameRules.BooleanRule> ALLOW_MOB_EVOLVED_STANDS = GameRuleRegistry.register("allowMobEvolvedStands", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
	public static GameRules.Key<GameRules.BooleanRule> CREAM_BLACK_HOLE_ERASES_BLOCKS = GameRuleRegistry.register("creamBlackHoleErasesBlocks", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	public static GameRules.Key<GameRules.IntRule> DEFAULT_SPEC = GameRuleRegistry.register("defaultSpec", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 1));
	//public static GameRules.Key<GameRules.IntRule> DAMAGE_MULT = GameRuleRegistry.register("jcraftDamageMult", GameRules.Category.MISC, GameRuleFactory.createIntRule(0, 0, 100));

	// Dimensional travel bullshit
	public static ArrayList<DimValues> pastDimensions = new ArrayList<>();
	private static List<ChunkPos> preloadedChunks = new ArrayList<>();
	private static void ClearPreloadedChunks(ServerWorld auWorld) {
		if (preloadedChunks.isEmpty()) { return; }
		for (ChunkPos p : preloadedChunks)
			auWorld.setChunkForced(p.x, p.z, false);
		preloadedChunks.clear();
	}
	public static void PreloadChunk(ServerWorld auWorld, int chunkX, int chunkY) {
		preloadedChunks.add(new ChunkPos(chunkX, chunkY));
		auWorld.setChunkForced(chunkX, chunkY, true);
	}

	// Item group
	public static final ItemGroup JCRAFT_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "main"))
			.icon(() -> new ItemStack(ModItemRegister.STANDARROW))
			.appendItems((itemStacks -> {
				itemStacks.add(new ItemStack(ModItemRegister.STANDDISC));

				itemStacks.add(new ItemStack(ModItemRegister.STANDARROW));
				itemStacks.add(new ItemStack(ModItemRegister.LIVINGARROW));
				itemStacks.add(new ItemStack(ModItemRegister.REQUIEMARROW));
				itemStacks.add(new ItemStack(ModItemRegister.REQUIEMRUBY));

				itemStacks.add(new ItemStack(ModItemRegister.ANUBIS));
				itemStacks.add(new ItemStack(ModItemRegister.ANUBISSHEATHED));
				itemStacks.add(new ItemStack(ModItemRegister.KNIFE));
				itemStacks.add(new ItemStack(ModItemRegister.KNIFEBUNDLE));
				itemStacks.add(ModItemRegister.FVREVOLVER.getDefaultStack());

				itemStacks.add(new ItemStack(ModItemRegister.SINNERSSOUL));
				itemStacks.add(new ItemStack(ModBlockRegister.SOUL_BLOCK));
				itemStacks.add(new ItemStack(ModItemRegister.GREENBABY));
				itemStacks.add(new ItemStack(ModItemRegister.DIOSDIARY));

				itemStacks.add(new ItemStack(ModItemRegister.BOXINGGLOVES));

				itemStacks.add(new ItemStack(ModItemRegister.DIOHEADBAND));
				itemStacks.add(new ItemStack(ModItemRegister.DIOJACKET));
				itemStacks.add(new ItemStack(ModItemRegister.DIOPANTS));
				itemStacks.add(new ItemStack(ModItemRegister.DIOBOOTS));

				itemStacks.add(new ItemStack(ModItemRegister.JOTAROCAP));
				itemStacks.add(new ItemStack(ModItemRegister.JOTAROJACKET));
				itemStacks.add(new ItemStack(ModItemRegister.JOTAROPANTS));
				itemStacks.add(new ItemStack(ModItemRegister.JOTAROBOOTS));

				itemStacks.add(new ItemStack(ModItemRegister.KQCOIN));
			}))
			.build();

	// Stand names
	public static final Map<Integer, MutableText> standNames = Map.ofEntries(
			Map.entry(1, Text.translatable("entity.jcraft.starplatinum")),
			Map.entry(2, Text.translatable("entity.jcraft.theworld")),
			Map.entry(3, Text.translatable("entity.jcraft.kingcrimson")),
			Map.entry(4, Text.translatable("entity.jcraft.d4c")),
			Map.entry(5, Text.translatable("entity.jcraft.cream")),
			Map.entry(6, Text.translatable("entity.jcraft.killerqueen")),
			Map.entry(7, Text.translatable("entity.jcraft.whitesnake")),
			Map.entry(8, Text.translatable("entity.jcraft.silverchariot")),
			Map.entry(9, Text.translatable("entity.jcraft.mr")),
			Map.entry(10, Text.translatable("entity.jcraft.thefool")),
			Map.entry(11, Text.translatable("entity.jcraft.goldenexperience")),

			Map.entry(-1, Text.translatable("entity.jcraft.cmoon")),
			Map.entry(-2, Text.translatable("entity.jcraft.mih")),
			Map.entry(-3, Text.translatable("entity.jcraft.twoh")),
			Map.entry(-4, Text.translatable("entity.jcraft.kqbtd")),
			Map.entry(-5, Text.translatable("entity.jcraft.ger"))
	);

	// Buttons to IDs and vice versa
	public static final Map<Integer, AttackQueue> idToButton = Map.ofEntries(
			Map.entry(0, AttackQueue.LIGHT),
			Map.entry(1, AttackQueue.HEAVY),
			Map.entry(2, AttackQueue.BARRAGE),
			Map.entry(3, AttackQueue.SPECIAL1),
			Map.entry(4, AttackQueue.ULTIMATE),
			Map.entry(5, AttackQueue.SPECIAL2),
			Map.entry(6, AttackQueue.SPECIAL3),
			Map.entry(7, AttackQueue.MIDDLEMOUSE)
	);

	public static final Map<AttackQueue, Integer> buttonToId = Map.ofEntries(
			Map.entry(AttackQueue.LIGHT, 0),
			Map.entry(AttackQueue.HEAVY, 1),
			Map.entry(AttackQueue.BARRAGE, 2),
			Map.entry(AttackQueue.SPECIAL1, 3),
			Map.entry(AttackQueue.ULTIMATE, 4),
			Map.entry(AttackQueue.SPECIAL2, 5),
			Map.entry(AttackQueue.SPECIAL3, 6),
			Map.entry(AttackQueue.MIDDLEMOUSE, 7)
	);

	public static StandEntity Summon(ServerWorld world, LivingEntity player) {
		if (player.hasStatusEffect(ModStatusRegister.Standless)) { return null; }

		StandEntity stand = null;

		//CMoonEntity(ModEntityRegister.MIH, world) works and i don't like that :(
		switch ( ((IEntityDataSaver)player).getPersistentData().getInt("StandID") ) {
			case 1 -> stand = new StarPlatinumEntity(ModEntityRegister.STARPLATINUM, world);
			case 2 -> stand = new TheWorldEntity(ModEntityRegister.THEWORLD, world);
			case 3 -> stand = new KingCrimsonEntity(ModEntityRegister.KINGCRIMSON, world);
			case 4 -> stand = new D4CEntity(ModEntityRegister.D4C, world);
			case 5 -> stand = new CreamEntity(ModEntityRegister.CREAM, world);
			case 6 -> stand = new KillerQueenEntity(ModEntityRegister.KILLERQUEEN, world);
			case 7 -> stand = new WhitesnakeEntity(ModEntityRegister.WHITESNAKE, world);
			case 8 -> stand = new SilverChariotEntity(ModEntityRegister.SILVERCHARIOT, world);
			case 9 -> stand = new MagiciansRedEntity(ModEntityRegister.MAGICIANSRED, world);
			case 10 -> stand = new TheFoolEntity(ModEntityRegister.THEFOOL, world);
			case 11 -> stand = new GoldenExperienceEntity(ModEntityRegister.GOLDENEXPERIENCE, world);
			// All evolutions have a negative ID
			case -1 -> stand = new CMoonEntity(ModEntityRegister.CMOON, world);
			case -2 -> stand = new MadeInHeavenEntity(ModEntityRegister.MIH, world);
			case -3 -> stand = new TheWorldOverHeavenEntity(ModEntityRegister.TWOH, world);
			case -4 -> stand = new KQBTDEntity(ModEntityRegister.KQBTD, world);
			case -5 -> stand = new GEREntity(ModEntityRegister.GER, world);
		}

		if (stand != null) {
			stand.setPosition(player.getPos().subtract(player.getRotationVector()));
			stand.startRiding(player);
			stand.setUser(player);
			world.spawnEntity(stand);
			return stand;
		}

		return null;
	}

	@Override
	public void onInitialize() {
		// Particle registration (serverside)
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "combo_break"), COMBO_BREAK);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "cooldown_cancel"), COOLDOWN_CANCEL);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "hitspark_1"), HITSPARK_1);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "hitspark_2"), HITSPARK_2);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, "kcparticle"), KCPARTICLE);

		// Geckolib
		GeckoLibMod.DISABLE_IN_DEV = true;
		GeckoLib.initialize();
		// Registration
		ModItemRegister.RegisterModItems();
		ModCommandRegister.registerCommands();
		ModEventsRegister.registerEvents();
		ModStatusRegister.registerStatuses();
		ModSoundRegister.registerSounds();
		ModEntityRegister.registerEntities();
		ModBlockRegister.registerBlocks();
		ModDimensionRegister.registerDimensions();

		ServerLivingEntityEvents.AFTER_DEATH.register(
				(living, source) -> {
					if (living instanceof ServerPlayerEntity player) { ((IEntityDataSaver)player).getPersistentData().putInt(standCBCD, 0); }
				}
		);

		ServerTickEvents.END_SERVER_TICK.register(server -> {

			// Player logic (cooldown handling and DamageTimer counting)
			for (ServerPlayerEntity player : PlayerLookup.all(server)) {
				if (player == null) { continue; }
				if (player.isAlive()) {
					IEntityDataSaver user = (IEntityDataSaver) player;
					NbtCompound userData = user.getPersistentData();
					if (player.getAttacker() != null) { userData.putInt("DamageTimer", 600); }

					// Damage timer
					if (userData.contains("DamageTimer")) { userData.putInt("DamageTimer", userData.getInt("DamageTimer") - 1); }

					// Handle cooldowns
					int i = 0;
					for (String cooldownType : cooldowns) {
						i++;
						if (!userData.contains(cooldownType)) { userData.putInt(cooldownType, 0); }

						int reducedCd = userData.getInt(cooldownType) - 1;
						userData.putInt(cooldownType, reducedCd);

						if (reducedCd % 2 == 0 || reducedCd < 1) {
							PacketByteBuf buf = PacketByteBufs.create();
							buf.writeShort(3);
							buf.writeInt(i);
							buf.writeDouble(MathHelper.clamp(reducedCd / 20.0, 0.0, 10000.0));
							ServerPlayNetworking.send(player, serverFeedbackChannel, buf);
						}
					}
				}
			}

			// Keeping track of dimhops
			Iterator<DimValues> iterator = pastDimensions.iterator();
			ArrayList<DimValues> newPastDimensions = new ArrayList<>();

			while (iterator.hasNext()) {
				DimValues dimValues = iterator.next();
				Entity user = dimValues.user;
				if (user == null)
					continue;

				ServerWorld au = (ServerWorld) user.getWorld();
				ServerWorld original = server.getWorld(dimValues.worldKey);
				if (au == original)
					continue;

				dimValues.timer--;
				if (dimValues.timer > 1) {
					newPastDimensions.add(dimValues);
					continue;
				}

				Vec3d dimPos = dimValues.pos;
				if (user instanceof ServerPlayerEntity player) {
					player.teleport(original, dimPos.x, dimPos.y, dimPos.z, player.getYaw(), player.getPitch());
				} else {
					teleportToWorld(user, original, dimPos.x, dimPos.y, dimPos.z);
				}
				ClearPreloadedChunks(au); //this can probably be optimized
			}

			pastDimensions = newPastDimensions;

			// Keeping track of timestops
			Iterator<DimValues> tsIter = activeTimestops.iterator();

			while (tsIter.hasNext()) {
				DimValues dimValues = tsIter.next();
				if (dimValues.user instanceof StandEntity stand && dimValues.user.isAlive()) {
					if (stand.getTSTime() > 0) {
						continue;
					}
				}

				activeTimestops.remove(dimValues);
				break;
			}

			// Burst handling
			Iterator<Map.Entry<LivingEntity, Integer>> burstIter = burstTimers.entrySet().iterator();
			HashMap<LivingEntity, Integer> newBurstTimers = new HashMap<>();

			while (burstIter.hasNext()) {
				Map.Entry<LivingEntity, Integer> burst = burstIter.next();
				LivingEntity player = burst.getKey();
				burst.setValue(burst.getValue() - 1);
				int newVal = burst.getValue();

				List<Entity> filter = new ArrayList<>();
				filter.add(player);
				if (player.hasPassengers()) {
					filter.addAll(player.getPassengerList());
				}

				if (newVal > 0) {
					newBurstTimers.put(player, newVal);
				} else {
					player.removeStatusEffect(ModStatusRegister.Dazed);
					Stun(player, 10, 1);
					Vec3d pPos = player.getEyePos();
					List<? extends Entity> toPush = JCraftUtils.GenerateHitbox(player.world, pPos, 4, Entity.class, filter);

					for (Entity ent : toPush) {
						Vec3d awayVector = ent.getPos().subtract(pPos).normalize();
						boolean pushAway = true;

						// If the stand was hit, the attack will stop and the user will be hit remotely
						if (ent instanceof StandEntity stand) {
							if (stand.hasUser()) {
								Stun(stand.getUser(), 10, 3);
								stand.CancelAttack();
							}
						} else if (ent.getFirstPassenger() instanceof StandEntity stand) { // Stands should not have passengers
							if (stand.blocking) {
								pushAway = false;
							} else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
								//awayVector = awayVector.multiply(0.5);
								Stun(living, 10, 3);
								stand.CancelAttack();
							}
						}

						if (pushAway) {
							ent.setVelocity(awayVector.x, awayVector.y / 5 + 0.4, awayVector.z);
							ent.velocityModified = true;

							if (ent instanceof ServerPlayerEntity serverPlayer) {
								serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
							}
						}
					}
				}
			}

			burstTimers = newBurstTimers;

			for (ServerWorld serverWorld : server.getWorlds()) {
				// Mob stand control logic
				List<MobEntity> mobEntities = (List<MobEntity>) serverWorld.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), EntityPredicates.VALID_ENTITY);

				for (MobEntity mob : mobEntities) {
					IEntityDataSaver user = (IEntityDataSaver) mob;
					NbtCompound mobData = user.getPersistentData();

					if (mob.isAlive()) {
						if (mobData != null) {
							// Damage timer
							if (mob.getAttacker() != null) {
								mobData.putInt("DamageTimer", 600);
							}
							if (mobData.contains("DamageTimer")) {
								mobData.putInt("DamageTimer", mobData.getInt("DamageTimer") - 1);
							}

							if (!mob.isAiDisabled()) {
								// Target priority
								if (mob.getFirstPassenger() instanceof StandEntity stand) {
									LivingEntity biggestAttacker = mob.getDamageTracker().getBiggestAttacker();
									LivingEntity primeAdversary = mob.getPrimeAdversary();
									LivingEntity target = mob.getTarget();
									if (primeAdversary != null && primeAdversary.isAlive()) {
										stand.MobAI(mob, primeAdversary);
									} else if (target != null && target.isAlive()) {
										stand.MobAI(mob, target);
									} else if (biggestAttacker != null && biggestAttacker.isAlive()) {
										mob.setTarget(biggestAttacker);
									}
								} else if (mobData.contains("StandID")) {
									Summon(serverWorld, mob);
								}

								// Handle cooldowns
								for (String cooldownType : cooldowns) {
									if (!mobData.contains(cooldownType)) {
										mobData.putInt(cooldownType, 0);
									}

									int reducedCd = mobData.getInt(cooldownType) - 1;
									mobData.putInt(cooldownType, reducedCd);
								}
							}
						}
					}
				}

				// Item attaction logic
				List<ItemEntity> itemEntities = (List<ItemEntity>) serverWorld.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), EntityPredicates.VALID_ENTITY);

				for (ItemEntity item : itemEntities) {
					if (item.getStack().isOf(ModItemRegister.ANUBIS))
						item.setPickupDelay(0);

					if (item.getStack().isOf(ModItemRegister.FVREVOLVER)) {
						if (item.age < 10)
							item.setPickupDelay(100);
						Vec3d iPos = item.getPos();

						// Item attraction logic
						List<ItemEntity> nearbyItems = serverWorld.getEntitiesByClass(ItemEntity.class,
								new Box(iPos.add(16, 16, 16), iPos.subtract(16, 16, 16)),
								EntityPredicates.VALID_ENTITY);

						for (ItemEntity item2 : nearbyItems) {
							if (!item2.getStack().isOf(ModItemRegister.FVREVOLVER)) {
								continue;
							}

							Vec3d converge = item2.getPos().subtract(iPos);
							Vec3d towardsVector = converge.normalize().multiply(0.25);
							item.addVelocity(towardsVector.x, towardsVector.y, towardsVector.z);
							item.velocityModified = true;

							if (!item2.equals(item) && item2.distanceTo(item) < 1.0) {
								Explosion explosion = serverWorld.createExplosion(null, iPos.x, iPos.y, iPos.z, 1f,
										serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? Explosion.DestructionType.BREAK : Explosion.DestructionType.NONE);
								item.kill();
								item2.kill();

								List<LivingEntity> toDamage = serverWorld.getEntitiesByClass(LivingEntity.class,
										new Box(iPos.add(2, 2, 2), iPos.subtract(2, 2, 2))
										, EntityPredicates.VALID_ENTITY);

								for (LivingEntity ent : toDamage) {
									ent.damage(DamageSource.explosion(explosion), 10);
									ent.addStatusEffect(new StatusEffectInstance(ModStatusRegister.Knockdown, 30, 0));
								}
							}
						}
					}
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(standControlChannel, (server, player, handler, buf, responseSender) -> {
			short control = buf.readShort();

			//todo: reformat all this shit
			boolean rmb = false;
			if (control == 3) { rmb = buf.readBoolean(); }
			boolean finalRmb = rmb;

			UUID uuid = null;
			if (control == 12) { uuid = buf.readUuid(); }
			UUID finalUUID = uuid;

			int forward = 0;
			int side = 0;
			boolean finalJump;
			if (control == 0) { // W A S D
				if (buf.readBoolean())
					forward += 1;
				if (buf.readBoolean())
					side += 1;
				if (buf.readBoolean())
					forward -= 1;
				if (buf.readBoolean())
					side -= 1;
				finalJump = buf.readBoolean();
			} else {
				finalJump = false;
			}
			int finalForward = forward;
			int finalSide = side;

			ServerWorld world = server.getWorld(player.getEntityWorld().getRegistryKey());

			//System.out.println("Control recieved: " + control);
			//...You will get errors related to the ref count if you try to read data on either server or client thread
			server.execute(() -> {
				switch (control) {
					// 0 - MOVEMENT INPUT SYNC
					case 0 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							stand.UpdateRemoteInputs(finalForward, finalSide, finalJump);
						}
					}
					// 1 - STAND SUMMON & DESUMMON
					case 1 -> {
						PacketByteBuf buf2 = PacketByteBufs.create();
						buf2.writeShort(6);
						buf2.writeInt(0);
						ServerPlayNetworking.send(player, serverFeedbackChannel, buf2);

						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT) {
								stand.queuedAttack = AttackQueue.STANDSUMMON;
							} else {
								stand.Desummon();
							}
						} else if (world != null) {
							Summon(world, player);
						}
					}
					// 2 - LIGHT ATTACK
					case 2 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();
							stand.InitLightAttack();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.LIGHT;
							}
						}
					}
					// 3 - BLOCK
					case 3 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							boolean blocking = stand.blocking;
							if (!blocking && stand.CanAttack() && finalRmb) {
								if (player.getMainHandStack().getUseAction() == UseAction.NONE && player.getOffHandStack().getUseAction() == UseAction.NONE) {
									stand.blocking = true;
								}
							} else if (blocking && !finalRmb) {
								stand.blocking = false;
							}
						}
					}
					// 4 - HEAVY
					case 4 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitHeavyAttack();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.HEAVY;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitHeavyAttack(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.HEAVY;
							}
						}
					}
					// 5 - BARRAGE
					case 5 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitBarrage();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.BARRAGE;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitBarrage(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.BARRAGE;
							}
						}
					}
					// 6 - SPECIAL 1
					case 6 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitSpecial1();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.SPECIAL1;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitSpecial1(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.SPECIAL1;
							}
						}
					}
					// 7 - Ultimate
					case 7 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitUlt();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.ULTIMATE;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitUlt(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.ULTIMATE;
							}
						}
					}
					// 8 - SPECIAL 2
					case 8 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitSpecial2();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.SPECIAL2;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitSpecial2(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.SPECIAL2;
							}
						}
					}
					// 9 - SPECIAL 3
					case 9 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitSpecial3();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.SPECIAL3;
							}
							break;
						}
						JCraftSpec spec = JCraftUtils.getSpec(player);
						if (spec != null) {
							spec.InitSpecial3(world);
							if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
								spec.queuedAttack = AttackQueue.SPECIAL3;
							}
						}
					}
					// 10 - Middle Click Action (TSTP, Explosive dash, Gun, etc.)
					case 10 -> {
						if (player.getFirstPassenger() instanceof StandEntity stand) {
							int moveStun = stand.getMoveStun();

							stand.InitMiddleClick();
							if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking()) {
								stand.queuedAttack = AttackQueue.MIDDLEMOUSE;
							}
						}
						else { StandEntity stand2 = Summon(world, player); if (stand2 != null) { stand2.InitMiddleClick(); } }
					}
					// 11 - Combo Breaker
					case 11 -> {
						StatusEffectInstance stun = player.getStatusEffect(ModStatusRegister.Dazed);
						if (JCraftUtils.isBlocking(player)) { return; }
						if (stun != null) { ComboBreak(world, player, stun); }
					}
					// 12 - D4C Clone Thinning
					case 12 -> {
						if (world.getEntity(finalUUID) instanceof PlayerCloneEntity clone) {
							LivingEntity ownerReference = clone.getOwner();
							PlayerCloneEntity slimClone = clone.convertTo(ModEntityRegister.PLAYERCLONE_SLIM, true);
							slimClone.setOwner(ownerReference);

							clone.switched = true;
							clone.switchedTo = slimClone;
						}
					}
					// 13 - Cooldown Cancel
					case 13 -> {
						if (player.isCreative()) {
							for (String cooldownType : cooldowns) {
								((IEntityDataSaver) player).getPersistentData().putInt(cooldownType, 0);
							}
							break;
						}

						StatusEffectInstance stun = player.getStatusEffect(ModStatusRegister.Dazed);
						if (stun == null) {
							CooldownCancel(world, player);
						}
					}
				}
			});
		});
	}
	
	public static void CreateParticle(ServerWorld world, double x, double y, double z, int id) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeShort(8);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeInt(id); // Combo breaker particle ID

		for (ServerPlayerEntity serverPlayer : world.getPlayers()) {
			ServerPlayNetworking.send(serverPlayer, serverFeedbackChannel, buf);
		}
	}

	public static List<String> unresettableCooldowns = List.of(standBarrageCD, standUltCD, barrageCD, ultCD, standCBCD, standCCCD);
	public static void CooldownCancel(ServerWorld world, LivingEntity player) {
		NbtCompound data = ((IEntityDataSaver) player).getPersistentData();

		if (data.getInt(standCCCD) <= 0) {
			for (String cooldownType : cooldowns) {
				if (unresettableCooldowns.contains(cooldownType)) { continue; }
				data.putInt(cooldownType, 0);
			}

			data.putInt(standCCCD, 900); // 45s

			Vec3d pPos = player.getEyePos();
			world.playSoundFromEntity(null, player, ModSoundRegister.COOLDOWN_CANCEL, SoundCategory.PLAYERS, 1, 1);
			CreateParticle(world, pPos.x, pPos.y, pPos.z, 1);
		}
	}

	public static HashMap<LivingEntity, Integer> burstTimers = new HashMap<>();
	public static void ComboBreak(ServerWorld world, LivingEntity player, StatusEffectInstance stun) {
		NbtCompound data = ((IEntityDataSaver) player).getPersistentData();
		//if (!user.getPersistentData().contains(JCraft.standCBCD)) { user.getPersistentData().putInt(JCraft.standCBCD, 0); } // Handled elsewhere
		if (stun.getDuration() > 1 && stun.getAmplifier() == 1 && data.getInt(standCBCD) <= 0) {
			data.putInt(standCBCD, 1200); // 60s

			Stun(player, 5, 2); // Player is slowed down considerably pre-burst

			world.playSoundFromEntity(null, player, ModSoundRegister.COMBO_BREAK, SoundCategory.PLAYERS, 1, 1);

			Vec3d pPos = player.getEyePos();
			burstTimers.put(player, 4);
			CreateParticle(world, pPos.x, pPos.y, pPos.z, 0);
		}
	}

	public static Entity teleportToWorld(Entity e, ServerWorld w, double x, double y, double z) {
		if (!e.isRemoved()) {
			e.detach();
			Entity entity = e.getType().create(w);
			if (entity != null) {
				entity.copyFrom(e);
				entity.refreshPositionAndAngles(x, y, z, e.getYaw(), e.getPitch());
				entity.setVelocity(e.getVelocity());
				w.onDimensionChanged(entity);
				e.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
				w.resetIdleTimeout();
				return entity;
			}
		}
		return null;
	}

	public static void DimensionHop(Entity entity, int heightOffset) {
		ServerWorld original = (ServerWorld) entity.getWorld();
		MinecraftServer server = original.getServer();
		ServerWorld au = server.getWorld(ModDimensionRegister.AU_DIMENSION_KEY);
		if (original == au) { return; }

		Vec3d pos = entity.getPos();
		Entity finalEnt = entity;

		if (entity instanceof ServerPlayerEntity player) {
			player.teleport(au, pos.x, pos.y - heightOffset, pos.z, entity.getYaw(), entity.getPitch());
			//todo: fix ability to get stuck in hell
		} else {
			finalEnt = teleportToWorld(entity, au, entity.getX(), entity.getY() - heightOffset, entity.getZ());
		}

		pastDimensions.add(new DimValues(finalEnt, pos, original.getRegistryKey()));
		au.playSound(null, pos.x, pos.y - heightOffset, pos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}
}
