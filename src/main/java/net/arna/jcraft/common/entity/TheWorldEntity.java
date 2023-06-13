package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
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

import java.util.List;

public class TheWorldEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(0, 2, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static Attack barrage = new Attack(2, 17, 0.75f, 50, 0, 2, 1f, 0.1f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static Attack donut = new Attack(1, 14, 1f, 48, 26, 2, 9f, 0.0f, AttackType.BOX, 4, 0, 0, JSoundRegister.TW_DONUT_HIT)
            .setHitspark(2)
            .setArmor(true)
            .setInfo("Donut", "slow, uninterruptable combo starter/extender, 1.5s stun on whiff");
    public static Attack charge = new Attack(4, 20, 7.5f, 19, 5, 1.5, 5f, 0.25f, AttackType.CHARGE, 1, 0, 9, JSoundRegister.TW_CHARGE_HIT)
            .setRanged(true)
            .disableBackstab()
            .setBlockstun(11)
            .setInfo("Forward Charge", "The World detaches from the user and lunges forward, combo starter");
    public static Attack roundhouse = new Attack(3, 11, 0.75f, 13, 7, 1.75, 5f, 0.3f, AttackType.BOX, 0.45f, -0.1f, 0, JSoundRegister.TW_KICK_HIT)
            .setBlockstun(12)
            .setInfo("Roundhouse", "fast poke, low stun");
    public static Attack timestop = new Attack(6, 70, 52, 45, 4, AttackType.TIMESTOP)
            .setUB(true)
            .setInfo("Timestop", "4 seconds");
    public static Attack feignbarrage = new Attack(5, 30, 0.75f, 50, 5, 0, 0f, 0f, AttackType.COUNTER)
            .setInfo("Feign Barrage", "counter, 0.25s windup, teleports behind attacker");
    public static Attack counterfollowup = new Attack(7, 0, 0.75f, 9, 5, 1.75, 6f, 0.7f, AttackType.BOX, 0.8f, 0.1f, 0, JSoundRegister.IMPACT_4)
            .appendHitbox(new Attack.HitboxData(1.25))
            .setArmor(true)
            .setLaunch();

    public TheWorldEntity(World worldIn) {
        super(StandType.THE_WORLD, worldIn);
        super.initialize();
        idleRotation = 225f;

        pros = List.of(
                "fast m1",
                "counter",
                "versatile ranged moves",
                "timestop & timeskip"
        );

        cons = List.of(
                "no knockdowns or knockbacks",
                "heavy is useless outside of combos"
        );

        description = "Mid Range DOMINATOR";

        freespace =
                """
                        BNBs:
                            the saucy racist
                            ppl without timeskips will suffer, but so will people with timeskips :)))
                            (M1>)Charge>M1>Roundhouse>Barrage>M1>Donut>Timestop{  }
                            the no ts racist
                            Donut>Roundhouse>Charge>M1>Barrage>M1""";

        moves = List.of(light, donut, barrage, roundhouse, timestop, charge, feignbarrage,
                new Attack().setMobility(MobilityType.TELEPORT).setMobility(MobilityType.TELEPORT).setInfo("Timeskip", "14m range")
        );
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) return;
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) return;
        if (handleAttack(donut, JCraft.standHeavyCD, 4))
            this.playSound(JSoundRegister.TW_DONUT, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, 5))
            this.playSound(JSoundRegister.TW_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) return;
        if (handleAttack(roundhouse, JCraft.standS1CD, 10))
            this.playSound(JSoundRegister.TW_KICK, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) return;
        if (handleAttack(timestop, JCraft.standUltCD, 7))
            this.playSound(JSoundRegister.TW_TS, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) return;
        if (handleAttack(charge, JCraft.standS2CD, 8))
            this.playSound(JSoundRegister.TW_CHARGE, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) return;
        if (handleAttack(feignbarrage, JCraft.standS3CD, 5))
            this.playSound(JSoundRegister.TW_BARRAGE, 1, 1);
    }

    @Override
    public void initMiddleClick() {
        CanAttackData data = this.canAttackWithData();
        if (!data.canAttack) return;
        if (this.getTSTime() > 0) return;
        IEntityDataSaver user = (IEntityDataSaver) data.user;
        if (user.getPersistentData().getInt(JCraft.utilCD) > 0) return;
        Vec3d eP = data.user.getEyePos();

        HitResult hitResult = this.world.raycast(new RaycastContext(eP, eP.add(data.user.getRotationVector().multiply(14)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, data.user));
        Vec3d pos = hitResult.getPos();

        data.user.teleport(pos.x, pos.y, pos.z);

        user.getPersistentData().putInt(JCraft.utilCD, 360); // 18 second timeskip cooldown

        if (user.getPersistentData().getInt(JCraft.standUltCD) < 60)
            user.getPersistentData().putInt(JCraft.standUltCD, 60); // 3 second timestop cooldown

        world.playSound(null, pos.x, pos.y, pos.z, JSoundRegister.TIME_SKIP, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (attack.id == 1) {
            LivingEntity user = this.getUser();
            // If missed, stun the user for 1.5 seconds
            if (entities.isEmpty()) {
                stun(user, 30, 0);
            } else {
                // If hit, impale and set position to middle of arm
                for (LivingEntity entity : entities) {
                    Vec3d pos = this.getPos().add(this.getRotationVector().multiply(1.5));
                    entity.teleport(pos.x, entity.getY(), pos.z);
                }
            }
        }
        if (attack.id == 7) {
            for (LivingEntity entity : entities)
                entity.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, false));
        }
    }

    @Override
    public void desummon() {
        if (this.getTSTime() < 1)
            super.desummon();
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);

        if (entity == null || !hasUser())
            return;
        LivingEntity user = this.getUser();
        Vec3d behind = entity.getPos().subtract(entity.getRotationVector());

        user.teleport(behind.x, behind.y, behind.z);
        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getEyePos());

        if (entity instanceof LivingEntity livingEntity) {
            stun(livingEntity, 20, 0);
            if (entity.getFirstPassenger() instanceof StandEntity stand) stand.cancelAttack();
        }

        setAttack(counterfollowup, 11);

        world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.TIME_SKIP, SoundCategory.PLAYERS, 1f, 1f);
        world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.TW_COUNTER, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.playSound(JSoundRegister.TW_SUMMON, 1f, 1f);
            if (JConfig.ANIME_VOICES)
                this.playSound(JSoundRegister.MUDA_DA, 1f, 1f);
        }

        super.tick();

        if (hasUser())
            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
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

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce("animation.theworld.summon"));
            return PlayState.CONTINUE;
        }

        if (this.getSameState()) controller.markNeedsReload();
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop("animation.theworld.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.theworld.light"));
            case 3 -> controller.setAnimation(builder.loop("animation.theworld.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.theworld.donut"));
            case 5 -> controller.setAnimation(builder.loop("animation.theworld.barrage"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.theworld.timestop"));
            case 8 -> controller.setAnimation(builder.loop("animation.theworld.charge"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.theworld.charge_hit"));
            case 10 -> controller.setAnimation(builder.playAndHold("animation.theworld.roundhouse"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.theworld.counter_hit"));
        }
        return PlayState.CONTINUE;
    }
}
