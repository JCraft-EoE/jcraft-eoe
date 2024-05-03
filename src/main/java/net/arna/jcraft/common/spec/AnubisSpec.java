package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.anubis.Rekka3Attack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class AnubisSpec extends JSpec<AnubisSpec, AnubisSpec.State> {
    public static final SimpleAttack<AnubisSpec> AERIAL_CLEAVE = new SimpleAttack<AnubisSpec>(100, 9, 15, 1f, 5f,
            15, 1.75f, 0.4f, 0.3f)
            .withCondition(AnubisSpec::isHoldingAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_SLASH)
            .withImpactSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withInfo(Text.literal("Aerial Cleave"), Text.literal("interruptible faster recovery"));
    public static final SimpleAttack<AnubisSpec> SLASH = new SimpleAttack<AnubisSpec>(220, 9, 20, 1f, 6f,
            15, 1.75f, 0.9f, 0f)
            .withCondition(AnubisSpec::isHoldingAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withAerialVariant(AERIAL_CLEAVE)
            .withSound(JSoundRegistry.ANUBIS_SLASH)
            .withImpactSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withHyperArmor()
            .withInfo(Text.literal("Slash"), Text.literal("uninterruptible get-off-me tool"));
    public static final SimpleAttack<AnubisSpec> POMMEL = new SimpleAttack<AnubisSpec>(180, 5, 8,
            1f, 4f, 7, 1.25f, 0.2f, 0f)
            .withSound(JSoundRegistry.ANUBIS_POMMEL)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Pommel Strike"), Text.literal("fast jab"));
    public static final SimpleMultiHitAttack<AnubisSpec> REKKA2 = new SimpleMultiHitAttack<AnubisSpec>(180,
            26, 1f, 4f, 15, 1.75f, 0.2f, -0.1f, IntSet.of(8, 20))
            .withCondition(AnubisSpec::isHoldingAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Cleaving Strikes (2 Hits)"), Text.empty());
    public static final KnockdownMultiHitAttack<AnubisSpec> REKKA_FINISHER = new KnockdownMultiHitAttack<AnubisSpec>(
            0, 40, 1f, 7f, 15, 2f, 0.9f, 0f,
            IntSet.of(32), 35)
            .withHitSpark(JParticleType.SWEEP_ATTACK);
    public static final Rekka3Attack REKKA3 = new Rekka3Attack(180, 40, 1f, 4f,
            15, 1.75f, 0.6f, -0.1f, IntSet.of(8, 20, 32))
            .withFollowup(REKKA_FINISHER)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_REKKA3)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Cleaving Strikes (3 Hits)"), Text.literal("last hit knocks down if on 0 Bloodlust"));
    public static final UppercutAttack<AnubisSpec> LOW_KICK = new UppercutAttack<AnubisSpec>(40, 10, 17,
            1.5f, 6f, 15, 1.33f, 0.3f, 0f, 0.3f)
            .withAction(AnubisSpec::resetLastHitTime)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withStaticY()
            .withInfo(Text.literal("Low Kick"), Text.literal("sheathed-only, launches slightly up"));
    public static final SimpleMultiHitAttack<AnubisSpec> UNSHEATHING_SWEEP = new SimpleMultiHitAttack<AnubisSpec>(100, 16, 1f,
            3f, 10, 1.25f, 0.3f, 0.3f, IntSet.of(6, 10))
            .withCondition(AnubisSpec::isHoldingSheathedAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withAction(AnubisSpec::unsheatheSweep)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Unsheating Sweep"), Text.literal("2 hits, knocks down"));
    public static final SimpleAttack<AnubisSpec> UNSHEATHING_ATTACK = new SimpleAttack<AnubisSpec>(100, 6, 12, 1f, 5f,
            13, 1.75f, 0.5f, 0f)
            .withCrouchingVariant(UNSHEATHING_SWEEP)
            .withCondition(AnubisSpec::isHoldingSheathedAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withAction(AnubisSpec::unsheatheAttack)
            .withImpactSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withInfo(Text.literal("Unsheathing Attack"), Text.literal("unsheathes Anubis"));

    @Setter
    private int ticksSinceLastHit = 0;
    @Getter
    protected float attackSpeedMult = 1f;

    private static void tryIncrementBloodlust(IAttacker<?, ?> attacker, LivingEntity living, MoveContext moveContext, Set<LivingEntity> targets) {
        if (targets.isEmpty()) return;
        boolean hit = true;

        for (LivingEntity target : targets) {
            if (JUtils.isBlocking(target)) {
                hit = false;
                break;
            }
        }

        if (hit && living instanceof PlayerEntity playerEntity) {
            AnubisSpec anubisSpec = (AnubisSpec) JUtils.getSpec(playerEntity);
            anubisSpec.setTicksSinceLastHit(0);
            if (anubisSpec.attackSpeedMult < 2.0f) {
                anubisSpec.attackSpeedMult += 0.2f;
                JComponents.getMiscData(playerEntity).setAttackSpeedMult(anubisSpec.attackSpeedMult);
            }
        }
    }

    private static void unsheatheAttack(AnubisSpec attacker, LivingEntity user, MoveContext ctx, @Nullable Set<LivingEntity> targets) {
        if (user.getWorld() instanceof ServerWorld serverWorld) {
            if (user.getMainHandStack().isOf(JObjectRegistry.ANUBISSHEATHED)) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE, serverWorld, user.getPos());
                user.setStackInHand(Hand.MAIN_HAND, new ItemStack(JObjectRegistry.ANUBIS));
            }
            if (user.getOffHandStack().isOf(JObjectRegistry.ANUBISSHEATHED)) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE, serverWorld, user.getPos());
                user.setStackInHand(Hand.OFF_HAND, new ItemStack(JObjectRegistry.ANUBIS));
            }
        }
    }

    private static void unsheatheSweep(AnubisSpec attacker, LivingEntity user, MoveContext ctx, Set<LivingEntity> targets) {
        int blow = UNSHEATHING_SWEEP.getBlow(attacker);
        if (blow == 1) {
            unsheatheAttack(attacker, user, ctx, targets);
            targets.forEach(
                    target -> {
                        if (!JUtils.isBlocking(target))
                            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0));
                    }
            );
        }
    }

    private void resetLastHitTime(LivingEntity living, MoveContext moveContext, Set<LivingEntity> targets) {
        if (targets.isEmpty()) return;
        if (living instanceof PlayerEntity playerEntity) {
            AnubisSpec anubisSpec = (AnubisSpec) JUtils.getSpec(playerEntity);
            anubisSpec.setTicksSinceLastHit(0);
        }
    }

    public AnubisSpec(PlayerEntity player) {
        super(SpecType.ANUBIS, player);
    }

    @Override
    protected void registerMoves(MoveMap<AnubisSpec, State> moves) {
        moves.register(MoveType.HEAVY, POMMEL, CooldownType.HEAVY, State.POMMEL);
        moves.register(MoveType.SPECIAL1, SLASH, CooldownType.SPECIAL1, State.SLASH).withAerialVariant(State.AERIAL_CLEAVE);
        moves.register(MoveType.SPECIAL1, UNSHEATHING_ATTACK, CooldownType.SPECIAL1, State.UNSHEATHING_ATTACK)
                .withCrouchingVariant(State.UNSHEATHING_SWEEP);
        moves.register(MoveType.SPECIAL2, REKKA2, CooldownType.SPECIAL2, State.REKKA2);
        moves.register(MoveType.SPECIAL3, REKKA3, CooldownType.SPECIAL2, State.REKKA3);
        moves.register(MoveType.SPECIAL3, LOW_KICK, CooldownType.SPECIAL3, State.SWEEP);
    }

    private static boolean isHoldingSheathedAnubis(AnubisSpec spec) {
        return spec.player.isHolding(JObjectRegistry.ANUBISSHEATHED);
    }
    private static boolean isHoldingAnubis(AnubisSpec spec) {
        return spec.player.isHolding(JObjectRegistry.ANUBIS);
    }

    @Override
    public AnubisSpec getThis() {
        return this;
    }

    // Attacks
    @Override
    public boolean initMove(MoveType type) {
        switch (type) {
            case HEAVY -> {
                return handleMove(POMMEL, CooldownType.HEAVY, isHoldingAnubis(this) ? State.POMMEL : State.POMMEL_IN, attackSpeedMult);
            }
            case SPECIAL1 -> {
                boolean s;
                if (isHoldingAnubis(this)) {
                    s = getUserOrThrow().isOnGround() ?
                            handleMove(SLASH, CooldownType.SPECIAL1, State.SLASH, attackSpeedMult) :
                            handleMove(AERIAL_CLEAVE, CooldownType.SPECIAL1, State.AERIAL_CLEAVE, attackSpeedMult);
                } else if (isHoldingSheathedAnubis(this)) {
                    s = getUserOrThrow().isSneaking() ?
                            handleMove(UNSHEATHING_SWEEP, CooldownType.SPECIAL1, State.UNSHEATHING_SWEEP, attackSpeedMult) :
                            handleMove(UNSHEATHING_ATTACK, CooldownType.SPECIAL1, State.UNSHEATHING_ATTACK, attackSpeedMult);
                } else return false;

                return s;
            }
            case SPECIAL3 -> {
                boolean s;
                if (isHoldingAnubis(this)) {
                    s = handleMove(REKKA3, CooldownType.SPECIAL2, State.REKKA3, attackSpeedMult);
                } else {
                    s = handleMove(LOW_KICK, CooldownType.SPECIAL3, State.SWEEP, attackSpeedMult);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, LOW_KICK.getDuration(), 2, true, false));
                }

                return s;
            }
            default -> {
                return handleMove(type, attackSpeedMult);
            }
        }
    }

    @Override
    public void tickSpec() {
        super.tickSpec();
        if (++ticksSinceLastHit > 80 && attackSpeedMult > 1f) {
            ticksSinceLastHit = 0; // Technically untrue, but all this serves for is counting 5s since last hit then rolling over
            attackSpeedMult -= 0.2f;
            JComponents.getMiscData(player).setAttackSpeedMult(attackSpeedMult);
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
        UNSHEATHING_SWEEP("an.uss"),;

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
