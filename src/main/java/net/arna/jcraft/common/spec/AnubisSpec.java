package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.anubis.Rekka3Attack;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.KnockdownMultiHitAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Set;

public class AnubisSpec extends JSpec<AnubisSpec, AnubisSpec.State> {
    public static final SimpleAttack<AnubisSpec> SLASH = new SimpleAttack<AnubisSpec>(340, 9, 20, 1f, 6f,
            15, 1.75f, 0.9f, 0f)
            .withCondition(AnubisSpec::isHoldingAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_SLASH)
            .withImpactSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .withHitSpark(JParticleType.SWEEP_ATTACK)
            .withHyperArmor()
            .withInfo(Text.literal("Slash"), Text.literal("uninterruptible get-off-me tool"));
    public static final SimpleAttack<AnubisSpec> POMMEL = new SimpleAttack<AnubisSpec>(180, 5, 8,
            1f, 4f, 7, 1.25f, 0.3f, 0f)
            .withSound(JSoundRegistry.ANUBIS_POMMEL)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withImpactSound(JSoundRegistry.IMPACT_3);
    public static final SimpleMultiHitAttack<AnubisSpec> REKKA2 = new SimpleMultiHitAttack<AnubisSpec>(280,
            26, 1f, 4f, 15, 1.75f, 0.6f, -0.1f, IntSet.of(8, 20))
            .withCondition(AnubisSpec::isHoldingAnubis)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Cleaving Strikes"), Text.literal("2 hits"));
    public static final KnockdownMultiHitAttack<AnubisSpec> REKKA_FINISHER = new KnockdownMultiHitAttack<AnubisSpec>(
            0, 40, 1f, 7f, 15, 2f, 0.9f, 0f,
            IntSet.of(32), 35)
            .withHitSpark(JParticleType.SWEEP_ATTACK);
    public static final Rekka3Attack REKKA3 = new Rekka3Attack(280, 40, 1f, 4f,
            15, 1.75f, 0.6f, -0.1f, IntSet.of(8, 20, 32))
            .withFollowup(REKKA_FINISHER)
            .withAction(AnubisSpec::tryIncrementBloodlust)
            .withSound(JSoundRegistry.ANUBIS_REKKA3)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Cleaving Strikes/Sweep"), Text.literal("3 hits, if 0 Bloodlust, last hit knocks down/sweeps while sheathed"));
    public static final KnockdownAttack<AnubisSpec> SWEEP = new KnockdownAttack<AnubisSpec>(220, 10, 17,
            1.5f, 7f, 9, 1.33f, 0.3f, 0f, 35)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Sweep"), Text.empty());

    private int ticksSinceLastHit = 0;
    @Getter
    protected float attackSpeedMult = 1f;

    private static void tryIncrementBloodlust(IAttacker<?, ?> attacker, LivingEntity living, MoveContext moveContext, Set<LivingEntity> targets) {
        if (targets.isEmpty()) return;
        if (living instanceof PlayerEntity player) {
            AnubisSpec anubisSpec = (AnubisSpec) JUtils.getSpec(player);
            if (anubisSpec.attackSpeedMult < 2.0f)
                anubisSpec.attackSpeedMult += 0.25f;
        }
    }

    public AnubisSpec(PlayerEntity player) {
        super(SpecType.ANUBIS, player);
    }

    @Override
    protected void registerMoves(MoveMap<AnubisSpec, State> moves) {
        moves.register(MoveType.HEAVY, POMMEL, CooldownType.HEAVY, State.POMMEL);
        moves.register(MoveType.SPECIAL1, SLASH, CooldownType.SPECIAL1, State.SLASH);
        moves.register(MoveType.SPECIAL2, REKKA2, CooldownType.SPECIAL2, State.REKKA2);
        moves.register(MoveType.SPECIAL3, REKKA3, CooldownType.SPECIAL3, State.REKKA3);
        moves.register(MoveType.SPECIAL3, SWEEP, CooldownType.SPECIAL3, State.SWEEP);
    }

    private static boolean isHoldingAnubis(AnubisSpec spec) {
        return spec.player.isHolding(JObjectRegistry.ANUBIS);
    }

    @Override
    protected AnubisSpec getThis() {
        return this;
    }

    // Attacks
    @Override
    public void initMove(MoveType type) {
        switch (type) {
            case HEAVY -> handleMove(POMMEL, CooldownType.HEAVY, isHoldingAnubis(this) ? State.POMMEL : State.POMMEL_IN, attackSpeedMult);
            case SPECIAL3 -> {
                if (isHoldingAnubis(this)) {
                    handleMove(REKKA3, CooldownType.SPECIAL2, State.REKKA3, attackSpeedMult);
                } else {
                    handleMove(SWEEP, CooldownType.SPECIAL3, State.SWEEP, attackSpeedMult);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, SWEEP.getDuration(), 2, true, false));
                }
            }
            default -> handleMove(type, attackSpeedMult);
        }
    }

    @Override
    public void tickSpec() {
        super.tickSpec();
        if (++ticksSinceLastHit > 100 && attackSpeedMult > 1f) {
            ticksSinceLastHit = 0; // Technically untrue, but all this serves for is counting 5s since last hit then rolling over
            attackSpeedMult -= 0.25f;
        }
    }

    public enum State implements SpecAnimationState<AnubisSpec> {
        SLASH("an.slsh"),
        POMMEL("an.pom"),
        POMMEL_IN("an.pmi"),
        REKKA2("an.2hit"),
        REKKA3("an.3hit"),
        SWEEP("an.swp");

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
