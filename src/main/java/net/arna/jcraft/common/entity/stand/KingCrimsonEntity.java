package net.arna.jcraft.common.entity.stand;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.kingcrimson.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JPacketRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public class KingCrimsonEntity extends StandEntity<KingCrimsonEntity, KingCrimsonEntity.State> {
    public static final KnockdownAttack<KingCrimsonEntity> SWEEP = new KnockdownAttack<KingCrimsonEntity>(40,
            10, 20, 0.85f, 5f, 20, 1.5f, 0.4f, 0.3f, 35)
            .withAnim(State.SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withBlockStun(6)
            .withExtraHitBox(1)
            .withInfo(
                    Text.literal("Sweep"),
                    Text.literal("quick combo finisher, knocks down")
            );
    public static final SimpleMultiHitAttack<KingCrimsonEntity> DUAL_CHOP = new SimpleMultiHitAttack<KingCrimsonEntity>(
            40, 23, 0.85f, 4f, 21, 1.5f, 0.2f, -0.1f,
            IntSet.of(10, 16))
            .withSound(JSoundRegistry.KC_DUAL_CHOP)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Dual Chop"),
                    Text.literal("quick combo starter")
            );
    public static final BarrageAttack<KingCrimsonEntity> BARRAGE_FINISHER = new BarrageAttack<KingCrimsonEntity>(0,
            0, 50, 0.85f, 1f, 10, 1.5f, 1.1f, 0f, 3)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Barrage (Final Hit)"),
                    Text.empty()
            );
    public static final MainBarrageAttack<KingCrimsonEntity> BARRAGE = new MainBarrageAttack<KingCrimsonEntity>(280,
            0, 40, 0.85f, 1f, 20, 1.5f, 0.1f, 0f, 3, Blocks.OBSIDIAN.getHardness())
            .withFinisher(36, BARRAGE_FINISHER)
            .withSound(JSoundRegistry.KC_BARRAGE)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender/finisher, medium stun, knocks back")
            );
    public static final KnockdownAttack<KingCrimsonEntity> OVERHEAD_HOOK = new KnockdownAttack<KingCrimsonEntity>(160,
            22, 32, 0.85f, 9f, 11, 2f, 1.5f, 0f, 35)
            .withSound(JSoundRegistry.KC_HEAVY2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withBlockStun(10)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Overhead Hook"),
                    Text.literal("long windup, knockdown")
            );
    public static final SimpleAttack<KingCrimsonEntity> VERTICAL_CHOP = new SimpleAttack<KingCrimsonEntity>(240,
            12, 19, 0.85f, 6f, 25, 1.5f, 0.6f, 0f)
            .withFollowup(OVERHEAD_HOOK)
            .withSound(JSoundRegistry.KC_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withExtraHitBox(0, 0.5, 1)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Vertical Chop"),
                    Text.literal("medium windup combo starter, has a true followup in the form of a slow, armored knockdown")
            );
    public static final BloodThrowAttack BLOOD_THROW = new BloodThrowAttack(260, 10, 15, 1f)
            .withInfo(
                    Text.literal("Blood Throw"),
                    Text.literal("throws a stunning, blinding blood projectile, crouch while it comes out for higher speed")
            );
    public static final EffectInflictingAttack<KingCrimsonEntity> EYE_CHOP = new EffectInflictingAttack<KingCrimsonEntity>(
            280, 20, 29, 1f, 9f, 27, 1.75f, 0.7f, -0.3f,
            List.of(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)))
            .withCrouchingVariant(BLOOD_THROW)
            .withSound(JSoundRegistry.KC_EYE_CHOP)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withExtraHitBox(0, 0.5, 1)
            .withBlockStun(4)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Eye Chop"),
                    Text.literal("blindness on hit, combo starter, low blockstun")
            );
    public static final KCDonutAttack DONUT = new KCDonutAttack(260, 30, 48, 1f,
            14f, 10, 1.75f, 1.5f, 0.1f)
            .withSound(JSoundRegistry.KC_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_7)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Donut"),
                    Text.literal("slow, uninterruptable, extremely damaging launcher")
            );
    public static final EpitaphAttack EPITAPH = new EpitaphAttack(480, 4, 34, -1f)
            .withInfo(
                    Text.literal("Epitaph"),
                    Text.literal("0.2s windup, 1.5s counter, combo starter. Cannot be buffered.")
            );
    public static final PredictionMove PREDICTION = new PredictionMove(600, 4, 104, -1f)
            .withCrouchingVariant(EPITAPH)
            .withSound(JSoundRegistry.KC_EPITAPH)
            .withInfo(
                    Text.literal("Prediction/Move Cancel"),
                    Text.literal("""
                              This move cannot be buffered.
                              Shows the projected future location of nearby entities, using Time Erase will force them to the projected locations. (20s TE cooldown)
                              While predicting, you are slowed down.
                              Move Cancel - Using Special 3 during any move cancels it and puts Time Erase on a 7s cooldown. (But does not require TE to be usable)""")
            );
    public static final TimeEraseMove TIME_ERASE = new TimeEraseMove(1000, 5, 15, 1f, 120)
            .withInfo(
                    Text.literal("Time Erase"),
                    Text.literal("6 seconds duration, cancellable by doing anything with King Crimson")
            );
    public static final TimeSkipMove<KingCrimsonEntity> TIME_SKIP = new TimeSkipMove<KingCrimsonEntity>(300, 16)
            .withSound(JSoundRegistry.TE_TP)
            .withInitAction((attacker, user, ctx) -> attacker.spawnTimeSkipParticles())
            .withInfo(
                    Text.literal("Timeskip"),
                    Text.literal("16m range")
            );

    private static final TrackedData<Integer> TIME_ERASE_TIME;


    public KingCrimsonEntity(World worldIn) {
        super(StandType.KING_CRIMSON, worldIn, JSoundRegistry.KC_SUMMON);

        idleDistance = 1f;
        idleRotation = -65f;

        description = "Close-Range Deadly PUNISHER";

        pros = List.of(
                "extremely high damage output",
                "priceless move cancel",
                "counter",
                "easy setups"
        );

        cons = List.of(
                "below average speed",
                "slow, limited armored options",
                "limited pressure",
                "hard to master"
        );

        freespace = """
                BNBs:
                    -the gamer (THE bnb)
                    M1>Barrage>delay.Move Cancel>M1>Heavy~Overhead
                    
                    -the loop zoopler (sub optimal damage for a setup that kills them if you guess right)
                    Eye Chop>Donut>M1>Heavy~Overhead>Time Erase
                    
                    -hits like a firetruck (death)
                    Donut>Move Cancel>Timeskip>Barrage>Move Cancel>M1>Heavy>Move Cancel>Eye Chop>Sweep
                    """;

        auraColors = new Vector3f[]{
                new Vector3f(1.0F, 0.0F, 0.0F),
                new Vector3f(0.9f, 0.5f, 0.7f),
                new Vector3f(1.0f, 0.4f, 0.4f),
                new Vector3f(0.3f, 0.0f, 0.5f)
        };
    }

    static {
        TIME_ERASE_TIME = DataTracker.registerData(KingCrimsonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getTETime() {
        return dataTracker.get(TIME_ERASE_TIME);
    }

    public void setTETime(int teTime) {
        dataTracker.set(TIME_ERASE_TIME, teTime);
    }

    @Override
    protected void registerMoves(MoveMap<KingCrimsonEntity, State> moves) {
        moves.register(MoveType.LIGHT, DUAL_CHOP, State.DUAL_CHOP).withCrouchingVariant(State.SWEEP);
        moves.register(MoveType.HEAVY, VERTICAL_CHOP, State.HEAVY).withFollowUp(State.OVERHEAD);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, EYE_CHOP, State.EYE_CHOP).withCrouchingVariant(State.BLOOD_THROW);
        moves.register(MoveType.SPECIAL2, DONUT, State.DONUT);
        moves.register(MoveType.SPECIAL3, PREDICTION, State.PREDICT).withCrouchingVariant(State.EPITAPH);
        moves.register(MoveType.ULTIMATE, TIME_ERASE, State.TIME_ERASE);

        moves.register(MoveType.UTILITY, TIME_SKIP, State.TIME_SKIP);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(TIME_ERASE_TIME, 0);
    }

    @Override
    public boolean initMove(MoveType type) {
        switch (type) {
            case HEAVY -> {
                boolean idling = getMoveStun() <= 0;

                if (curMove == null || curMove.getOriginalMove() != VERTICAL_CHOP) {
                    if (idling)
                        return super.initMove(type);
                    else
                        return false;
                } else if (getMoveStun() < 7) setMove(OVERHEAD_HOOK, State.OVERHEAD);
            }
            case ULTIMATE -> {
                // If predicting, and Time Erase isn't on cooldown
                if (curMove != null && curMove.getOriginalMove() == PREDICTION && hasUser()) {
                    CooldownsComponent cooldowns = JComponents.getCooldowns(getUser());
                    if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) <= 0) {
                        cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 400);
                        PredictionMove.finishPrediction(this);
                    }
                }

                // If not predicting, do other Time Erase logic
                if (!canAttack())
                    return false;

                if (getTETime() > 0) {
                    cancelTE();
                    return true;
                }

                return super.initMove(type);
            }
            case SPECIAL3 -> {
                LivingEntity user = getUserOrThrow();
                boolean start = getMoveStun() <= 0;

                if (start) return super.initMove(type);

                // When used during a move, cancels it and puts time erase on cooldown
                moveCancel();

                // 7 second time erase cooldown
                CooldownsComponent cooldowns = JComponents.getCooldowns(user);
                if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) < 140)
                    cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 140);

                // Particle effects
                Vec3d oPos = user.getPos();
                Box bBox = user.getBoundingBox();
                for (ServerPlayerEntity serverPlayer : ((ServerWorld) getWorld()).getPlayers()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeVarInt(2);
                    buf.writeDouble(oPos.x);
                    buf.writeDouble(oPos.y);
                    buf.writeDouble(oPos.z);
                    buf.writeDouble(bBox.getXLength());
                    buf.writeDouble(bBox.getYLength());
                    buf.writeDouble(bBox.getZLength());
                    ServerChannelFeedbackPacket.send(serverPlayer, buf);
                }

                // Stop epitaph state
                if (user instanceof ServerPlayerEntity player)
                    ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE, new PacketByteBuf(Unpooled.buffer().writeBoolean(false)));
            }
            case UTILITY -> {
                if (getTETime() > 0) cancelTE();
                return super.initMove(type);
            }
            default -> {
                return super.initMove(type);
            }
        }

        return true;
    }

    private void spawnTimeSkipParticles() {
        LivingEntity user = getUserOrThrow();

        Vec3d pos = user.getPos();
        Box bBox = user.getBoundingBox();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(2);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(bBox.getXLength());
        buf.writeDouble(bBox.getYLength());
        buf.writeDouble(bBox.getZLength());

        PlayerLookup.world((ServerWorld) getWorld()).forEach(serverPlayer -> ServerChannelFeedbackPacket.send(serverPlayer, buf));
    }

    public void moveCancel() {
        // Epitaph
        PredictionMove.cancelPrediction(this);

        // General
        curMove = null;
        queuedMove = null;

        setMoveStun(2);
        setState(State.IDLE);
        setReset(true);
    }

    @Override
    public void desummon() {
        if (this.getTETime() < 1)
            super.desummon();
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getTETime() > 0) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x, y, z, x, y + 0.1, z);
        }
        return super.calculateBoundingBox();
    }

    public void cancelTE() {
        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) - getTETime() * 2);

        setTETime(0);
        MobEntity doppelganger = moveContext.get(TimeEraseMove.DOPPELGANGER);
        if (doppelganger != null) doppelganger.discard();

        if (user instanceof ServerPlayerEntity serverPlayer)
            ShaderDeactivationPacket.send(serverPlayer, ShaderActivationPacket.Type.CRIMSON);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity user = this.getUser();
        if (user == null) return;
        if (getWorld().isClient) return;
        TIME_ERASE.tickTimeErase(this);
    }

    @Override
    public void queueMove(MoveInputType type) {
        if (type == MoveInputType.SPECIAL3) return;
        super.queueMove(type);
    }

    @Override
    @NonNull
    public KingCrimsonEntity getThis() {
        return this;
    }

    // Animations
    public enum State implements StandAnimationState<KingCrimsonEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.kingcrimson.idle"))),
        DUAL_CHOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.dual_chop"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.kingcrimson.block"))),
        OVERHEAD(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.overhead"))),
        DONUT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.donut"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.kingcrimson.barrage"))),
        EYE_CHOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.eye_chop"))),
        TIME_ERASE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.time_erase"))),
        EPITAPH(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.epitaph"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.heavy"))),
        BLOOD_THROW(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.bloodthrow"))),
        PREDICT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.predict"))),
        COUNTER_MISS(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.counter_miss"))),
        SWEEP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.kingcrimson.sweep"))),
        TIME_SKIP(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.kingcrimson.idle")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KingCrimsonEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.kingcrimson.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
