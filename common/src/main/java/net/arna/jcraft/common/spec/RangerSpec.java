package net.arna.jcraft.common.spec;

import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonGunslingerComponent;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.common.attack.moves.ranger.RangerFocusMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerHolsterMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerRollMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerSlideMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleUppercutAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.IJInputStateManagerHolder;
import net.arna.jcraft.common.util.InputStateManager;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class RangerSpec extends JSpec<RangerSpec, RangerSpec.State> {
    public static final float MAX_FOCUS = 100f; // 5 seconds of use
    public static final float FOCUS_DRAIN = 1f;
    public static final float FOCUS_REGEN = 1f; // empty to full in 5 seconds
    public static final float CROUCH_FOCUS_REGEN = 0.7f;

    public static final MoveSet<RangerSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.RANGER, RangerSpec::registerMoves, RangerSpec.class, State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.ranger"))
            .description(Component.literal("Mobile skirmisher"))
            .details(Component.literal("""
                    Roll and Slide let you weave through fights and reposition.
                    Slide carries hit enemies, launching them up for juggles."""))
            .build();

    public static final SimpleAttack<RangerSpec> BUTTSTROKE = new SimpleAttack<RangerSpec>(80, 8, 13,
            1f, 6f, 18, 1.5f, 0.3f, 0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Buttstroke"),
                    Component.literal("Heavy downwards blow with high stun. Slower, longer cooldown.")
            );
    public static final SimpleAttack<RangerSpec> SHOULDER_BASH = new SimpleAttack<RangerSpec>(30, 7, 14,
            1f, 5f, 14, 1.5f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withArmor(1)
            .withStaticY()
            .withLaunch()
            .withInfo(
                    Component.literal("Shoulder Bash"),
                    Component.literal("Springs up from a squat, launching enemies. 1 point of armor.")
            );
    public static final SimpleAttack<RangerSpec> WHIP = new SimpleAttack<RangerSpec>(15, 5, 8,
            1f, 4f, 7, 1.25f, 0.2f, 0f)
            .withAerialVariant(BUTTSTROKE)
            .withCrouchingVariant(SHOULDER_BASH)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Whip"),
                    Component.literal("Fast strike with the held item.")
            );

    public static final RangerRollMove ROLL = new RangerRollMove(100, 0, 20, 0f)
            .withInfo(
                    Component.literal("Roll"),
                    Component.literal("Combat roll that greatly reduces your hitbox and negates fall damage, but exhausts you at the end.")
            );

    public static final RangerSlideMove SLIDE = new RangerSlideMove(140, 0, 30, 0f,
            2f, 10, 1.5f, 2f, 0f)
            .withCrouchingVariant(ROLL)
            .withLaunchNoShockwave()
            .withBackstab(false)
            .withInfo(
                    Component.literal("Slide"),
                    Component.literal("Held. Fast crawl that carries hit enemies, launching them up for juggles.")
            );

    public static final RangerHolsterMove HOLSTER = new RangerHolsterMove(10, 0, 0, 0f)
            .withInfo(
                    Component.literal("Holster"),
                    Component.literal("Stows the held non-stackable item, or draws the stowed one. Swaps if holding another.")
            );

    public static final RangerFocusMove FOCUS = new RangerFocusMove(10)
            .withSound(JSoundRegistry.RANGER_FOCUS)
            .withSound(JSoundRegistry.RANGER_FOCUS_START)
            .withInfo(
                    Component.literal("Focus"),
                    Component.literal("Slows you to a crawl and guides your projectiles towards the target closest to your crosshair. Can shoot other projectiles out of the air.")
            );

    public RangerSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.RANGER.get(), livingEntity);
    }

    private static void registerMoves(MoveMap<RangerSpec, State> moves) {
        MoveMap.Entry<RangerSpec, State> hvy = moves.register(MoveClass.HEAVY, WHIP, CooldownType.HEAVY, State.WHIP);
        hvy.withCrouchingVariant(State.BASH);
        hvy.withAerialVariant(State.BUTTSTROKE);

        moves.register(MoveClass.BARRAGE, SLIDE, CooldownType.BARRAGE, State.SLIDE_START)
                .withCrouchingVariant(State.ROLL);

        moves.register(MoveClass.SPECIAL1, FOCUS, CooldownType.SPECIAL1, null);
        moves.register(MoveClass.SPECIAL2, HOLSTER, CooldownType.SPECIAL2, null);
    }

    @Override
    public void tickSpec() {
        super.tickSpec();

        if (getEntityWorld().isClientSide || (user != null && user.isSpectator())) {
            return;
        }

        final CommonGunslingerComponent gunslinger = JComponentPlatformUtils.getGunslinger(user);
        if (gunslinger.isFocusActive()) {
            final float focus = gunslinger.getFocus() - FOCUS_DRAIN;
            gunslinger.setFocus(Math.max(focus, 0f));
            if (focus <= 0f) {
                gunslinger.setFocusActive(false);
            } else {
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 4, true, false));
            }
        } else if (gunslinger.getFocus() < MAX_FOCUS) {
            final float regen = getFocusRegen();
            gunslinger.setFocus(Math.min(gunslinger.getFocus() + regen, MAX_FOCUS));
        }
    }

    private float getFocusRegen() {
        if (user instanceof ServerPlayer player) {
            final InputStateManager input = ((IJInputStateManagerHolder) player).jcraft$getJInputStateManager();
            final boolean moving = input.calcForward() != 0 || input.calcSide() != 0;
            if (!moving) {
                return FOCUS_REGEN;
            }
            return input.sneaking ? CROUCH_FOCUS_REGEN : 0f;
        }
        if (user.getDeltaMovement().horizontalDistanceSqr() <= 1.0E-4) {
            return FOCUS_REGEN;
        }
        return user.isShiftKeyDown() ? CROUCH_FOCUS_REGEN : 0f;
    }

    @Override
    public RangerSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<RangerSpec> {
        WHIP("whip"),
        BUTTSTROKE("buttstroke"),
        BASH("bash"),
        ROLL("roll"),
        SLIDE_START("slide_start"),
        SLIDE_LOOP("slide_loop"),
        ;

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(RangerSpec spec) {
            return key;
        }
    }
}
