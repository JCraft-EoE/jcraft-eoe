package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.attack.moves.silverchariot.*;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.arna.jcraft.common.attack.moves.silverchariot.CircleSlashAttack.CHARGE_TIME;

public class SilverChariotEntity extends StandEntity<SilverChariotEntity, SilverChariotEntity.State> {
    public static final LastShotAttack LAST_SHOT = new LastShotAttack(140, 12, 15, 1f)
            .withAnim(State.LAST_SHOT)
            .withInfo(Text.literal("Last Shot"), Text.literal("Silver Chariot fires his rapier, " +
                    "which can bounce 5 times off walls, nerfs all hitboxes and damage by 25% until returned"));
    public static final SimpleAttack<SilverChariotEntity> LIGHT_FOLLOWUP = new SimpleAttack<SilverChariotEntity>(
            0, 6, 14, 0.65f, 6f, 12, 1.5f, 1.2f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withInfo(Text.literal("Slash"), Text.literal("quick combo finisher"));
    public static final SimpleAttack<SilverChariotEntity> LIGHT = SimpleAttack.<SilverChariotEntity>lightAttack(5, 9, 5f,
                    11, 0.75f, 0.65f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(LAST_SHOT)
            .withSound(JSoundRegistry.SC_POKE)
            .withInfo(Text.literal("Stab"), Text.literal("quick combo starter, links into Spinning Blade while armor is off"));
    public static final MainBarrageAttack<SilverChariotEntity> BARRAGE = new MainBarrageAttack<SilverChariotEntity>(
            240, 0, 60, 0.65f, 0.9f, 25, 2.25f, 0.1f, 0f, 3, 1.25F)
            .withSound(JSoundRegistry.SC_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static final SimpleAttack<SilverChariotEntity> HEAVY = new SimpleAttack<SilverChariotEntity>(
            200, 20, 28, 0.65f, 8f, 10, 2f, 1.5f, 0f)
            .withSound(JSoundRegistry.SC_HEAVY)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Impaling Thrust"), Text.literal("slow, uninterruptible launcher"));
    //todo: spin barrage deflecting projectiles
    public static final SpinBarrageAttack SPIN_BARRAGE = new SpinBarrageAttack(240, 7, 24,
            0.65f, 1f, 10, 2f, 0.1f, 0f, 2)
            .withSound(JSoundRegistry.SC_SPIN)
            .withInfo(Text.literal("Spinning Blade"), Text.literal("fast reliable combo starter/extender, low stun"));
    public static final RayDartAttack RAY_DART = new RayDartAttack(160, 13, 25,
            0.65f, 5f, 15, 1.75f, 0.25f, -0.2f)
            .withSound(JSoundRegistry.SC_CHARGE)
            .withBlockStun(17)
            .withInfo(Text.literal("Ray Dart"), Text.literal("Silver Chariot and the user charge forward, combo finisher"));
    public static final CleaveAttack CLEAVE = new CleaveAttack(260, 12, 21, 0.75f, 9f,
            20, 2.5f, 0.8f, 0f)
            .withSound(JSoundRegistry.SC_CLEAVE)
            .withHyperArmor()
            .withInfo(Text.literal("Cleave"), Text.literal("Silver Chariot detaches from the user, delivering an uninterruptible, combo-starting slice"));
    public static final SCChargeAttack CHARGE = new SCChargeAttack(280, 5, 19, 8f,
            5f, 17, 1.5f, 0.25f, 0f, State.P_CHARGE_HIT)
            .withSound(JSoundRegistry.SC_SUMMON)
            .withBackstab(false)
            .withInfo(Text.literal("Shooting Star"), Text.literal("Silver Chariot detaches from the user and charges in the looked direction, combo starter/extender"));
    public static final SCCounterAttack COUNTER = new SCCounterAttack(480, 4, 34, 0.5f)
            .withInfo(Text.literal("Counter"), Text.literal("0.2s windup, 1.5s duration, stuns when hit"));
    public static final SimpleMultiHitAttack<SilverChariotEntity> GOD_OF_DEATH_FINAL = new SimpleMultiHitAttack<SilverChariotEntity>(
            0, 59, 0.65f, 6f, 20, 2.5f, 1.25f, 0f,
            IntSet.of(54))
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("God of Death (Final Hit)"), Text.empty());
    public static final GodOfDeathHitAttack GOD_OF_DEATH_HIT = new GodOfDeathHitAttack(0, 59, 0.65f,
            4.5f, 32, 2f, 0.75f, 0f, IntSet.of(13, 23))
            .withFollowup(GOD_OF_DEATH_FINAL)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(Text.literal("God of Death (Hit)"), Text.empty());
    public static final GodOfDeathAttack GOD_OF_DEATH = new GodOfDeathAttack(1000, 23, 28,
            0.65f, 4f, 40, 1.75f, 0f, 0f)
            .withFollowup(GOD_OF_DEATH_HIT)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(Text.literal("God of Death"), Text.literal("high-damage beatdown, 1.5s stun on whiff, cannot be combo broken"));
    public static final ArmorOffAttack ARMOR_OFF = new ArmorOffAttack(1200, 6, 15, 0.65f,
            4f, 7, 1.75f, 0.75f, 0f)
            .withSound(JSoundRegistry.SC_ARMOROFF)
            .withLaunch()
            .withInfo(Text.literal("Armor Off"), Text.literal("25s of faster moves"));
    public static final CircleSlashAttack CIRCLE_SLASH = new CircleSlashAttack(0, 2, 20,
            0.65f, 5f, 20, 1.75f, 0f, 0f)
            .withExtraHitBox(-0.65, 0, 2)
            .withLaunch()
            .withInfo(Text.literal("Circle Slash (Hit)"), Text.empty());
    public static final SimpleAttack<SilverChariotEntity> CIRCLE_CHARGE = new SimpleAttack<SilverChariotEntity>(
            260, 101, 100, 0.65f, 0f, 0, 0f, 0f, 0f)
            .withFollowup(CIRCLE_SLASH)
            .withArmor(2)
            .withInfo(Text.literal("Circle Slash"), Text.literal("charges for a minimum of 0.75s, tap again to release, 2 armor points"));
    private static final TrackedData<Boolean> HAS_RAPIER;
    private static final TrackedData<Integer> MODE;

    static {
        MODE = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HAS_RAPIER = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public SilverChariotEntity(World worldIn) {
        super(StandType.SILVER_CHARIOT, worldIn, JSoundRegistry.SC_SUMMON);
        idleRotation = 225f;

        pros = List.of(
                "fast m1",
                "two barrages",
                "excellent pokes and pressure",
                "counter (Anubis only)"
        );

        cons = List.of(
                "high execution requirement",
                "low damage output",
                "lacking in mobility"
        );

        setNormalDesc();

        auraColors = new Vec3f[]{
                new Vec3f(0.4f, 0.5f, 1f),
                new Vec3f(0.9f, 0.6f, 0.3f),
                new Vec3f(0.6f, 0.7f, 1f),
                new Vec3f(0.8f, 0.8f, 0.8f)
        };
    }

    @Override
    public Vec3f getAuraColor() {
        if (isPossessed()) return Vec3f.POSITIVE_X;
        return super.getAuraColor();
    }

    private void setNormalDesc() {
        description = "Close Range RUSHDOWN";

        freespace =
                """
                        BNBs:
                            (Armor ON) M1>Barrage>M1>Cleave>Spinning Blade>Shooting Star>M1
                            (Armor ON) Shooting Star>M1>Barrage>Impaling Thrust
                            (Armor OFF) Shooting Star>M1>Spinning Blade>Barrage>M1>Cleave>Impaling Thrust
                            (Armor OFF) M1>Spinning Blade>Barrage>Shooting Star>Cleave>M1
                            (Armor OFF) Impaling Thrust>dash>Barrage>...
                        """;

        registerMoves();
    }

    private void setPossessedDesc() {
        description = "Mid Range TRICKSTER";

        freespace =
                """
                BNBs:
                    (M1>)Charge~Barrage>M1>Spinning Blade>M1~M1
                    (M1>)Charge~Barrage>God of Death""";

        registerMoves();
    }

    @Override
    protected void registerMoves(MoveMap<SilverChariotEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.STAB);

        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);
        moves.register(MoveType.SPECIAL1, SPIN_BARRAGE, State.SPIN);
        moves.register(MoveType.SPECIAL2, isPossessed() ? RAY_DART : CHARGE, isPossessed() ? State.CHARGE : State.P_CHARGE);
        moves.register(MoveType.SPECIAL3, isPossessed() ? COUNTER : CLEAVE, isPossessed() ? State.COUNTER : State.CLEAVE);
        moves.register(MoveType.ULTIMATE, isPossessed() ? GOD_OF_DEATH : ARMOR_OFF, isPossessed() ? State.BEAT_DOWN_START : State.ARMOR_OFF);
        moves.register(MoveType.UTILITY, CIRCLE_CHARGE, State.CIRCLE_CHARGE);
    }

    public Mode getMode() {
        return Mode.values()[dataTracker.get(MODE)];
    }

    public void setMode(Mode mode) {
        dataTracker.set(MODE, mode.ordinal());
    }

    public boolean hasRapier() {
        return dataTracker.get(HAS_RAPIER);
    }

    public void setHasRapier(boolean hasRapier) {
        dataTracker.set(HAS_RAPIER, hasRapier);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(HAS_RAPIER, true);
        dataTracker.startTracking(MODE, 1);
    }

    @Override
    public void initMove(MoveType type) {
        if (type == MoveType.UTILITY) {
            if (curMove != null && curMove.getMoveType() == MoveType.UTILITY && getMoveStun() <= 85)
                setMove(CIRCLE_SLASH.copy(), State.CIRCLE_SLASH);
            else { // Reset charge time, and begin charging again
                getMoveContext().setInt(CHARGE_TIME, 0);
                handleMove(MoveType.UTILITY);
            }
        } else if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super SilverChariotEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else super.initMove(type);
    }

    @Override
    public boolean handleMove(AbstractMove<?, ? super SilverChariotEntity> move, CooldownType cooldownType, State animState) {
        if (!move.canBeInitiated(this)) return false;

        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0) return false;

        AbstractMove<?, ? super SilverChariotEntity> attackRef = move.copy();
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

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        Mode mode = getMode();

        if (world.isClient) {
            // Possession particles
            if (mode == Mode.POSSESSED)
                for (int i = 0; i < 16; i++)
                    world.addParticle(
                            ParticleTypes.ASH,
                            getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 0.25 + 0.5, getZ() + random.nextDouble() - 0.5,
                            0.0, 0.0, 0.0
                    );

            return;
        }

        boolean hasAnubis = user instanceof PlayerEntity player ? player.getInventory().contains(JObjectRegistry.ANUBIS.getDefaultStack()) :
                user.getMainHandStack().getItem() == JObjectRegistry.ANUBIS;

        if (hasAnubis && mode != Mode.POSSESSED) {
            for (int i = 0; i < 128; i++)
                world.addParticle(
                        ParticleTypes.ASH,
                        getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 2, getZ() + random.nextDouble() - 0.5,
                        0.0, 0.1, 0.0
                );

            // Set possession state
            setMode(Mode.POSSESSED);
            setPossessedDesc();
        } else if (!hasAnubis && mode == Mode.POSSESSED) {
            for (int i = 0; i < 128; i++)
                world.addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 2, getZ() + random.nextDouble() - 0.5,
                        0.0, 0.1, 0.0
                );

            // Reset
            setMode(Mode.REGULAR);
            setNormalDesc();
        }

