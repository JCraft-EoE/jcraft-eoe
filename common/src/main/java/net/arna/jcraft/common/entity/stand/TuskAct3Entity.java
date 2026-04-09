package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.attack.conditions.TuskNailCondition;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import java.lang.ref.WeakReference;
import net.minecraft.world.phys.Vec3;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.tusk.*;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;

public class TuskAct3Entity extends StandEntity<TuskAct3Entity, TuskAct3Entity.State> {
    public static final MoveSet<TuskAct3Entity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.TUSK_ACT_3,
            TuskAct3Entity::registerMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(315f)
            .blockDistance(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.tusk_act3"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                            Contains up to 10 nails. Nails regenerate passively at the cost of hunger.
                            Herbal Tea grants Keratin Growth, speeding up nail regen."""))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.TUSK_MIMIMIN))
            .build();

    public static final EntityDataAccessor<Float> NAILS = SynchedEntityData.defineId(TuskAct3Entity.class, EntityDataSerializers.FLOAT);
    public static final float NAILS_MAX = 10.0f;

    // ---- Moves ----

    public static final Act3NailShotAttack NAIL_SHOT = new Act3NailShotAttack(0, 1, 15, 0.75f, 4.0f, 20.0f, 15.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInfo(
                    Component.literal("Golden Rectangle Nail"),
                    Component.literal("Fires a spinning nail (15 blocks). On hit, creeps 8 blocks through terrain."));

    // Heavy: Nail Jab — close-range spinning nail poke
    public static final SimpleAttack<TuskAct3Entity> NAIL_JAB = new SimpleAttack<TuskAct3Entity>(
            200, 5, 15, 0.5f, 7.0f, 15, 2f, 0.5f, 0.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withSound(JSoundRegistry.ARMORED_HIT)
            .withInfo(
                    Component.literal("Nail Jab"),
                    Component.literal("Close-range nail poke."));

    // Barrage: Nail Swipes — 5 hits, quick startup
    public static final NailBarrageAttack NAIL_SWIPES = new NailBarrageAttack(
            280, 2, 50, 1.5f, 2.0f, 8, 2.0f, 0.3f, 0.0f, 10)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withSound(JSoundRegistry.CREAM_COMBO)
            .withInfo(
                    Component.literal("Nail Swipes"),
                    Component.literal("Quick 5-hit nail swipes. Medium cooldown."));

    public static final WormholeAttack WORMHOLE = new WormholeAttack(100, 5, 10, 0.75f)
            .withInfo(
                    Component.literal("Wormhole Nail"),
                    Component.literal("First use: fires a slow homing nail.\nSecond use: teleports to nail."))
            .withCondition(TuskNailCondition.atLeast(1.0f));

    public static final HandholeAttack HANDHOLE = new HandholeAttack(80, 8, 15, 0.75f)
            .withInfo(
                    Component.literal("Handhole"),
                    Component.literal("Fires a hole onto a surface. While active, Light fires from that position.\nPress Sp1 again to recall the arm."))
            .withCondition(TuskNailCondition.atLeast(1.0f));

    public static final VortexAttack VORTEX = new VortexAttack(120, 8, 15, 0.75f)
            .withInfo(
                    Component.literal("Vortex"),
                    Component.literal("Fires a hole that drags nearby entities (and the user) toward it."))
            .withCondition(TuskNailCondition.atLeast(1.0f));

    public static final VoidShotAttack VOID_SHOT = new VoidShotAttack(300, 10, 30, 0.75f)
            .withInfo(
                    Component.literal("Voidshot"),
                    Component.literal("Fire at your own head and enter the wormhole.\nInvulnerable for up to 1 second. Punishable on entry/exit."))
            .withCondition(TuskNailCondition.atLeast(1.0f));


    public static final TuskActCycleMove<TuskAct3Entity> ACT_CYCLE = new TuskActCycleMove<TuskAct3Entity>(
            0, 1, 1, 0f, 3, false)
            .withInfo(
                    Component.literal("Cycle Act"),
                    Component.literal("Cycle through unlocked Tusk acts.\n" +
                            "Utility → Cycle forward (Act 1→2→3→1...)\n" +
                            "Shift + Utility → Cycle backward (Act 3→2→1→3...)"));

    @Getter
    @Setter
    private CommonMiscComponent miscComponent;

    /** Ticks remaining of Voidshot nail-piloting (server-side). */
    private int voidTicks = 0;
    private int regenTick = 40;
    private WeakReference<NailProjectile> voidNail = null;

    public TuskAct3Entity(Level worldIn) {
        super(JStandTypeRegistry.TUSK_ACT_3.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.4f, 0.8f),
                new Vector3f(0.8f, 0.2f, 0.6f),
                new Vector3f(0.9f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.5f, 0.9f)
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(NAILS, NAILS_MAX);
    }

    @Override
    public void setUser(@Nullable LivingEntity user) {
        super.setUser(user);
        if (user == null) return;
        miscComponent = JComponentPlatformUtils.getMiscData(getUser());
        if (miscComponent == null) return;
        setNails(miscComponent.getTuskNails());
    }

    private static void registerMoves(MoveMap<TuskAct3Entity, State> moves) {
        moves.register(MoveClass.LIGHT, NAIL_SHOT, State.NAIL_SHOT);
        moves.register(MoveClass.BARRAGE, NAIL_SWIPES, State.NAIL_SWIPES);
        moves.register(MoveClass.HEAVY, NAIL_JAB, State.NAIL_JAB);
        moves.register(MoveClass.SPECIAL1, HANDHOLE, State.HANDHOLE);
        moves.register(MoveClass.SPECIAL2, VORTEX, State.VORTEX);
        moves.register(MoveClass.SPECIAL3, VOID_SHOT, State.VOID_SHOT);
        moves.register(MoveClass.ULTIMATE, WORMHOLE, State.WORMHOLE);
        moves.register(MoveClass.UTILITY, ACT_CYCLE, State.ACT_CYCLE);
    }

    public float getNails() {
        return entityData.get(NAILS);
    }

    public void setNails(float nails) {
        entityData.set(NAILS, nails);
        if (miscComponent == null) return;
        miscComponent.setTuskNails(nails);
    }

    public void addNails(float add) {
        setNails(Mth.clamp(entityData.get(NAILS) + add, 0f, NAILS_MAX));
    }

    public boolean drainNails(float amount) {
        float nails = getNails();
        if (nails < amount) return false;
        setNails(nails - amount);
        return true;
    }

    /** Enter Voidshot — user becomes the nail for up to VOID_DURATION ticks. */
    public void enterVoid(NailProjectile nail) {
        voidTicks = VoidShotAttack.VOID_DURATION;
        voidNail = new WeakReference<>(nail);
        LivingEntity user = getUser();
        if (user != null) {
            user.setInvulnerable(true);
            user.setInvisible(true); // hide player — they are the nail now
        }
    }

    /** Force the user out of the void early. */
    public void exitVoid() {
        voidTicks = 0;
        NailProjectile nail = voidNail == null ? null : voidNail.get();
        if (nail != null && nail.isAlive()) nail.discard();
        voidNail = null;
        LivingEntity user = getUser();
        if (user != null) {
            user.setInvulnerable(false);
            user.setInvisible(false);
        }
    }

    public boolean isInVoid() {
        return voidTicks > 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && hasUser()) {
            // Nail regeneration
            LivingEntity user = getUser();
            if (user instanceof Player player) {
                if (player.isCreative()) {
                    setNails(NAILS_MAX);
                } else if (--regenTick <= 0) {
                    regenTick = Math.max(1, TuskAct1Entity.calcNailRegenInterval(player));
                    if (getNails() < NAILS_MAX) {
                        addNails(1.0f);
                        TuskAct1Entity.drainNailResource(player);
                        playSound(JSoundRegistry.TUSK_NAIL_GROWTH.get(), 0.5f, 1.0f);
                    }
                }
            }

            // Voidshot — pilot the nail
            if (voidTicks > 0) {
                NailProjectile nail = voidNail == null ? null : voidNail.get();
                if (nail != null && nail.isAlive() && !nail.inGround) {
                    // Steer nail by player look direction
                    Vec3 lookVec = user.getLookAngle();
                    Vec3 currentVel = nail.getDeltaMovement();
                    nail.setDeltaMovement(currentVel.lerp(lookVec.scale(currentVel.length()), 0.3));
                    // Teleport user (and stand) to nail position
                    user.teleportTo(nail.getX(), nail.getY(), nail.getZ());
                    voidTicks--;
                } else {
                    // Nail stopped or gone — exit void
                    exitVoid();
                }
                if (voidTicks <= 0) {
                    exitVoid();
                }
            }
        }
    }

    @Override
    public void onUserMoveInput(AbstractMove<?, ? super TuskAct3Entity> currentMove, MoveInputType type, boolean pressed, boolean moveInitiated) {
        if (!pressed) return;

        // Exit void if player tries to use any move inside it
        if (isInVoid()) {
            exitVoid();
            return;
        }

        MoveClass moveClass = type.getMoveClass(standby);
        if (moveClass == null) return;

        // Delegate non-stand moves to spec
        if (moveClass != MoveClass.LIGHT && moveClass != MoveClass.HEAVY
                && moveClass != MoveClass.BARRAGE && moveClass != MoveClass.ULTIMATE
                && moveClass != MoveClass.SPECIAL1 && moveClass != MoveClass.SPECIAL2
                && moveClass != MoveClass.SPECIAL3 && moveClass != MoveClass.UTILITY) {
            if (hasUser() && getUser() instanceof Player player) {
                JSpec<?, ?> spec = JUtils.getSpec(player);
                if (spec != null && spec.canAttack()) {
                    if (spec.initMove(moveClass)) {
                    } else if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT) {
                        spec.queuedMove = type;
                    }
                }
            }
            return;
        }

        // Handhole intercept: while hole is active, only fire from hole (never normal M1)
        if (moveClass == MoveClass.LIGHT) {
            HandholeAttack handhole = getMove(HandholeAttack.class);
            if (handhole != null && handhole.isHoleActive()) {
                handhole.tryFireFromHole(this, getUser());
                return; // Always suppress normal M1 when hole is active
            }
        }

        // Wormhole teleport: bypass ALL cooldown/movestun — activates regardless of state, like BTD
        if (moveClass == MoveClass.ULTIMATE) {
            WormholeAttack wormhole = getMove(WormholeAttack.class);
            if (wormhole != null && wormhole.isNailActive()) {
                wormhole.doTeleportNow(this, getUser());
                return;
            }
        }

        if (canAttack()) {
            initMove(moveClass);
        } else if (getMoveStun() > 0 && getMoveStun() < QUEUE_MOVESTUN_LIMIT) {
            queueMove(type);
        }
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.UTILITY) {
            TuskActCycleMove.tryCycle(3, getUser());
            return true;
        }
        return super.initMove(moveClass);
    }

    @Override
    public boolean allowMoveHandling() {
        return true;
    }

    @Override
    public void tryBlock() {
    }

    @Override
    @NonNull
    public TuskAct3Entity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<TuskAct3Entity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.idle", AzPlayBehaviors.LOOP)),
        NAIL_SHOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.nail_shot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        NAIL_JAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.nail_shot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        NAIL_SWIPES(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.nail_shot", AzPlayBehaviors.LOOP)),
        WORMHOLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.wormhole", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HANDHOLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.wormhole", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        VORTEX(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.wormhole", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        VOID_SHOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.wormhole", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ACT_CYCLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.idle", AzPlayBehaviors.LOOP));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TuskAct3Entity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.IDLE;
    }
}
