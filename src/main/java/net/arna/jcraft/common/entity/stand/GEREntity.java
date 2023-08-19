package net.arna.jcraft.common.entity.stand;

import lombok.Data;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.*;
import java.util.function.Consumer;

public class GEREntity extends StandEntity<GEREntity, GEREntity.State> {
    public static final Attack airlight = new Attack(1, JCraft.lightCooldown, 0.75f, 12, 5, 1.25, 4f, 0.75f, AttackType.BOX, 1, 0.33f, 0, JSoundRegistry.IMPACT_1)
            .appendHitbox(new HitBoxData(0, -1, 1))
            .setInfo("Downward Kick", "medium stun combo starter, low hitbox, low blockstun");
    public static final Attack airheavy = new Attack(3, 17, 1f, 24, 14, 1.5, 9f, 0.8f, AttackType.BOX, 2, 0.25f, 0, JSoundRegistry.IMPACT_1).setHitspark(2)
            .appendHitbox(new HitBoxData(0, -1, 1))
            .setInfo("Overhead Kick", "slow, high stun combo starter");
    public static final Attack airbarrage = Attack.barrageAttack(5, 14, 1f, 48, 0, 1.5, 1f, 0.3f, 1, 0, 3)
            .setInfo("Kick Barrage", "fast combo finisher, knocks back");
    // JCraft.lightCooldown -> 0 | 0.55f -> 0.4f
    public static final Attack light = new Attack(0, JCraft.lightCooldown / 2, 0.75f, 9, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.4f, -0.1f, 0, JSoundRegistry.IMPACT_1)
            .aerialVariation(airlight)
            .setInfo("Punch/Downward Kick", "quick combo starter");
    public static final Attack heavy = new Attack(2, 17, 1f, 19, 10, 1.5, 9f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.IMPACT_2)
            .aerialVariation(airheavy)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .setInfo("Overhead Smash", "slow, uninterruptable knockdown");
    public static final Attack barrage = Attack.barrageAttack(4, 14, 0.75f, 30, 0, 2, 1f, 0.25f, 1.5f, 0, 3)
            .aerialVariation(airbarrage)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack healself = new Attack(6, 26, 1f, 14, 10, 0, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand", "standing: heals user for 2 hearts, crouching: heals others for 3 hearts, pacifies angered mobs");
    public static final Attack heal = new Attack(7, 26, 1f, 16, 10, 1.25, 0f, 0f, AttackType.BOX)
            .setInfo("Healing Hand (Others)", "");
    public static final Attack chargelaser = new Attack(8, 24, 1.1f, 28, 18, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Life Beam (Charged)", "slower, poisoning variant");
    public static final Attack laser = new Attack(8, 24, 1f, 20, 10, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .crouchingVariation(chargelaser)
            .setInfo("Life Beam", "summons a quick, stunning rock projectile that turns into a scorpion a small time after landing");

    public static final Attack counter = new Attack(9, 26, 1f, 35, 5, 0, 0f, 0f, AttackType.COUNTER)
            .setInfo("Nullification", "0.25s windup, 1.5s counter, stuns on hit");
    public static final Attack rtz = new Attack(10, 60, 32, 30, 0, 1, AttackType.BOX)
            .setInfo("Return to Zero", "initial press: saves the state of every entity in a 4 chunk radius, second press: reverts all states except users\nDoesn't affect player inventories");
    private final HashMap<Entity, NbtCompound> rtzEntityData = new HashMap<>();

    @Data
    private static class ReturnData {
        Vec3d originalPos;
        Entity entity;

        public ReturnData(Vec3d originalPos, Entity entity) {
            this.originalPos = originalPos;
            this.entity = entity;
        }
    }

    private final ArrayList<ReturnData> returnInformation = new ArrayList<>();

    private static final TrackedData<Integer> FLIGHTTIME;

    public Integer getFlightTime() {
        return this.dataTracker.get(FLIGHTTIME);
    }

    public void setFlightTime(int i) {
        this.dataTracker.set(FLIGHTTIME, i);
    }


    public GEREntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE_REQUIEM, worldIn);

        idleRotation = 0f;

        description = "Impossible Ascended DEFENSE";

        pros = List.of(
                "very mobile",
                "wide toolkit",
                "excellent defense",
                "setplay/combo tool (life beam)",
                "undo button"
        );

        cons = List.of(
                "low damage output",
                "limited pressure"
        );

        freespace = """
                BNBs:
                the scorpy patty (sets up stand off transition)
                (M1>)Barrage>jump>Overhead Kick>Life Beam>M1>Life Beam (second hit)
                knockdown experience
                M1>Barrage>Life Beam>M1~Overhead Smash>Life Beam (second hit)""";

        moves = List.of(light, heavy, barrage, healself, rtz, laser, counter,
                new Attack().setInfo("Flight", "1 second").setMobility(MobilityType.FLIGHT));

        super.initialize();
    }

    static {
        FLIGHTTIME = DataTracker.registerData(GEREntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FLIGHTTIME, 0);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        CanAttackData data = canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isOnGround())
            handleMove(light, CooldownType.STAND_LIGHT, State.LIGHT);
        else handleMove(airlight, CooldownType.STAND_LIGHT, State.AIR_LIGHT);
    }

