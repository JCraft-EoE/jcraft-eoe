package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.common.attack.moves.anubis.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownMultiHitAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class AnubisSpec extends JSpec<AnubisSpec, AnubisSpec.State> {
    public static final MoveSet<AnubisSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.ANUBIS, AnubisSpec::registerMoves, State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.anubis"))
            .description(Component.literal("Accelerating offense"))
            .details(Component.literal("""
                    PASSIVE: Bloodlust
                    Landing blows on opponents speeds up Anubis' attacks up to 2x, with +0.2x per hit.
                    Not hitting opponents reduces Bloodlust by one stack every 4 seconds."""))
            .build();

    public static final SimpleAnubisAttack AERIAL_CLEAVE = new SimpleAnubisAttack(0, 9, 15, 1f, 5f,
            15, 1.75f, 0.4f, 0.3f, true, true)
            .withSound(JSoundRegistry.ANUBIS_SLASH)
            .withImpactSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withInfo(Component.literal("Aerial Cleave"), Component.literal("interruptible faster recovery"));
    public static final SimpleAnubisAttack SLASH = new SimpleAnubisAttack(20, 9, 20, 1f, 6f,
            15, 1.75f, 0.9f, 0f, true, true)
            .withAerialVariant(AERIAL_CLEAVE)
            .withSound(JSoundRegistry.ANUBIS_SLASH)
            .withImpactSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withHyperArmor()
            .withInfo(Component.literal("Slash"), Component.literal("uninterruptible get-off-me tool"));
    public static final SimpleAnubisAttack POMMEL = new SimpleAnubisAttack(20, 5, 8,
            1f, 4f, 7, 1.25f, 0.2f, 0f, false, true)
            .withSound(JSoundRegistry.ANUBIS_POMMEL)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Component.literal("Pommel Strike"), Component.literal("fast jab"));
    public static final SimpleAnubisMultiHitAttack REKKA2 = new SimpleAnubisMultiHitAttack(10,
            26, 1f, 4f, 15, 1.75f, 0.2f, -0.1f, IntSet.of(8, 20), false)
            .withSound(JSoundRegistry.ANUBIS_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(Component.literal("Cleaving Strikes (2 Hits)"), Component.empty());
    public static final KnockdownMultiHitAttack<AnubisSpec> REKKA_FINISHER = new KnockdownMultiHitAttack<AnubisSpec>(
            0, 40, 1f, 7f, 15, 2f, 0.9f, 0f,
            IntSet.of(32), 35)
            .withHitSpark(JParticleType.SWEEP_ATTACK);
    public static final Rekka3Attack REKKA3 = new Rekka3Attack(10, 40, 1f, 4f,
            15, 1.75f, 0.6f, -0.1f, IntSet.of(8, 20, 32))
            .withFollowup(REKKA_FINISHER)
            .withSound(JSoundRegistry.ANUBIS_REKKA3)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withInfo(Component.literal("Cleaving Strikes (3 Hits)"), Component.literal("last hit knocks down if on 0 Bloodlust"));
    public static final LowKickAttack LOW_KICK = new LowKickAttack(10, 10, 17,
            1.5f, 6f, 15, 1.33f, 0.3f, 0f, 0.3f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withStaticY()
            .withInfo(Component.literal("Low Kick"), Component.literal("sheathed-only, launches slightly up"));
    public static final SimpleAnubisMultiHitAttack UNSHEATHING_SWEEP = new SimpleAnubisMultiHitAttack(5, 16, 1f,
            3f, 10, 1.25f, 0.3f, 0.3f, IntSet.of(6, 10), true)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Component.literal("Unsheating Sweep"), Component.literal("2 hits, knocks down"));
    public static final UnsheathingAttack UNSHEATHING_ATTACK = new UnsheathingAttack(5, 6, 12, 1f, 5f,
            13, 1.75f, 0.5f, 0f)
            .withCrouchingVariant(UNSHEATHING_SWEEP)
            .withImpactSound(SoundEvents.PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withInfo(Component.literal("Unsheathing Attack"), Component.literal("unsheathes Anubis"));

    @Setter
    private int ticksSinceLastHit = 0;
    @Getter
    protected float attackSpeedMult = 1f;

    public AnubisSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.ANUBIS.get(), livingEntity);
    }

    public void tryIncrementBloodlust(Set<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return;
        }
        boolean hit = true;

        for (LivingEntity target : targets) {
            if (JUtils.isBlocking(target)) {
                hit = false;
                break;
            }
        }

        if (hit) {
            AnubisSpec anubisSpec = (AnubisSpec) JUtils.getSpec(user);
            anubisSpec.setTicksSinceLastHit(0);
            if (anubisSpec.attackSpeedMult < 2.0f) {
                anubisSpec.attackSpeedMult += 0.2f;
                JComponentPlatformUtils.getMiscData(user).setAttackSpeedMult(anubisSpec.attackSpeedMult);
            }
        }
    }

    public void unsheatheAttack(@Nullable Set<LivingEntity> targets) {
        if (user.level() instanceof ServerLevel serverWorld) {
            if (user.getMainHandItem().is(JItemRegistry.ANUBIS_SHEATHED.get())) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE.get(), serverWorld, user.position());
                user.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JItemRegistry.ANUBIS.get()));
            }
            if (user.getOffhandItem().is(JItemRegistry.ANUBIS_SHEATHED.get())) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE.get(), serverWorld, user.position());
                user.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JItemRegistry.ANUBIS.get()));
            }
        }
    }

    private static void registerMoves(MoveMap<AnubisSpec, State> moves) {
        moves.register(MoveClass.HEAVY, POMMEL, CooldownType.HEAVY, State.POMMEL);
        moves.register(MoveClass.SPECIAL1, SLASH, CooldownType.SPECIAL1, State.SLASH).withAerialVariant(State.AERIAL_CLEAVE);
        moves.register(MoveClass.SPECIAL1, UNSHEATHING_ATTACK, CooldownType.SPECIAL1, State.UNSHEATHING_ATTACK)
                .withCrouchingVariant(State.UNSHEATHING_SWEEP);
        moves.register(MoveClass.SPECIAL2, REKKA2, CooldownType.SPECIAL2, State.REKKA2);
        moves.register(MoveClass.SPECIAL3, REKKA3, CooldownType.SPECIAL2, State.REKKA3);
        moves.register(MoveClass.SPECIAL3, LOW_KICK, CooldownType.SPECIAL3, State.SWEEP);
    }

    private static boolean isHoldingSheathedAnubis(AnubisSpec spec) {
        return spec.user.isHolding(JItemRegistry.ANUBIS_SHEATHED.get());
    }

    public boolean isHoldingSheathedAnubis() {
        return isHoldingSheathedAnubis(this);
    }

    public static boolean isHoldingAnubis(AnubisSpec spec) {
        return spec.user.isHolding(JItemRegistry.ANUBIS.get());
    }

    public boolean isHoldingAnubis() {
        return isHoldingAnubis(this);
    }

    @Override
    public AnubisSpec getThis() {
        return this;
    }

    // Attacks
    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case HEAVY -> {
                return handleMove(POMMEL, CooldownType.HEAVY, isHoldingAnubis() ? State.POMMEL : State.POMMEL_IN, attackSpeedMult);
            }
            case SPECIAL1 -> {
                boolean s;
                if (isHoldingAnubis()) {
                    s = getUserOrThrow().onGround() ?
                            handleMove(SLASH, CooldownType.SPECIAL1, State.SLASH, attackSpeedMult) :
                            handleMove(AERIAL_CLEAVE, CooldownType.SPECIAL1, State.AERIAL_CLEAVE, attackSpeedMult);
                } else if (isHoldingSheathedAnubis()) {
                    s = getUserOrThrow().isShiftKeyDown() ?
                            handleMove(UNSHEATHING_SWEEP, CooldownType.SPECIAL1, State.UNSHEATHING_SWEEP, attackSpeedMult) :
                            handleMove(UNSHEATHING_ATTACK, CooldownType.SPECIAL1, State.UNSHEATHING_ATTACK, attackSpeedMult);
                } else {
                    return false;
                }

                return s;
            }
            case SPECIAL3 -> {
                boolean s;
                if (isHoldingAnubis()) {
                    s = handleMove(REKKA3, CooldownType.SPECIAL2, State.REKKA3, attackSpeedMult);
                } else {
                    s = handleMove(LOW_KICK, CooldownType.SPECIAL3, State.SWEEP, attackSpeedMult);
                    user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, LOW_KICK.getDuration(), 2, true, false));
                }

                return s;
            }
            default -> {
                return handleMove(moveClass, attackSpeedMult);
            }
        }
    }

    @Override
    public void tickSpec() {
        super.tickSpec();
        if (user != null && user.isSpectator()) {
            return;
        }
        if (++ticksSinceLastHit > 80 && attackSpeedMult > 1f) {
            ticksSinceLastHit = 0; // Technically untrue, but all this serves for is counting 5s since last hit then rolling over
            attackSpeedMult -= 0.2f;
            JComponentPlatformUtils.getMiscData(user).setAttackSpeedMult(attackSpeedMult);
        }
    }

    public enum State implements SpecAnimationState<AnubisSpec> {
        SLASH("an.slsh"),
        POMMEL("an.pom"),
        POMMEL_IN("an.pmi"),
        REKKA2("an.2hit"),
        REKKA3("an.3hit"),
        SWEEP("an.swp"),
        AERIAL_CLEAVE("an.acl"),
        UNSHEATHING_ATTACK("an.usa"),
        UNSHEATHING_SWEEP("an.uss"),
        ;

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(AnubisSpec spec) {
            return key;
        }
    }
}
