package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.common.attack.actions.PlaySoundAction;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.moves.magiciansred.*;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.attack.moves.shared.TossChargeMove;
import net.arna.jcraft.common.attack.moves.shared.TossMove;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collection;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Magician%27s_Red">Magician's Red</a>.
 * @see JStandTypeRegistry#MAGICIANS_RED
 * @see net.arna.jcraft.client.renderer.entity.stands.MagiciansRedRenderer MagiciansRedRenderer
 * @see CrossfireAttack
 * @see CrossfireHurricaneAttack
 * @see CrossfireVariationAttack
 * @see FlamethrowerAttack
 * @see LifeDetectorAttack
 * @see RedBindAttack
 * @see RedirectAttack
 */
public class MagiciansRedEntity extends StandEntity<MagiciansRedEntity, MagiciansRedEntity.State> {
    public static final MoveSet<MagiciansRedEntity, MagiciansRedEntity.State> MOVE_SET = MoveSetManager.create(
            JStandTypeRegistry.MAGICIANS_RED, "default", MagiciansRedEntity::registerMoves, MagiciansRedEntity.State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(225f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.mr"))
                    .proCount(3)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                PASSIVE: Fire Resistance
                
                BNBs:
                    -the "this move is fire"
                    Light>Crossfire
                
                    -the happy camper
                    Light>Low Kick>Variation/Life Detector
                
                    -the "omg i have setups????"
                    Light>Hammerfist>dash>Light>Red Bind>
                    ...Life Detector/Variation>any physical hit
                    ...Hurricane"""))
                    .skinName(Component.literal("Purple"))
                    .skinName(Component.literal("OVA"))
                    .skinName(Component.literal("Neon"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.MR_SUMMON))
            .build();

    public static final RedirectAttack REDIRECT = new RedirectAttack(0, 7, 10, 0.75f)
            .withAnim(State.REDIRECT)
            .withSound(JSoundRegistry.MR_REDIRECT)
            .withInfo(
                    Component.literal("Redirect"),
                    Component.literal("redirects all the users ankhs to where they're looking")
            );
    public static final SimpleAttack<MagiciansRedEntity> LIGHT_FOLLOWUP = new SimpleAttack<MagiciansRedEntity>(0,
            6, 14, 0.65f, 6f, 12, 1.5f, 1.2f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<MagiciansRedEntity> LIGHT = new SimpleAttack<MagiciansRedEntity>(JCraft.LIGHT_COOLDOWN,
            5, 8, 0.75f, 5f, 16, 1.5f, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(REDIRECT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter")
            );
    public static final KnockdownAttack<MagiciansRedEntity> HEAVY = new KnockdownAttack<MagiciansRedEntity>(0,
            12, 22, 1f, 7f, 10, 1.75f, 0.5f, 0.6f, 40)
            .withAnim(State.HEAVY)
            .withSound(JSoundRegistry.MR_HEAVY)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(
                    Component.literal("Low Kick"),
                    Component.literal("medium windup knockdown")
            );
    public static final SimpleAttack<MagiciansRedEntity> HAMMERFIST_FLARE = new SimpleAttack<MagiciansRedEntity>(0,
            1, 5, 1f, 6f, 10, 1.75f, 1.5f, -0.2f)
            .withLaunch()
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withAction(PlaySoundAction.playSound(SoundEvents.FIRECHARGE_USE))
            .withInfo(
                    Component.literal("Hammerfist Flare"),
                    Component.literal("launcher")
            );
    public static final SimpleAttack<MagiciansRedEntity> HAMMERFIST = new SimpleAttack<MagiciansRedEntity>(0,
            10, 20, 1f, 3f, 13, 1.75f, 0.2f, 0)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withFinisher(15, HAMMERFIST_FLARE)
            .withCrouchingVariant(HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Hammerfist"),
                    Component.literal("two-hit launcher")
            );
    public static final FlamethrowerAttack FLAMETHROWER = new FlamethrowerAttack(200,
            0,40, 0.75f, 0.4f, 0, 2, 0.25f, 0, 3)
            .withArmor(1)
            .withSound(JSoundRegistry.MR_BARRAGE)
            .withInfo(
                    Component.literal("Flamethrower"),
                    Component.literal("fast reliable damage cash-out tool, no stun, burns for 3 seconds")
            );
    public static final CrossfireAttack CROSSFIRE = new CrossfireAttack(100, 8, 10, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(
                    Component.literal("Crossfire"),
                    Component.literal("fires 3 stunning ankhs")
            );
    public static final CrossfireVariationAttack CROSSFIRE_VARIATION = new CrossfireVariationAttack(600, 12, 17, 0.75f)
            .withSound(JSoundRegistry.MR_CROSSFIRE)
            .withInfo(
                    Component.literal("Crossfire Variation"),
                    Component.literal("summons 6 ankhs that orbit around the user, crouch as they come out to increase orbit distance")
            );
    public static final CrossfireHurricaneAttack CROSSFIRE_HURRICANE = new CrossfireHurricaneAttack(800, 18, 22, 0.75f)
            .withSound(JSoundRegistry.MR_ULT)
            .withInfo(
                    Component.literal("Crossfire Hurricane"),
                    Component.literal("summons slow, homing fire hurricane that knocks down, lasts for 3 seconds after hitting anything")
            );
    public static final RedBindAttack RED_BIND = new RedBindAttack(100,
            12, 22, 0.75f, 3, 15, 1.5f, 0, 0)
            .withSound(JSoundRegistry.MR_REDBIND)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(
                    Component.literal("Red Bind"),
                    Component.literal("on hit, wraps opponent in fiery rings that launch them in the direction they are hit")
            );
    public static final LifeDetectorAttack LIFE_DETECTOR = new LifeDetectorAttack(200, 13, 20, 0.75f)
            .withSound(JSoundRegistry.MR_DETECTOR)
            .withInfo(
                    Component.literal("Life Detector"),
                    Component.literal("tracks down nearby life, lasts 15s")
            );
    // TODO add move info x2
    // TODO balance x2
    public static final TossMove<MagiciansRedEntity> TOSS = new TossMove<MagiciansRedEntity>(0, 1, 1, 0.75f)
            .withAnim(MagiciansRedEntity.State.ITEM_TOSS);
    public static final TossChargeMove<MagiciansRedEntity> TOSS_CHARGE = new TossChargeMove<MagiciansRedEntity>(70, 3 * 20 + 1, 3 * 20, 1.0f, 10)
            .withFollowup(TOSS);

    public MagiciansRedEntity(Level worldIn) {
        super(JStandTypeRegistry.MAGICIANS_RED.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.6f, 0.3f),
                new Vector3f(0.8f, 0.3f, 1.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(1.0f, 0.2f, 0.4f)
        };
    }

    private static void registerMoves(MoveMap<MagiciansRedEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.registerImmediate(MoveClass.HEAVY, HAMMERFIST, State.HAMMER);
        moves.register(MoveClass.BARRAGE, FLAMETHROWER, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, CROSSFIRE, State.CROSSFIRE);
        moves.register(MoveClass.SPECIAL2, CROSSFIRE_VARIATION, State.CROSSFIRE_VARIATION);
        moves.register(MoveClass.SPECIAL3, RED_BIND, State.RED_BIND);

        moves.register(MoveClass.ULTIMATE, CROSSFIRE_HURRICANE, State.CROSSFIRE_HURRICANE);

        moves.register(MoveClass.UTILITY, LIFE_DETECTOR, State.DETECTOR);

        moves.register(MoveClass.TOSS, TOSS_CHARGE, State.ITEM_TOSS_CHARGE).withFollowup(State.ITEM_TOSS);
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (moveClass == MoveClass.LIGHT && getCurrentMove() != null && getCurrentMove().getMoveClass() == MoveClass.LIGHT && getMoveStun() < getCurrentMove().getWindupPoint()) {
            AbstractMove<?, ? super MagiciansRedEntity> followup = getCurrentMove().getFollowup();
            if (followup != null) {
                setMove(followup, (State) followup.getAnimation());
            }
        } else {
            return super.initMove(moveClass);
        }

        return true;
    }

    public static void ignite(Level world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        Collection<Property<?>> properties = state.getProperties();

        boolean cantIgnite = false;
        if (properties.contains(BlockStateProperties.WATERLOGGED)) {
            cantIgnite = state.getValue(BlockStateProperties.WATERLOGGED);
        }
        if (block == Blocks.REDSTONE_LAMP) {
            return;
        }
        if (cantIgnite) {
            return;
        }

        if (properties.contains(BlockStateProperties.LIT)) {
            world.setBlockAndUpdate(blockPos, state.setValue(BlockStateProperties.LIT, true));
        }
        if (block == Blocks.WET_SPONGE) { // WetSpongeBlock has no drying function to call
            world.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 3);
            world.levelEvent(2009, blockPos, 0);
            world.playSound(null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (1.0F + world.getRandom().nextFloat() * 0.2F) * 0.7F);
        }
        if (world.getBlockEntity(blockPos) instanceof AbstractFurnaceBlockEntity furnaceBlock) {
            furnaceBlock.litTime = 220;
        }
        if (block instanceof IceBlock iceBlock) {
            iceBlock.melt(state, world, blockPos);
        }
    }

    @Override
    public double getEngagementDistance() {
        return 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) {
            return;
        }

        // TODO client ticking system for moves
        if (level().isClientSide && getState() == State.BARRAGE && FLAMETHROWER.hasWindupPassed(this)) {
            Vec3 rotVec = getLookAngle();
            Vec3 mouthPos = getEyePosition().add(rotVec);
            for (int i = 0; i < 16; i++) {
                Vec3 vel = getUserOrThrow().getDeltaMovement().add(
                        rotVec
                                .xRot(random.nextFloat() - 0.5f)
                                .yRot(random.nextFloat() - 0.5f)
                                .zRot(random.nextFloat() - 0.5f)
                                .scale(0.2)
                );
                level().addParticle(
                        random.nextInt(6) == 5 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                        mouthPos.x, mouthPos.y, mouthPos.z,
                        vel.x, vel.y, vel.z
                );
            }
        }

        getUserOrThrow().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 0, true, false));
    }

    @Override
    @NonNull
    public MagiciansRedEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<MagiciansRedEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.mr.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.mr.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.barrage", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROSSFIRE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.crossfire", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROSSFIRE_HURRICANE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.crossfirehurricane", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        CROSSFIRE_VARIATION(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.crossfirevariation", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REDIRECT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.redirect", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        RED_BIND(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.redbind", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        DETECTOR(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.detector", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HAMMER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.mr.hammer", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow", AzPlayBehaviors.PLAY_ONCE));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(MagiciansRedEntity attacker) {
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
