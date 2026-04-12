package net.arna.jcraft.common.entity.stand;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.pose.ModifierCondition;
import net.arna.jcraft.api.pose.PoseModifiers;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.PoseModifierGroup;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.common.attack.moves.kingcrimson.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/King_Crimson">King Crimson</a>.
 * @see JStandTypeRegistry#KING_CRIMSON
 * @see net.arna.jcraft.client.model.entity.stand.KingCrimsonModel KingCrimsonModel
 * @see net.arna.jcraft.client.renderer.entity.stands.KingCrimsonRenderer KingCrimsonRenderer
 * @see BloodThrowAttack
 * @see EpitaphAttack
 * @see KCDonutAttack
 * @see PredictionMove
 * @see TimeEraseMove
 */
public class KingCrimsonEntity extends StandEntity<KingCrimsonEntity, KingCrimsonEntity.State> {
    public static final MoveSet<KingCrimsonEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.KING_CRIMSON,
            KingCrimsonEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleDistance(1f)
            .idleRotation(-65f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.kingcrimson"))
                    .proCount(4)
                    .conCount(4)
                    .freeSpace(Component.literal("""
                BNBs:
                    -the gamer (THE bnb)
                    Light>Barrage>delay.Move Cancel>Light>Heavy~Overhead
                
                    -the loop zoopler (sub optimal damage for a setup that kills them if you guess right)
                    Eye Chop>Donut>Light>Heavy~Overhead>Time Erase
                
                    -hits like a firetruck (death)
                    Donut>Move Cancel>Timeskip>Barrage>Move Cancel>Light>Heavy>Move Cancel>Eye Chop>Sweep
                """))
                    .skinName(Component.literal("Royal"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Nightshade"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.KC_SUMMON))
            .build();
    public static final Supplier<IPoseModifier> POSE = () -> PoseModifierGroup.builder()
            .condition(ModifierCondition.USER_NOT_MOVING)
            .modifier(PoseModifiers.parse("body.yRot += 30deg"))
            .modifier(PoseModifiers.parse("""
                    leftArm.yRot += 30deg;
                    leftArm.z -= 2.1;
                    """, ModifierCondition.LEFT_ARM_EMPTY))
            .modifier(PoseModifiers.parse("""
                    rightArm.yRot += 30deg;
                    rightArm.z += 2.1;
                    """, ModifierCondition.RIGHT_ARM_EMPTY_OR_ITEM))
            .modifier(PoseModifiers.parse("""
                    leftLeg.z -= 1;
                    rightLeg.z += 1.5;
                    
                    rightLeg.yRot += 45deg;
                    """))
            .build();

    public static final KnockdownAttack<KingCrimsonEntity> SWEEP = new KnockdownAttack<KingCrimsonEntity>(0,
            10, 20, 0.85f, 5f, 20, 1.5f, 0.4f, 0.3f, 35)
            .withAnim(State.SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withBlockStun(6)
            .withExtraHitBox(1)
            .withInfo(
                    Component.literal("Sweep"),
                    Component.literal("quick combo finisher, knocks down")
            );
    public static final SimpleMultiHitAttack<KingCrimsonEntity> DUAL_CHOP = new SimpleMultiHitAttack<KingCrimsonEntity>(40,
            23, 0.85f, 4f, 21, 1.5f, 0.2f, -0.1f, IntSet.of(10, 16))
            .noLoopPrevention()
            .withSound(JSoundRegistry.KC_DUAL_CHOP)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Dual Chop"),
                    Component.literal("quick combo starter")
            );
    public static final BarrageAttack<KingCrimsonEntity> BARRAGE_FINISHER = new BarrageAttack<KingCrimsonEntity>(0,
            0, 50, 0.85f, 1f, 10, 1.5f, 1.1f, 0f, 3)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Barrage (Final Hit)"),
                    Component.empty()
            );
    public static final MainBarrageAttack<KingCrimsonEntity> BARRAGE = new MainBarrageAttack<KingCrimsonEntity>(280,
            0, 40, 0.85f, 1f, 20, 1.5f, 0.1f, 0f, 3, Blocks.OBSIDIAN.defaultDestroyTime())
            .withFinisher(36, BARRAGE_FINISHER)
            .withSound(JSoundRegistry.KC_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender/finisher, medium stun, knocks back")
            );
    public static final KnockdownAttack<KingCrimsonEntity> OVERHEAD_HOOK = new KnockdownAttack<KingCrimsonEntity>(0,
            22, 32, 0.85f, 9f, 11, 2f, 1.5f, 0f, 35)
            .withSound(JSoundRegistry.KC_HEAVY2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withBlockStun(10)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Overhead Hook"),
                    Component.literal("long windup, knockdown")
            );
    public static final SimpleAttack<KingCrimsonEntity> VERTICAL_CHOP = new SimpleAttack<KingCrimsonEntity>(0,
            12, 19, 0.85f, 6f, 25, 1.5f, 0.6f, 0f)
            .withFollowup(OVERHEAD_HOOK)
            .withFollowupFrame(7)
            .withSound(JSoundRegistry.KC_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withExtraHitBox(0, 0.5, 1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Vertical Chop"),
                    Component.literal("medium windup combo starter, has a true followup in the form of a slow, armored knockdown")
            );
    public static final BloodThrowAttack BLOOD_THROW = new BloodThrowAttack(200, 10, 15, 1f)
            .withInfo(
                    Component.literal("Blood Throw"),
                    Component.literal("throws a stunning, blinding blood projectile, crouch while it comes out for higher speed")
            );
    public static final SimpleAttack<KingCrimsonEntity> EYE_CHOP = new SimpleAttack<KingCrimsonEntity>(0,
            20, 29, 1f, 9f, 27, 1.75f, 0.7f, -0.3f)
            .withCrouchingVariant(BLOOD_THROW)
            .withSound(JSoundRegistry.KC_EYE_CHOP)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withAction(EffectAction.inflict(MobEffects.BLINDNESS, 200, 0))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withExtraHitBox(0, 0.5, 1)
            .withBlockStun(4)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Eye Chop"),
                    Component.literal("blindness on hit, combo starter, low blockstun")
            );
    public static final KCDonutAttack DONUT = new KCDonutAttack(48, 30, 48, 1f,
            14f, 10, 1.75f, 1.5f, 0.1f)
            .withSound(JSoundRegistry.KC_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_7)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Donut"),
                    Component.literal("slow, uninterruptable, extremely damaging launcher")
            );
    public static final EpitaphAttack EPITAPH = new EpitaphAttack(480, 4, 34, -1f)
            .withInfo(
                    Component.literal("Epitaph"),
                    Component.literal("0.2s windup, 1.5s counter, combo starter. Cannot be buffered.")
            );
    public static final PredictionMove PREDICTION = new PredictionMove(480, 4, 104, -1f)
            .withCrouchingVariant(EPITAPH)
            .withSound(JSoundRegistry.KC_EPITAPH)
            .withInfo(
                    Component.literal("Prediction/Move Cancel"),
                    Component.literal("""
                            This move cannot be buffered.
                            Shows the projected future location of nearby entities, using Time Erase will force them to the projected locations. (20s TE cooldown)
                            While predicting, you are slowed down.
                            Move Cancel - Using Special 3 during any move cancels it and puts Time Erase on a 7s cooldown. (But does not require TE to be usable)""")
            );
    public static final TimeEraseMove TIME_ERASE = new TimeEraseMove(1000, 5, 15, 1f, 120)
            .withInfo(
                    Component.literal("Time Erase"),
                    Component.literal("6 seconds duration, cancellable by doing anything with King Crimson")
            );
    public static final TimeSkipMove<KingCrimsonEntity> TIME_SKIP = new TimeSkipMove<KingCrimsonEntity>(300, 16)
            .withSound(JSoundRegistry.TE_TP)
            .withParticles()
            .withInfo(
                    Component.literal("Timeskip"),
                    Component.literal("16m range")
            );

    private static final EntityDataAccessor<Integer> TIME_ERASE_TIME;

    public KingCrimsonEntity(Level worldIn) {
        super(JStandTypeRegistry.KING_CRIMSON.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.9f, 0.5f, 0.7f),
                new Vector3f(1.0f, 0.4f, 0.4f),
                new Vector3f(0.3f, 0.0f, 0.5f)
        };
    }

