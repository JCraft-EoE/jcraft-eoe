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
import net.arna.jcraft.common.attack.conditions.TuskNailCondition;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.tusk.*;
import net.arna.jcraft.common.util.CooldownType;
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
                            Tusk Act 3 is an evolution stand.
                            Contains up to 10 nails.
                            Nails regenerate passively at the cost of hunger.
                            M1: Golden Rectangle Nail (same as Act 2)
                            Ult: Wormhole teleport nail.
                            Sp1: Handhole arm.
                            Sp2: Vortex pull hole.
                            Sp3: Voidshot invulnerability.
                            Heavy: Nail Jab.
                            Barrage: Nail Swipes.

                            Utility: Cycle acts
                            - Click: Next act (1→2→3→1)
                            - Shift+Click: Previous act (1→3→2→1)"""))
                    .build())
            .summonData(SummonData.of(() -> null))
            .build();

    public static final EntityDataAccessor<Float> NAILS = SynchedEntityData.defineId(TuskAct3Entity.class, EntityDataSerializers.FLOAT);
    public static final float NAILS_MAX = 10.0f;

    // ---- Moves ----

    public static final Act3NailShotAttack NAIL_SHOT = new Act3NailShotAttack(0, 10, 15, 0.75f, 2.5f, 15.0f, 8.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInfo(
                    Component.literal("Golden Rectangle Nail"),
                    Component.literal("Fires a spinning nail (15 blocks). On hit, creeps 8 blocks through terrain."));

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

    // Heavy: Nail Jab — close-range spinning nail poke, can't combo-start
    public static final SimpleAttack<TuskAct3Entity> NAIL_JAB = new SimpleAttack<TuskAct3Entity>(
            200, 5, 15, 1.5f, 3.0f, 5, 1.5f, 0.5f, 0.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInfo(
                    Component.literal("Nail Jab"),
                    Component.literal("Close-range nail poke. Cannot combo-start. Requires nails."));

    // Barrage: Nail Swipes — 5 hits, quick startup
    public static final NailBarrageAttack NAIL_SWIPES = new NailBarrageAttack(
            280, 2, 50, 1.5f, 2.0f, 3, 2.0f, 0.3f, 0.0f, 10)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInfo(
                    Component.literal("Nail Swipes"),
                    Component.literal("Quick 5-hit nail swipes. Medium cooldown."));

    public static final TuskActCycleMove<TuskAct3Entity> ACT_CYCLE = new TuskActCycleMove<TuskAct3Entity>(
            0, 0, 1, 0f, 3, false)
            .withInfo(
                    Component.literal("Cycle Act"),
                    Component.literal("Cycle through unlocked Tusk acts.\n" +
                            "Utility → Cycle forward (Act 1→2→3→1...)\n" +
                            "Shift + Utility → Cycle backward (Act 3→2→1→3...)"));

    @Getter
    @Setter
    private CommonMiscComponent miscComponent;

    /** Ticks remaining of Voidshot invulnerability (server-side). */
    private int voidTicks = 0;

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
        moves.register(MoveClass.HEAVY, NAIL_JAB, State.NAIL_JAB);
        moves.register(MoveClass.BARRAGE, NAIL_SWIPES, State.NAIL_SWIPES);
        moves.register(MoveClass.ULTIMATE, WORMHOLE, State.WORMHOLE);
        moves.register(MoveClass.SPECIAL1, HANDHOLE, State.HANDHOLE);
        moves.register(MoveClass.SPECIAL2, VORTEX, State.VORTEX);
        moves.register(MoveClass.SPECIAL3, VOID_SHOT, State.VOID_SHOT);
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

    /** Enter Voidshot invulnerability for VOID_DURATION ticks. */
    public void enterVoid() {
        voidTicks = VoidShotAttack.VOID_DURATION;
        LivingEntity user = getUser();
        if (user != null) {
            user.setInvulnerable(true);
        }
    }

    /** Force the user out of the void early (used when they try to use a move inside). */
    public void exitVoid() {
        voidTicks = 0;
        LivingEntity user = getUser();
        if (user != null) {
            user.setInvulnerable(false);
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
            if (getNails() < NAILS_MAX && tickCount % 30 == 0) {
                LivingEntity user = getUser();
                if (user instanceof Player player) {
                    int currentHunger = player.getFoodData().getFoodLevel();
                    if (currentHunger > 0) {
                        addNails(1.0f);
                        player.getFoodData().setFoodLevel(currentHunger - 1);
                    }
                }
            }

            // Voidshot countdown
            if (voidTicks > 0) {
                voidTicks--;
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

        // Handhole intercept: while hole is active, redirect Light to fire from the hole
        if (moveClass == MoveClass.LIGHT) {
            MoveMap.Entry<TuskAct3Entity, State> sp1Entry = getMoveMap().getFirstValidEntry(
                    MoveClass.SPECIAL1, this, false, false);
            if (sp1Entry != null && sp1Entry.getMove() instanceof HandholeAttack handhole) {
                if (handhole.isHoleActive() && handhole.tryFireFromHole(this, getUser())) {
                    return; // Shot fired from hole, suppress normal M1
                }
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
            MoveMap.Entry<TuskAct3Entity, State> entry = getMoveMap().getFirstValidEntry(
                    MoveClass.UTILITY, this, false, false);

            if (entry != null && entry.getMove() instanceof TuskActCycleMove<?>) {
                TuskActCycleMove<?> originalMove = (TuskActCycleMove<?>) entry.getMove();
                boolean shifting = getUser() != null && getUser().isShiftKeyDown();
                TuskActCycleMove<TuskAct3Entity> directedMove = new TuskActCycleMove<>(
                        originalMove.getCooldown(), originalMove.getWindup(),
                        originalMove.getDuration(), originalMove.getMoveDistance(),
                        3, shifting);
                return handleMove(directedMove, CooldownType.UTILITY, State.ACT_CYCLE);
            }
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
