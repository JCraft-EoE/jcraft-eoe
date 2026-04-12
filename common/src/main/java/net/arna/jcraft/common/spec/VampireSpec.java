package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.vampire.*;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Getter
public class VampireSpec extends JSpec<VampireSpec, VampireSpec.State> {
    public static final MoveSet<VampireSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.VAMPIRE, VampireSpec::registerMoves, State.class);
    public static final SpecData DATA = SpecData.builder()
            .name(Component.translatable("spec.jcraft.vampire"))
            .description(Component.literal("Supernatural all-ranger"))
            .details(Component.literal("""
                    PASSIVES: Burns in sunlight, Replaces hunger with blood, Night vision
                    Excellent frametraps with Sweep or Axe Kick.
                    Bloodsuck is extremely rewarding and allows linking into almost any move."""))
            .build();

    public static final SimpleUppercutAttack<VampireSpec> AIR_KICK = new SimpleUppercutAttack<VampireSpec>(0, 6,
            12, 1f, 5f, 14, 1.5f, 0.2f, 0.5f, -0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStaticY()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(Component.literal("Axe Kick"), Component.literal("jab"));

    public static final SimpleUppercutAttack<VampireSpec> SWEEP = new SimpleUppercutAttack<VampireSpec>(0, 6,
            12, 1f, 5f, 12, 1.5f, 0.2f, 0.5f, 0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStaticY()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(Component.literal("Sweep Kick"), Component.literal("fast launcher"));

    public static final SimpleAttack<VampireSpec> ROUNDHOUSE = new SimpleAttack<VampireSpec>(0, 8,
            15, 1f, 6f, 19, 1.5f, 1.5f, 0f)
            .withCrouchingVariant(SWEEP)
            .withAerialVariant(AIR_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Component.literal("Wheel Kick"), Component.literal("fast launcher"));

    public static final SimpleMultiHitAttack<VampireSpec> COMBO = new SimpleMultiHitAttack<VampireSpec>(240,
            23, 1f, 2.5f, 12, 1.5f, 0.2f, -0.1f, IntSet.of(5, 8, 12, 16, 20))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(5)
            .withInfo(Component.literal("Beatdown"), Component.literal("hits 5 times, combo starter/extender"));

    public static final BloodSuckHitsAttack BLOODSUCK_HITS = new BloodSuckHitsAttack(0, 25, 1f,
            4, 5, 1.5f, 0.6f, -0.1f, IntSet.of(8, 16, 24))
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(Component.literal("Blood Suck (Hit)"), Component.empty());
    public static final BloodSuckAttack BLOODSUCK = new BloodSuckAttack(240, 10, 18,
            1f, 1f, BLOODSUCK_HITS.getDuration(), 1.5f, 0f, 0f, BLOODSUCK_HITS,
            BLOODSUCK_HITS.getDuration(), 2f)
            .withSound(JSoundRegistry.VAMPIRE_GRAB_HIT)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.BACK_STAB) // todo: bloodsuck particles
            .withInfo(Component.literal("Blood Suck"), Component.literal("blockable grab"));

    public static final SpaceRipperAttack SPACE_RIPPER_ATTACK = new SpaceRipperAttack(300, 1, 10, 1f)
            .withInfo(Component.literal("Space Ripper Stingy Eyes (Fire)"), Component.empty());
    public static final SimpleHoldableMove<VampireSpec> SPACE_RIPPER_CHARGE = new SimpleHoldableMove<VampireSpec>(
            300, 0, 32, 1f, 14)
            .withFollowup(SPACE_RIPPER_ATTACK)
            .withSound(JSoundRegistry.VAMPIRE_LASER)
            .shouldSetMoveStun()
            .withInfo(Component.literal("Space Ripper Stingy Eyes"), Component.literal("""
                    Chargeable laser beam attack.
                    Laser velocity depends on charge time.
                    After charging for 1.2s, becomes unblockable.
                    """));

    public static final NightVisionMove TOGGLE_NV = new NightVisionMove(20)
            .withInfo(Component.literal("Toggle Night Vision"), Component.empty());

    public static final ReviveMove REVIVE_MOVE = new ReviveMove(300, 16, 20, 5)
            .withCrouchingVariant(TOGGLE_NV)
            .withSound(JSoundRegistry.VAMPIRE_REANIMATE)
            .withInfo(Component.literal("Resurrection"), Component.literal("revives humanoid/undead enemies within 5 meters, that died within the last 1 minute"));

    public static final float MAX_BLOOD = 20f;

    private final CommonVampireComponent vampireComponent;

    public VampireSpec(LivingEntity livingEntity) {
        super(JSpecTypeRegistry.VAMPIRE.get(), livingEntity);
        vampireComponent = JComponentPlatformUtils.getVampirism(livingEntity);
    }

    private static void registerMoves(MoveMap<VampireSpec, State> moves) {
        MoveMap.Entry<VampireSpec, State> hvy = moves.register(MoveClass.HEAVY, ROUNDHOUSE, CooldownType.HEAVY, State.ROUNDHOUSE);
        hvy.withCrouchingVariant(State.SWEEP);
        hvy.withAerialVariant(State.AXE_KICK);

        moves.register(MoveClass.BARRAGE, COMBO, CooldownType.BARRAGE, State.COMBO);

        moves.register(MoveClass.SPECIAL1, SPACE_RIPPER_CHARGE, CooldownType.SPECIAL1, State.SPACE_RIPPER_CHARGE).withFollowup(State.SPACE_RIPPERS);
        moves.register(MoveClass.SPECIAL2, BLOODSUCK, CooldownType.SPECIAL2, State.BLOODSUCK);
        moves.register(MoveClass.SPECIAL3, REVIVE_MOVE, CooldownType.SPECIAL3, State.RESURRECT).withCrouchingVariant(null);
    }

    @Override
    public VampireSpec getThis() {
        return this;
    }

    public enum State implements SpecAnimationState<VampireSpec> {
        SWEEP("vm.swp"),
        ROUNDHOUSE("vm.rnd"),
        AXE_KICK("vm.axe"),

        COMBO("vm.5hit"),

        SPACE_RIPPER_CHARGE("vm.srsc"),
        SPACE_RIPPERS("vm.srse"),

        BLOODSUCK("vm.bsk"),
        BLOODSUCK_HIT("vm.bsh"),

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
