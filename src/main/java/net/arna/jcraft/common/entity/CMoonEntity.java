package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
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
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
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

public class CMoonEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(0, 2, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");

    public static Attack barrage = new Attack(2, 17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 1, 0, 4, JSoundRegister.IMPACT_3)
            .setInfo("Barrage", "fast reliable combo starter/extender/finisher, medium stun");
    public static Attack gutpunch = new Attack(1, 17, 1f, 30, 19, 2.0, 10f, 1.5f, AttackType.BOX, 0.5f, 0, 0, JSoundRegister.TW_KICK_HIT).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Gut Punch", "slow, uninterruptable combo finisher");
    public static Attack gun = new Attack(4, 20, 21, 15, 1, 0.75f, AttackType.BOX).setRanged(true)
            .setInfo("Gun", "fully aimable, combo starter");
    public static Attack gravpunch = new Attack(3, 24, 1f, 32, 20, 1.75, 8f, 0.35f, AttackType.BOX, 1.75f, -0.3f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT).setHitspark(2).setArmor(true)
            .setUB(true)
            .setInfo("Only One Punch", "lifts enemy on hit");
    public static Attack groundslam = new Attack(5, 28, 1f, 18, 10, 3, 7f, 0.2f, AttackType.BOX, 0.85f, 1.4f, 0, JSoundRegister.CMOON_GRAVPUNCHHIT)
            .setUB(true)
            .setInfo("Ground Slam", "lifts the ground, combo starter/extender, knockdown when used while crouching");
    public static Attack gravshift = new Attack(6, 70, 32, 20, 7, AttackType.BOX)
            .setInfo("Gravity Shift", "increases user jump height, applies hypergravity to everything in a 64 block radius");


    public static TrackedData<Integer> SHIFTTIME;

    public ArrayList<Float> invertDamages = new ArrayList<>();
    public ArrayList<LivingEntity> invertEntities = new ArrayList<>();
    public ArrayList<Integer> invertTimes = new ArrayList<>();

    /*
    C-Moon

    Pros:
    >damaging aftereffect
    >useful jump with glide
    >multipurpose ultimate
    >fast m1
    Cons:
    >only one punch is unconfirmable
    >gun is fully blockable
    >ground slam is jumpable

    LIGHT: M1 TANDEM
    BARRAGE: Basic Barrage
    HEAVY: Gut Punch, medium windup, combo ender
    SPECIAL 1: Only One Punch, unconfirmable, lifts enemy on hit
    SPECIAL 2: Glock, combo starter
    SPECIAL 3: Ground Slam, lifts the ground, combo starter/extender, knockdown when used while crouching

    ULTIMATE: Gravity Shift, increases user jump height, applies hypergravity to everything in a 64 block radius

    MIDDLE CLICK:


     */

    public CMoonEntity(World worldIn) {
        super(StandType.C_MOON, worldIn);
        super.initialize();
        idleRotation = 220f;

        pros = List.of(
                "fast m1",
                "useful jump with glide",
                "multipurpose ultimate",
                "damaging aftereffect"
        );

        cons = List.of(
                "only one punch is unconfirmable",
                "gun is fully blockable"
        );

        freespace = """
                Passive: Inversion, all physical hits deal an extra half heart after 2s

                    BNBs:
                    (Only One Punch>)Gun>Ground Slam>M1>Barrage>Gut Punch
                    Ground Slam>M1>Barrage>Gun>Gravity Shift""";

        moves = List.of(light, gutpunch, barrage, gravpunch, gravshift, gun, groundslam
                , new Attack().setMobility(MobilityType.HIGHJUMP).setInfo("Gravitational Hop", "jumps up and grants 2s slow falling"));
    }

    static {
        SHIFTTIME = DataTracker.registerData(CMoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getShiftTime() {
        return this.dataTracker.get(SHIFTTIME);
    }

    public void setShiftTime(int sTime) {
        this.dataTracker.set(SHIFTTIME, sTime);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(SHIFTTIME, 0);
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
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.CMOON_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(gutpunch, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.CMOON_DONUT, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(gravpunch, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.CMOON_GRAVPUNCH, 1, 1);
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) return;
        handleAttack(gun, JCraft.standS2CD, 9);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (handleAttack(gravshift, JCraft.standUltCD, 10)) {
            this.playSound(JSoundRegister.CMOON_GRAVSHIFT, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        if (handleAttack(groundslam, JCraft.standS3CD, 7)) {
            this.playSound(JSoundRegister.CMOON_GROUNDSLAM, 1, 1);
        }
    }


    @Override
    public void initMiddleClick() {
        if (!this.canAttack()) return;
        if (hasUser()) {
            LivingEntity user = this.getUser();
            IEntityDataSaver userData = (IEntityDataSaver) user;
            if (userData.getPersistentData().getInt(JCraft.standMMBCD) > 0) {
                return;
            }

            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 1));
            user.addVelocity(0, 1.0, 0);
            user.velocityModified = true;

            userData.getPersistentData().putInt(JCraft.standMMBCD, 340);
        }
    }

    @Override
    public void standBlock() {
        LivingEntity user = getUser();
        if (user == null) return;
        // Projectile deflection
        List<ProjectileEntity> toDeflect = this.world.getEntitiesByClass(ProjectileEntity.class, this.getBoundingBox().expand(0.75f), EntityPredicates.VALID_ENTITY);

        for (ProjectileEntity projectile : toDeflect) {
            if (projectile.getOwner() == user) continue;
            projectile.setVelocity(projectile.getPos().subtract(this.getPos()).normalize());
            projectile.velocityModified = true;
        }

        stun(user, 2, 2);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 2, false, false));
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = this.getUser();
        for (LivingEntity ent : entities) {
            invertDamages.add(attack == barrage ? 0.25f : 2f);
            invertEntities.add(ent);
            invertTimes.add(40);

            if (attack == gravpunch) {
                ent.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 80, 2));
                ent.addVelocity(0.0, 0.6, 0.0);
                ent.velocityModified = true;
            }
        }

        if (attack.id == gun.id) {
            this.playSound(JSoundRegister.WS_GUN, 1, 1);

            Box box = this
                    .getBoundingBox()
                    .stretch(user.getRotationVec(1.0F).multiply(1024))
                    .expand(1.0D);

            EntityHitResult hitResult = ProjectileUtil.raycast(
                    this,
                    this.getEyePos(),
                    this.getEyePos().add(user.getRotationVector().multiply(1024)),
                    box,
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                    1024
            );

            if (hitResult != null) {
                Entity entity = hitResult.getEntity();
                if (entity instanceof LivingEntity livingEntity)
                    damageLogic(world, livingEntity, Vec3d.ZERO, (int) attack.stun * 20, 1, false, 6, false, 4, DamageSource.mob(user), user);
            }
        } else if (attack.id == groundslam.id) {
            for (LivingEntity ent : entities) {
                ent.setVelocity(new Vec3d(0.0, -0.5, 0.0));
                ent.velocityModified = true;
                if (user.isSneaking()) ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 30, 0));
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
                        }
                    }
                }
            }
        } else if (attack.id == gravshift.id) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 350, 1));
            user.onLanding();
            this.setShiftTime(350);
        }
    }

    @Override
    public void tick() {
        if (age == 1) this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.CMOON_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            Vec3d pos = this.getPos();

            if (!this.world.isClient()) {
                for (int i = 0; i < invertTimes.size(); i++) {
                    int time = invertTimes.get(i);
                    invertTimes.set(i, time - 1);
                    if (time < 1) {
                        LivingEntity entity = invertEntities.get(i);
                        damage(invertDamages.get(i), DamageSource.mob(user), entity);
                        invertTimes.remove(i);
                        invertEntities.remove(i);
                        invertDamages.remove(i);
                    }
                }

                if (this.curAttack != gun) {
                    this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
                } else {
                    this.setAlpha(0f);
                }

                int sTime = this.getShiftTime();

                if (sTime > 0) {
                    List<Entity> toCatch = world.getEntitiesByClass(Entity.class,
                            new Box(pos.add(64.0, 64.0, 64.0), pos.subtract(64.0, 64.0, 64.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                    toCatch.remove(this);
                    toCatch.remove(user);

                    for (Entity entity : toCatch) {
                        Vec3d vel = entity.getVelocity();
                        entity.addVelocity(-vel.x / 3.0, -0.1, -vel.z / 3.0);
                        entity.velocityModified = true;
                    }

                    this.setShiftTime(sTime - 1);
                }
            } else {
                int sTime = this.getShiftTime();

                if (sTime > 0) {
                    for (int h = 0; h < 256; ++h) {
                        double x = pos.x + random.nextTriangular(0, 100);
                        double y = pos.y + random.nextTriangular(0, 10);
                        double z = pos.z + random.nextTriangular(0, 100);
                        this.world.addParticle(
                                ParticleTypes.REVERSE_PORTAL,
                                x,
                                y,
                                z,
                                0.0, 64 / new Vec3d(x, y, z).squaredDistanceTo(pos), 0.0);
                    }
                }
            }
        }
    }

    // Animation code
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
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
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.cmoon.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.cmoon.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.cmoon.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.cmoon.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.cmoon.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.cmoon.gravpunch"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.cmoon.groundslam"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.cmoon.gun"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.cmoon.gravshift"));

            //default -> throw new IllegalStateException("Unexpected value: " + this.getState());
        }
        return PlayState.CONTINUE;
    }
}
