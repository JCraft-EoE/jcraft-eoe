package net.arna.jcraft.common.entity.vehicle;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class RoadRollerEntity extends AbstractGroundVehicleEntity {

    private static boolean recipeChecked = false;
    private static Recipe<?> recipe = null; // lazy-loaded (allows dynamic loading in case someone is trying to replace our recipe)
    public RoadRollerEntity(final Level level) {
        super(JEntityTypeRegistry.ROAD_ROLLER.get(), level);
        if (!recipeChecked) {
            var maybe = level.getRecipeManager()
                    .getRecipes()
                    .stream()
                    .filter(r -> r.getResultItem(level.registryAccess()).is(JItemRegistry.ROAD_ROLLER.get()))
                    .findFirst();
            maybe.ifPresent(value -> recipe = value);
            recipeChecked = true;
        }
    }

    private int ridingTicks = 0;
    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (isVehicle()) {
                SHAKE_CMD.sendForEntity(this);
                if ((ridingTicks - 52) % 82 == 0)
                    level().playLocalSound(getX(), getY(), getZ(), JSoundRegistry.ROAD_ROLLER_ACTIVE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f, true);
                ridingTicks++;

                final Direction gravity = GravityChangerAPI.getGravityDirection(this);

                final float yaw = yRotO + 90.0f;
                final Vec3 down = new Vec3(gravity.getStepX(), gravity.getStepY(), gravity.getStepZ());
                final Vec3 forward = new Vec3(
                        Mth.cos(yaw * Mth.DEG_TO_RAD),
                        0,
                        Mth.sin(yaw * Mth.DEG_TO_RAD)
                );
                final Vec3 right = new Vec3(
                        Mth.cos((yaw + 90.0f) * Mth.DEG_TO_RAD),
                        0,
                        Mth.sin((yaw + 90.0f) * Mth.DEG_TO_RAD)
                );

                final Vec3 localPos = new Vec3(
                        -forward.x * 2.62 - right.x * (0.65 + random.nextDouble() / 5.0 - 0.1) - down.x * 2.25,
                        -forward.y * 2.62 - right.y * (0.65 + random.nextDouble() / 5.0 - 0.1) - down.y * 2.25,
                        -forward.z * 2.62 - right.z * (0.65 + random.nextDouble() / 5.0 - 0.1) - down.z * 2.25
                );
                //.rotateAxis(yaw * JUtils.DEG_TO_RAD, gravity.getStepX(), gravity.getStepY(), gravity.getStepZ());

                final Vec3 worldPos = RotationUtil.vecWorldToPlayer(
                        localPos.x,
                        localPos.y,
                        localPos.z,
                        gravity
                ).add(position());

                level().addParticle(ParticleTypes.SMOKE,
                        worldPos.x,
                        worldPos.y,
                        worldPos.z,
                        down.x * 0.03 - forward.x * 0.06 + random.nextDouble() * 0.05 - 0.025,
                        down.y * 0.03 - forward.y * 0.06 + random.nextDouble() * 0.05 - 0.025,
                        down.z * 0.03 - forward.z * 0.06 + random.nextDouble() * 0.05 - 0.025
                );
            } else {
                ridingTicks = 0;
            }
            // other animations
            if (steeringLeft()) {
                STEER_LEFT_CMD.sendForEntity(this);
            }
            else if (steeringRignt()) {
                STEER_RIGHT_CMD.sendForEntity(this);
            }
            else {
                STEER_NEUTRAL_CMD.sendForEntity(this);
            }
            if (movingForward()) {
                MOVE_FORWARD_CMD.sendForEntity(this);
            }
            else if (movingBack()) {
                MOVE_BACKWARD_CMD.sendForEntity(this);
            }
            if (getHurtTime() > 0) {
                HIT_CMD.sendForEntity(this);
            }
            if (deathTime > 0) {
                DEATH_CMD.sendForEntity(this);
            }
        }
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (deathTime == 1) {
            final Level level = level();
            final Vec3 pos = position();

            if (!level.isClientSide()) {
                level.explode(this,
                        pos.x,
                        pos.y,
                        pos.z,
                        2.0f,
                        level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ?
                                Level.ExplosionInteraction.MOB :
                                Level.ExplosionInteraction.NONE
                );

                for (Ingredient ingredient : recipe.getIngredients()) {
                    final ItemStack[] items = ingredient.getItems();
                    for (ItemStack stack : items) {
                        final ItemEntity drop = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
                        drop.setPickUpDelay(40);
                        drop.setDeltaMovement(JUtils.randUnitVec(random));
                        level.addFreshEntity(drop);
                    }
                }
            }
        }
    }

    private static final double INLINE_SPEED = 0.1d, TURN_RATE = 5.0d;
    private static final Map<BlockState, BlockState> FLATTENED_BLOCK_STATES = Map.ofEntries(
            Map.entry(Blocks.DIRT.defaultBlockState(), Blocks.FARMLAND.defaultBlockState()),
            Map.entry(Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT_PATH.defaultBlockState()),
            Map.entry(Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE.defaultBlockState()),
            Map.entry(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState()),
            Map.entry(Blocks.SANDSTONE.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState())
    );
    @Override
    public void movementTick(boolean w, boolean a, boolean s, boolean d, boolean space, boolean sneak) {
        double drag = 0.99;
        final Level level = level();
        final Direction gravity = GravityChangerAPI.getGravityDirection(this);
        oldYRot = getYRot();

        Vec3 movement = getDeltaMovement();
        if (Double.isNaN(movement.x)) movement = new Vec3(0, movement.y, movement.z);
        if (Double.isNaN(movement.y)) movement = new Vec3(movement.x, 0, movement.z);
        if (Double.isNaN(movement.z)) movement = new Vec3(movement.x, movement.y, 0);

        boolean grounded = onGround();
        if (grounded) {
            drag = getGroundFriction();
        } else { // Edge case coverage
            Optional<BlockPos> supporting = level.findSupportingBlock(this, getBoundingBox().inflate(0.1));
            if (supporting.isPresent()) {
                grounded = true;
                drag = level.getBlockState(supporting.get()).getBlock().getFriction();
            }
            // setOnGround(grounded);
        }

        if (grounded) {
            if (w || s) {
                double inline;
                if (w) inline = INLINE_SPEED;
                else inline = -INLINE_SPEED;

                if (a || d) {
                    double movementSpeed = movement.length(); // blocks per tick
                    if (Double.isNaN(movementSpeed)) movementSpeed = 0.0d;

                    float turnCW = 0.0f;
                    if (d) turnCW += Math.sqrt(movementSpeed);
                    else turnCW -= Math.sqrt(movementSpeed);
                    turnCW *= s ? -TURN_RATE : TURN_RATE;

                    setYRot(oldYRot + turnCW);
                }

                setDeltaMovement(
                        movement.add(getForward().scale(inline))
                );
            }
        } else if (movement.lengthSqr() == 0) { // Weird edge-case from standing on entities
            setDeltaMovement(
                    movement.add(RotationUtil.vecPlayerToWorld(new Vec3(0, -0.02, 0), gravity))
            );
        }

        if (level instanceof ServerLevel serverLevel) {
            for (int x = -1; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        Vec3 offset = new Vec3(x, -0.1 * y, z);
                        offset = RotationUtil.vecPlayerToWorld(offset, gravity);
                        final BlockPos blockPos = blockPosition().offset(
                                Mth.floor(offset.x),
                                Mth.floor(offset.y),
                                Mth.floor(offset.z)
                        );

                        // JCraft.createParticle(serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), y == 0 ? JParticleType.BACK_STAB : JParticleType.GO);

                        final BlockState state = serverLevel.getBlockState(blockPos);
                        final Block block = state.getBlock();
                        if (FLATTENED_BLOCK_STATES.containsKey(state)) {
                            if (JServerConfig.ROLLER_FLATTENING.getValue()) {
                                serverLevel.setBlock(blockPos, FLATTENED_BLOCK_STATES.get(state), Block.UPDATE_ALL);
                            }
                        }
                        // Break replaceable blocks or ones under a certain resistance on the same height level as the Road Roller
                        else if (block != Blocks.DIRT_PATH && JServerConfig.ROLLER_DESTROYING.getValue() &&
                                (state.canBeReplaced() || (y == 0 && block.getExplosionResistance() <= 3.0f))
                        ) {
                            serverLevel.destroyBlock(blockPos, true);
                        }
                    }
                }
            }
        }

        setDeltaMovement(getDeltaMovement().scale(drag));
    }

    @Override
    public @NonNull InteractionResult interact(final @NonNull Player player, final @NonNull InteractionHand hand) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;

        if (getFirstPassenger() == null) {
            if (!level().isClientSide()) {
                return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return JItemRegistry.ROAD_ROLLER.get().getDefaultInstance();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(final @NonNull DamageSource damageSource) {
        return JSoundRegistry.ROAD_ROLLER_HIT.get();
    }

    @Override
    protected void addPassenger(final @NonNull Entity passenger) {
        super.addPassenger(passenger);
        if (passenger == getFirstPassenger()) playSound(JSoundRegistry.ROAD_ROLLER_IGNITION.get());
    }

    @Override
    public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        final MobEffect effect = effectInstance.getEffect();
        if (effect == JStatusRegistry.KNOCKDOWN.get() ||
            effect == JStatusRegistry.BLEEDING.get() ||
            effect == MobEffects.POISON ||
            effect == MobEffects.WITHER
        ) return false;

        return super.addEffect(effectInstance, entity);
    }

    @Override
    public boolean hurt(@NonNull DamageSource source, float amount) {
        if (source.is(DamageTypes.SWEET_BERRY_BUSH) ||
            source.is(DamageTypes.STING) ||
            source.is(DamageTypes.THORNS) ||
            source.is(DamageTypes.STARVE)
        ) return false;

        return super.hurt(source, amount);
    }

    @Override
    public void resetFallDistance() {
        final Level level = level();
        if (!level.isClientSide()) {
            if (fallDistance > 6.0) {
                final DamageSource ds = level().damageSources().cramming();
                final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), position(), 3.5, Set.of(this));

                for (LivingEntity living : hurt) {
                    if (!JUtils.canDamage(ds, living)) {
                        continue;
                    }

                    final LivingEntity target = JUtils.getUserIfStand(living);
                    if (getOwner() != target) {
                        damageLogic(level(), target, target.position().subtract(position()), 25, 3,
                                false, 12.0f, false, 21, ds, getOwner(), CommonHitPropertyComponent.HitAnimation.CRUSH, false);
                    }
                }

                JComponentPlatformUtils.getShockwaveHandler(level).addShockwave(
                        position(),
                        Vec3.atLowerCornerOf(GravityChangerAPI.getGravityDirection(this).getNormal()),
                        4.5f
                        );

                final var poofPacket = new ClientboundLevelParticlesPacket(
                        ParticleTypes.POOF, false,
                        getX(), getY(), getZ(),
                        0.2f, 0.2f, 0.2f,
                        0.3f, 32
                );

                JUtils.tracking(this).forEach(
                        serverPlayer -> serverPlayer.connection.send(poofPacket)
                );

                playSound(JSoundRegistry.ROAD_ROLLER_SLAM.get());
            }
        }
        super.resetFallDistance();
    }

    public double getPassengersRidingOffset() {
        return 1.0d;
    }

    @Override
    public void readAdditionalSaveData(final @NonNull CompoundTag compound) {}
    @Override
    public void addAdditionalSaveData(final @NonNull CompoundTag compound) {}

    // Animations
    /*

    private static final RawAnimation FORWARD = RawAnimation.begin().thenLoop("forward");
    private static final RawAnimation BACK = RawAnimation.begin().thenLoop("back");
    private <T extends GeoAnimatable> PlayState movePredicate(AnimationState<T> state) {
        if (movingForward()) return state.setAndContinue(FORWARD);
        if (movingBack()) return state.setAndContinue(BACK);
        return PlayState.STOP;
    }

    private static final RawAnimation SHAKE = RawAnimation.begin().thenLoop("shake");
    private <T extends GeoAnimatable> PlayState shakePredicate(AnimationState<T> state) {
        if (isVehicle()) return state.setAndContinue(SHAKE);
        return PlayState.STOP;
    }

    private static final RawAnimation HIT = RawAnimation.begin().thenLoop("hit");
    private <T extends GeoAnimatable> PlayState hitPredicate(AnimationState<T> state) {
        if (getHurtTime() > 0) return state.setAndContinue(HIT);
        return PlayState.STOP;
    }

    private static final RawAnimation DEATH = RawAnimation.begin().thenLoop("explode");
    private <T extends GeoAnimatable> PlayState deathPredicate(AnimationState<T> state) {
        if (deathTime > 0) return state.setAndContinue(DEATH);
        return PlayState.STOP;
    }*/
}
