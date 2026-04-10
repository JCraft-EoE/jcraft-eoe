package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.PilotModeMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleUppercutAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.ChargedSpewAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.GiveStandAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.MeltYourHeartAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.PoisonSpewAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.StealStandAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Whitesnake">Whitesnake</a>.
 * @see JStandTypeRegistry#WHITE_SNAKE
 * @see net.arna.jcraft.client.renderer.entity.stands.WhiteSnakeRenderer WhiteSnakeRenderer
 * @see ChargedSpewAttack
 * @see GiveStandAttack
 * @see MeltYourHeartAttack
 * @see PoisonSpewAttack
 */
public class WhiteSnakeEntity extends StandEntity<WhiteSnakeEntity, WhiteSnakeEntity.State> {
    public static final MoveSet<WhiteSnakeEntity, State> DEFAULT_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.WHITE_SNAKE,
            WhiteSnakeEntity::registerDefaultMoves, State.class);
    public static final MoveSet<WhiteSnakeEntity, State> REMOTE_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.WHITE_SNAKE,
            "remote", WhiteSnakeEntity::registerRemoteMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(220f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.whitesnake"))
                    .proCount(3)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                            BNBs:
                                -the gimp
                                Light>Gut Punch>Poison Spew
                            
                                -the el mayo (optimal damage with disk moves)
                                Memory Disk>Light>Barrage>Leg Crusher>Stand Disk>Light~Light
                            
                                -the gazebo (optimal damage without disk)
                                Light>Barrage>Leg Crusher>Gut Punch>Light~Light
                            
                                -the protein shake (sets up mixups)
                                Light>Barrage>Leg Crusher>Charged Spew"""))
                    .skinName(Component.literal("Mamba"))
                    .skinName(Component.literal("Peppermint"))
                    .skinName(Component.literal("Radioactive"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.WS_SUMMON))
            .build();

    public static final SimpleUppercutAttack<WhiteSnakeEntity> UPPERCUT = new SimpleUppercutAttack<WhiteSnakeEntity>(0,
            8, 14, 1, 6f, 16, 1.25f, 0.5f, -0.5f, 0.5f)
            .withAnim(State.UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withExtraHitBox(1)
            .withInfo(
                    Component.literal("Uppercut"),
                    Component.literal("decent stun, launches up")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT_FOLLOWUP = new SimpleAttack<WhiteSnakeEntity>(0,
            7, 13, 0.75f, 6f, 10, 1.5f, 1f, 0.2f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Finisher"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT = SimpleAttack.<WhiteSnakeEntity>lightAttack(
                    7, 11, 0.75f, 5f, 13, 0.2f, 0.2f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final SimpleAttack<WhiteSnakeEntity> MEDIUM = new SimpleAttack<WhiteSnakeEntity>(0,
            8, 13, 1, 7f, 16, 1.75f, 0.4f, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Gut Punch"),
                    Component.literal("combo starter/extender")
            );
    public static final MainBarrageAttack<WhiteSnakeEntity> BARRAGE = new MainBarrageAttack<WhiteSnakeEntity>(240,
            0, 40, 0.75f, 1, 20, 2, 0.25f, 0, 3, Blocks.OAK_PLANKS.defaultDestroyTime())
            .withSound(JSoundRegistry.WS_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final GiveStandAttack GIVE_STAND = new GiveStandAttack(400,
            22, 34, 1, 1, 2, 0, 0)
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(null)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Component.literal("Give Stand Disk"),
                    Component.literal("gives a single hit target a stand, provided they do not have one already, from a disk in the user's off hand")
            );
    public static final StealStandAttack STAND_DISC = new StealStandAttack(480,
            22, 34, 1, 8f, 20, 2, 0.5f, 0)
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withAction(EffectAction.inflict(JStatusRegistry.STANDLESS, 160, 0))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withCrouchingVariant(GIVE_STAND)
            .withInfo(
                    Component.literal("Take Stand Disk"),
                    Component.literal("uninterruptible & unblockable, removes enemy stand for 8s")
            );
    public static final SimpleAttack<WhiteSnakeEntity> LEG_CRUSHER = new SimpleAttack<WhiteSnakeEntity>(0,
            16, 22, 0.75f, 7, 32, 1.75f, 0.35f, 0.4f)
            .withSound(JSoundRegistry.WS_LEGCRUSH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Leg Crusher"),
                    Component.literal("high stun, medium windup")
            );
    public static final SimpleAttack<WhiteSnakeEntity> MEMORY_DISC = new SimpleAttack<WhiteSnakeEntity>(140,
            22, 34, 1, 7f, 20, 2, 0.5f, 0)
            .withSound(JSoundRegistry.WS_MEMORY_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withAction(EffectAction.inflict(
                    new MobEffectInstance(MobEffects.WEAKNESS, 600, 0),
                    new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, 0)
            ))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Take Memory Disk"),
                    Component.literal("uninterruptible& unblockable, gives mining fatigue & weakness for 30s")
            );
    public static final ChargedSpewAttack CHARGED_SPEW = new ChargedSpewAttack(160,
            20, 26, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(
                    Component.literal("Poison Spew"),
                    Component.literal("fires a spread of 5 acid projectiles that slow enemies and persist on the surface they hits for 5s")
            );
    public static final PoisonSpewAttack POISON_SPEW = new PoisonSpewAttack(100,
            10, 14, 0.75f, 0f, 0, 2f, 0f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withCrouchingVariant(CHARGED_SPEW)
            .withInfo(
                    Component.literal("Poison Spew"),
                    Component.literal("fires an acid projectile that slows enemies and persists on the surface it hits for 5s")
            );
    public static final MeltYourHeartAttack MELT_YOUR_HEART = new MeltYourHeartAttack(800,
            40, 50, 1f, 3f, 20, 2f, 1f, 0f)
            .withSound(JSoundRegistry.WS_MYH)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withLaunch()
            .withInfo(
                    Component.literal("Melt your Heart"),
                    Component.literal("remote-only and armored, expels a sphere of poison")
            );
    public static final PilotModeMove<WhiteSnakeEntity> PILOT_MODE = new PilotModeMove<WhiteSnakeEntity>(20)
            .withInfo(
                    Component.literal("Pilot Mode"),
                    Component.empty()
            );

    public WhiteSnakeEntity(Level worldIn) {
        super(JStandTypeRegistry.WHITE_SNAKE.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1f, 1f, 1f),
                new Vector3f(1f, 1f, 1f),
                new Vector3f(0.4f, 0.4f, 0.5f),
                new Vector3f(1.0f, 0.0f, 0.0f)
        };
    }

    private static void registerDefaultMoves(MoveMap<WhiteSnakeEntity, State> moves) {
        registerMoves(moves, false);
    }

    private static void registerRemoteMoves(MoveMap<WhiteSnakeEntity, State> moves) {
        registerMoves(moves, true);
    }

    private static void registerMoves(MoveMap<WhiteSnakeEntity, State> moves, boolean remote) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.register(MoveClass.HEAVY, MEDIUM, State.MEDIUM);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, MEMORY_DISC, State.DISC_TAKE);
        moves.register(MoveClass.SPECIAL2, LEG_CRUSHER, State.LEG_CRUSHER);
        moves.register(MoveClass.SPECIAL3, POISON_SPEW, State.ACID_SPEW).withCrouchingVariant(State.ACID_SPEW_CHARGED);
        if (remote) {
            moves.register(MoveClass.ULTIMATE, MELT_YOUR_HEART, State.MELT_YOUR_HEART);
        } else {
            moves.register(MoveClass.ULTIMATE, STAND_DISC, State.DISC_TAKE).withCrouchingVariant(State.DISC_GIVE);
        }

        moves.register(MoveClass.UTILITY, PILOT_MODE);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) {
            return true;
        } else {
            return super.initMove(moveClass);
        }
    }

    @Override
    public void tick() {
        super.tick();
        idleOverride = isRemote();

        if (!isRemoteAndControllable()) {
            return;
        }

        if (level().isClientSide) {
            // Called for EVERYONE
            JCraft.getClientEntityHandler().whiteSnakeRemoteClientTick(this);
        } else {
            final double f = getRemoteForwardInput(), s = getRemoteSideInput();
            final boolean jump = getRemoteJumpInput();

            tickRemoteMovement(f, s, jump);

            if (getMoveStun() <= 0) {
                if (f == 0) {
                    if (s > 0) {
                        setStateNoReset(onGround() ? State.RIGHT : State.RIGHT_DASH);
                    }
                    if (s < 0) {
                        setStateNoReset(onGround() ? State.LEFT : State.LEFT_DASH);
                    }
                } else {
                    if (f < 0) {
                        setStateNoReset(onGround() ? State.BACKWARD : State.BACKWARD_DASH);
                    }
                    if (f > 0) {
                        setStateNoReset(onGround() ? State.FORWARD : State.FORWARD_DASH);
                    }
                }
            }
        }
    }

    /**
     * Movement control for a grounded remote stand.
     *
     * @param f    Forward input
     * @param s    +Right/-Left input
     * @param jump Jump input
     */
    public void tickRemoteMovement(double f, double s, boolean jump) {
        // 1 tick of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - tickCount > 2) {
            updateRemoteInputs(0, 0, false, false);
        }
        final Vec3 pos = position();
        final Vec3 rotVec = new Vec3(getLookAngle().x, 0, getLookAngle().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        final boolean onGround = onGround();
        final boolean climbing = getFeetBlockState().getTags().anyMatch(tag -> tag == BlockTags.CLIMBABLE);
        final boolean swimming = !level().getFluidState(blockPosition()).isEmpty();

        if (climbing || swimming) {
            dragMult *= 0.5;
        }

        if ((climbing || swimming) && jump) { // Climb or Swim
            push(0, 0.1, 0);
        } else { // Jump
            if (onGround) {
                if (jump) {
                    push(0, 0.75, 0);
                    setRemoteJumpInput(false);
                }
            } else {
                //JCraft.LOGGER.info("Airborne");
                moveSpeed = 0.024;
                dragMult = 0.4;
            }
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.scale(f * moveSpeed)) // Forward movement
                .add(rotVec.yRot(1.5707963f).scale(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.scale(dragMult);

        final Vec3 userPos = getUserOrThrow().position();
        if (pos.add(remoteSpeed).distanceToSqr(userPos) > 400) {
            remoteSpeed = userPos.subtract(pos).scale(0.025); // 1/40th so it scales with distance
        }

        if (f == 0 && s == 0 && !jump) {
            push(-getDeltaMovement().x * 0.4, -getDeltaMovement().y * 0.4, -getDeltaMovement().z * 0.4);
        }

        push(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        hurtMarked = true;
    }

    @Override
    protected void beginRemote() {
        super.beginRemote();
        switchMoveSet(REMOTE_MOVE_SET.getName());
    }

    @Override
    protected void endRemote() {
        super.endRemote();
        switchMoveSet(DEFAULT_MOVE_SET.getName());
    }

    @Override
    @NonNull
    public WhiteSnakeEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<WhiteSnakeEntity> {
        // TODO reenable remote idle
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.block", AzPlayBehaviors.LOOP)),
        MEDIUM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.medium", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.barrage", AzPlayBehaviors.LOOP)),
        LEG_CRUSHER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.legcrusher", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ACID_SPEW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.acidspew", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ACID_SPEW_CHARGED(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.acidspew_charged", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DISC_TAKE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.disc_take", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DISC_GIVE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.disc_give", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        UPPERCUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.uppercut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        FORWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.forw", AzPlayBehaviors.LOOP)),
        BACKWARD(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.back", AzPlayBehaviors.LOOP)),
        LEFT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.left", AzPlayBehaviors.LOOP)),
        RIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.right", AzPlayBehaviors.LOOP)),
        FORWARD_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.fdash", AzPlayBehaviors.LOOP)),
        BACKWARD_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.bdash", AzPlayBehaviors.LOOP)),
        LEFT_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.ldash", AzPlayBehaviors.LOOP)),
        RIGHT_DASH(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.whitesnake.rdash", AzPlayBehaviors.LOOP)),

        MELT_YOUR_HEART(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.meltyourheart", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.whitesnake.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(WhiteSnakeEntity attacker) {
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
