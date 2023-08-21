package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class SPTWEntity extends AbstractStarPlatinumEntity<SPTWEntity, SPTWEntity.State> {
    public static final Attack crm1 = new Attack(8, JCraft.lightCooldown, 0.75f, 19, 12, 1.8, 7f, 0f, AttackType.BOX, 0.55f, 0.8f, 0, JSoundRegistry.IMPACT_8)
            .setLaunch()
            .setInfo("Ground Slam", "low hitbox, decent damage, launches");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 5, 1.5, 5f, 0.2f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegistry.IMPACT_1)
            .crouchingVariation(crm1)
            .setInfo("Punch", "quick combo starter, low knockback");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack timestrike = new Attack(3, 20, 0.75f, 11, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, -0.25f, 0, JSoundRegistry.IMPACT_1)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setInfo("Time Strike", "teleports forward 2.5m after a short windup, then delivers a fast, low stun hit/crouch to turn around after teleport");
    public static final Attack backhand = new Attack(4, 12, 0.75f, 12, 7, 1.5, 6f, 0.25f, AttackType.BOX, 1f, 0, 0, JSoundRegistry.IMPACT_1)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setInfo("Backhand", "fast poke, decent stun");
    public static final Attack grab = new Attack(5, 26, 1f, 20, 8, 1.5, 2f, 0.1f, AttackType.BOX, 1, 0, 0, JSoundRegistry.SPTW_GRABHIT)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setGrab()
            .setBlockstun(4)
            .setInfo("What an Ugly Watch", "grab, high recovery");
    public static final Attack grabhit = new Attack(7, 0, 1f, 24, 16, 1.75, 9f, 0.4f, AttackType.BOX, 1, 0, 0, JSoundRegistry.IMPACT_6)
            .setHitspark(2)
            .setLaunch()
            .hyperArmor()
            .setInfo("What an Ugly Watch (Hit)", "");
    public static final Attack timestop = new Attack(6, 30, 10, 5, 1.75f, AttackType.TIMESTOP)
            .setUB(true)
            .setInfo("Timestop", "1.5 second, extremely low windup");
    private static final Attack timeskip = new Attack(-2, 18, 2, 2)
            .setMobility(MobilityType.TELEPORT)
            .setInfo("Timeskip", "");

    private boolean turnAround;

    public SPTWEntity(World worldIn) {
        super(StandType.STAR_PLATINUM_THE_WORLD, worldIn);

        idleRotation = 315f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
                "high whiff punish power",
                "high mobility",
                "excellent mixups",
                "near-instant timestop"
        );

        cons = List.of(
                "burns through options quickly",
                "hard to hitconfirm important options without using TS"
        );

        freespace = """
                    BNBs:
                                            
                        -the superman
                        M1>cr.Time Strike>Backhand>What an Ugly Watch>delay M1>Timestop~Star Breaker>dash/Timeskip>Barrage>M1""";

        moves = List.of(light, heavy, barrage, timestrike, timestop, backhand, timestrike, timeskip);

        super.initialize();

        if (world.isClient) return;
        timestop.stun = JServerConfig.SPTW_TIME_STOP_DURATION.getValue() / 20.0f;
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking())
            handleMove(crm1, CooldownType.STAND_LIGHT, State.GROUND_SLAM);
        else
            handleMove(light, CooldownType.STAND_LIGHT, State.PUNCH);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleMove(heavy, CooldownType.STAND_HEAVY, State.HEAVY)) {
            playSound(JSoundRegistry.STAR_BREAKER, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleMove(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE)) {
            playSound(JSoundRegistry.STAR_PLATINUM_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;
        if (handleMove(timestrike, CooldownType.STAND_SP1, State.TIME_STRIKE)) {
            turnAround = getUserOrThrow().isSneaking();
            //playSound(JSoundRegister.SPTW_TIMESTRIKE, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleMove(timestop, CooldownType.STAND_ULT, State.TIME_STOP))
            playSound(JSoundRegistry.STAR_PLATINUM_THE_WORLD, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleMove(backhand, CooldownType.STAND_SP2, State.BACK_HAND))
            playSound(JSoundRegistry.SPTW_BACKHAND, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        // Uses a copy because otherwise the main one gets overwritten by specialAttack()
        if (handleMove(grab, CooldownType.STAND_SP3, State.GRAB))
            playSound(JSoundRegistry.SPTW_GRAB, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack() || tsTime > 0) return;
        handleMove(timeskip, CooldownType.UTIL, State.TIME_SKIP);
    }

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> timeSkip(14, JSoundRegistry.STAR_PLATINUM_TIMESKIP);
            case (5) -> {
                if (entities.isEmpty()) return;
                setMove(grabhit, State.GRAB_HIT);
                playSound(JSoundRegistry.SPTW_UPPERCUT, 1, 1);

                for (LivingEntity ent : entities)
                    if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand)
                        stand.blocking = false;
            }
            case (7) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 10, true, false));
            }
            case (8) -> {
                if (!hasUser()) return;

                Vec3d pos = getUserOrThrow().getPos();
                for (LivingEntity living : entities) {
                    Vec3d launchVec = living.getPos().subtract(pos).normalize().multiply(1.3);
                    living.addVelocity(launchVec.x, launchVec.y + 0.4, launchVec.z);

                    living.velocityModified = true;
                    if (living instanceof ServerPlayerEntity serverPlayer)
                        serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();

        if (world.isClient || curMove == null || curMove.id != timestrike.id || getMoveStun() != 7) return;
        /*
            NbtCompound userData = ((IEntityDataSaver)user).getPersistentData();
            if (userData.getInt(JCraft.utilCD) < 200)
                userData.putInt(JCraft.utilCD, 200);
             */

        Vec3d prevPos = user.getEyePos();

        timeSkip(2.5, JSoundRegistry.STAR_PLATINUM_TIMESKIP);
        if (turnAround)
            user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, prevPos);
    }

    // Animation code
    public enum State implements StandAnimationState<SPTWEntity> {
        IDLE(builder -> builder.loop("animation.sptw.idle")),
        PUNCH(builder -> builder.playAndHold("animation.sptw.punch")),
        BLOCK(builder -> builder.loop("animation.sptw.block")),
        HEAVY(builder -> builder.playAndHold("animation.sptw.heavy")),
        BARRAGE(builder -> builder.loop("animation.sptw.barrage")),
        TIME_STRIKE(builder -> builder.playAndHold("animation.sptw.timestrike")),
        TIME_STOP(builder -> builder.playAndHold("animation.sptw.timestop")),
        BACK_HAND(builder -> builder.playAndHold("animation.sptw.backhand")),
        GRAB(builder -> builder.playAndHold("animation.sptw.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.sptw.grabhit")),
        TIME_SKIP(builder -> builder.loop("animation.sptw.idle")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.sptw.groundslam"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SPTWEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.sptw.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
