package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.AzControllerSetAnimationSpeedAction;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.common.attack.conditions.HoldingAnubisCondition;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.silverchariot.*;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Silver_Chariot">Silver Chariot</a>.
 * @see JStandTypeRegistry#SILVER_CHARIOT
 * @see net.arna.jcraft.client.renderer.entity.stands.SilverChariotRenderer SilverChariotRenderer
 * @see ArmorOffAttack
 * @see CircleSlashAttack
 * @see CleaveAttack
 * @see GodOfDeathAttack
 * @see GodOfDeathHitAttack
 * @see LastShotAttack
 * @see RayDartAttack
 * @see SCChargeAttack
 * @see SCCounterAttack
 * @see SpinBarrageAttack
 */
public class SilverChariotEntity extends StandEntity<SilverChariotEntity, SilverChariotEntity.State> {
    public static final MoveSet<SilverChariotEntity, State> DEFAULT_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.SILVER_CHARIOT,
            SilverChariotEntity::registerDefaultMoves, State.class);
    public static final MoveSet<SilverChariotEntity, State> POSSESSED_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.SILVER_CHARIOT,
            "possessed", SilverChariotEntity::registerPossessedMoves, State.class);

    public static final StandData DATA = StandData.builder()
            .idleRotation(225f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.silverchariot"))
                    .proCount(4)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                        BNBs:
                            (Armor ON) Light>Barrage>Light>Cleave>Spinning Blade>Shooting Star>Light
                            (Armor ON) Shooting Star>Light>Barrage>Impaling Thrust
                            (Armor OFF) Shooting Star>Light>Spinning Blade>Barrage>Light>Cleave>Impaling Thrust
                            (Armor OFF) Light>Spinning Blade>Barrage>Shooting Star>Cleave>Light
                            (Armor OFF) Impaling Thrust>dash>Barrage>...
                        """))
                    .skinName(Component.literal("Gold Chariot"))
                    .skinName(Component.literal("OVA"))
                    .skinName(Component.literal("Vento"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.SC_SUMMON))
            .build();

    public static final StandData POSSESSED_DATA = DATA.withInfo(info ->
            info.freeSpace(Component.literal("""
                    BNBs:
                        (Light>)Charge~Barrage>Light>Spinning Blade>Light~Light
                        (Light>)Charge~Barrage>God of Death""")));

    public static final LastShotAttack LAST_SHOT = new LastShotAttack(100, 12, 15, 1f)
            .withAnim(State.LAST_SHOT)
            .withInfo(
                    Component.literal("Last Shot"),
                    Component.literal("Silver Chariot fires his rapier, which can bounce 5 times off walls, " +
                            "nerfs all hitboxes and damage by 25% until returned")
            );
    public static final SimpleAttack<SilverChariotEntity> LIGHT_FOLLOWUP = new SimpleAttack<SilverChariotEntity>(0,
            6, 14, 0.65f, 6f, 12, 1.5f, 1.2f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Slash"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<SilverChariotEntity> LIGHT = SimpleAttack.<SilverChariotEntity>lightAttack(
            5, 9, 0.65f, 5f,11, 0.15f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LAST_SHOT)
            .withSound(JSoundRegistry.SC_POKE)
            .withInfo(
                    Component.literal("Stab"),
                    Component.literal("quick combo starter, links into Spinning Blade while armor is off")
            );
    public static final MainBarrageAttack<SilverChariotEntity> BARRAGE = new MainBarrageAttack<SilverChariotEntity>(240,
            0, 40, 0.65f, 0.9f, 25, 2.25f, 0.1f, 0f, 3, 1.25F)
            .withSound(JSoundRegistry.SC_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );
    public static final SimpleAttack<SilverChariotEntity> HEAVY = new SimpleAttack<SilverChariotEntity>(28,
            20, 28, 0.65f, 8f, 10, 2f, 1.5f, 0f)
            .withExtraHitBox(2, 0.1, 1)
            .withSound(JSoundRegistry.SC_HEAVY)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Impaling Thrust"),
                    Component.literal("slow, uninterruptible launcher")
            );

    public static final SpinBarrageAttack ANUBIS_SPIN_BARRAGE = new SpinBarrageAttack(0,
            7, 24,0.65f, 1f, 10, 2f, 0.15f, -0.2f, 2)
            .withAnim(State.SPIN_2)
            .withCondition(HoldingAnubisCondition.holdingAnubis())
            .withSound(JSoundRegistry.SC_SPIN)
            .withInfo(
                    Component.literal("Divine Blade"),
                    Component.literal("fast reliable combo starter/extender, low stun")
            );
    public static final BarrageAttack<SilverChariotEntity> SPIN_BARRAGE = new BarrageAttack<SilverChariotEntity>(100,
            7, 24,0.65f, 1f, 10, 2f, 0.1f, -0.2f, 2)
            .withFollowup(ANUBIS_SPIN_BARRAGE)
            .withFollowupFrame(6)
            .withSound(JSoundRegistry.SC_SPIN)
            .withInfo(
                    Component.literal("Spinning Blade"),
                    Component.literal("fast reliable combo starter/extender, low stun")
            );

    public static final RayDartAttack RAY_DART_LOW = new RayDartAttack(100,
            10, 18,0.65f, 6f, 20, 1.75f, 0.25f, 0.2f)
            .withSound(JSoundRegistry.SC_CHARGE)
            .withSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withBlockStun(9)
            .withInfo(
                    Component.literal("Lacerate"),
                    Component.literal("Anubis Chariot and the user charge forward, high stun, low blockstun.")
            );
    public static final RayDartAttack RAY_DART_HIGH = new RayDartAttack(100,
            12, 20,0.65f, 6f, 15, 2.0f, 0.25f, 0.2f)
            .withCrouchingVariant(RAY_DART_LOW)
            .withSound(JSoundRegistry.SC_CHARGE)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(16)
            .withExtraHitBox(1, 1, 1)
            .withInfo(
                    Component.literal("Split"),
                    Component.literal("Anubis Chariot and the user charge forward, low stun, high blockstun.")
            );
    public static final CleaveAttack CLEAVE = new CleaveAttack(0, 12, 21, 0.75f, 9f,
            20, 2.5f, 0.8f, 0f)
            .withSound(JSoundRegistry.SC_CLEAVE)
            .withImpactSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withHyperArmor()
            .withInfo(
                    Component.literal("Cleave"),
                    Component.literal("Silver Chariot detaches from the user, delivering an uninterruptible, combo-starting slice")
            );
    public static final SCChargeAttack CHARGE = new SCChargeAttack(280, 5, 19, 8f,
            5f, 17, 1.5f, 0.25f, 0f)
            .withSound(JSoundRegistry.SC_SUMMON)
            .withBackstab(false)
            .withInfo(
                    Component.literal("Shooting Star"),
                    Component.literal("Silver Chariot detaches from the user and charges in the looked direction, combo starter/extender")
            );
    public static final SCCounterAttack COUNTER = new SCCounterAttack(480, 4, 34, 0.5f)
            .withInfo(
                    Component.literal("Counter"),
                    Component.literal("0.2s windup, 1.5s duration, stuns when hit")
            );
    public static final SimpleMultiHitAttack<SilverChariotEntity> GOD_OF_DEATH_FINAL = new SimpleMultiHitAttack<SilverChariotEntity>(
            0, 59, 0.65f, 6f, 20, 2.5f, 1.25f, 0f,
            IntSet.of(54))
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Component.literal("God of Death (Final Hit)"),
                    Component.empty()
            );
    public static final GodOfDeathHitAttack GOD_OF_DEATH_HIT = new GodOfDeathHitAttack(0, 59, 0.65f,
            4.5f, 32, 2f, 0.25f, 0f, IntSet.of(13, 23))
            .withFollowup(GOD_OF_DEATH_FINAL)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(
                    Component.literal("God of Death (Hit)"),
                    Component.empty()
            );
    public static final GodOfDeathAttack GOD_OF_DEATH = new GodOfDeathAttack(1000, 23, 28,
            0.65f, 4f, 40, 1.75f, 0f, 0f)
            .withFollowup(GOD_OF_DEATH_HIT)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(
                    Component.literal("God of Death"),
                    Component.literal("high-damage beatdown, 1.5s stun on whiff, cannot be combo broken")
            );
    public static final ArmorOffAttack ARMOR_OFF = new ArmorOffAttack(1200, 6, 15, 0.65f,
            4f, 7, 1.75f, 0.75f, 0f)
            .withSound(JSoundRegistry.SC_ARMOROFF)
            .withLaunch()
            .withInfo(
                    Component.literal("Armor Off"),
                    Component.literal("25s of faster moves")
            );
    public static final CircleSlashAttack CIRCLE_SLASH = new CircleSlashAttack(0, 2, 20,
            0.65f, 5f, 20, 1.75f, 0f, 0f)
            .withExtraHitBox(-0.65, 0, 2)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Circle Slash (Hit)"),
                    Component.empty()
            );
    public static final SimpleHoldableMove<SilverChariotEntity> CIRCLE_CHARGE = new SimpleHoldableMove<SilverChariotEntity>(
            0, 101, 100, 0.65f, 15)
            .withFollowup(CIRCLE_SLASH.withAnim(State.CIRCLE_SLASH))
            .withArmor(2)
            .withInfo(
                    Component.literal("Circle Slash"),
                    Component.literal("""
                            2 armor points
                            Can be held, and released 0.75s in.
                            Depending on how much you hold, the damage and launch height increase."""
                    ));
    private static final EntityDataAccessor<Boolean> HAS_RAPIER;
    private static final EntityDataAccessor<Integer> MODE;
    private static final EntityDataAccessor<Integer> ARMOR_TIME;

    static {
        HAS_RAPIER = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.BOOLEAN);
        MODE = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.INT);
        ARMOR_TIME = SynchedEntityData.defineId(SilverChariotEntity.class, EntityDataSerializers.INT);
    }

    public SilverChariotEntity(Level worldIn) {
        super(JStandTypeRegistry.SILVER_CHARIOT.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.4f, 0.5f, 1f),
                new Vector3f(0.9f, 0.6f, 0.3f),
                new Vector3f(0.6f, 0.7f, 1f),
                new Vector3f(0.8f, 0.8f, 0.8f)
        };
    }

    @Override
    public Vector3f getAuraColor() {
        if (isPossessed()) {
            return new Vector3f(1.0f, 0f, 0f);
        }
        return super.getAuraColor();
    }

    @Override
    public void playSummonAnimation() {
        if (isPossessed()) {
            POSSESSED_SUMMON.sendForEntity(this);
            return;
        }

        super.playSummonAnimation();
    }

    @Override
    protected void switchMoveSet(String name) {
        final Vec3 lookDir = getMoveMap().findMoveByType(SCChargeAttack.class)
                .map(SCChargeAttack::getLookDir)
                .orElse(null);
        super.switchMoveSet(name);
        getMoveMap().findMoveByType(SCChargeAttack.class)
                .ifPresent(m -> m.setLookDir(lookDir));
    }

    @Override
    public StandData getStandData() {
        if (isPossessed()) {
            return StandTypeUtil.getStandData(JCraft.id("silver_chariot_possessed"));
        }

        return super.getStandData();
    }

    @Override
    public int getModeOrdinal() {
        return getMode().ordinal();
    }

    public int getArmorTime() {
        return entityData.get(ARMOR_TIME);
    }

    public void setArmorTime(int armorTime) {
        entityData.set(ARMOR_TIME, armorTime);
    }

    /**
     * Decrements the armor time and returns the decreased amount.
     */
    public int decrementArmorTime() {
        setArmorTime(getArmorTime() - 1);
        return getArmorTime();
    }

    private static void registerDefaultMoves(MoveMap<SilverChariotEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.STAB);

        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, SPIN_BARRAGE, State.SPIN);
        moves.register(MoveClass.SPECIAL2, CHARGE, State.P_CHARGE);
        moves.register(MoveClass.SPECIAL3, CLEAVE, State.CLEAVE);

        moves.register(MoveClass.ULTIMATE, ARMOR_OFF, State.ARMOR_OFF);

        moves.register(MoveClass.UTILITY, CIRCLE_CHARGE, State.CIRCLE_CHARGE).withFollowup(State.CIRCLE_SLASH);
    }

    private static void registerPossessedMoves(MoveMap<SilverChariotEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.STAB);

        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, SPIN_BARRAGE, State.SPIN).withFollowup(State.SPIN_2);
        moves.register(MoveClass.SPECIAL2, RAY_DART_HIGH, State.CHARGE_HIGH).withCrouchingVariant(State.CHARGE_LOW);
        moves.register(MoveClass.SPECIAL3, COUNTER, State.COUNTER);

        moves.register(MoveClass.ULTIMATE, GOD_OF_DEATH, State.BEAT_DOWN_START);

        moves.register(MoveClass.UTILITY, CIRCLE_CHARGE, State.CIRCLE_CHARGE).withFollowup(State.CIRCLE_SLASH);
    }

    public Mode getMode() {
        return Mode.values()[entityData.get(MODE)];
    }

    public void setMode(Mode mode) {
        entityData.set(MODE, mode.ordinal());
    }

    public boolean hasRapier() {
        return entityData.get(HAS_RAPIER);
    }

    public void setHasRapier(boolean hasRapier) {
        entityData.set(HAS_RAPIER, hasRapier);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HAS_RAPIER, true);
        entityData.define(MODE, Mode.REGULAR.ordinal());
        entityData.define(ARMOR_TIME, 0);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        return tryFollowUp(moveClass, MoveClass.LIGHT) || super.initMove(moveClass);
    }

    @Override
    public boolean handleMove(AbstractMove<?, ? super SilverChariotEntity> move, CooldownType cooldownType, State animState) {
        if (!move.canBeInitiated(this)) {
            return false;
        }

        final LivingEntity user = getUserOrThrow();
        final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0) {
            return false;
        }

        final AbstractMove<?, ? super SilverChariotEntity> attackRef = move.copy();
        if (getMode() == Mode.ARMORLESS) {
            attackRef.withWindup((int) (attackRef.getWindup() * 0.67));
            attackRef.withDuration((int) (attackRef.getDuration() * 0.67));
        }
        if (!hasRapier() && attackRef instanceof AbstractSimpleAttack<?, ?> simpleAttackRef) {
            simpleAttackRef.withHitboxSize(simpleAttackRef.getHitboxSize() * 0.75f);
            simpleAttackRef.withDamage(simpleAttackRef.getDamage() * 0.75f);
        }
        setMove(attackRef, animState);

        cooldowns.setCooldown(cooldownType, move.getCooldown());
        return true;
    }

    public boolean isPossessed() {
        return getMode() == Mode.POSSESSED;
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) {
            return;
        }
        final LivingEntity user = getUserOrThrow();
        final Mode mode = getMode();

        if (level().isClientSide) {
            // Possession particles
            if (mode == Mode.POSSESSED) {
                for (int i = 0; i < 16; i++) {
                    level().addParticle(
                            ParticleTypes.ASH,
                            getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 0.25 + 0.5, getZ() + random.nextDouble() - 0.5,
                            0.0, 0.0, 0.0
                    );
                }
            }

            return;
        }

        // getOffHandStack() must be an AnubisItem
        boolean hasAnubis = getOffhandItem().is(JItemRegistry.ANUBIS.get()) || user.getMainHandItem().getItem() == JItemRegistry.ANUBIS.get();

        if (user instanceof Player player) {
            hasAnubis |= player.getInventory().contains(JItemRegistry.ANUBIS.get().getDefaultInstance());

            if (getCurrentMove() == null) {
                player.addItem(getOffhandItem());
                getOffhandItem().shrink(1);
            }
        } else if (!hasRapier() && random.nextFloat() < 0.1f) {
            desummon();
        }

        if (hasAnubis && mode != Mode.POSSESSED) {
            // Set possession state
            setMode(Mode.POSSESSED);
            switchMoveSet(POSSESSED_MOVE_SET.getName());
        } else if (!hasAnubis && mode == Mode.POSSESSED) {
            // Reset
            setMode(Mode.REGULAR);
            switchMoveSet(DEFAULT_MOVE_SET.getName());
        }
    }

    @Override
    @NonNull
    public SilverChariotEntity getThis() {
        return this;
    }

    public enum Mode {
        REGULAR,
        ARMORLESS,
        POSSESSED
    }

    // Animation code

    private static final AzCommand POSSESSED_SUMMON = AzCommand.create(JCraft.BASE_CONTROLLER, "summon_possessed");

    public enum State implements StandAnimationState<SilverChariotEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.silverchariot.idle", AzPlayBehaviors.LOOP)),
        STAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.stab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.silverchariot.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.barrage", AzPlayBehaviors.LOOP)),
        SPIN(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.spin", AzPlayBehaviors.LOOP)),
        SPIN_2(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.spin_2", AzPlayBehaviors.LOOP)),

        CHARGE_LOW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.charge_low", AzPlayBehaviors.LOOP)),
        CHARGE_HIGH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.charge_high", AzPlayBehaviors.LOOP)),

        P_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.pcharge", AzPlayBehaviors.LOOP)),
        P_CHARGE_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.pchargehit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.counter", AzPlayBehaviors.LOOP)),
        BEAT_DOWN_START(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.beatdownstart", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BEAT_DOWN(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.beatdown", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CLEAVE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.cleave", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ARMOR_OFF(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.armor_off", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER_MISS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LAST_SHOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.lastshot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CIRCLE_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.circle_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CIRCLE_SLASH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.circle_slash", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.silverchariot.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),

        POSSESSED_IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.silverchariot.idle_possessed", AzPlayBehaviors.LOOP)),
        ARMORLESS_IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.silverchariot.idle_armorless", AzPlayBehaviors.LOOP)),
        ;

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SilverChariotEntity attacker) {
            final Mode mode = attacker.getMode();

            if (this == IDLE) {
                switch (mode) {
                    case REGULAR -> IDLE.animator.sendForEntity(attacker);
                    case ARMORLESS -> ARMORLESS_IDLE.animator.sendForEntity(attacker);
                    case POSSESSED -> POSSESSED_IDLE.animator.sendForEntity(attacker);
                }

                return;
            }

            if (mode == Mode.ARMORLESS) {
                doubleSpeedAnimations[ordinal()].sendForEntity(attacker);
                return;
            }

            animator.sendForEntity(attacker);
        }

        private static final State[] allStates = State.values();
        private static final AzCommand[] doubleSpeedAnimations = new AzCommand[allStates.length];

        static {
            for (int i = 0; i < allStates.length; i++) {
                final AzCommand original = allStates[i].animator;

                final List<AzAction> modifiedActions = new ArrayList<>();

                for (final AzAction action : original.actions()) {
                    if (action instanceof AzControllerSetAnimationSpeedAction speedAction) { // Should only happen once
                        final AzControllerSetAnimationSpeedAction adjustedSpeedAction =
                                new AzControllerSetAnimationSpeedAction(
                                        speedAction.controllerName(),
                                speedAction.animationSpeed() * 1.33
                                );

                        modifiedActions.add(adjustedSpeedAction);
                    } else {
                        modifiedActions.add(action);
                    }
                }

                doubleSpeedAnimations[i] = new AzCommand(modifiedActions);
            }
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