    static {
        TIME_ERASE_TIME = SynchedEntityData.defineId(KingCrimsonEntity.class, EntityDataSerializers.INT);
    }

    public int getTETime() {
        return entityData.get(TIME_ERASE_TIME);
    }

    public void setTETime(int teTime) {
        entityData.set(TIME_ERASE_TIME, teTime);
    }

    private static void registerMoves(MoveMap<KingCrimsonEntity, State> moves) {
        moves.register(MoveClass.LIGHT, DUAL_CHOP, State.DUAL_CHOP).withCrouchingVariant(State.SWEEP);
        moves.register(MoveClass.HEAVY, VERTICAL_CHOP, State.HEAVY).withFollowup(State.OVERHEAD);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, EYE_CHOP, State.EYE_CHOP).withCrouchingVariant(State.BLOOD_THROW);
        moves.register(MoveClass.SPECIAL2, DONUT, State.DONUT);
        moves.register(MoveClass.SPECIAL3, PREDICTION, State.PREDICT).withCrouchingVariant(State.EPITAPH);
        moves.register(MoveClass.ULTIMATE, TIME_ERASE, State.TIME_ERASE);

        moves.register(MoveClass.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(TIME_ERASE_TIME, 0);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case ULTIMATE -> {
                if (super.initMove(moveClass)) {
                    return true;
                }

                if (canAttack() && getTETime() > 0) {
                    getTimeEraseMove().cancelTE(this);
                    return true;
                }

                return false;
            }
            case SPECIAL3 -> {
                final LivingEntity user = getUserOrThrow();
                final boolean start = getMoveStun() <= 0;

                if (start) {
                    return super.initMove(moveClass);
                }

                // When used during a move, cancels it and puts time erase on cooldown
                moveCancel();

                // 7 second time erase cooldown
                final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
                if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) < 140) {
                    cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 140);
                }

                // Particle effects
                final Vec3 oPos = user.position();
                final AABB bBox = user.getBoundingBox();
                final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeVarInt(2);
                buf.writeDouble(oPos.x);
                buf.writeDouble(oPos.y);
                buf.writeDouble(oPos.z);
                buf.writeDouble(bBox.getXsize());
                buf.writeDouble(bBox.getYsize());
                buf.writeDouble(bBox.getZsize());

                ServerChannelFeedbackPacket.send(JUtils.around((ServerLevel) level(), oPos, 128), buf);

                // Stop epitaph state
                if (user instanceof ServerPlayer player) {
                    NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_EPITAPH_STATE, new FriendlyByteBuf(Unpooled.buffer().writeBoolean(false)));
                }
            }
            case UTILITY -> {
                if (getTETime() > 0) {
                    getTimeEraseMove().cancelTE(this);
                }
                return super.initMove(moveClass);
            }
            default -> {
                if (moveClass == MoveClass.HEAVY && getState() == State.HEAVY && queuedMove == MoveInputType.HEAVY)
                    queuedMove = null;
                return super.initMove(moveClass);
            }
        }

        return true;
    }

    @Override
    public void queueMove(MoveInputType type) {
        if ( (getState() == State.HEAVY || getState() == State.OVERHEAD) && type == MoveInputType.HEAVY) return;
        super.queueMove(type);
    }

    public void moveCancel() {
        // Epitaph
        getMoveMap().findMoveByType(PredictionMove.class)
                .ifPresent(m -> m.cancelPrediction(this));

        // General
        setCurrentMove(null);
        queuedMove = null;

        setMoveStun(2);
        setState(State.IDLE);
        setReset(true);
    }

    @Override
    public void desummon() {
        if (getTETime() < 1) {
            super.desummon();
        } else {
            getTimeEraseMove().cancelTE(this);
        }
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        if (getTETime() > 0) {
            final double x = getX(), y = getY(), z = getZ();
            return new AABB(x, y, z, x, y + 0.1, z);
        }
        return super.makeBoundingBox();
    }

    public TimeEraseMove getTimeEraseMove() {
        return getMove(TimeEraseMove.class);
    }

    @Override
    @NonNull
    public KingCrimsonEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KingCrimsonEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.kingcrimson.idle", AzPlayBehaviors.LOOP)),
        DUAL_CHOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.dual_chop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.kingcrimson.block", AzPlayBehaviors.LOOP)),
        OVERHEAD(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.overhead", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DONUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.donut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.barrage", AzPlayBehaviors.LOOP)),
        EYE_CHOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.eye_chop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_ERASE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.time_erase", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        EPITAPH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.epitaph", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOOD_THROW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.bloodthrow", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        PREDICT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.predict", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_MISS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SWEEP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.sweep", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TIME_SKIP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.kingcrimson.idle", AzPlayBehaviors.LOOP));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KingCrimsonEntity attacker) {
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
