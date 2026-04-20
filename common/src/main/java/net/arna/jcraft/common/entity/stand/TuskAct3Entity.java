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
import net.arna.jcraft.common.attack.actions.UserAnimationAction;
import net.arna.jcraft.common.attack.conditions.TuskNailCondition;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.world.phys.AABB;
import java.lang.ref.WeakReference;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
            .evolution(true)
            .idleRotation(315f)
            .idleDistance(1.75f)
            .blockDistance(0f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.tusk_act3"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                            Contains up to 10 nails. Nails regenerate passively at the cost of hunger.
                            Herbal Tea grants Keratin Growth, speeding up nail regen."""))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.TUSK_SUMMON3))
            .build();

    public static final EntityDataAccessor<Float> NAILS = SynchedEntityData.defineId(TuskAct3Entity.class, EntityDataSerializers.FLOAT);
    public static final float NAILS_MAX = 10.0f;

    public static final Act3NailShotAttack NAIL_SHOT = new Act3NailShotAttack(0, 10, 15, 0.75f, 2.7f, 20.0f, 15.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInitAction(UserAnimationAction.play("tsk.grn"))
            .withSound(JSoundRegistry.TUSK_SHOT)
            .withInfo(
                    Component.literal("Golden Rectangle Nail"),
                    Component.literal("Fires a spinning nail (15 blocks). On hit, creeps 8 blocks through terrain."));

    public static final SimpleAttack<TuskAct3Entity> NAIL_JAB = new SimpleAttack<TuskAct3Entity>(
            200, 5, 15, 1f, 7.0f, 15, 2f, 0.5f, 0.0f)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInitAction(UserAnimationAction.play("tsk.njb"))
            .withImpactSound(JSoundRegistry.ARMORED_HIT)
            .withInfo(
                    Component.literal("Nail Jab"),
                    Component.literal("Close-range nail poke."));

    public static final NailBarrageAttack NAIL_SWIPES = new NailBarrageAttack(
            280, 2, 36, 0f, 3.0f, 12, 2.0f, 0.3f, 1.0f, 6)
            .withCondition(TuskNailCondition.atLeast(1.0f))
            .withInitAction(UserAnimationAction.play("tsk.barrage"))
            .withSound(JSoundRegistry.TUSK_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Nail Swipes"),
                    Component.literal("Quick 5-hit nail swipes. Medium cooldown."));

    public static final HandholeAttack HANDHOLE = new HandholeAttack(80, 8, 15, 0.75f)
            .withInitAction(UserAnimationAction.play("tsk.hh"))
            .withSound(JSoundRegistry.TUSK_SHOT)
            .withInfo(
                    Component.literal("Handhole"),
                    Component.literal("Fires a hole onto a surface or entity. While active, ALL attacks fire from that position.\nPress Sp1 again to recall the arm."))
            .withCondition(TuskNailCondition.atLeast(1.0f));

    public static final VortexAttack VORTEX = new VortexAttack(120, 1, 15, 0.75f)
            .withInitAction(UserAnimationAction.play("tsk.nls"))
            .withSound(JSoundRegistry.TUSK_SHOT)
            .withInfo(
                    Component.literal("Vortex"),
                    Component.literal("Fires a hole that drags nearby entities (and the user) toward it."))
            .withCondition(TuskNailCondition.atLeast(1.0f));

    public static final VoidShotAttack VOID_SHOT = new VoidShotAttack(300, 25, 30, 0.75f)
            .withExitDamage(8.0f, 3.0f)
            .withInitAction(UserAnimationAction.play("tsk.vs"))
            .withInfo(
                    Component.literal("Voidshot"),
                    Component.literal("Fire at your own head and enter the wormhole.\nInvulnerable for up to 1.4 seconds. Deals 8 damage in a 3-block radius on impact. Punishable on entry/exit."))
            .withCondition(TuskNailCondition.atLeast(1.0f));


    public static final TuskActCycleMove<TuskAct3Entity> ACT_CYCLE = new TuskActCycleMove<TuskAct3Entity>(
            0, 1, 1, 0f, 3, false)
            .withInfo(
                    Component.literal("Cycle Act"),
                    Component.literal("Cycle through unlocked Tusk acts.\n" +
                            "Utility → Cycle forward (Act 1→2→3→1...)\n" +
                            "Shift + Utility → Cycle backward (Act 3→2→1→3...)"));

    public static final WormholeAttack WORMHOLE = new WormholeAttack(200, 5, 10, 0.75f)
            .withInitAction(UserAnimationAction.play("tsk.grn"))
            .withTeleportDamage(10.0f, 4.0f)
            .withSound(JSoundRegistry.TUSK_SHOT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Wormhole Nail"),
                    Component.literal("First use: fires a slow homing nail.\nSecond use: teleports to nail, dealing 10 damage in 4-block radius."))
            .withCondition(TuskNailCondition.atLeast(1.0f));
    
    @Getter
    @Setter
    private CommonMiscComponent miscComponent;

    /** Ticks remaining of Voidshot nail-piloting (server-side). */
    private int voidTicks = 0;
    private int regenTick = 40;
    private WeakReference<NailProjectile> voidNail = null;

    /**
     * Directly tracked by HandholeAttack.tick() — avoids getMove() instance identity issues.
     * Non-null and inGround = hole is active.
     */
    public NailProjectile activeHandholeNail = null;

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
        moves.register(MoveClass.SPECIAL3, WORMHOLE, State.WORMHOLE);
        moves.register(MoveClass.ULTIMATE, VOID_SHOT, State.VOID_SHOT);
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
            user.startRiding(nail, true); // player rides the nail — follows it automatically
            // Full invisibility: flag + effect (hides armor/particles for others)
            user.setInvisible(true);
            user.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, VoidShotAttack.VOID_DURATION + 10, 0, false, false));
            user.setInvulnerable(true);
        }
        this.setInvisible(true); // hide stand too
    }

    /** Force the user out of the void early. */
    public void exitVoid() {
        voidTicks = 0;
        NailProjectile nail = voidNail == null ? null : voidNail.get();
        LivingEntity user = getUser();
        if (user != null) {
            user.stopRiding();
            user.setInvisible(false);
            user.removeEffect(MobEffects.INVISIBILITY);
            user.setInvulnerable(false);
        }
        this.setInvisible(false); // show stand again
        if (nail != null && nail.isAlive()) nail.discard();
        voidNail = null;
    }

    /**
     * If the handhole is active, repositions the nail to fire from the hole toward the crosshair.
     * Call BEFORE addFreshEntity. Returns true if the nail was redirected.
     */
    public boolean redirectThroughHandhole(NailProjectile nail, LivingEntity user, float speed) {
        if (activeHandholeNail == null || !activeHandholeNail.isAlive()
                || (!activeHandholeNail.inGround && activeHandholeNail.entityStuckTo == null)) {
            return false;
        }
        Vec3 origin = activeHandholeNail.position().add(0, 1, 0);
        nail.setPos(origin);
        Vec3 target = JUtils.getCrosshairTarget(user, 50.0);
        nail.setDeltaMovement(target.subtract(origin).normalize().scale(speed));
        return true;
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

            // Voidshot — player rides the nail
            if (voidTicks > 0) {
                NailProjectile nail = voidNail == null ? null : voidNail.get();
                if (nail == null || !nail.isAlive() || nail.inGround) {
                    // Nail hit something — apply exit AoE if configured
                    if (nail != null && nail.inGround) {
                        VoidShotAttack vs = getMove(VoidShotAttack.class);
                        if (vs != null && vs.getExitDamage() > 0) {
                            float r = vs.getExitBlastRadius();
                            Vec3 nailPos = nail.position();
                            AABB blastBox = new AABB(nailPos.x - r, nailPos.y - r, nailPos.z - r,
                                    nailPos.x + r, nailPos.y + r, nailPos.z + r);
                            LivingEntity finalUser = user;
                            level().getEntitiesOfClass(LivingEntity.class, blastBox,
                                    e -> e != finalUser && e.isAlive() && !e.isSpectator())
                                    .forEach(e -> Attacks.trueDamage(vs.getExitDamage(), JDamageSources.stand(this), e));
                        }
                    }
                    exitVoid();
                } else if (!nail.hasPassenger(user)) {
                    // Player dismounted (pressed sneak, etc.)
                    exitVoid();
                } else {
                    voidTicks--;
                    if (voidTicks <= 0) exitVoid();
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

        // If initMove already handled this input, don't call initMove again
        if (moveInitiated) return;

        if (canAttack()) {
            initMove(moveClass);
        } else if (getMoveStun() > 0 && getMoveStun() < QUEUE_MOVESTUN_LIMIT) {
            queueMove(type);
        }
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        // Act cycle — guard against auto-cycle on fresh summon (PlayerInputPacket calls initMove(UTILITY) on the new stand)
        if (moveClass == MoveClass.UTILITY) {
            if (tickCount < 2) return false;
            TuskActCycleMove.tryCycle(3, getUser());
            return true;
        }

        LivingEntity user = getUser();

        // Peacemaker lock — Tusk cannot fire attacks while the gun is held
        if (user != null) {
            if (user.getMainHandItem().getItem() instanceof Peacemaker
                    || user.getOffhandItem().getItem() instanceof Peacemaker) {
                return false;
            }
        }

        // Wormhole teleport: if nail is alive, teleport instead of firing a new nail
        if (moveClass == MoveClass.SPECIAL3) {
            WormholeAttack wormhole = getMove(WormholeAttack.class);
            if (wormhole != null && wormhole.isNailActive()) {
                wormhole.doTeleportNow(this, user);
                return true;
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
        NAIL_JAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        NAIL_SWIPES(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.barrage", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WORMHOLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.wormhole_shoot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HANDHOLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.hand_hole", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        VORTEX(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.vortex", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        VOID_SHOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.tusk_act_3.void_shot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
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
