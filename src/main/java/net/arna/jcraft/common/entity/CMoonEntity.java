package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//todo: 3d, rotatable shockwave particle effect
//todo: particles on gravpunch and both slams
public class CMoonEntity extends StandEntity<CMoonEntity, CMoonEntity.State> {
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");

    public static final Attack barrage = new Attack(2, 17, 0.75f, 50, 0, 2, 0.75f, 0.25f, AttackType.BARRAGE, 1, 0, 4, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static final Attack gutpunch = new Attack(1, 17, 1f, 30, 19, 2.0, 8f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2).hyperArmor().setLaunch()
            .appendHitbox(new HitBoxData(0, 0.25, 1.25))
            .setInfo("Gut Punch", "slow, uninterruptable combo finisher");
    public static final Attack launch = new Attack(4, 22, 0.75f, 21, 14, 1.75, 5f, 0.9f, AttackType.BOX, 0.95f, 0.3f, 0, JSoundRegister.IMPACT_5)
            .setHitspark(2)
            .setRanged(true)
            .setInfo("Block Launch", "lifts a block from the ground and launches it at a delay/crouching and using this button resets the delay on nearby blocks");
    public static final Attack gravpunch = new Attack(3, 24, 1f, 32, 20, 1.75, 6f, 0.35f, AttackType.BOX, 2.25f, -0.3f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT).setHitspark(2).hyperArmor()
            .setUB(true)
            .appendHitbox(new HitBoxData(1))
            .setInfo("Only One Punch", "lifts enemy on hit");
    public static final Attack groundslam = new Attack(5, 23, 1f, 18, 10, 3, 7f, 0.2f, AttackType.BOX, 0.85f, 1.4f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT)
            .setUB(true)
            .setInfo("Ground Slam", "lifts the ground, combo starter/extender, knockdown when used while crouching");
    public static final Attack gravshift = new Attack(6, 70, 32, 20, 7, AttackType.BOX)
            .setInfo("Gravity Shift", """
                    increases user jump height, changes the gravity of everything in a 64 block radius
                    Types: REPULSE, ATTRACT, NONE
                    swap between types by pressing the key again""");
    public static final Attack directionalshift = new Attack(7, 70, 32, 20, 7, AttackType.BOX)
            .crouchingVariation(gravshift)
            .setInfo("Gravity Shift Pulse", """
                    changes the gravitational direction of nearby entities to the users looked direction""");

    public final ArrayList<Float> invertDamages = new ArrayList<>();
    public final ArrayList<LivingEntity> invertEntities = new ArrayList<>();
    public final ArrayList<Integer> invertTimes = new ArrayList<>();

    public CMoonEntity(World worldIn) {
        super(StandType.C_MOON, worldIn);
        super.initialize();
        idleRotation = 220f;

        pros = List.of(
                "fast m1",
                "very multipurpose",
                "damaging aftereffect",
                "good pressure"
        );

        cons = List.of(
                "execution intensive",
                "lacking in controlled horizontal movement"
        );

        freespace = """
                Passive: Inversion, all physical hits deal an extra half heart after 2s

                    BNBs:
                    the mean green bean
                    M1>Barrage>jump>Block Launch>M1>Only One Punch>Block hits>Grav. Hop>Ground Slam""";

        moves = List.of(light, gutpunch, barrage, gravpunch, directionalshift, launch, groundslam
                , new Attack().setMobility(MobilityType.HIGHJUMP)
                        .setInfo("Gravitational Hop/Local Gravity Change", "jumps up and grants 2s slow falling/crouch to change your gravitational direction")
        );
    }

    private static final TrackedData<Integer> SHIFTTYPE;
    private static final TrackedData<Integer> SHIFTTIME;
    static {
        SHIFTTIME = DataTracker.registerData(CMoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SHIFTTYPE = DataTracker.registerData(CMoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }
    public int getShiftTime() { return this.dataTracker.get(SHIFTTIME); }
    public void setShiftTime(int sTime) { this.dataTracker.set(SHIFTTIME, sTime); }
    public int getShiftType() { return this.dataTracker.get(SHIFTTYPE); }
    public void setShiftType(int sType) { this.dataTracker.set(SHIFTTYPE, sType); }
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(SHIFTTIME, 0);
        getDataTracker().startTracking(SHIFTTYPE, 0);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) return;
        handleAttack(light, JCraft.standLightCD, State.LIGHT);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            this.playSound(JSoundRegister.CMOON_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(gutpunch, JCraft.standHeavyCD, State.DONUT))
            this.playSound(JSoundRegister.CMOON_DONUT, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(gravpunch, JCraft.standS1CD, State.GRAV_PUNCH))
            this.playSound(JSoundRegister.CMOON_GRAVPUNCH, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        if (user.isSneaking()) {
            List<BlockProjectile> blocks = world.getEntitiesByClass(BlockProjectile.class, getBoundingBox().expand(16), EntityPredicates.VALID_LIVING_ENTITY);
            for (BlockProjectile block :
                    blocks) {
                if (block.getMaster() != user) continue;
                block.markRefresh();
            }
        } else if (canAttack() && handleAttack(launch, JCraft.standS2CD, State.GROUND_SHOOT))
            playSound(JSoundRegister.CMOON_GROUNDSHOOT, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        if (handleAttack(groundslam, JCraft.standS3CD, State.GROUND_SLAM))
            playSound(JSoundRegister.CMOON_GROUNDSLAM, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (getShiftTime() <= 0) {
            if (getUserOrThrow().isSneaking() && handleAttack(gravshift, JCraft.standUltCD, State.GRAV_SHIFT))
                playSound(JSoundRegister.CMOON_GRAVSHIFT, 1, 1);
            else if (handleAttack(directionalshift, JCraft.standUltCD, State.DIRECTIONAL_SHIFT))
                playSound(JSoundRegister.CMOON_GRAVSHIFT_DIRECTIONAL, 1, 1);

        } else {
            int shiftType = getShiftType();
            if (++shiftType > 2)
                shiftType = 0;
            setShiftType(shiftType);
        }
    }

    private int directionChangeCooldown = 0;
    @Override
    public void initUtil() {
        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();

        if (user.isOnGround() && directionChangeCooldown <= 0) {
            StatusEffectInstance weightless = user.getStatusEffect(JStatusRegister.WEIGHTLESS);
            if (weightless != null && weightless.getAmplifier() == 1) {
                user.removeStatusEffect(JStatusRegister.WEIGHTLESS);
                user.addStatusEffect(new StatusEffectInstance(JStatusRegister.WEIGHTLESS, weightless.getDuration(), 1));
            }

            directionChangeCooldown = 10;
            return;
        }

        if (!this.canAttack()) return;
        IEntityDataSaver userData = (IEntityDataSaver) user;
        if (userData.getPersistentData().getInt(JCraft.utilCD) > 0) return;

        if (user.isOnGround()) {
            user.addStatusEffect(new StatusEffectInstance(JStatusRegister.WEIGHTLESS, 200, 1));
        } else {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 1));
            user.addVelocity(0, 1.0, 0);
        }

        user.velocityModified = true;
        userData.getPersistentData().putInt(JCraft.utilCD, 340);
    }

    @Override
    public void standBlock() {
        LivingEntity user = getUser();
        if (user == null) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = world.getEntitiesByClass(ProjectileEntity.class, getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getPos().subtract(getPos()).normalize());
            projectile.velocityModified = true;
        }

        stun(user, 2, 2);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 2, false, false));
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();