    @Override
    public void initHeavyAttack() {
        CanAttackData data = canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isOnGround()) {
            if (handleMove(heavy, CooldownType.STAND_HEAVY, State.HEAVY))
                playSound(JSoundRegistry.GER_HEAVY, 1, 1);
        } else {
            if (handleMove(airheavy, CooldownType.STAND_HEAVY, State.AIR_HEAVY))
                playSound(JSoundRegistry.GER_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        CanAttackData data = canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isOnGround() && handleMove(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.GE_BARRAGE, 1, 1);
        else if (handleMove(airbarrage, CooldownType.STAND_BARRAGE, State.AIR_BARRAGE))
            playSound(JSoundRegistry.GER_KICKBARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        CanAttackData data = canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isSneaking()) {
            if (handleMove(heal, CooldownType.STAND_SP1, State.HEAL))
                playSound(JSoundRegistry.GE_HEAL, 1, 1);
        } else {
            if (handleMove(healself, CooldownType.STAND_SP1, State.HEAL_SELF))
                playSound(JSoundRegistry.GE_HEAL, 1, 1);
        }
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser()) return;

        CooldownsComponent cooldowns = JComponents.getCooldowns(getUser());
        if (cooldowns.getCooldown(CooldownType.UTIL) > 0) return;
        cooldowns.setCooldown(CooldownType.UTIL, 360); // 18 second flight cd
        setFlightTime(20);

        playSound(JSoundRegistry.GER_FLY, 1, 1);
    }

    @Override
    public void initSpecial2() {
        CanAttackData data = canAttackWithData();
        if (!data.canAttack()) return;

        if (data.user().isSneaking() && handleMove(chargelaser, CooldownType.STAND_SP2, State.SLOW_LASER))
            playSound(JSoundRegistry.GER_SLOW_LASER, 1, 1);
        else if (handleMove(laser, CooldownType.STAND_SP2, State.LASER))
            playSound(JSoundRegistry.GER_LASER, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        if (handleMove(counter, CooldownType.STAND_SP3, State.COUNTER))
            playSound(JSoundRegistry.GE_HEAL, 1, 1);
    }


    private static final int counterStopTime = 20; // Convenience

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);
        if (entity == null || !hasUser()) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(14);
        buf.writeInt(entity.getId());
        buf.writeInt(counterStopTime);
        for (PlayerEntity sendPlayer : world.getPlayers())
            if (sendPlayer instanceof ServerPlayerEntity serverPlayerEntity)
                ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
        JComponents.getTimeStopData(entity).setTicks(counterStopTime);

        StandEntity<?, ?> stand = entity instanceof LivingEntity living ? JUtils.getStand(living) : null;
        if (stand != null)
            stand.cancelAttack();

        if (entity instanceof LivingEntity living)
            stun(living, 10, 0);