        ARMOR_OFF.tickArmor(this);
        if (getMoveStun() % 10 == 0 && curMove != null && curMove.getOriginalMove() == CIRCLE_CHARGE)
            getMoveContext().incrementInt(CHARGE_TIME, 1);
    }

    @Override
    protected @NonNull SilverChariotEntity getThis() {
        return this;
    }

    public enum Mode {
        REGULAR,
        ARMORLESS,
        POSSESSED
    }

    // Animation code
    public enum State implements StandAnimationState<SilverChariotEntity> {
        IDLE((silverChariot, builder) -> builder.loop("animation.silverchariot.idle" + switch (silverChariot.getMode()) {
            case REGULAR -> "";
            case ARMORLESS -> "_armorless";
            case POSSESSED -> "_possessed";
        })),
        STAB(builder -> builder.playAndHold("animation.silverchariot.stab")),
        BLOCK(builder -> builder.loop("animation.silverchariot.block")),
        HEAVY(builder -> builder.playAndHold("animation.silverchariot.heavy")),
        BARRAGE(builder -> builder.loop("animation.silverchariot.barrage")),
        SPIN(builder -> builder.loop("animation.silverchariot.spin")),
        CHARGE(builder -> builder.loop("animation.silverchariot.charge")),
        P_CHARGE(builder -> builder.loop("animation.silverchariot.pcharge")),
        P_CHARGE_HIT(builder -> builder.playAndHold("animation.silverchariot.pchargehit")),
        COUNTER(builder -> builder.loop("animation.silverchariot.counter")),
        BEAT_DOWN_START(builder -> builder.playAndHold("animation.silverchariot.beatdownstart")),
        BEAT_DOWN(builder -> builder.playAndHold("animation.silverchariot.beatdown")),
        CLEAVE(builder -> builder.playAndHold("animation.silverchariot.cleave")),
        ARMOR_OFF(builder -> builder.playAndHold("animation.silverchariot.armor_off")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.silverchariot.counter_miss")),
        LAST_SHOT(builder -> builder.playAndHold("animation.silverchariot.lastshot")),
        CIRCLE_CHARGE(builder -> builder.playAndHold("animation.silverchariot.circle_charge")),
        CIRCLE_SLASH(builder -> builder.playAndHold("animation.silverchariot.circle_slash")),
        LIGHT_FOLLOWUP(builder -> builder.playAndHold("animation.silverchariot.light_followup"));

        private final BiConsumer<SilverChariotEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<SilverChariotEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SilverChariotEntity attacker, AnimationBuilder builder) {
            animator.accept(attacker, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.silverchariot.summon" + (isPossessed() ? "_possessed" : "");
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