        for (LivingEntity ent : entities) {
            invertDamages.add(attack == barrage ? 0.25f : 0.5f);
            invertEntities.add(ent);
            invertTimes.add(40);

            if (attack.id == gravpunch.id) {
                GravityChangerAPI.addGravity(ent, new Gravity(Direction.UP, 2, 60, "stand"));
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0, true, false));
                ent.velocityModified = true;
            }
        }

        switch (attack.id) {
            case (4) -> { // Block Launch
                BlockProjectile block = new BlockProjectile(JEntityTypeRegister.BLOCK_PROJECTILE, world);
                BlockState steppingState = getSteppingBlockState();
                if (steppingState.isAir() || !steppingState.isOpaque())
                    block.setBlockStack(Items.STONE.getDefaultStack());
                else
                    block.setBlockStack(steppingState.getBlock().asItem().getDefaultStack());

                Vec3i hoverDir = GravityChangerAPI.getGravityDirection(user).getVector().multiply(-1);

                block.setMaster(user);
                block.refreshPositionAndAngles(getX() + hoverDir.getX() * 1.5, getY() + hoverDir.getY() * 1.5, getZ() + hoverDir.getZ() * 1.5, getYaw(), getPitch());
                block.setVelocity(hoverDir.getX() * 0.4, hoverDir.getY() * 0.4, hoverDir.getZ() * 0.4);
                world.spawnEntity(block);
            }
            case (5) -> { // Ground Slam
                for (LivingEntity ent : entities) {
                    ent.setVelocity(new Vec3d(0.0, -0.75, 0.0));
                    ent.velocityModified = true;
                    if (user.isSneaking())
                        ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 30, 0));
                }

                if (world.getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
                    BlockPos bPos = getBlockPos();
                    for (int x = -2; x < 3; x++) {
                        for (int y = -1; y < 1; y++) {
                            for (int z = -2; z < 3; z++) {
                                BlockPos curPos = bPos.add(x, y, z);
                                BlockState curState = world.getBlockState(curPos);

                                if (curState.getBlock().getBlastResistance() > 10f || curState.isAir()) continue;

                                FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, curPos, curState);
                                fallingBlock.setVelocity(0, 0.5, 0);
                                fallingBlock.timeFalling = -120;
                                fallingBlock.velocityModified = true;
                                fallingBlock.velocityDirty = true;
                            }
                        }
                    }
                }
            }
            case (6) -> { // Area Gravity Shift
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 350, 1));
                user.onLanding();
                setShiftTime(200);
            }
            case (7) -> { // Directional Gravity Shift
                Direction lookDir = JUtils.getLookDirection(user);
                List<Entity> toCatch = world.getEntitiesByClass(Entity.class, getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                toCatch.remove(this);
                toCatch.remove(user);

                directionShiftAge = age;

                for (Entity entity : toCatch) {
                    directionShiftedEntities.add(entity);

                    GravityChangerAPI.addGravity(entity, new Gravity(lookDir, 3, gravityChangeDuration, "stand_ultimate"));
                    if (entity instanceof LivingEntity living)
                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, gravityChangeDuration, 0, true, false));
                }
            }
        }
    }

    private static final int gravityChangeDuration = 600;
    private int directionShiftAge;
    private final List<Entity> directionShiftedEntities = new ArrayList<>();

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.CMOON_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUserOrThrow();
            Vec3d pos = getPos();
            int sTime = getShiftTime();

            if (world.isClient) {
                setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);

                if (sTime > 0) {
                    for (int h = 0; h < 256; ++h) {
                        Vec3d vel = Vec3d.ZERO;
                        double x = pos.x + random.nextTriangular(0, 100);
                        double y = pos.y + random.nextTriangular(0, 10);
                        double z = pos.z + random.nextTriangular(0, 100);
                        switch (getShiftType()) {
                            case (0) -> vel = new Vec3d(x, y, z).subtract(pos);
                            case (1) -> vel = pos.subtract(x, y, z);
                        }
                        this.world.addParticle(
                                ParticleTypes.REVERSE_PORTAL,
                                x, y, z,
                                vel.x, vel.y, vel.z);
                    }
                }
            } else {
                directionChangeCooldown--;

                for (int i = 0; i < invertTimes.size(); i++) {
                    int time = invertTimes.get(i);
                    invertTimes.set(i, time - 1);
                    if (time < 1) {
                        LivingEntity entity = invertEntities.get(i);
                        damage(invertDamages.get(i), DamageSource.mob(user), entity);
                        invertTimes.remove(i);
                        invertEntities.remove(i);
                        invertDamages.remove(i);
                        i--;
                    }
                }

                if (age - directionShiftAge >= gravityChangeDuration && !directionShiftedEntities.isEmpty())
                    directionShiftedEntities.clear();
                else
                    for (Entity entity : directionShiftedEntities) {
                        if (entity.squaredDistanceTo(this) > 10000) // 100m
                            GravityChangerAPI.clearGravity(entity); // todo: this interferes with other gravities, solve later
                    }

                if (sTime > 0 && !user.hasStatusEffect(JStatusRegister.DAZED)) {
                    List<Entity> toCatch = world.getEntitiesByClass(Entity.class, getBoundingBox().expand(64), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    toCatch.remove(this);
                    toCatch.remove(user);

                    for (Entity entity : toCatch) {
                        if ( entity instanceof BlockProjectile block && block.getMaster() == user)
                            continue;
                        //Vec3d vel = entity.getVelocity();
                        switch (getShiftType()) {
                            //case (0) -> entity.addVelocity(-vel.x / 3.0, -0.1, -vel.z / 3.0);
                            case (0) -> entity.setVelocity(
                                    entity.getVelocity().add( entity.getPos().subtract(pos).normalize().multiply(0.1) )
                            );
                            case (1) -> entity.setVelocity(
                                    entity.getVelocity().add( pos.subtract(entity.getPos()).normalize().multiply(0.1) )
                            );
                        }

                        if ( entity instanceof ServerPlayerEntity serverPlayerEntity)
                            serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                        entity.velocityModified = true;
                    }

                    this.setShiftTime(sTime - 1);
                }
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<CMoonEntity> {
        IDLE(builder -> builder.loop("animation.cmoon.idle")),
        LIGHT(builder -> builder.playAndHold("animation.cmoon.light")),
        BLOCK(builder -> builder.loop("animation.cmoon.block")),
        DONUT(builder -> builder.playAndHold("animation.cmoon.donut")),
        BARRAGE(builder -> builder.loop("animation.cmoon.barrage")),
        GRAV_PUNCH(builder -> builder.playAndHold("animation.cmoon.gravpunch")),
        GROUND_SLAM(builder -> builder.playAndHold("animation.cmoon.groundslam")),
        GROUND_SHOOT(builder -> builder.playAndHold("animation.cmoon.groundshoot")),
        GRAV_SHIFT(builder -> builder.playAndHold("animation.cmoon.gravshift")),
        DIRECTIONAL_SHIFT(builder -> builder.playAndHold("animation.cmoon.directionalshift"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(CMoonEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.cmoon.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
