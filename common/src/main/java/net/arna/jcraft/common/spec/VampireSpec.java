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
    public static final MoveSet<VampireSpec, State> MOVE_SET = MoveSetManager.create(JSpecTypeRegistry.VAMPIRE, VampireSpec::registerMoves, VampireSpec.class, State.class);
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
            .withInfo(
                    Component.literal("Axe Kick"),
                    Component.literal("Fast jab. Spikes down on air hit.")
            );

    public static final SimpleUppercutAttack<VampireSpec> SWEEP = new SimpleUppercutAttack<VampireSpec>(0, 6,
            12, 1f, 5f, 12, 1.5f, 0.2f, 0.5f, 0.5f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withStaticY()
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LOW)
            .withInfo(
                    Component.literal("Sweep Kick"),
                    Component.literal("Fast, vertical launcher.")
            );

    public static final SimpleAttack<VampireSpec> ROUNDHOUSE = new SimpleAttack<VampireSpec>(0, 8,
            15, 1f, 6f, 19, 1.5f, 1.5f, 0f)
            .withCrouchingVariant(SWEEP)
            .withAerialVariant(AIR_KICK)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Wheel Kick"),
                    Component.literal("Launcher with good damage. Slower than the other kicks. Combos into either Blood Suck.")
            );

    public static final SimpleMultiHitAttack<VampireSpec> COMBO = new SimpleMultiHitAttack<VampireSpec>(240,
            23, 1f, 2.5f, 12, 1.5f, 0.2f, -0.1f, IntSet.of(5, 8, 12, 16, 20))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withBlockStun(5)
            .withInfo(
                    Component.literal("Beatdown"),
                    Component.literal("Hits 5 times, combo starter/extender.")
            );

    public static final SimpleAttack<VampireSpec> FEEDING_BLOODSUCK_LAUNCH = new SimpleAttack<VampireSpec>(0, 38,
            46, 1f, 3f, 9, 1.75f, 1.5f, 0f)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(Component.literal("Feeding Blood Suck (Finisher)"), Component.empty());
    public static final BloodSuckHitsAttack FEEDING_BLOODSUCK_HITS = new BloodSuckHitsAttack(0, 46, 0.8f,
            2.5f, 19, 1.75f, 0.6f, -0.1f, IntSet.of(8, 16, 24), 2.0f)
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(24, FEEDING_BLOODSUCK_LAUNCH)
            .withInfo(Component.literal("Feeding Blood Suck (Hit)"), Component.empty());
    public static final BloodSuckAttack FEEDING_BLOODSUCK = new BloodSuckAttack(240, 10, 18,
            0.8f, 1f, 20, 1.5f, 0f, 0f, FEEDING_BLOODSUCK_HITS,
            FEEDING_BLOODSUCK_HITS.getDuration(), 2f, true)
            .withSound(JSoundRegistry.VAMPIRE_GRAB_HIT)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.BLOOD_SPARK_2)
            .withInfo(
                    Component.literal("Feeding Blood Suck"),
                    Component.literal("Blockable grab. Much more efficient for feeding than Blood Bottles. Launches the victim away.")
            );

    public static final BloodSuckHitsAttack BLOODSUCK_HITS = new BloodSuckHitsAttack(0, 25, 0.8f,
            4, 5, 1.75f, 0.6f, -0.1f, IntSet.of(8, 16, 24), 1.0f)
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(Component.literal("Blood Suck (Hit)"), Component.empty());
    public static final BloodSuckAttack BLOODSUCK = new BloodSuckAttack(240, 10, 18,
            1f, 1f, BLOODSUCK_HITS.getDuration(), 1.5f, 0f, 0f, BLOODSUCK_HITS,
            BLOODSUCK_HITS.getDuration(), 2f, false)
            .withCrouchingVariant(FEEDING_BLOODSUCK)
            .withSound(JSoundRegistry.VAMPIRE_GRAB_HIT)
            .withImpactSound(JSoundRegistry.IMPACT_9)
            .withHitSpark(JParticleType.BLOOD_SPARK_2)
            .withInfo(
                    Component.literal("Blood Suck"),
                    Component.literal("Blockable grab. More efficient for feeding than Blood Bottles. Allows combo extension.")
            );

    public static final SpaceRipperAttack<VampireSpec> SPACE_RIPPER_ATTACK = new SpaceRipperAttack<VampireSpec>(300, 1, 10, 1f)
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
                    """)
            );

    public static final NightVisionMove<VampireSpec> TOGGLE_NV = new NightVisionMove<VampireSpec>(20)
            .withInfo(Component.literal("Toggle Night Vision"), Component.empty());

    public static final ReviveMove<VampireSpec> REVIVE_MOVE = new ReviveMove<VampireSpec>(300, 16, 20, 5)
            .withCrouchingVariant(TOGGLE_NV)
            .withSound(JSoundRegistry.VAMPIRE_REANIMATE)
            .withInfo(
                    Component.literal("Resurrection"),
                    Component.literal("Revives humanoid/undead enemies within 5 meters, that died within the last 1 minute.")
            );

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
        moves.register(MoveClass.SPECIAL2, BLOODSUCK, CooldownType.SPECIAL2, State.BLOODSUCK)
                .withCrouchingVariant(State.FEEDING_BLOODSUCK);

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

        FEEDING_BLOODSUCK("vm.fbsk"),
        FEEDING_BLOODSUCK_HIT("vm.fbsh"),

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
