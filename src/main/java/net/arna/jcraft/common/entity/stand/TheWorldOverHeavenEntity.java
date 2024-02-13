package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.overheaven.*;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class TheWorldOverHeavenEntity extends StandEntity<TheWorldOverHeavenEntity, TheWorldOverHeavenEntity.State> {
    public static final LungeAttack LUNGE = new LungeAttack(30, 10, 16, 0.75f,
            8f, 16, 1.75f, 1f, 0f)
            .withAnim(State.LUNGE)
            .withSound(JSoundRegistry.MUDA_DA)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Text.literal("Lunge"), Text.literal("medium speed launcher"));
    public static final SimpleAttack<TheWorldOverHeavenEntity> LIGHT_FOLLOWUP = new SimpleAttack<TheWorldOverHeavenEntity>(
            0, 9, 13, 0.75f, 6, 8, 1.75f, 1.25f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withInfo(Text.literal("Roundhouse"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<TheWorldOverHeavenEntity> PUNCH = SimpleAttack.<TheWorldOverHeavenEntity>lightAttack(
            4, 7, 6f, 11, 0.75f, 0.75f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LUNGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final MainBarrageAttack<TheWorldOverHeavenEntity> BARRAGE = new MainBarrageAttack<TheWorldOverHeavenEntity>(
            280, 0, 50, 0.75f, 1f, 30, 2f, 0.1f, 0f, 3, Blocks.OBSIDIAN.getHardness())
            .withSound(JSoundRegistry.TWOH_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static final SingularityAttack SINGULARITY = new SingularityAttack(260, 10, 22,
            1f, 0f, 20, 2f, 0f, 0f)
            .withSound(JSoundRegistry.TWOH_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Text.literal("Singularity"), Text.literal("block bypass, low stun, medium windup"));
    public static final SmiteAttack AIR_SMITE = new SmiteAttack(300, 10, 20, 1f,
            6f, 21, 3f, 0f, 0f, true)
            .withSound(JSoundRegistry.TWOH_SMITE)
            .withBlockStun(13)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(Text.literal("You won't run away!"), Text.literal("summons a weaker lightning bolt at the aimed position"));
    public static final SmiteAttack SMITE = new SmiteAttack(300, 10, 20, 1f,
            8f, 21, 3f, 0f, 0f, false)
            .withAerialVariant(AIR_SMITE)
            .withSound(JSoundRegistry.TWOH_SMITE)
            .withBlockStun(13)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(Text.literal("Evaporate"), Text.literal("summons a powerful lightning bolt that deals high damage and stun"));
    public static final OverwriteAttack OVERWRITE = new OverwriteAttack(0, 7, 23, 1f,
            0f, 40, 2f, 1f, 0f)
            .withSound(JSoundRegistry.TWOH_OVERWRITE)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withLaunch()
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(Text.literal("Overwrite (Hit)"), Text.empty());
    // Does absolutely nothing on its own.
    public static final NoOpMove<TheWorldOverHeavenEntity> CHARGE_OVERWRITE = new NoOpMove<TheWorldOverHeavenEntity>(
            360, 70, 1f)
            .withFollowup(OVERWRITE)
            .withSound(JSoundRegistry.TWOH_CHARGE_OVERWRITE)
            .withInfo(Text.literal("Reality Overwrite"), Text.literal("""
                            charges (for a minimum of 1s) an unblockable punch that changes the reality of the hit victims
                            While charging, (de)activate overwrite by pressing:
                            SPECIAL 1 - makes victims unable to look at you
                            SPECIAL 2 - applies every damage over time effect to victims
                            SPECIAL 3 - heals and enslaves mobs"""));

    public static final AerialDivineFinisherAttack AERIAL_DIVINE_FINISHER = new AerialDivineFinisherAttack(280,
            16, 22, 0.75f, 0f, 20, 1.5f, 0f, 0f)
            .withSound(JSoundRegistry.TWOH_KNIFETHROW)
            .withBlockStun(6)
            .withInfo(Text.literal("Aerial Divine Finisher"), Text.empty());
    public static final DivineFinisherAttack DIVINE_FINISHER = new DivineFinisherAttack(280, 16, 22,
            0.75f, 0f, 20, 1.5f, 0f, 0f)
            .withAerialVariant(AERIAL_DIVINE_FINISHER)
            .withSound(JSoundRegistry.TWOH_AIRKNIVES)
            .withBlockStun(6)
            .withInfo(Text.literal("Divine Finisher"), Text.literal("fires 4 stunning knives that launch at a delay/in air summons and launches 8 knives"));
    public static final TimeStopMove<TheWorldOverHeavenEntity> TIME_STOP = new TimeStopMove<TheWorldOverHeavenEntity>(
            1400, 45, 50, JServerConfig.TWOH_TIME_STOP_DURATION::getValue)
            .withSound(JSoundRegistry.TWOH_TS)
            .withInfo(Text.literal("Timestop"), Text.literal("5 seconds"));

    public static final TimeSkipMove<TheWorldOverHeavenEntity> TIME_SKIP = new TimeSkipMove<TheWorldOverHeavenEntity>(
            300, 14)
            .withSound(JSoundRegistry.TWOH_TIMESKIP)
            .withInfo(Text.literal("Timeskip"), Text.literal("14m range"));
    private static final TrackedData<Integer> OVERWRITE_TYPE;

    static {
        OVERWRITE_TYPE = DataTracker.registerData(TheWorldOverHeavenEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public TheWorldOverHeavenEntity(World worldIn) {
        super(StandType.THE_WORLD_OVER_HEAVEN, worldIn, JSoundRegistry.TWOH_SUMMON);
        idleRotation = -45f;
        summonAnimDuration = 29;

        description = "All Range DOMINATOR";

        pros = List.of(
                "fast m1",
                "unblockables",
                "good ranged coverage",
                "longest timestop"
        );

        cons = List.of(
                "no knockdowns",
                "unsafe pressure",
                "committal",
                "extremely expensive timestop setup"
        );

        freespace =
                """
                        BNBs:
                            -the ultrakill
                            M1>Barrage>M1>Knives>Overwrite~S2/S3>dash>Smite>Heavy>M1""";

        auraColors = new Vec3f[]{
                new Vec3f(0.1f, 0.1f, 0.1f),
                new Vec3f(1f, 1f, 1f),
                new Vec3f(0.9f, 0.9f, 1.0f),
                new Vec3f(1.0f, 0.0f, 0.2f)
        };

        if (world.isClient) return;
        // TODO
//        timestop.stun = JServerConfig.TWOH_TIME_STOP_DURATION.getValue() / 20.0f;
    }

    public int getOverwriteType() {
        return dataTracker.get(OVERWRITE_TYPE);
    }

    public void setOverwriteType(int type) {
        dataTracker.set(OVERWRITE_TYPE, type);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) return;
        super.desummon();
    }

    @Override
    protected void registerMoves(MoveMap<TheWorldOverHeavenEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, PUNCH, State.LIGHT);

        moves.register(MoveType.HEAVY, SINGULARITY, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, SMITE, State.SMITE);
        moves.register(MoveType.SPECIAL2, DIVINE_FINISHER, State.AIR_KNIVES).withAerialVariant(State.THROW);
        moves.register(MoveType.SPECIAL3, CHARGE_OVERWRITE, State.CHARGE_OVERWRITE);
        moves.register(MoveType.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveType.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    public void initMove(MoveType type) {
        switch (type) {
            case SPECIAL1, SPECIAL2, SPECIAL3 -> {
                if (curMove != null && curMove.getOriginalMove() == CHARGE_OVERWRITE && getMoveStun() < 50)
                    initOverwrite(switch (type) {
                        default -> 1;
                        case SPECIAL2 -> 2;
                        case SPECIAL3 -> 3;
                    });
                else super.initMove(type);
            }
            case ULTIMATE -> {
                if (tsTime <= 0) super.initMove(type);
                else if (hasUser()) {
                    JCraft.stopTimestop(getUserOrThrow());
                    tsTime = 0;
                }
            }
            case LIGHT -> {
                if (curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
                    AbstractMove<?, ? super TheWorldOverHeavenEntity> followup = curMove.getFollowup();
                    if (followup != null) setMove(followup, (State) followup.getAnimation());
                } else super.initMove(type);
            }
            default -> super.initMove(type);
        }
    }

    private void initOverwrite(int type) {
        setOverwriteType(type);
        setMove(OVERWRITE, State.OVERWRITE);
        playSound(JSoundRegistry.TWOH_OVERWRITE, 1, 1);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OVERWRITE_TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;

        IntList overwriteTimes = moveContext.get(OverwriteAttack.OVERWRITE_TIMES);
        List<LivingEntity> overwriteTargets = moveContext.get(OverwriteAttack.OVERWRITE_TARGETS);
        LivingEntity user = getUserOrThrow();

        if (age == 1) {
            List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, new Box(
                            getPos().add(-64, -64, -64), getPos().add(64, 64, 64)),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != user));

            for (LivingEntity ent : hit) {
                UUID slavedTo = JComponents.getMiscData(ent).getSlavedTo();

                if (slavedTo == null || !slavedTo.equals(user.getUuid())) continue;
                overwriteTargets.add(ent);
                overwriteTimes.add(1048576); // 2 to the whatever
            }
        }

        if (world.isClient) return;

        int moveStun = getMoveStun();
        if (moveStun <= 0 && getOverwriteType() != 0) setOverwriteType(0);

        for (int i = 0; i < overwriteTimes.size(); i++) {
            int time = overwriteTimes.getInt(i);
            overwriteTimes.set(i, time - 1);

            if (time < 1) {
                overwriteTimes.removeInt(i);
                overwriteTargets.remove(i);
                i--;
            } else {
                LivingEntity entity = overwriteTargets.get(i);

                if (entity instanceof MobEntity mob && time > 200) { // Targeting and movement for mobs
                    LivingEntity victim = user.getAttacking();
                    if (victim == null) {
                        LivingEntity adv = user.getPrimeAdversary();
                        if (adv != null && adv.isAlive()) mob.setTarget(adv);
                    } else if (victim.isAlive()) mob.setTarget(victim);

                    if (mob.squaredDistanceTo(this) > 256)
                        mob.getNavigation().startMovingTo(this, 1);
                }

                // Inability to look at master
                double range = 1024.0;

                Box box = entity
                        .getBoundingBox()
                        .stretch(entity.getRotationVec(1.0F).multiply(range))
                        .expand(1.0D);
                EntityHitResult hitResult = ProjectileUtil.raycast(
                        entity, entity.getEyePos(),
                        entity.getEyePos().add(entity.getRotationVector().multiply(range)),
                        box, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR, range);

                if (hitResult == null) continue;
                Entity lookEntity = hitResult.getEntity();

                if (lookEntity != user && lookEntity != this) continue;
                entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, getEyePos().add(
                        random.nextInt() * 10,
                        random.nextInt() * 10,
                        random.nextInt() * 10));
            }
        }
    }

    @Override
    protected void playSummonSound() {
        if (shouldNotPlaySummonSound()) return;

        playSound(JSoundRegistry.TWOH_SUMMON, 1f, 1f);
        playSound(JSoundRegistry.TW_SUMMON, 1f, 1f);
    }

    @Override
    protected @NonNull TheWorldOverHeavenEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldOverHeavenEntity> {
        IDLE(builder -> builder.loop("animation.twoh.idle")),
        LIGHT(builder -> builder.playAndHold("animation.twoh.light")),
        BLOCK(builder -> builder.loop("animation.twoh.block")),
        HEAVY(builder -> builder.playAndHold("animation.twoh.heavy")),
        BARRAGE(builder -> builder.loop("animation.twoh.barrage")),
        SMITE(builder -> builder.playAndHold("animation.twoh.smite")),
        TIME_STOP(builder -> builder.playAndHold("animation.twoh.timestop")),
        CHARGE_OVERWRITE(builder -> builder.loop("animation.twoh.chargeoverwrite")),
        OVERWRITE(builder -> builder.playAndHold("animation.twoh.overwrite")),
        THROW(builder -> builder.playAndHold("animation.twoh.throw")),
        AIR_KNIVES(builder -> builder.playAndHold("animation.twoh.airknives")),
        TIME_SKIP(builder -> builder.loop("animation.twoh.idle")),
        LUNGE(builder -> builder.loop("animation.twoh.lunge")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.twoh.light_followup"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldOverHeavenEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.twoh.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
