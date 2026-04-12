package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.LevitationPoseModifier;
import net.arna.jcraft.api.registry.JMarkerExtractorRegistry;
import net.arna.jcraft.api.registry.JMarkerInjectorRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.common.attack.moves.goldexperience.requiem.*;
import net.arna.jcraft.common.attack.moves.mandom.CountdownMove;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation.CRUSH;
import static net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation.HIGH;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Gold_Experience_Requiem">Gold Experience Requiem</a>.
 * @see JStandTypeRegistry#GOLD_EXPERIENCE_REQUIEM
 * @see FlightMove
 * @see LifeBeamAttack
 * @see NullificationAttack
 * @see OverheadKickAttack
 * @see ReturnToZeroMove
 */
public class GEREntity extends StandEntity<GEREntity, GEREntity.State> {
    public static final MoveSet<GEREntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.GOLD_EXPERIENCE_REQUIEM,
            GEREntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-30f)
            .evolution(true)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.ger"))
                    .proCount(5)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                BNBs:
                -the scorpy patty (sets up stand off transition)
                (Light>)Barrage>jump>Overhead Kick>Life Beam>Light>Life Beam (second hit)
                -knockdown experience
                Light>Barrage>Life Beam>Light~Overhead Smash>Life Beam (second hit)"""))
                    .skinName(Component.literal("Silver"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Cherry Blossom"))
                    .build())
            .build();
    public static final Supplier<IPoseModifier> POSE = LevitationPoseModifier::new;

    public static final SimpleAttack<GEREntity> LIGHT_FOLLOWUP = new SimpleAttack<GEREntity>(
            0, 6, 13, 0.75f, 6f, 8, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<GEREntity> DOWNWARD_KICK = new SimpleAttack<GEREntity>(JCraft.LIGHT_COOLDOWN,
            5, 12, 0.75f, 4f, 20, 1.25f, 0.4f, 0.33f)
            .withAnim(State.AIR_LIGHT)
            .withFollowup(LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -1, 1)
            .withHitAnimation(HIGH)
            .withInfo(
                    Component.literal("Downward Kick"),
                    Component.literal("medium stun combo starter, low hitbox, low blockstun")
            );
    public static final OverheadKickAttack OVERHEAD_KICK = new OverheadKickAttack(24,
            14, 24, 1f, 9f, 40, 1.5f, 0.8f, 0.25f)
            .withSound(JSoundRegistry.GER_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, -1, 1)
            .withHitAnimation(CRUSH)
            .withInfo(
                    Component.literal("Overhead Kick"),
                    Component.literal("slow, high stun combo starter")
            );
    public static final SimpleAttack<GEREntity> KICK_BARRAGE_FINISHER = new SimpleAttack<GEREntity>(0,
            6, 9, 1f, 1f, 10, 1.75f, 1.1f, 0f)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunchNoShockwave()
            .withInfo(
                    Component.literal("Kick Barrage (Final Hit)"),
                    Component.empty()
            );
    public static final BarrageAttack<GEREntity> KICK_BARRAGE = new BarrageAttack<GEREntity>(140, 0, 48,
            1f, 1f, 20, 1.5f, 0.3f, 0f, 3)
            .withFinisher(37, KICK_BARRAGE_FINISHER)
            .withSound(JSoundRegistry.GER_KICKBARRAGE)
            .withInfo(
                    Component.literal("Kick Barrage"),
                    Component.literal("fast combo finisher, knocks back")
            );
    // JCraft.lightCooldown -> 0 | 0.55f -> 0.4f
    public static final SimpleAttack<GEREntity> PUNCH = new SimpleAttack<GEREntity>(JCraft.LIGHT_COOLDOWN / 2,
            5, 9, 0.75f, 5f, 8, 1.5f, 0.2f, -0.1f)
            .noLoopPrevention()
            .withAerialVariant(DOWNWARD_KICK)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(4)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );

    // TODO: add an armored, high-damage knockdown with a cooldown (cr.H) which does the job of this move but without being spam material
    public static final KnockdownAttack<GEREntity> OVERHEAD_SMASH = new KnockdownAttack<GEREntity>(0, 10, 19,
            1f, 9f, 10, 1.5f, 1.1f, 0f, 30)
            .withAerialVariant(OVERHEAD_KICK)
            .withSound(JSoundRegistry.GER_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withArmor(1)
            .withLaunch()
            .withExtraHitBox(1.5)
            .withInfo(
                    Component.literal("Overhead Smash"),
                    Component.literal("slow, uninterruptible knockdown")
            );
    public static final MainBarrageAttack<GEREntity> BARRAGE = new MainBarrageAttack<GEREntity>(280, 0, 30,
            0.75f, 1f, 20, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.defaultDestroyTime())
            .withAerialVariant(KICK_BARRAGE)
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );
    public static final HealMove<GEREntity> HEAL = new HealMove<GEREntity>(520, 10, 16,
            1f, 1.25f, 0f, 6f, HealMove.HealTarget.TARGETS, true)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand (Others)"),
                    Component.empty()
            );
    public static final HealMove<GEREntity> HEAL_SELF = new HealMove<GEREntity>(520, 10, 14,
            1f, 0f, 0f, 4f, HealMove.HealTarget.USER, false)
            .withCrouchingVariant(HEAL)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand"),
                    Component.literal("standing: heals user for 2 hearts, crouching: heals others for 3 hearts, pacifies angered mobs")
            );
    public static final LifeBeamAttack LIFE_BEAM = new LifeBeamAttack(0, 1, 10, 1.1f)
            .withSound(JSoundRegistry.GER_LASER_FIRE)
            .withInfo(
                    Component.literal("Life Beam"),
                    Component.literal("")
            );
    public static final SimpleHoldableMove<GEREntity> LIFE_BEAM_CHARGE = new SimpleHoldableMove<GEREntity>(100,
            0, 40, 1.1f, 9)
            .withFollowup(LIFE_BEAM)
            .withSound(JSoundRegistry.GER_LASER)
            .withInfo(
                    Component.literal("Life Beam"),
                    Component.literal("""
                            Summons a fast rock projectile that turns into a homing scorpion a small time after landing.
                            If charged for a minimum of 0.9 seconds, the scorpion inflicts poison and deals more stun.""")
            );
    public static final NullificationAttack NULLIFICATION = new NullificationAttack(480, 5, 35, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Nullification"),
                    Component.literal("0.25s windup, 1.5s counter, stuns on hit")
            );
    public static final ReturnToZeroMove RETURN_TO_ZERO = new ReturnToZeroMove(1200, 30, 32, 1f, 64, 200, CountdownMove.ENTITY_STUFF_TO_SAVE, JMarkerExtractorRegistry.ALL.get(), JMarkerInjectorRegistry.ALL.get())
            .withSound(JSoundRegistry.GER_SETUP)
            .withInfo(
                    Component.literal("Return to Zero"),
                    Component.literal("initial press: saves the state of " +
                            "every entity in a 4 chunk radius, second press: reverts all states except users\nDoesn't affect player inventories")
            );
    public static final FlightMove FLIGHT = new FlightMove(320, 1, 0, 0f, 20)
            .withSound(JSoundRegistry.GER_FLY)
            .withInfo(
                    Component.literal("Flight"),
                    Component.literal("1 second of flight")
            );

    private static final EntityDataAccessor<Integer> FLIGHT_TIME = SynchedEntityData.defineId(GEREntity.class, EntityDataSerializers.INT);

    public GEREntity(Level worldIn) {
        super(JStandTypeRegistry.GOLD_EXPERIENCE_REQUIEM.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.7f, 0.8f, 1.0f),
                new Vector3f(0.8f, 0.7f, 1.0f),
                new Vector3f(1.0f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.0f, 1.0f)
        };
    }

    private static void registerMoves(MoveMap<GEREntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, PUNCH, State.LIGHT);
        moves.register(MoveClass.HEAVY, OVERHEAD_SMASH, State.HEAVY).withAerialVariant(State.AIR_HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE).withAerialVariant(State.AIR_BARRAGE);

        moves.register(MoveClass.SPECIAL1, HEAL_SELF, State.HEAL_SELF).withCrouchingVariant(State.HEAL);
        moves.register(MoveClass.SPECIAL2, LIFE_BEAM_CHARGE, State.LASER).withFollowup(State.LASER_FIRE);
        moves.register(MoveClass.SPECIAL3, NULLIFICATION, State.COUNTER);
        moves.register(MoveClass.ULTIMATE, RETURN_TO_ZERO, State.SETUP);

        moves.register(MoveClass.UTILITY, FLIGHT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLIGHT_TIME, 0);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.ULTIMATE) {
            final ReturnToZeroMove rtzMove = getMoveMap().findMoveByType(ReturnToZeroMove.class).orElse(null);
            if (rtzMove != null && rtzMove.returnToZero(this)) {
                return true;
            } else {
                return super.initMove(moveClass);
            }
        } else if (tryFollowUp(moveClass, MoveClass.LIGHT)) {
            return true;
        } else {
            return super.initMove(moveClass);
        }
    }

    public int getFlightTime() {
        return this.entityData.get(FLIGHT_TIME);
    }

    public void setFlightTime(int i) {
        this.entityData.set(FLIGHT_TIME, i);
    }

    @Override
    public void desummon() {
        if (getFlightTime() > 0) {
            setFlightTime(0);
            return;
        }
        super.desummon();
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (getUser() instanceof Player player && player.getAbilities().flying && !player.isCreative() && !player.isSpectator()) {
            final Abilities abilities = player.getAbilities();

            abilities.flying = false;
            abilities.mayfly = false;

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
            }
        }
        super.remove(reason);
    }

    @Override
    public void tick() {
        if (tickCount == 1) {
            playSound(JSoundRegistry.GER_SUMMON.get(), 1f, 1f);
        }
        super.tick();

        // TODO client move ticker
        if (level().isClientSide) {
            // Fully charged life beam particle effect
            if (getState() == State.LASER && getMoveStun() == (LIFE_BEAM_CHARGE.getDuration() - 18)) {
                final Vec3 offset = GravityChangerAPI.getEyeOffset(this);
                final double x = getX() + offset.x, y = getY() + offset.y, z = getZ() + offset.z;
                for (int i = 0; i < 12; i++) {
                    level().addParticle(ParticleTypes.WITCH, x, y, z, random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
                }
            }

            if (getUser() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().flying = (getFlightTime() > 1);
            }
        }
    }

    @Override
    @NonNull
    public GEREntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<GEREntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.ger.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.ger.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.barrage", AzPlayBehaviors.LOOP)),
        HEAL_SELF(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.healself", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.heal", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LASER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.laser", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LASER_FIRE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.laser_fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.counter", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_MISS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.airheavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.airlight", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        AIR_BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.airbarrage", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SETUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.setup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ger.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GEREntity attacker) {
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
