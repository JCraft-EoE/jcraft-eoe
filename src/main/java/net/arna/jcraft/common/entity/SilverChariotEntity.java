package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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

public class SilverChariotEntity extends StandEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public Attack light = new Attack(2, 0.65f, 9, 6, 1.75, 5f, 0.75f, AttackType.BOX, 0.5f, -0.1f, 0)
            .setInfo("Stab", "quick combo starter, links into Spinning Blade while armor is off");
    public Attack barrage = new Attack(17, 0.65f, 60, 0, 2.25, 0.9f, 0.1f, AttackType.BARRAGE, 1.25f, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public Attack heavy = new Attack(18, 0.65f, 28, 20, 2.0, 8f, 1.5f, AttackType.BOX, 0.5f).setHitspark(2).setArmor(true).setLaunch()
            .setInfo("Impaling Thrust", "slow, uninterruptable launcher");
    public Attack spinbarrage = new Attack(25, 0.65f, 24, 7, 2, 1f, 0.1f, AttackType.BARRAGE, 0.50f, 0, 2)
            .setInfo("Spinning Blade", "fast reliable combo starter/extender, low stun");
    public Attack charge = new Attack(18, 0.65f, 25, 15, 1.75, 5f, 0.25f, AttackType.BOX, 0.65f, -0.2f, 0).setRanged(true).setMobility(MobilityType.DASH)
            .setInfo("Ray Dart", "Silver Chariot and the user charge forward, combo finisher (base form), combo extender (armor off)");
    public Attack cleave = new Attack(26, 0.75f, 21, 12, 2.5, 9f, 0.8f, AttackType.BOX, 1f, 0, 0).setHitspark(2).setArmor(true)
            .setInfo("Cleave", "Silver Chariot detaches from the user, delivering an uninterruptable, combo-starting slice");
    public Attack pcharge = new Attack(22, 8f, 19, 5, 1.5, 5f, 0.25f, AttackType.CHARGE, 0.85f, 0, 9).setRanged(true)
            .setInfo("Invincible Blade", "Silver Chariot detaches from the user and charges forward, combo starter/extender");
    public Attack counter = new Attack(32, 0.5f, 44, 4, 0, 0, 0, AttackType.COUNTER)
            .setInfo("Counter", "0.2s windup, 2s duration, stuns when hit");
    public Attack pbeatdown = new Attack(60, 0.65f, 28, 23, 1.75, 4f, 0f, AttackType.BOX, 2, 0, 0).setHitspark(2)
            .setInfo("God of Death", "high-damage beatdown, 2s stun on whiff");
    public Attack realbeatdown = new Attack(0, 0.65f, 59, 0, 2.0, 3.5f, 0.75f, AttackType.MULTIHIT, 1.1f, 0, List.of(1, 6, 13, 14, 24, 36, 56), JSoundRegister.IMPACT_1);

    public static TrackedData<Integer> MODE;
    private int armorTime;

    private void setNormalDesc() {
        description = "Close Range RUSHDOWN";

        freespace =
                "BNBs:\n" +
                        "    (Armor ON) M1>Barrage>M1>Cleave>Spin\n" +
                        "    (Armor OFF) Charge>M1>Spin>Barrage>M1>Cleave>Impale\n" +
                        "    (Armor OFF) Spin>M1>Barrage>Charge>Cleave>M1\n";

        moves = List.of(light, heavy, barrage, spinbarrage, new Attack().setRanged(true).setInfo("Armor Off", "25s of faster moves"), charge, cleave, unusable);
    }

    private void setPossessedDesc() {
        description = "Mid Range TRICKSTER";

        freespace =
                "BNBs:\n" +
                        "    (M1>)Charge~Barrage>M1>Spin\n" +
                        "    (M1>)Charge~Barrage>God of Death";

        // Possessed moveset
        moves = List.of(light, heavy, barrage, spinbarrage, pbeatdown, pcharge, counter, unusable);
    }

    public SilverChariotEntity(World worldIn) {
        super(StandType.SILVER_CHARIOT, worldIn);
        super.initialize();
        idleRotation = 225f;

        pros = List.of(
                "fast m1",
                "counter",
                "two barrages",
                "excellent pokes and pressure"
        );

        cons = List.of(
                "high execution requirement",
                "individual forms have limited movesets",
                "below-average damage output"
        );

        setNormalDesc();
    }

    static {
        MODE = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getMode() {
        return this.dataTracker.get(MODE);
    }

    public void setMode(int m) {
        this.dataTracker.set(MODE, m);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(MODE, 1);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!this.canAttack()) {
            return;
        }
        handleAttack(this.light, JCraft.standLightCD, 2);
    }

    @Override
    public void initHeavyAttack() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(this.heavy, JCraft.standHeavyCD, 4)) {
            this.playSound(JSoundRegister.SC_HEAVY, 1, 1);
        }
    }

    @Override
    public void initBarrage() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(this.barrage, JCraft.standBarrageCD, 5)) {
            this.playSound(JSoundRegister.SC_BARRAGE, 1, 1);
        }
    }

    @Override
    public void initSpecial1() {
        if (!this.canAttack()) {
            return;
        }
        if (handleAttack(this.spinbarrage, JCraft.standS1CD, 6)) {
            this.playSound(JSoundRegister.SC_SPIN, 1, 1);
        }
    }

    @Override
    public void initUlt() {
        if (!this.canAttack()) {
            return;
        }
        if (this.getMode() == 3) {
            if (handleAttack(this.pbeatdown, JCraft.standUltCD, 11)) {
                //this.playSound(ModSoundRegister.PSC_BEATDOWN,1, 1);
            }
        } else if (hasUser()) {
            IEntityDataSaver user = (IEntityDataSaver) getUser();
            int cooldown = user.getPersistentData().getInt(JCraft.standUltCD);

            if (cooldown > 0) {
                return;
            }

            user.getPersistentData().putInt(JCraft.standUltCD, 1400);

            this.setMode(2);
            this.armorTime = 500;
        }
    }

    @Override
    public void initSpecial2() {
        if (!this.canAttack()) {
            return;
        }
        if (this.getMode() == 3) {
            if (handleAttack(this.pcharge, JCraft.standS2CD, 8)) {
                //this.playSound(ModSoundRegister.PSC_CHARGE,1, 1);
            }
        } else {
            if (handleAttack(this.charge, JCraft.standS2CD, 7)) {
                Entity ent = this.getUser();
                if (ent.isOnGround()) {
                    ent.setVelocity(ent.getVelocity().add(this.getRotationVector().multiply(0.75)).add(0.0, 0.15, 0.0));
                    ent.velocityModified = true;
                }
                this.playSound(JSoundRegister.SC_CHARGE, 1, 1);
            }
        }
    }

    @Override
    public void initSpecial3() {
        if (!this.canAttack()) {
            return;
        }
        if (this.getMode() == 3) {
            if (handleAttack(this.counter, JCraft.standS3CD, 10)) {
                //this.playSound(ModSoundRegister.PSC_CHARGE,1, 1);
            }
        } else {
            if (handleAttack(this.cleave, JCraft.standS3CD, 13)) {
                this.setFreePos(new Vec3f(getUser().getPos().add(getUser().getRotationVector().multiply(1.5))));
                this.setFree(true);
                //this.playSound(ModSoundRegister.SC_CLEAVE,1, 1);
            }
        }
    }

    /*
    @Override
    public void InitMiddleClick() {
        if (!this.CanAttack()) { return; }
        if (this.getVehicle() instanceof LivingEntity player) { }
    }
     */

    @Override
    public boolean handleAttack(Attack attack, String cooldownName, int animState) {
        LivingEntity user = this.getUser();
        IEntityDataSaver userData = (IEntityDataSaver) user;
        int cooldown = userData.getPersistentData().getInt(cooldownName);

        if (cooldown > 0) {
            return false;
        }

        // Can't be compacted due to == check in SpecialAttack()
        if (this.getMode() == 2) {
            Attack attackRef = Attack.copyOf(attack);

            attackRef.initTime *= 0.67;
            attackRef.moveStun *= 0.67;

            this.curAttack = attackRef;
            this.setMoveStun(attackRef.moveStun);
        } else {
            this.curAttack = attack;
            this.setMoveStun(attack.moveStun);
        }

        userData.getPersistentData().putInt(cooldownName, attack.cooldown * 20);

        this.setState(animState);
        return true;
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        if (attack == this.pbeatdown) {
            if (entities.size() < 1) {
                stun(getUser(), 90, 1);
            } else {
                this.curAttack = this.realbeatdown;
                this.setMoveStun(59);
                this.setState(12);
            }
        } else if (attack == this.realbeatdown && this.getMoveStun() < 10) {
            Vec3d rotVec = this.getRotationVector();
            for (LivingEntity ent : entities) {
                ent.removeStatusEffect(JStatusRegister.DAZED);
                ent.takeKnockback(3, -rotVec.x, -rotVec.z);
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);
        if (entity instanceof LivingEntity ent) {
            stun(ent, 30, 0);
            if (entity.getFirstPassenger() instanceof StandEntity stand) {
                stand.cancelAttack();
            }
        }
    }

    @Override
    public void tick() {
        if (age == 1) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), JSoundRegister.SC_SUMMON, SoundCategory.PLAYERS, 1f, 1f);
        }
        super.tick();

        if (hasUser()) {
            LivingEntity user = this.getUser();
            boolean hasAnubis = (user instanceof PlayerEntity playerEntity) ? playerEntity.getInventory().contains(JObjectRegistry.ANUBIS.getDefaultStack()) : user.getMainHandStack().getItem() == JObjectRegistry.ANUBIS;
            int mode = this.getMode();

            if (hasAnubis && mode != 3) {
                // Possession state
                this.setMode(3);

                for (int i = 0; i < 128; i++) {
                    this.world.addParticle(
                            ParticleTypes.ASH,
                            this.getX() + random.nextDouble() - 0.5, this.getY() + random.nextDouble() * 2, this.getZ() + random.nextDouble() - 0.5,
                            0.0, 0.1, 0.0
                    );
                }

                this.setPossessedDesc();
            } else if (!hasAnubis && mode == 3) {
                // Reset
                this.setMode(1);

                for (int i = 0; i < 128; i++) {
                    this.world.addParticle(
                            ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + random.nextDouble() - 0.5, this.getY() + random.nextDouble() * 2, this.getZ() + random.nextDouble() - 0.5,
                            0.0, 0.1, 0.0
                    );
                }

                this.setNormalDesc();
            }

            if (this.world.isClient()) {
                if (mode == 3) {
                    for (int i = 0; i < 16; i++) {
                        this.world.addParticle(
                                ParticleTypes.ASH,
                                this.getX() + random.nextDouble() - 0.5, this.getY() + random.nextDouble() * 0.25 + 0.5, this.getZ() + random.nextDouble() - 0.5,
                                0.0, 0.0, 0.0
                        );
                    }
                }
            } else {
                if (mode == 2) {
                    armorTime -= 1;
                    if (armorTime < 1) {
                        this.setMode(1);
                        this.armorTime = 0;
                    }
                }
            }

            this.setAlpha((float) MathHelper.clamp(255.0 * this.squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);
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
        String idleAnim = this.getMode() == 3 ? "animation.silverchariot.idle2" : "animation.silverchariot.idle";
        AnimationController controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();
        if (this.getSameState()) {
            controller.markNeedsReload();
        }
        switch (this.getState()) {
            default -> controller.setAnimation(builder.loop(idleAnim));
            case 2 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.stab"));
            case 3 -> controller.setAnimation(builder.loop("animation.silverchariot.block"));
            case 4 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.heavy"));
            case 5 -> controller.setAnimation(builder.loop("animation.silverchariot.barrage"));
            case 6 -> controller.setAnimation(builder.loop("animation.silverchariot.spin"));
            case 7 -> controller.setAnimation(builder.loop("animation.silverchariot.charge"));
            case 8 -> controller.setAnimation(builder.loop("animation.silverchariot.pcharge"));
            case 9 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.pchargehit"));
            case 10 -> controller.setAnimation(builder.loop("animation.silverchariot.counter"));
            case 11 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.beatdownstart"));
            case 12 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.beatdown"));
            case 13 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.cleave"));
        }
        controller.setAnimationSpeed(this.getMode() == 2 ? 1.5 : 1);
        return PlayState.CONTINUE;
    }
}
