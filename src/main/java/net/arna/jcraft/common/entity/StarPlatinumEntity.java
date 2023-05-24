package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.client.network.s2c.ShaderDeactivationPacket;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegister;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

public class StarPlatinumEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public static Attack light = new Attack(2, 0.75f, 7, 5, 1.5, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0, JSoundRegister.IMPACT_1)
            .setInfo("Punch", "quick combo starter");
    public static Attack heavy = new Attack(17, 1f, 30, 20, 2.0, 10f, 1.5f, AttackType.BOX, 0.5f).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Star Breaker", "uninterruptable launcher");
    public static Attack barrage = new Attack(17, 0.75f, 60, 0, 2, 1f, 0.25f, AttackType.BARRAGE, 2, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static Attack starfinger = new Attack(20, 1.25f, 20, 12, 1.75, 6f, -0.25f, AttackType.BOX, 1.5f, -0.25f).setHitspark(2)
            .setInfo("Star Finger", "medium windup, combo starter/extender");
    public static Attack timestop = new Attack(60, 40, 39, 3, AttackType.TIMESTOP) // TS = (moveStun-initTime)/20
            .setInfo("Timestop", "3 seconds");
    public static Attack lowkick = new Attack(12, 0.75f, 12, 7, 1.5, 7f, 0.25f, AttackType.BOX, 0.4f, 0)
            .setInfo("Roundhouse", "fast poke, low stun");
    public static Attack chargebarrage = new Attack(26, 4f, 55, 5, 1.5, 0.6f, 0.4f, AttackType.CHARGEBARRAGE, 1, 0, 3).setRanged(true)
            .setInfo("Advancing Barrage", "fast combo starter/extender, medium stun, extremely punishable on whiff");

    public StarPlatinumEntity(EntityType<? extends StandEntity> type, World worldIn) {
        super(type, worldIn);
        super.initialize();
        idleRotation = 225f;

        description = "High Speed RUSHDOWN";

        pros = List.of(
                "fast m1",
                "long, damaging combos",
                "low cooldowns",
                "timestop & timeskip"
        );

        cons = List.of(
                "predictable playstyle",
                "weak ranged coverage"
        );

        freespace =
                "BNBs:\n" +
                        "    advancing barrage is only confirmed if the opponent is lifted, so the M1 between regular and advancing barrage may be removed\n" +
                        "    -the classic\n" +
                        "    M1>Low Kick>Barrage>M1>Advancing Barrage>(queue)Star Finger>(queue)M1>(queue)Star Breaker\n" +
                        "    works as a timestop setup that's beaten by mobility options\n" +
                        "    ...>(queue)Timestop{ Timeskip>[Spam weapon crits]M1>M1 }>M1>Low kick\n" +
                        "\n" +
                        "    -the poke\n" +
                        "    Star Finger>Low Kick>Barrage>M1>Advancing Barrage>(queue)M1>(queue)Star Breaker";

        moves = List.of(light, heavy, barrage, starfinger, timestop, lowkick, starfinger,
                new Attack().setMobility(MobilityType.TELEPORT).setInfo("Timeskip", "14m range")
        );
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) {
            return;
        }
        handleAttack(light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(heavy, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.STAR_BREAKER, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.STAR_PLATINUM_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(starfinger, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.STAR_FINGER, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(timestop, JCraft.standUltCD, 7)) {
            this.playSound(JSoundRegister.STAR_PLATINUM_THE_WORLD, 1, 1);
            PlayerLookup.tracking(this).forEach(tracked -> ShaderActivationPacket.send(tracked, this, 20, (int) timestop.stun * 20, ShaderActivationPacket.Type.ZA_WARDO));
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(lowkick, JCraft.standS2CD, 8)) {
            this.playSound(JSoundRegister.STAR_PLATINUM_KICK, 1, 1);
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(chargebarrage, JCraft.standS3CD, 5)) {
            this.playSound(JSoundRegister.STAR_PLATINUM_ADVANCING_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initMiddleClick() {
        CanAttackData data = this.canAttackWithData();
        if (!data.canAttack) {
            return;
        }
        if (this.getTSTime() > 0) {
            return;
        }
        IEntityDataSaver user = (IEntityDataSaver) data.user;
        if (user.getPersistentData().getInt(JCraft.standMMBCD) > 0) {
            return;
        }
        Vec3d eP = data.user.getEyePos();

        HitResult hitResult = this.world.raycast(new RaycastContext(eP, eP.add(data.user.getRotationVector().multiply(14)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, data.user));
        Vec3d pos = hitResult.getPos();

        data.user.teleport(pos.x, pos.y, pos.z);

        user.getPersistentData().putInt(JCraft.standMMBCD, 360); // 18 second timeskip cooldown

        if (user.getPersistentData().getInt(JCraft.standUltCD) < 60)
            user.getPersistentData().putInt(JCraft.standUltCD, 60); // 3 second timestop cooldown

        this.world.playSound(null, pos.x, pos.y, pos.z, JSoundRegister.TIME_SKIP, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    public void desummon() {
        if (this.getTSTime() < 1) {
            super.desummon();
        }
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (attack == chargebarrage && entities.size() > 0) { // Lock-on
            Vec3d avgPos = Vec3d.ZERO;
            float c = 0;
            for (LivingEntity ent : entities) {
                if (ent instanceof StandEntity)
                    continue;
                avgPos = avgPos.add(ent.getPos());
                c += 1f;
            }
            avgPos = avgPos.multiply(1f / c);
            this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, avgPos);
        }
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.STAND_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }

        super.tick();

        if (hasUser()) {
            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
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

    @Override
    public boolean canAttack() {
        PlayerLookup.tracking(this).forEach(tracked -> ShaderDeactivationPacket.send(tracked, ShaderActivationPacket.Type.ZA_WARDO));
        return super.canAttack();
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (age < 15 && getState() < 2) {
            controller.setAnimation(builder.playOnce("animation.starplatinum.summon"));
            return PlayState.CONTINUE;
        }

        if (getSameState()) {
            controller.markNeedsReload();
        }
        switch (getState()) {
            default -> controller.setAnimation(builder.loop("animation.starplatinum.idle"));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.punch"));
            case 3 -> controller.setAnimation(builder.loop("animation.starplatinum.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.starplatinum.barrage"));
            case 6 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.star_finger"));
            case 7 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.timestop"));
            case 8 -> controller.setAnimation(builder.playAndHold("animation.starplatinum.low_kick"));
        }

        return PlayState.CONTINUE;
    }
}