        Vec3d eP = this.getEyePos();
        JCraft.createParticle((ServerWorld) world, eP.x, eP.y, eP.z, -1);
    }

    private static final Attack counterMiss = new Attack(11, 0, 20, 21, 1, AttackType.BOX);

    @Override
    public void whiffCounter() {
        setAttack(counterMiss, State.COUNTER_MISS);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (rtzEntityData.isEmpty()) {
            if (handleMove(rtz, CooldownType.STAND_ULT, State.SETUP)) // Setup
                playSound(JSoundRegistry.GER_SETUP, 1, 1);
        } else {
            returnToZero();
        }
    }

    private void returnToZero() {
        for (Map.Entry<Entity, NbtCompound> data : rtzEntityData.entrySet()) {
            Entity ent = data.getKey();
            if (!ent.isAlive()) continue;
            NbtCompound nbt = data.getValue();

            if (ent instanceof PlayerEntity playerEntity) {
                nbt.put("Inventory", playerEntity.getInventory().writeNbt(new NbtList()));
                nbt.put("EnderItems", playerEntity.getEnderChestInventory().toNbtList());

                ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) playerEntity);
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ent));
                NbtList list = nbt.getList("Pos", 6);
                serverPlayer.teleport(list.getDouble(0), list.getDouble(1), list.getDouble(2));
            }

            ent.readNbt(nbt);
        }

        rtzEntityData.clear();
        returnInformation.clear();

        playSound(JSoundRegistry.GER_RTZ, 1, 1);
    }

    @Override
    public void desummon() {
        if (getFlightTime() > 0) {
            setFlightTime(0);
            return;
        }
        super.desummon();
    }

    private static final Attack barrageFinisher = new Attack(11, 17, 1f, 9, 6, 1.75, 1f, 1.1f, AttackType.BOX, 0.5f, 0, 0, JSoundRegistry.TW_KICK_HIT)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Kick Barrage (Final hit)", "");

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        LivingEntity user = getUser();
        switch (attack.id) {
            case (2) -> {
                for (LivingEntity l : entities)
                    l.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 30, 0, false, false));
            }
            case (3) -> {
                for (LivingEntity ent : entities) {
                    ent.addVelocity(0, -1, 0);
                    ent.velocityModified = true;
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 19, false, false));
                }
            }
            case (5) -> {
                if (getMoveStun() < 12) this.curAttack = barrageFinisher;
            }
            case (6) -> {
                if (user != null) user.heal(4f);
            }
            case (7) -> {
                for (LivingEntity ent : entities) {
                    ent.heal(6f);
                    ent.setAttacker(null);

                    if (ent instanceof MobEntity mob) {
                        stun(mob, 10, 0);
                        mob.setTarget(null);
                        mob.setAttacking(null);
                        if (mob instanceof Angerable angerable)
                            angerable.stopAnger();
                    }
                }
            }
            case (8) -> {
                if (user == null) return;

                GERScorpionEntity scorpion = new GERScorpionEntity(JEntityTypeRegistry.GER_SCORPION, world);
                if (attack.moveStun == 28) scorpion.charge(); // If it's the slow variation
                scorpion.setInitialVel(user.getRotationVector().multiply(2));
                Vec3d ePos = this.getEyePos();
                scorpion.refreshPositionAndAngles(ePos.x, ePos.y, ePos.z, -user.getYaw() - 90f, getPitch());
                scorpion.setMaster(user);
                world.spawnEntity(scorpion);
            }
            case (10) -> {
                List<Entity> toReturn = world.getEntitiesByClass(Entity.class, this.getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                toReturn.remove(this);
                toReturn.remove(user);

                for (Entity e : toReturn) {
                    NbtCompound data = new NbtCompound();
                    e.writeNbt(data);
                    rtzEntityData.put(e, data);
                    returnInformation.add(new ReturnData(e.getEyePos(), e));
                }
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegistry.GER_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            PlayerEntity userPlayer = null;
            LivingEntity user = getUserOrThrow();
            // Must be run on client and server because of fun mod compatibility
            int flightTime = getFlightTime();
            flightTime -= 1;
            setFlightTime(flightTime);
            if (user instanceof PlayerEntity playerEntity) {
                userPlayer = playerEntity;

                if (!playerEntity.isCreative() && !playerEntity.isSpectator())
                    playerEntity.getAbilities().flying = (flightTime > 1);
            } else if (flightTime > 1) {
                double y = user.getY();
                Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);
                // Targetting priority
                LivingEntity targetEntity = user.getDamageTracker().getBiggestAttacker();
                if (targetEntity == null && user instanceof MobEntity mob)
                    targetEntity = mob.getTarget();
                if (targetEntity == null)
                    targetEntity = user.getAttacker();
                // If target wasn't found, search in a radius
                Vec3d target = targetEntity != null ? targetEntity.getEyePos() : this.getPos().add(Math.sin(this.age * 0.2) * 3, 0, Math.cos(this.age * 0.2) * 3);

                double dY = MathHelper.clamp(target.getY() - y, -1, 1);
                y += dY;

                vel = vel.add(target.subtract(user.getPos()).normalize()).multiply(0.4);

                user.setVelocity(vel);
                user.setPos(user.getX(), y, user.getZ());

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
            }

            if (world.isClient) return;
                /*
                if (rtzTimer-- > 0)
                    if (rtzTimer == 0)
                        rtzEntityData.clear();
                 */

            if (!(userPlayer instanceof ServerPlayerEntity serverPlayer)) return;
            for (ReturnData data : returnInformation) {
                Entity entity = data.getEntity();
                if (entity == null || !entity.isAlive()) continue;
                Vec3d position = data.getOriginalPos();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeShort(7);

                buf.writeInt(entity.getId());

                buf.writeDouble(position.getX());
                buf.writeDouble(position.getY());
                buf.writeDouble(position.getZ());

                ServerChannelFeedbackPacket.send(serverPlayer, buf);
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<GEREntity> {
        IDLE(builder -> builder.loop("animation.ger.idle")),
        LIGHT(builder -> builder.playAndHold("animation.ger.light")),
        BLOCK(builder -> builder.loop("animation.ger.block")),
        HEAVY(builder -> builder.playAndHold("animation.ger.heavy")),
        BARRAGE(builder -> builder.loop("animation.ger.barrage")),
        HEAL_SELF(builder -> builder.playAndHold("animation.ger.healself")),
        HEAL(builder -> builder.playAndHold("animation.ger.heal")),
        LASER(builder -> builder.playAndHold("animation.ger.laser")),
        SLOW_LASER(builder -> builder.playAndHold("animation.ger.slowlaser")),
        COUNTER(builder -> builder.playAndHold("animation.ger.counter")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.ger.counter_miss")),
        AIR_HEAVY(builder -> builder.playAndHold("animation.ger.airheavy")),
        AIR_LIGHT(builder -> builder.playAndHold("animation.ger.airlight")),
        AIR_BARRAGE(builder -> builder.playAndHold("animation.ger.airbarrage")),
        SETUP(builder -> builder.playAndHold("animation.ger.setup"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GEREntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.ger.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
