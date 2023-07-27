package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class SPTWEntity extends AbstractStarPlatinumEntity<SPTWEntity, SPTWEntity.State> {
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 5, 1.5, 5f, 0.25f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter, low knockback");
    public static final Attack heavy = new Attack(1, 17, 1f, 30, 20, 2.0, 10f, 1.5f, AttackType.BOX, 0.7f, 0, 0, JSoundRegister.IMPACT_1)
            .setHitspark(2)
            .appendHitbox(new HitBoxData(0, 0, 1.5))
            .hyperArmor()
            .setLaunch()
            .setInfo("Star Breaker", "uninterruptable launcher");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack timestrike = new Attack(3, 20, 0.75f, 11, 7, 1.5, 5f, 0.75f, AttackType.BOX, 0.6f, -0.25f, 0, JSoundRegister.IMPACT_1)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setInfo("Time Strike", "teleports forward 2.5m after a short windup, then delivers a fast, low stun hit/crouch to turn around after teleport");
    public static final Attack backhand = new Attack(4, 12, 0.75f, 12, 7, 1.5, 6f, 0.25f, AttackType.BOX, 1f, 0, 0, JSoundRegister.IMPACT_1)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setInfo("Backhand", "fast poke, decent stun");
    public static final Attack grab = new Attack(5, 26, 1f, 20, 8, 1.5, 2f, 0.4f, AttackType.BOX, 1, 0, 0, JSoundRegister.SPTW_GRABHIT)
            .appendHitbox(new HitBoxData(0, 0, 1))
            .setGrab()
            .setBlockstun(4)
            .setInfo("What an Ugly Watch", "grab, high recovery");
    public static final Attack grabhit = new Attack(7, 0, 1f, 24, 16, 1.75, 9f, 0.4f, AttackType.BOX, 1, 0, 0, JSoundRegister.IMPACT_1)
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
        super(StandType.STAR_PLATINUM_THE_WORLD, State.class, worldIn);
        super.initialize();
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

        freespace =
                """
                        BNBs:
                                                
                            -the
                            M1>cr.Time Strike>Backhand>What an Ugly Watch>delay M1>Timestop~Star Breaker>dash/Timeskip>Barrage>M1""";

        moves = List.of(light, heavy, barrage, timestrike, timestop, backhand, timestrike, timeskip);
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
        handleAttack(light, JCraft.standLightCD, State.PUNCH);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY)) {
            playSound(JSoundRegister.STAR_BREAKER, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE)) {
            playSound(JSoundRegister.STAR_PLATINUM_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!canAttack() || !hasUser()) return;
        if (handleAttack(timestrike, JCraft.standS1CD, State.TIME_STRIKE)) {
            turnAround = getUserOrThrow().isSneaking();
            //playSound(JSoundRegister.SPTW_TIMESTRIKE, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (handleAttack(timestop, JCraft.standUltCD, State.TIME_STOP))
            playSound(JSoundRegister.STAR_PLATINUM_THE_WORLD, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        if (handleAttack(backhand, JCraft.standS2CD, State.BACK_HAND))
            playSound(JSoundRegister.SPTW_BACKHAND, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        // Uses a copy because otherwise the main one gets overwritten by specialAttack()
        if (handleAttack(grab, JCraft.standS3CD, State.GRAB))
            playSound(JSoundRegister.SPTW_GRAB, 1, 1);
    }

    @Override
    public void initUtil() {
        if (!canAttack()) return;
        handleAttack(timeskip, JCraft.utilCD, State.IDLE);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (-2) -> {
                if (tsTime > 0) return;
                timeSkip(14, JSoundRegister.STAR_PLATINUM_TIMESKIP);
            }
            case (5) -> {
                if (entities.isEmpty()) return;
                setAttack(grabhit, State.GRAB_HIT);
                playSound(JSoundRegister.SPTW_UPPERCUT, 1, 1);

                for (LivingEntity ent : entities)
                    if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand)
                        stand.blocking = false;
            }
            case (7) -> {
                for (LivingEntity ent : entities)
                    ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 10, true, false));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity user = getUser();

        if (user != null) {
            if (world.isClient) setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
            else if (curAttack != null && curAttack.id == timestrike.id && getMoveStun() == 7) {
                /*
                NbtCompound userData = ((IEntityDataSaver)user).getPersistentData();
                if (userData.getInt(JCraft.utilCD) < 200)
                    userData.putInt(JCraft.utilCD, 200);
                 */

                Vec3d prevPos = user.getEyePos();

                timeSkip(2.5, JSoundRegister.STAR_PLATINUM_TIMESKIP);
                if (turnAround)
                    user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, prevPos);
            }
        }
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
        GRAB_HIT(builder -> builder.playAndHold("animation.sptw.grabhit"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SPTWEntity stand, AnimationBuilder builder) {
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
