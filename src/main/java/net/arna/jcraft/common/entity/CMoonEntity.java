package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.*;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

//todo: 3d, rotatable shockwave particle effect
//todo: particles on gravpunch and both slams
public class CMoonEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    public static final Attack light = new Attack(0, 2, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");

    public static final Attack barrage = new Attack(2, 17, 0.75f, 50, 0, 2, 0.75f, 0.25f, AttackType.BARRAGE, 1, 0, 4, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");
    public static final Attack gutpunch = new Attack(1, 17, 1f, 30, 19, 2.0, 8f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2).setArmor(true).setLaunch()
            .appendHitbox(new Attack.HitboxData(0, 0.25, 1.25))
            .setInfo("Gut Punch", "slow, uninterruptable combo finisher");
    public static final Attack launch = new Attack(4, 22, 0.75f, 21, 14, 1.75, 5f, 0.9f, AttackType.BOX, 0.95f, 0.3f, 0, JSoundRegister.IMPACT_5)
            .setHitspark(2)
            .setRanged(true)
            .setInfo("Block Launch", "lifts a block from the ground and launches it at a delay/crouching and using this button resets the delay on nearby blocks");
    public static final Attack gravpunch = new Attack(3, 24, 1f, 32, 20, 1.75, 6f, 0.35f, AttackType.BOX, 2.25f, -0.3f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT).setHitspark(2).setArmor(true)
            .setUB(true)
            .appendHitbox(new Attack.HitboxData(1))
            .setInfo("Only One Punch", "lifts enemy on hit");
    public static final Attack groundslam = new Attack(5, 23, 1f, 18, 10, 3, 7f, 0.2f, AttackType.BOX, 0.85f, 1.4f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT)
            .setUB(true)
            .setInfo("Ground Slam", "lifts the ground, combo starter/extender, knockdown when used while crouching");
    public static final Attack gravshift = new Attack(6, 70, 32, 20, 7, AttackType.BOX)
            .setInfo("Gravity Shift", """
                    increases user jump height, changes the gravity of everything in a 64 block radius
                    Types: HYPER-GRAVITY, ATTRACT, REPULSE
                    swap between types by tapping the key during the shift""");

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

        moves = List.of(light, gutpunch, barrage, gravpunch, gravshift, launch, groundslam
                , new Attack().setMobility(MobilityType.HIGHJUMP).setInfo("Gravitational Hop", "jumps up and grants 2s slow falling"));
    }

    public static final TrackedData<Integer> SHIFTTYPE;
    public static final TrackedData<Integer> SHIFTTIME;
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
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            this.playSound(JSoundRegister.CMOON_BARRAGE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(gutpunch, JCraft.standHeavyCD, 4))
            this.playSound(JSoundRegister.CMOON_DONUT, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(gravpunch, JCraft.standS1CD, 6))
            this.playSound(JSoundRegister.CMOON_GRAVPUNCH, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (hasUser()) {
            LivingEntity user = getUser();
            if (user.isSneaking()) {
                List<BlockProjectile> blocks = world.getEntitiesByClass(BlockProjectile.class, getBoundingBox().expand(16), EntityPredicates.VALID_LIVING_ENTITY);
                for (BlockProjectile block :
                        blocks) {
                    if (block.getMaster() != user) continue;
                    block.markRefresh();
                }
            } else if (canAttack() && handleAttack(launch, JCraft.standS2CD, 9))
                playSound(JSoundRegister.CMOON_GROUNDSHOOT, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        if (handleAttack(groundslam, JCraft.standS3CD, 7))
            playSound(JSoundRegister.CMOON_GROUNDSLAM, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (getShiftTime() <= 0) {
            if (handleAttack(gravshift, JCraft.standUltCD, 10))
                playSound(JSoundRegister.CMOON_GRAVSHIFT, 1, 1);
        } else {
            int shiftType = getShiftType();
            if (shiftType++ > 2)
                shiftType = 0;
            JCraft.LOGGER.info(shiftType);
            setShiftType(shiftType);
        }
    }

    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) return;
        LivingEntity user = getUser();

        IEntityDataSaver userData = (IEntityDataSaver) user;
        if (userData.getPersistentData().getInt(JCraft.utilCD) > 0) return;

        if (user.isSneaking()) {
            user.addStatusEffect(new StatusEffectInstance(JStatusRegister.WEIGHTLESS, 30, 1));
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
        LivingEntity user = this.getUser();
        for (LivingEntity ent : entities) {
            invertDamages.add(attack == barrage ? 0.25f : 0.5f);
            invertEntities.add(ent);
            invertTimes.add(40);

            if (attack.id == gravpunch.id) {
                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.WEIGHTLESS, 80, 0));
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10, 4, true, false));
                ent.velocityModified = true;
            }
        }

        if (attack.id == launch.id) {
            BlockProjectile block = new BlockProjectile(JEntityTypeRegister.BLOCK_PROJECTILE, world);
            BlockState steppingState = getSteppingBlockState();
            if (steppingState.isAir() || !steppingState.isOpaque())
                block.setBlockStack(Items.STONE.getDefaultStack());
            else
                block.setBlockStack(steppingState.getBlock().asItem().getDefaultStack());
            block.setMaster(user);
            block.refreshPositionAndAngles(getX(), getY() + 1.5, getZ(), getYaw(), getPitch());
            block.setVelocity(0, 0.4, 0);
            world.spawnEntity(block);
        } else if (attack.id == groundslam.id) {
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
        } else if (attack.id == gravshift.id) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 350, 1));
            user.onLanding();
            setShiftTime(200);
        }
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.CMOON_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            Vec3d pos = this.getPos();
            int sTime = this.getShiftTime();

            if (world.isClient) {
                setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);

                if (sTime > 0) {
                    for (int h = 0; h < 256; ++h) {
                        Vec3d vel = Vec3d.ZERO;
                        double x = pos.x + random.nextTriangular(0, 100);
                        double y = pos.y + random.nextTriangular(0, 10);
                        double z = pos.z + random.nextTriangular(0, 100);
                        switch (getShiftType()) {
                            case (0) -> vel = new Vec3d(0.0, 64 / new Vec3d(x, y, z).squaredDistanceTo(pos), 0.0);
                            case (1) -> vel = new Vec3d(x, y, z).subtract(pos);
                            case (2) -> vel = pos.subtract(x, y, z);
                        }
                        this.world.addParticle(
                                ParticleTypes.REVERSE_PORTAL,
                                x, y, z,
                                vel.x, vel.y, vel.z);
                    }
                }
            } else {
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

                if (sTime > 0 && !user.hasStatusEffect(JStatusRegister.DAZED)) {
                    List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                            new Box(pos.add(64.0, 64.0, 64.0), pos.subtract(64.0, 64.0, 64.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    toCatch.remove(this);
                    toCatch.remove(user);

                    for (Entity entity : toCatch) {
                        if ( entity instanceof BlockProjectile block && block.getMaster() == user)
                            continue;
                        Vec3d vel = entity.getVelocity();
                        switch (getShiftType()) {
                            case (0) -> entity.addVelocity(-vel.x / 3.0, -0.1, -vel.z / 3.0);
                            case (1) -> entity.setVelocity(
                                    entity.getVelocity().add( entity.getPos().subtract(pos).normalize().multiply(0.1) )
                            );
                            case (2) -> entity.setVelocity(
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
    final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.cmoon.summon"));
            return PlayState.CONTINUE;
        }

        if (this.getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.cmoon.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.cmoon.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.cmoon.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.cmoon.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.cmoon.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.cmoon.gravpunch"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.cmoon.groundslam"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.cmoon.groundshoot"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.cmoon.gravshift"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }
        return PlayState.CONTINUE;
    }
}
