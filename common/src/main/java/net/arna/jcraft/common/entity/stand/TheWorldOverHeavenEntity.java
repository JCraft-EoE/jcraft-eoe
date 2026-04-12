package net.arna.jcraft.common.entity.stand;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.core.object.Color;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.LevitationPoseModifier;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.theworld.overheaven.*;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/The_World">The World Over Heaven</a>.
 * @see JStandTypeRegistry#THE_WORLD
 * @see net.arna.jcraft.client.renderer.entity.stands.TheWorldOverHeavenRenderer TheWorldOverHeavenRenderer
 * @see AerialDivineFinisherAttack
 * @see DivineFinisherAttack
 * @see LungeAttack
 * @see OverwriteAttack
 * @see SingularityAttack
 * @see SmiteAttack
 */
public class TheWorldOverHeavenEntity extends StandEntity<TheWorldOverHeavenEntity, TheWorldOverHeavenEntity.State> {
    public static final MoveSet<TheWorldOverHeavenEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.THE_WORLD_OVER_HEAVEN,
            TheWorldOverHeavenEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-45f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.twoh"))
                    .proCount(4)
                    .conCount(4)
                    .freeSpace(Component.literal("""
                            BNBs:
                                -the ultrakill
                                Light>Barrage>Light>Knives>Overwrite~S1/S2>dash>Singularity>Smite>Light~Light
                            
                                -JUDGE MENT
                                crouching Light~Light>dash>Barrage>..."""))
                    .skinName(Component.literal("Shooting Star"))
                    .skinName(Component.literal("Above the Clouds"))
                    .skinName(Component.literal("Dirt to Divinity"))
                    .build())
            .summonData(SummonData.builder()
                    .sound(JSoundRegistry.TWOH_SUMMON)
                    .animDuration(29)
                    .build())
            .build();
    public static final Supplier<IPoseModifier> POSE = LevitationPoseModifier::new;

    public static final LungeAttack LUNGE = new LungeAttack(0, 10, 16, 0.75f,
            8f, 10, 1.75f, 1f, 0f, 11, 5)
            .withAnim(State.LUNGE)
            .withSound(JSoundRegistry.MUDA_DA)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Lunge"),
                    Component.literal("medium speed launcher")
            );
    public static final SimpleAttack<TheWorldOverHeavenEntity> LOW_KICK = SimpleAttack.<TheWorldOverHeavenEntity>lightAttack(
                    6, 12, 0.75f, 6f, 14, 0.25f, 0.25f)
            .withAnim(State.LOW_KICK)
            .withFollowup(LUNGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(7)
            .withInfo(
                    Component.literal("Low Kick"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<TheWorldOverHeavenEntity> LIGHT_FOLLOWUP = new SimpleAttack<TheWorldOverHeavenEntity>(0,
            9, 13, 0.75f, 6f, 8, 1.75f, 1.25f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Roundhouse"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<TheWorldOverHeavenEntity> PUNCH = SimpleAttack.<TheWorldOverHeavenEntity>lightAttack(
                    4, 7, 0.75f, 5f, 11, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LOW_KICK)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final MainBarrageAttack<TheWorldOverHeavenEntity> BARRAGE = new MainBarrageAttack<TheWorldOverHeavenEntity>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.1f, 0f, 3, Blocks.OBSIDIAN.defaultDestroyTime())
            .withSound(JSoundRegistry.TWOH_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );
    public static final SingularityAttack SINGULARITY = new SingularityAttack(140,
            11, 23,1f, 0f, 25, 2f, 0.4f, 0.2f, true)
            .withSound(JSoundRegistry.TWOH_SINGULARITY)
            .withAnim(State.SINGULARITY)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Component.literal("Singularity"),
                    Component.literal("block bypass (stun will always hit, but the opponent can stay blocking)")
            );
    public static final SimpleUppercutAttack<TheWorldOverHeavenEntity> OVERHEAD_KICK = new SimpleUppercutAttack<TheWorldOverHeavenEntity>(0,
            10, 20, 1.25f, 8f, 20, 1.5f, 0.3f, 0f, -1)
            //.withSound(JSoundRegistry.TWOH_HEAVY)
            .withAnim(State.AIR_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withExtraHitBox(1, 0.75, 1)
            .withExtraHitBox(1, -0.5, 1)
            .withInfo(
                    Component.literal("Overhead Kick"),
                    Component.literal("high damage, good reach, launches down")
            );
    public static final SingularityAttack TRUE_STRIKE = new SingularityAttack(0,
            10, 22,1f, 0f, 20, 2f, 0.3f, 0f, false)
            .withBlockStun(20)
            .withAerialVariant(OVERHEAD_KICK)
            .withCrouchingVariant(SINGULARITY)
            .withSound(JSoundRegistry.TWOH_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("True Strike"),
                    Component.literal("damage ignores potions and enchantments, good stun, high blockstun, medium windup")
            );
    public static final SmiteAttack AIR_SMITE = new SmiteAttack(200,
            10, 20, 1f,6f, 21, 3f, 0f, 0f, true, 7, 9)
            .withSound(JSoundRegistry.TWOH_SMITE)
            .withBlockStun(13)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("You won't run away!"),
                    Component.literal("summons a weaker lightning bolt at the aimed position")
            );
    public static final SmiteAttack SMITE = new SmiteAttack(200,
            10, 20, 1f,8f, 21, 3f, 0f, 0f, false, 7, 9)
            .withAerialVariant(AIR_SMITE)
            .withSound(JSoundRegistry.TWOH_SMITE)
            .withBlockStun(13)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Evaporate"),
                    Component.literal("summons a powerful lightning bolt that deals high damage and stun")
            );
    public static final OverwriteAttack OVERWRITE = new OverwriteAttack(0, 7, 23, 1f,
            0f, 40, 2f, 1f, 0f)
            .withSound(JSoundRegistry.TWOH_OVERWRITE)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withLaunch()
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Component.literal("Overwrite (Hit)"),
                    Component.empty()
            );
    public static final ChargeOverwriteMove CHARGE_OVERWRITE = new ChargeOverwriteMove( // Does absolutely nothing on its own.
            360, 71, 70, 1f, 20)
            .withFollowup(OVERWRITE)
            .withSound(JSoundRegistry.TWOH_CHARGE_OVERWRITE)
            .withInfo(Component.literal("Reality Overwrite"), Component.literal("""
                    charges (for a minimum of 1s) an unblockable punch that changes the reality of the hit victims
                    While charging, (de)activate overwrite by pressing:
                    SPECIAL 1 - makes victims unable to look at you (stops if TW:OH is desummoned)
                    SPECIAL 2 - applies every damage over time effect to victims
                    SPECIAL 3 - heals and enslaves mobs"""));

    public static final AerialDivineFinisherAttack AERIAL_DIVINE_FINISHER = new AerialDivineFinisherAttack(200,
            16, 22, 0.75f, 0f, 20, 1.5f, 0f, 0f)
            .withSound(JSoundRegistry.TWOH_KNIFETHROW)
            .withBlockStun(6)
            .withInfo(
                    Component.literal("Aerial Divine Finisher"),
                    Component.literal("briefly stalls mid-air")
            );
    public static final DivineFinisherAttack DIVINE_FINISHER = new DivineFinisherAttack(280,
            16, 22,0.75f, 0f, 20, 1.5f, 0f, 0f)
            .withAerialVariant(AERIAL_DIVINE_FINISHER)
            .withSound(JSoundRegistry.TWOH_KNIFESUMMON)
            .withBlockStun(6)
            .withInfo(
                    Component.literal("Divine Finisher"),
                    Component.literal("fires 4 stunning knives that launch at a delay/in air summons and launches 8 knives")
            );
    public static final TimeStopMove<TheWorldOverHeavenEntity> TIME_STOP = new TimeStopMove<TheWorldOverHeavenEntity>(
            1400, 45, 50, Either.right(JServerConfig.TWOH_TIME_STOP_DURATION))
            .withSound(JSoundRegistry.TWOH_TS)
            .withInfo(
                    Component.literal("Timestop"),
                    Component.literal("5 seconds")
            );

    public static final TimeSkipMove<TheWorldOverHeavenEntity> TIME_SKIP = new TimeSkipMove<TheWorldOverHeavenEntity>(
            300, 14)
            .withSound(JSoundRegistry.TWOH_TIMESKIP)
            .withInfo(
                    Component.literal("Timeskip"),
                    Component.literal("14m range")
            );
    private static final EntityDataAccessor<Integer> OVERWRITE_TYPE = SynchedEntityData.defineId(TheWorldOverHeavenEntity.class, EntityDataSerializers.INT);

    public TheWorldOverHeavenEntity(Level worldIn) {
        super(JStandTypeRegistry.THE_WORLD_OVER_HEAVEN.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.1f, 0.1f, 0.1f),
                new Vector3f(1f, 0.6f, 0.8f),
                new Vector3f(0.9f, 0.9f, 1.0f),
                new Vector3f(1.0f, 0.0f, 0.2f)
        };
    }

    @Override
    public Vector3f getAuraColor() {
        if (getSkin() > 0) {
            return super.getAuraColor();
        }
        final Color auraColor = Color.ofHSB(tickCount % 360f / 360f, 0.5f, 0.5f);
        return new Vector3f(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue());
    }

    public int getOverwriteType() {
        return entityData.get(OVERWRITE_TYPE);
    }

    public void setOverwriteType(int type) {
        entityData.set(OVERWRITE_TYPE, type);
    }

    @Override
    public void desummon() {
        if (tsTime > 0) {
            return;
        }
        super.desummon();
    }

    private static void registerMoves(MoveMap<TheWorldOverHeavenEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, State.LIGHT);

        moves.registerImmediate(MoveClass.HEAVY, TRUE_STRIKE, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, SMITE, State.SMITE);
        moves.register(MoveClass.SPECIAL2, DIVINE_FINISHER, State.AIR_KNIVES).withAerialVariant(State.THROW);
        moves.register(MoveClass.SPECIAL3, CHARGE_OVERWRITE, State.CHARGE_OVERWRITE).withFollowup(State.OVERWRITE);
        moves.register(MoveClass.ULTIMATE, TIME_STOP, State.TIME_STOP);

        moves.register(MoveClass.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case ULTIMATE -> {
                if (tsTime <= 0) {
                    return super.initMove(moveClass);
                } else if (hasUser()) {
                    JCraft.stopTimestop(getUserOrThrow());
                    tsTime = 0;
                }
            }
            case LIGHT -> {
                if (!tryFollowUp(moveClass, MoveClass.LIGHT)) {
                    return super.initMove(moveClass);
                }
            }
            default -> {
                return super.initMove(moveClass);
            }
        }

        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OVERWRITE_TYPE, 0);
    }

    @Override
    protected void playSummonSound() {
        if (shouldNotPlaySummonSound()) {
            return;
        }

        playSound(JSoundRegistry.TWOH_SUMMON.get(), 1f, 1f);
        playSound(JSoundRegistry.TW_SUMMON.get(), 1f, 1f);
    }

    @Override
    @NonNull
    public TheWorldOverHeavenEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheWorldOverHeavenEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.twoh.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.twoh.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.barrage", AzPlayBehaviors.LOOP)),
        SMITE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.smite", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_STOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.timestop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CHARGE_OVERWRITE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.chargeoverwrite", AzPlayBehaviors.LOOP)),
        OVERWRITE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.overwrite", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THROW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.throw", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_KNIVES(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.airknives", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_SKIP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.idle", AzPlayBehaviors.LOOP)),
        LUNGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.lunge", AzPlayBehaviors.LOOP)),
        LOW_KICK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.low_kick", AzPlayBehaviors.LOOP)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SINGULARITY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.singularity", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.twoh.air_heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheWorldOverHeavenEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
