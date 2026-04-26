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
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.EffectAction;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.weatherreport.*;
import net.arna.jcraft.common.entity.projectile.CloudPuffEntity;
import net.arna.jcraft.common.entity.projectile.LargeIcicleProjectile;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class WeatherReportEntity extends StandEntity<WeatherReportEntity, WeatherReportEntity.State> {
    public static final MoveSet<WeatherReportEntity, State> DEFAULT_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.WEATHER_REPORT,
            WeatherReportEntity::registerMoves, State.class);
    public static final MoveSet<WeatherReportEntity, State> ELECTRIFIED_MOVE_SET = MoveSetManager.create(JStandTypeRegistry.WEATHER_REPORT,
            "electrified", WeatherReportEntity::registerElectrifiedMoves, State.class);

    public static final EntityDataAccessor<Integer> ELECTRIFIED_TICKS =
            SynchedEntityData.defineId(WeatherReportEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> WIND_SPEED_INDEX =
            SynchedEntityData.defineId(WeatherReportEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> WEATHER_METER =
            SynchedEntityData.defineId(WeatherReportEntity.class, EntityDataSerializers.FLOAT);

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.weather_report"))
                    .skinName(Component.literal("1"))
                    .skinName(Component.literal("2"))
                    .skinName(Component.literal("3"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAND_SUMMON))
            .build();

    public static final SimpleAttack<WeatherReportEntity> WATER_SPLASH = new SimpleAttack<WeatherReportEntity>(
                    23, 8, 15, 0.75f, 5f, 10, 1.5f, 0.1f, 0f)
            .withAction(EffectAction.inflict(MobEffects.BLINDNESS, 30, 0, true, false, false))
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Water Blade"),
                    Component.literal("Slices water into a razor-sharp blade, temporarily blinding the target for 1.5 seconds."));

    public static final WindDisplacementAttack LIGHT = new WindDisplacementAttack(
                    23, 7, 13, 0.75f, 5f, 10, 1.5f, 0.2f, 0f)
            .withCrouchingVariant(WATER_SPLASH)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Wind Displacement"),
                    Component.literal("Shoots out a gust of wind dealing M1 damage. Electrified: Electrified wind gust dealing an extra 0.5 hearts."));

    public static final SimpleAttack<WeatherReportEntity> LIGHT_ELECTRIFIED = new SimpleAttack<WeatherReportEntity>(
                    23, 7, 13, 0.75f, 6f, 10, 1.5f, 0.25f, 0f)
            .noLoopPrevention()
            .withCrouchingVariant(WATER_SPLASH)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Electrified Wind Displacement"),
                    Component.literal("Electrified wind gust dealing an additional 0.5 hearts of damage."));

    public static final HailstormBarrageAttack HAILSTORM = new HailstormBarrageAttack(
                    200, 6, 60, 1f, 1f, 8, 1.0f, 0.1f, 0f, 5, 1.8f, 0.15f)
            .withInfo(
                    Component.literal("Hailstorm Barrage"),
                    Component.literal("Weather Report turns into a cloud and fires hail projectiles. Electrified: Electrified Ground Ripple — shocks the ground, rippling lightning to up to 5 nearby targets."));

    public static final WindShockwaveAttack WIND_SHOCKWAVE = new WindShockwaveAttack(
                    180, 5, 21, 0.75f, 3f, 5, 1.5f, 0.3f, 0f, 3)
            .withCrouchingVariant(HAILSTORM)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Wind Shockwave"),
                    Component.literal("A fast-moving barrage with a loud crack on activation. 6 hits dealing 1.5 hearts each. Electrified: Wind gusts exploding into electricity, 0.5 hearts/hit + damage over time."));

    public static final ElectrifiedGroundRippleAttack GROUND_RIPPLE = new ElectrifiedGroundRippleAttack(
                    180, 8, 22, 0.5f, 1f, 8, 1f, 0.2f, 0f, 5, 10f)
            .withInfo(
                    Component.literal("Electrified Ground Ripple"),
                    Component.literal("Shocks the ground, sending lightning ripples to up to 5 nearby targets."));

    public static final ElectrifiedBarrageAttack ELECTRIFIED_BARRAGE = new ElectrifiedBarrageAttack(
                    180, 5, 54, 0.75f, 1f, 5, 1.5f, 0.2f, 0f, 4, 60, 1)
            .withCrouchingVariant(GROUND_RIPPLE)
            .withInfo(
                    Component.literal("Electrified Barrage"),
                    Component.literal("Wind gusts that explode into electricity. 0.5 hearts per hit with damage over time."));

    public static final UpdraftAttack GALE_FORCE = new UpdraftAttack(
                    140, 8, 20, 1.0f, 4f, 16, 2.5f, 0.0f, 0f, 2.5f, 1.35f)
            .withInfo(
                    Component.literal("Gale Force"),
                    Component.literal("Unleashes a violent column of rising air, launching all nearby enemies straight up.")
            );

    public static final HeavyWindSlashAttack HEAVY = new HeavyWindSlashAttack(
                    140, 10, 22, 1.0f, 9f, 18, 2.5f, 0.5f, 0f)
            .withCrouchingVariant(GALE_FORCE)
            .withImpactSound(JSoundRegistry.IMPACT_5)
            .withInfo(
                    Component.literal("Heavy Wind Slash"),
                    Component.literal("A charged blade of condensed air. Large AOE, follows the user's look direction. 4.5 hearts. Electrified: Thunderstorm Shock — summons a lightning bolt at the target."));

    public static final ThunderstormShockAttack THUNDERSTORM_SHOCK = new ThunderstormShockAttack(
                    180, 12, 22, 1.0f, 6f, 16, 2.0f, 0.5f, 0f, 24f)
            .withInfo(
                    Component.literal("Thunderstorm Shock"),
                    Component.literal("Summons a lightning bolt at the target. Same damage as vanilla lightning."));

    public static final ElectrifiedShotAttack ELECTRIFIED_SHOT = new ElectrifiedShotAttack(
                    60, 5, 12, 0.5f, 0.6f)
            .withInfo(
                    Component.literal("Electrified Shot"),
                    Component.literal("Fires a slow-moving electric projectile that electrifies wind constructs."));

    public static final LightningChargeMove LIGHTNING_CHARGE = new LightningChargeMove(
                    600, 10, 20, 0.5f, 500)
            .withCrouchingVariant(ELECTRIFIED_SHOT)
            .withInfo(
                    Component.literal("Lightning Charge"),
                    Component.literal("Fills Weather Report with lightning, enabling Electrified mode for 25 seconds."));

    public static final WindTunnelSpeedSelectMove WIND_SPEED_SELECT = new WindTunnelSpeedSelectMove(
                    10, 1, 5, 0.5f)
            .withInfo(
                    Component.literal("Wind Tunnel Speed Select"),
                    Component.literal("Cycles wind tunnel speed: Light Breeze → Gale → Hurricane."));

    public static final WindTunnelAttack WIND_TUNNEL = new WindTunnelAttack(
                    240, 8, 60, 0.5f, 15.0, 2.0)
            .withCrouchingVariant(WIND_SPEED_SELECT)
            .withInfo(
                    Component.literal("Wind Tunnel"),
                    Component.literal("Creates a 4x4x15 wind tunnel in the look direction. Can be used vertically."));

    public static final IcicleAccumulationFireAttack ICICLE_FIRE = new IcicleAccumulationFireAttack(
                    0, 1, 28, 1.0f, 0.8f, 2.0f, 1.6f, 4.0f)
            .withInfo(
                    Component.literal("Icicle Accumulation Canon"),
                    Component.literal("Releases the accumulated icicle. Charge time determines distance and size."));

    public static final DryIceMove DRY_ICE = new DryIceMove(
                    140, 8, 20, 0.5f, 0f, 12, 2.5f, 0.1f, 0f)
            .withInfo(
                    Component.literal("Dry Ice"),
                    Component.literal("A burst of super-cooled air that deep-freezes the target for 2 seconds. Deals no damage."));

    public static final IcicleAccumulationChargeMove ICICLE_CHARGE = new IcicleAccumulationChargeMove(
                    360, 1, IcicleAccumulationChargeMove.MAX_CHARGE_TIME + 1, 1.0f, 12)
            .withFollowup(ICICLE_FIRE)
            .withCrouchingVariant(DRY_ICE)
            .withInfo(
                    Component.literal("Icicle Accumulation"),
                    Component.literal("Hold to accumulate ice. Release to fire a large icicle that pushes targets."));

    public static final AtmosphericAccumulationAttack ATMOSPHERIC_ACCUMULATION = new AtmosphericAccumulationAttack(
                    1200, 10, AtmosphericAccumulationAttack.MAX_CHARGE_TICKS + 20, 0.5f, AtmosphericAccumulationAttack.MAX_CHARGE_TICKS)
            .withInfo(
                    Component.literal("Atmospheric Accumulation"),
                    Component.literal("Hold to charge. Weather Report floats you up and accumulates weather phenomena. Release to unleash."));

    public static final WeatherToggleMove WEATHER_TOGGLE = new WeatherToggleMove(
                    80, 1, 10, 0.5f)
            .withInfo(
                    Component.literal("Weather Toggle"),
                    Component.literal("Cycles between clear, rain, and thunder weather."));

    public static final WindMovementDetectionMove WIND_DETECTION = new WindMovementDetectionMove(
                    50, 1, 40, 0.5f, 20f)
            .withCrouchingVariant(WEATHER_TOGGLE)
            .withInfo(
                    Component.literal("Wind Movement Detection"),
                    Component.literal("Reveals nearby entity movement via wind particles."));

    private static void registerMoves(final MoveMap<WeatherReportEntity, WeatherReportEntity.State> moves) {
        moves.register(MoveClass.LIGHT, LIGHT, State.LIGHT)
                .withCrouchingVariant(State.WATER_SPLASH);
        moves.register(MoveClass.BARRAGE, WIND_SHOCKWAVE, State.BARRAGE)
                .withCrouchingVariant(State.HAILSTORM);
        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY)
                .withCrouchingVariant(State.GALE_FORCE);
        moves.register(MoveClass.SPECIAL1, LIGHTNING_CHARGE, State.LIGHTNING_CHARGE)
                .withCrouchingVariant(State.ELECTRIFIED_SHOT);
        moves.register(MoveClass.SPECIAL2, WIND_TUNNEL, State.WIND_TUNNEL)
                .withCrouchingVariant(State.WIND_SPEED_SELECT);
        var icicleEntry = moves.register(MoveClass.SPECIAL3, ICICLE_CHARGE, State.ICICLE_CHARGE);
        icicleEntry.withFollowup(State.ICICLE_FIRE);
        icicleEntry.withCrouchingVariant(State.DRY_ICE);
        moves.register(MoveClass.ULTIMATE, ATMOSPHERIC_ACCUMULATION, State.ATMOSPHERIC_ACCUMULATION);
        moves.register(MoveClass.UTILITY, WIND_DETECTION, State.WIND_DETECTION)
                .withCrouchingVariant(State.WEATHER_TOGGLE);
    }

    private static void registerElectrifiedMoves(final MoveMap<WeatherReportEntity, WeatherReportEntity.State> moves) {
        moves.register(MoveClass.LIGHT, LIGHT_ELECTRIFIED, State.LIGHT_ELECTRIFIED)
                .withCrouchingVariant(State.WATER_SPLASH);
        moves.register(MoveClass.BARRAGE, ELECTRIFIED_BARRAGE, State.ELECTRIFIED_BARRAGE)
                .withCrouchingVariant(State.GROUND_RIPPLE);
        moves.register(MoveClass.HEAVY, THUNDERSTORM_SHOCK, State.THUNDERSTORM_SHOCK);
        moves.register(MoveClass.SPECIAL1, LIGHTNING_CHARGE, State.LIGHTNING_CHARGE)
                .withCrouchingVariant(State.ELECTRIFIED_SHOT);
        moves.register(MoveClass.SPECIAL2, WIND_TUNNEL, State.WIND_TUNNEL)
                .withCrouchingVariant(State.WIND_SPEED_SELECT);
        var icicleEntryE = moves.register(MoveClass.SPECIAL3, ICICLE_CHARGE, State.ICICLE_CHARGE);
        icicleEntryE.withFollowup(State.ICICLE_FIRE);
        icicleEntryE.withCrouchingVariant(State.DRY_ICE);
        moves.register(MoveClass.ULTIMATE, ATMOSPHERIC_ACCUMULATION, State.ATMOSPHERIC_ACCUMULATION);
        moves.register(MoveClass.UTILITY, WIND_DETECTION, State.WIND_DETECTION)
                .withCrouchingVariant(State.WEATHER_TOGGLE);
    }

    @Nullable
    private LargeIcicleProjectile chargeIcicle;

    @Nullable
    public LargeIcicleProjectile getChargeIcicle() { return chargeIcicle; }
    public void setChargeIcicle(@Nullable LargeIcicleProjectile icicle) { chargeIcicle = icicle; }

    public WeatherReportEntity(Level world) {
        super(JStandTypeRegistry.WEATHER_REPORT.get(), world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ELECTRIFIED_TICKS, 0);
        entityData.define(WIND_SPEED_INDEX, 0);
        entityData.define(WEATHER_METER, 0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && getElectrifiedTicks() > 0) {
            setElectrifiedTicks(getElectrifiedTicks() - 1);
        }

        final LivingEntity user = getUser();
        if (user == null) return;

        if (isElectrified() && level().isClientSide && tickCount % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                final double angle = random.nextDouble() * Math.PI * 2;
                final double r = 0.3 + random.nextDouble() * 0.5;
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + Math.cos(angle) * r,
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + Math.sin(angle) * r,
                        0, 0.03, 0);
            }
        }

        final boolean isFalling = user.getDeltaMovement().y < -0.1 && !user.onGround();
        final boolean blockNearby = hasBlockWithin5Below(user);
        if (isFalling && blockNearby) {
            if (!level().isClientSide) {
                user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, true, false));
                if (tickCount % 6 == 0) {
                    final CloudPuffEntity puff = CloudPuffEntity.footCloud(level(), user);
                    puff.setPos(user.getX(), user.getY() + 0.1, user.getZ());
                    level().addFreshEntity(puff);
                }
            }
        }

        if (!level().isClientSide && tickCount % 55 == 0) {
            final CloudPuffEntity puff = CloudPuffEntity.idlePuff(level(), user);
            final double angle = random.nextDouble() * Math.PI * 2;
            puff.setPos(
                    getX() + Math.cos(angle) * (0.4 + random.nextDouble() * 0.6),
                    getY() + getBbHeight() * 0.8,
                    getZ() + Math.sin(angle) * (0.4 + random.nextDouble() * 0.6));
            level().addFreshEntity(puff);
        }
    }

    private boolean hasBlockWithin5Below(final LivingEntity entity) {
        for (int i = 1; i <= 5; i++) {
            if (!level().getBlockState(entity.blockPosition().below(i)).isAir()) {
                return true;
            }
        }
        return false;
    }

    public int getElectrifiedTicks() {
        return entityData.get(ELECTRIFIED_TICKS);
    }

    public void setElectrifiedTicks(final int ticks) {
        final boolean wasElectrified = isElectrified();
        entityData.set(ELECTRIFIED_TICKS, ticks);
        if (level().isClientSide) return;
        if (!wasElectrified && ticks > 0) beginElectrified();
        else if (wasElectrified && ticks == 0) endElectrified();
    }

    public boolean isElectrified() {
        return getElectrifiedTicks() > 0;
    }

    public void beginElectrified() {
        switchMoveSet(ELECTRIFIED_MOVE_SET.getName());
    }

    public void endElectrified() {
        switchMoveSet(DEFAULT_MOVE_SET.getName());
    }

    public int getWindSpeedIndex() {
        return entityData.get(WIND_SPEED_INDEX);
    }

    public void setWindSpeedIndex(final int index) {
        entityData.set(WIND_SPEED_INDEX, Mth.clamp(index, 0, WindSpeed.values().length - 1));
    }

    public double getWindTunnelSpeed() {
        return WindSpeed.values()[getWindSpeedIndex()].velocity();
    }

    public float getWeatherMeter() {
        return entityData.get(WEATHER_METER);
    }

    public void setWeatherMeter(final float value) {
        entityData.set(WEATHER_METER, Mth.clamp(value, 0f, 1f));
    }

    @Override
    public @NonNull WeatherReportEntity getThis() {
        return this;
    }

    public enum State implements StandAnimationState<WeatherReportEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.weather_report.idle", AzPlayBehaviors.LOOP)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.weather_report.block", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WATER_SPLASH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.water_splash", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_ELECTRIFIED(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.light_electrified", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.barrage", AzPlayBehaviors.LOOP)),
        HAILSTORM(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.hailstorm", AzPlayBehaviors.LOOP)),
        ELECTRIFIED_BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.electrified_barrage", AzPlayBehaviors.LOOP)),
        GROUND_RIPPLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.ground_ripple", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WIND_WAVE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.wind_wave", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THUNDERSTORM_SHOCK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.thunderstorm_shock", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHTNING_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.lightning_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ELECTRIFIED_SHOT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.electrified_shot", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WIND_TUNNEL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.wind_tunnel", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WIND_SPEED_SELECT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.wind_speed_select", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ICICLE_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.icicle_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ICICLE_FIRE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.icicle_fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ATMOSPHERIC_ACCUMULATION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.atmospheric_accumulation", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WIND_DETECTION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.wind_detection", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        WEATHER_TOGGLE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.weather_toggle", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GALE_FORCE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.gale_force", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DRY_ICE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.weather_report.dry_ice", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ;

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull WeatherReportEntity attacker) {
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
