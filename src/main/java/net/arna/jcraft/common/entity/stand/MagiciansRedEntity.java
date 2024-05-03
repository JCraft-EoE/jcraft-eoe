package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.magiciansred.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class MagiciansRedEntity extends StandEntity<MagiciansRedEntity, MagiciansRedEntity.State> {
    public static final RedirectAttack REDIRECT = new RedirectAttack(0, 7, 10, 0.75f)
            .withAnim(State.REDIRECT)
            .withSound(JSoundRegistry.MR_REDIRECT)
            .withInfo(
                    Text.literal("Redirect"),
                    Text.literal("redirects all the users ankhs to where they're looking")
            );
    public static final SimpleAttack<MagiciansRedEntity> LIGHT_FOLLOWUP = new SimpleAttack<MagiciansRedEntity>(
            0, 6, 14, 0.65f, 6f, 12, 1.5f, 1.2f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo finisher")
            );
    public static final SimpleAttack<MagiciansRedEntity> LIGHT = new SimpleAttack<MagiciansRedEntity>(JCraft.LIGHT_COOLDOWN,
            5, 8, 0.75f, 5f, 16, 1.5f, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(REDIRECT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("quick combo starter")
            );
    public static final KnockdownAttack<MagiciansRedEntity> HEAVY = new KnockdownAttack<MagiciansRedEntity>(100,
            12, 22, 1f, 7f, 10, 1.75f, 0.5f, 0.6f, 40)
            .withAnim(State.HEAVY)
            .withSound(JSoundRegistry.MR_HEAVY)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(
                    Text.literal("Low Kick"),
                    Text.literal("medium windup knockdown")
            );
    public static final SimpleAttack<MagiciansRedEntity> HAMMERFIST_FLARE = new SimpleAttack<MagiciansRedEntity>(0,
            1, 5, 1f, 6f, 10, 1.75f, 1.5f, -0.2f)
            .withLaunch()
            .withAction((attacker, user, ctx, targets) -> attacker.playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.0f, 1.0f))
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Text.literal("Hammerfist Flare"),
                    Text.literal("launcher")
            );
    public static final SimpleAttack<MagiciansRedEntity> HAMMERFIST = new SimpleAttack<MagiciansRedEntity>(100,
            10, 20, 1f, 3f, 13, 1.75f, 0.2f, 0)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withFinisher(15, HAMMERFIST_FLARE)
            .withCrouchingVariant(HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(HitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Text.literal("Hammerfist"),
                    Text.literal("two-hit launcher")
            );
    public static final FlamethrowerAttack FLAMETHROWER = new FlamethrowerAttack(300, 0,
            40, 0.75f, 0.4f, 0, 2, 0.25f, 0, 3)
            .withArmor(1)
            .withSound(JSoundRegistry.MR_BARRAGE)
            .withInfo(
                    Text.literal("Flamethrower"),
                    Text.literal("fast reliable damage cash-out tool, no stun, burns for 3 seconds")
            );
    public static final CrossfireAttack CROSSFIRE = new CrossfireAttack(240, 8, 10, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(
                    Text.literal("Crossfire"),
                    Text.literal("fires 3 stunning ankhs")
            );
    public static final CrossfireVariationAttack CROSSFIRE_VARIATION = new CrossfireVariationAttack(600, 12, 17, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(
                    Text.literal("Crossfire Variation"),
                    Text.literal("summons 6 ankhs that orbit around the user, crouch as they come out to increase orbit distance")
            );
    public static final CrossfireHurricaneAttack CROSSFIRE_HURRICANE = new CrossfireHurricaneAttack(800, 18, 22, 0.75f)
            .withSound(JSoundRegistry.MR_ULT)
            .withInfo(
                    Text.literal("Crossfire Hurricane"),
                    Text.literal("summons slow, homing fire hurricane that knocks down, lasts for 3 seconds after hitting anything")
            );
    public static final RedBindAttack RED_BIND = new RedBindAttack(300, 12, 22, 0.75f, 3, 15, 1.5f, 0, 0)
            .withSound(JSoundRegistry.MR_REDBIND)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Text.literal("Red Bind"),
                    Text.literal("on hit, wraps opponent in fiery rings that launch them in the direction they are hit")
            );
    public static final LifeDetectorAttack LIFE_DETECTOR = new LifeDetectorAttack(280, 13, 20, 0.75f)
            .withSound(JSoundRegistry.MR_DETECTOR)
            .withInfo(
                    Text.literal("Life Detector"),
                    Text.literal("tracks down nearby life, lasts 15s")
            );

    public MagiciansRedEntity(World worldIn) {
        super(StandType.MAGICIANS_RED, worldIn, JSoundRegistry.MR_SUMMON);
        idleRotation = 225f;

        description = "Tailor-made, Blazing ZONER";

        pros = List.of(
                "incredible setups",
                "high damage",
                "two knockdowns"
        );

        cons = List.of(
                "easily blockable projectiles",
                "no mobility options",
                "no armored options"
        );

        freespace = """
                    PASSIVE: Fire Resistance
    
                    BNBs:
                        -the "this move is fire"
                        M1>Crossfire
                        
                        -the happy camper
                        M1>Low Kick>Variation/Life Detector
                        
                        -the "omg i have setups????"
                        M1>Hammerfist>dash>M1>Red Bind>
                        ...Life Detector/Variation>any physical hit
                        ...Hurricane""";

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.6f, 0.3f),
                new Vector3f(0.8f, 0.3f, 1.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(1.0f, 0.2f, 0.4f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<MagiciansRedEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, LIGHT, State.LIGHT);

        moves.registerImmediate(MoveType.HEAVY, HAMMERFIST, State.HAMMER);
        moves.register(MoveType.BARRAGE, FLAMETHROWER, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, CROSSFIRE, State.CROSSFIRE);
        moves.register(MoveType.SPECIAL2, CROSSFIRE_VARIATION, State.CROSSFIRE_VARIATION);
        moves.register(MoveType.SPECIAL3, RED_BIND, State.RED_BIND);

        moves.register(MoveType.ULTIMATE, CROSSFIRE_HURRICANE, State.CROSSFIRE_HURRICANE);

        moves.register(MoveType.UTILITY, LIFE_DETECTOR, State.DETECTOR);
    }

    @Override
    public boolean initMove(MoveType type) {
        if (type == MoveType.LIGHT && curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
            AbstractMove<?, ? super MagiciansRedEntity> followup = curMove.getFollowup();
            if (followup != null) setMove(followup, (State) followup.getAnimation());
        } else return super.initMove(type);

        return true;
    }

    public static void ignite(World world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        Collection<Property<?>> properties = state.getProperties();

        boolean cantIgnite = false;
        if (properties.contains(Properties.WATERLOGGED))
            cantIgnite = state.get(Properties.WATERLOGGED);
        if (block == Blocks.REDSTONE_LAMP) return;
        if (cantIgnite) return;

        if (properties.contains(Properties.LIT))
            world.setBlockState(blockPos, state.with(Properties.LIT, true));
        if (block == Blocks.WET_SPONGE) { // WetSpongeBlock has no drying function to call
            world.setBlockState(blockPos, Blocks.SPONGE.getDefaultState(), 3);
            world.syncWorldEvent(2009, blockPos, 0);
            world.playSound(null, blockPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, (1.0F + world.getRandom().nextFloat() * 0.2F) * 0.7F);
        }
        if (world.getBlockEntity(blockPos) instanceof AbstractFurnaceBlockEntity furnaceBlock)
            furnaceBlock.burnTime = 220;
        if (block instanceof IceBlock iceBlock)
            iceBlock.melt(state, world, blockPos);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;

        if (getWorld().isClient && getState() == State.BARRAGE && FLAMETHROWER.hasWindupPassed(this)) {
            Vec3d rotVec = getRotationVector();
            Vec3d mouthPos = getEyePos().add(rotVec);
            for (int i = 0; i < 16; i++) {
                Vec3d vel = getUserOrThrow().getVelocity().add(
                        rotVec
                                .rotateX(random.nextFloat() - 0.5f)
                                .rotateY(random.nextFloat() - 0.5f)
                                .rotateZ(random.nextFloat() - 0.5f)
                                .multiply(0.2)
                );
                getWorld().addParticle(
                        random.nextInt(6) == 5 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                        mouthPos.x, mouthPos.y, mouthPos.z,
                        vel.x, vel.y, vel.z
                );
            }
        }

        getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20, 0, true, false));
        CROSSFIRE_HURRICANE.tickHurricane(this);
    }

    @Override
    @NonNull
    public MagiciansRedEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<MagiciansRedEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.mr.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.mr.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.heavy"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.barrage"))),
        CROSSFIRE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.crossfire"))),
        CROSSFIRE_HURRICANE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.crossfirehurricane"))),
        CROSSFIRE_VARIATION(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.crossfirevariation"))),
        REDIRECT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.redirect"))),
        RED_BIND(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.redbind"))),
        DETECTOR(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.detector"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.light_followup"))),
        HAMMER(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.mr.hammer")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MagiciansRedEntity attacker, AnimationState builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.mr.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
