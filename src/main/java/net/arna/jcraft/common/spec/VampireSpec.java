package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.attack.moves.shared.UppercutAttack;
import net.arna.jcraft.common.attack.moves.vampire.BloodSuckAttack;
import net.arna.jcraft.common.attack.moves.vampire.ReviveMove;
import net.arna.jcraft.common.attack.moves.vampire.SpaceRipperAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.VampireComponent;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class VampireSpec extends JSpec<VampireSpec, VampireSpec.State> {
    public static final UppercutAttack<VampireSpec> SWEEP = new UppercutAttack<VampireSpec>(30, 6,
            12, 1f, 5f, 12, 1.5f, 0.5f, 0.5f, 0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStaticY()
            .withInfo(Text.literal("Sweep Kick"), Text.literal("fast launcher"));

    public static final SimpleAttack<VampireSpec> ROUNDHOUSE = new SimpleAttack<VampireSpec>(30, 8,
            15, 1f, 6f, 15, 1.5f, 1.5f, 0f)
            .withCrouchingVariant(SWEEP)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Text.literal("Wheel Kick"), Text.literal("fast launcher"));

    public static final SimpleMultiHitAttack<VampireSpec> COMBO = new SimpleMultiHitAttack<VampireSpec>(180,
            21, 1f, 2.5f, 12, 1.5f, 0.2f, -0.1f, IntSet.of(5, 8, 11, 14, 18))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(5)
            .withInfo(Text.literal("Beatdown"), Text.literal("hits 5 times, combo starter/extender"));

    public static final SimpleMultiHitAttack<VampireSpec> BLOODSUCK_HITS = new SimpleMultiHitAttack<VampireSpec>(0,
            40, 1f, 4, 5, 1.5f, 0.6f, -0.1f, IntSet.of(13, 26, 39))
            .withAction((attacker, user, ctx, targets) -> {
                user.heal(1);
                attacker.vampireComponent.setBlood(attacker.vampireComponent.getBlood() + 2 * JUtils.getBloodMult(ctx.get(BloodSuckAttack.TARGET)));
                JUtils.serverPlaySound(SoundEvents.ENTITY_GENERIC_DRINK, (ServerWorld) user.getWorld(), user.getPos(), 32);
            })
            .withStunType(StunType.LAUNCH)
            .withInfo(Text.literal("Blood Suck (Hit)"), Text.empty());
    public static final BloodSuckAttack<VampireSpec, State> BLOODSUCK = new BloodSuckAttack<>(240, 11, 18,
            1f, 1f, 40, 1.5f, 0f, 0f, BLOODSUCK_HITS, State.BLOODSUCK_HIT, 40, 2f)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.BACK_STAB) // todo: bloodsuck particles
            .withInfo(Text.literal("Blood Suck"), Text.literal("blockable grab"));

    public static final SpaceRipperAttack SPACE_RIPPER_ATTACK = new SpaceRipperAttack(300, 16, 25,1f)
            .withInfo(Text.literal("Space Ripper Stingy Eyes"), Text.literal("unblockable laser beam"));

    public static final ReviveMove<VampireSpec> REVIVE_MOVE = new ReviveMove<VampireSpec>(300, 16, 20, 5)
            .withInfo(Text.literal("Resurrection"), Text.literal("revives humanoid/undead enemies within 5 meters, that died within the last 1 minute"));

    private final VampireComponent vampireComponent;

    public VampireSpec(PlayerEntity player) {
        super(SpecType.VAMPIRE, player);
        vampireComponent = JComponents.getVampirism(player);
        vampireComponent.setVampire(true);
    }

    @Override
    protected void registerMoves(MoveMap<VampireSpec, State> moves) {
        moves.register(MoveType.HEAVY, ROUNDHOUSE, CooldownType.HEAVY, State.ROUNDHOUSE).withCrouchingVariant(State.SWEEP);
        moves.register(MoveType.BARRAGE, COMBO, CooldownType.BARRAGE, State.COMBO);

        moves.register(MoveType.SPECIAL1, SPACE_RIPPER_ATTACK, CooldownType.SPECIAL1, State.SPACE_RIPPERS);
        moves.register(MoveType.SPECIAL2, BLOODSUCK, CooldownType.SPECIAL2, State.BLOODSUCK);
        moves.register(MoveType.SPECIAL3, REVIVE_MOVE, CooldownType.SPECIAL3, State.RESURRECT);
    }

    @Override
    protected VampireSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<VampireSpec> {
        ROUNDHOUSE("vm.rnd"),
        COMBO("vm.5hit"),

        SPACE_RIPPERS("vm.srse"),

        BLOODSUCK("vm.bsk"),
        BLOODSUCK_HIT("vm.bsh"),

        SWEEP("vm.swp"),

        RESURRECT("vm.rsr");

        private final String key;

        State(String key) {
            this.key = key;
        }

        @Override
        public String getKey(VampireSpec spec) {
            return key;
        }
    }
}
