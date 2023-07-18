package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

import static net.arna.jcraft.common.util.Attack.unusable;

public class SilverChariotEntity extends StandEntity {
    public final Attack light = new Attack(0, 2, 0.65f, 9, 5, 1.75, 5f, 0.75f, AttackType.BOX, 0.55f, -0.1f, 0)
            .setInfo("Stab", "quick combo starter, links into Spinning Blade while armor is off");
    public final Attack barrage = new Attack(2, 17, 0.65f, 60, 0, 2.25, 0.9f, 0.1f, AttackType.BARRAGE, 1.25f, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public final Attack heavy = new Attack(1, 15, 0.65f, 28, 20, 2.0, 8f, 1.5f, AttackType.BOX, 0.5f)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Impaling Thrust", "slow, uninterruptable launcher");
    public final Attack spinbarrage = new Attack(3, 25, 0.65f, 24, 7, 2, 1f, 0.1f, AttackType.BARRAGE, 0.50f, 0, 2)
            .setInfo("Spinning Blade", "fast reliable combo starter/extender, low stun");
    public final Attack pcharge = new Attack(4, 18, 0.65f, 25, 13, 1.75, 5f, 0.25f, AttackType.BOX, 0.75f, -0.2f, 0)
            .setRanged(true)
            .setMobility(MobilityType.DASH)
            .setBlockstun(17)
            .setInfo("Ray Dart", "Silver Chariot and the user charge forward, combo finisher");
    public final Attack cleave = new Attack(5, 23, 0.75f, 21, 12, 2.5, 9f, 0.8f, AttackType.BOX, 1f, 0, 0)
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Cleave", "Silver Chariot detaches from the user, delivering an uninterruptable, combo-starting slice");
    public final Attack charge = new Attack(6, 22, 8f, 19, 5, 1.5, 5f, 0.25f, AttackType.CHARGE, 0.85f, 0, 9)
            .setRanged(true)
            .disableBackstab()
            .setInfo("Shooting Star", "Silver Chariot detaches from the user and charges in the looked direction, combo starter/extender");
    public final Attack counter = new Attack(7, 32, 0.5f, 34, 4, 0, 0, 0, AttackType.COUNTER)
            .setInfo("Counter", "0.2s windup, 1.5s duration, stuns when hit");
    public final Attack pbeatdown = new Attack(8, 50, 0.65f, 28, 23, 1.75, 4f, 0f, AttackType.BOX, 2, 0, 0)
            .setHitspark(-4)
            .setStunType(0)
            .setInfo("God of Death", "high-damage beatdown, 1.5s stun on whiff, cannot be combo broken");
    public final Attack mainbeatdown = new Attack(9, 0, 0.65f, 59, 0, 2.0, 4.5f, 0.75f, AttackType.MULTIHIT, 1.6f, 0, List.of(13, 23), JSoundRegister.IMPACT_1)
            .setStunType(0);
    public final Attack beatdownfinish = new Attack(10, 0, 0.65f, 59, 0, 2.5, 6f, 1.25f, AttackType.MULTIHIT, 1, 0, List.of(54), JSoundRegister.TW_KICK_HIT)
            .setLaunch()
            .setHitspark(2);
    public final Attack armoroff = new Attack(11, 60, 0.65f, 15, 6, 1.75, 4f, 0.75f, AttackType.BOX, 0.35f, 0f, 0)
            .setLaunch()
            .setInfo("Armor Off", "25s of faster moves");
    private int armorTime;

    private void setNormalDesc() {
        description = "Close Range RUSHDOWN";

        freespace =
                """
                        BNBs:
                            (Armor ON) M1>Barrage>M1>Cleave>Spin
                            (Armor OFF) Charge>M1>Spin>Barrage>M1>Cleave>Impale
                            (Armor OFF) Spin>M1>Barrage>Charge>Cleave>M1
                        """;

        moves = List.of(light, heavy, barrage, spinbarrage, armoroff, charge, cleave, unusable);
    }

    private void setPossessedDesc() {
        description = "Mid Range TRICKSTER";

        freespace =
                """
                        BNBs:
                            (M1>)Charge~Barrage>M1>Spin
                            (M1>)Charge~Barrage>God of Death""";

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
                "below-average damage output",
                "lacking in mobility"
        );

        setNormalDesc();
    }

    public static final TrackedData<Integer> MODE;

    static {
        MODE = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getMode() {
        return dataTracker.get(MODE);
    }

    public void setMode(int m) {
        dataTracker.set(MODE, m);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MODE, 1);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (handleAttack(this.light, JCraft.standLightCD, 2))
            playSound(JSoundRegister.SC_POKE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(this.heavy, JCraft.standHeavyCD, 4))
            playSound(JSoundRegister.SC_HEAVY, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(this.barrage, JCraft.standBarrageCD, 5))
            playSound(JSoundRegister.SC_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(this.spinbarrage, JCraft.standS1CD, 6))
            playSound(JSoundRegister.SC_SPIN, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (this.getMode() == 3)
            handleAttack(pbeatdown, JCraft.standUltCD, 11);
            //playSound(ModSoundRegister.PSC_BEATDOWN,1, 1);
        else if (handleAttack(armoroff, JCraft.standUltCD, 14))
            playSound(JSoundRegister.SC_ARMOROFF, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack()) return;
        Entity ent = getUser();
        if (getMode() == 3) {
            if (handleAttack(this.pcharge, JCraft.standS2CD, 7)) {
                //playSound(ModSoundRegister.PSC_CHARGE,1, 1);
                if (ent.isOnGround()) {
                    ent.setVelocity(ent.getVelocity().add(getRotationVector().multiply(0.85)).add(0.0, 0.15, 0.0));
                    ent.velocityModified = true;
                }
                playSound(JSoundRegister.SC_CHARGE, 1, 1);

            }
        } else {
            if (handleAttack(this.charge, JCraft.standS2CD, 8)) {
                lookDirY = (float) ent.getRotationVector().y;
                lookDirY *= MathHelper.abs(lookDirY);
            }
        }
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        if (this.getMode() == 3) {
            handleAttack(this.counter, JCraft.standS3CD, 10);
            //playSound(ModSoundRegister.PSC_CHARGE,1, 1);
        } else {
            if (handleAttack(this.cleave, JCraft.standS3CD, 13)) {
                this.setFreePos(new Vec3f(getUser().getPos().add(getUser().getRotationVector().multiply(1.5))));
                this.setFree(true);
                playSound(JSoundRegister.SC_CLEAVE, 1, 1);
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

        if (cooldown > 0) return false;

        // Can't be compacted due to == check in SpecialAttack()
        if (getMode() == 2) {
            Attack attackRef = Attack.copyOf(attack);

            attackRef.initTime *= 0.67;
            attackRef.moveStun *= 0.67;

            curAttack = attackRef;
            setMoveStun(attackRef.moveStun);
        } else {
            curAttack = attack;
            setMoveStun(attack.moveStun);
        }

        userData.getPersistentData().putInt(cooldownName, attack.cooldown * 20);

        setState(animState);
        return true;
    }

    private float lookDirY = 0.0F;
    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        switch (attack.id) {
            case (8) -> {
                if (entities.isEmpty())
                    stun(getUser(), 30, 1);
                else
                    setAttack(mainbeatdown, 12);
            }
            case (9) -> {
                if (getMoveStun() == 36)
                    curAttack = beatdownfinish;
            }
            case (11) -> {
                setMode(2);
                armorTime = 500;
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);
        if (entity instanceof LivingEntity ent) {
            stun(ent, 30, 0);
            StandEntity stand = ((IEntityDataSaver)ent).getStand();
            if (stand != null) stand.cancelAttack();
        }
    }

    private static final Attack counterMiss = new Attack(8, 0, 20, 21, 0.5f, AttackType.BOX);
    @Override
    public void whiffCounter() {
        setAttack(counterMiss, 15);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void tick() {
        if (age == 1) playSound(JSoundRegister.SC_SUMMON, 1f, 1f);
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUser();
            int mode = getMode();

            if (world.isClient) {
                setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(user) / 2, 0.0, 255.0) / 255f);

                if (mode == 3)
                    for (int i = 0; i < 16; i++)
                        world.addParticle(
                                ParticleTypes.ASH,
                                getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 0.25 + 0.5, getZ() + random.nextDouble() - 0.5,
                                0.0, 0.0, 0.0
                        );
            } else {
                boolean hasAnubis = (user instanceof PlayerEntity playerEntity) ? playerEntity.getInventory().contains(JObjectRegistry.ANUBIS.getDefaultStack()) : user.getMainHandStack().getItem() == JObjectRegistry.ANUBIS;

                if (hasAnubis && mode != 3) {
                    for (int i = 0; i < 128; i++)
                        world.addParticle(
                                ParticleTypes.ASH,
                                getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 2, getZ() + random.nextDouble() - 0.5,
                                0.0, 0.1, 0.0
                        );

                    // Possession state
                    setMode(3);
                    setPossessedDesc();
                } else if (!hasAnubis && mode == 3) {
                    for (int i = 0; i < 128; i++)
                        world.addParticle(
                                ParticleTypes.ELECTRIC_SPARK,
                                getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 2, getZ() + random.nextDouble() - 0.5,
                                0.0, 0.1, 0.0
                        );

                    // Reset
                    setMode(1);
                    setNormalDesc();
                }

                if (mode == 2 && armorTime-- < 1) setMode(1);

                if (curAttack != null && curAttack.id == charge.id) {
                    Vec3f chargePos = getFreePos();
                    chargePos.add(0, lookDirY, 0);
                    setFreePos(chargePos);
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

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        String idleAnim = "animation.silverchariot.idle";
        if (getMode() == 2)
            idleAnim = "animation.silverchariot.idle_armorless";
        if (getMode() == 3)
            idleAnim = "animation.silverchariot.idle_possessed";

        AnimationController<E> controller = event.getController();
        AnimationBuilder builder = new AnimationBuilder();

        if (playSummonAnim) {
            controller.setAnimation(builder.playOnce(getMode() == 3 ? "animation.silverchariot.summon_possessed" : "animation.silverchariot.summon"));
            return PlayState.CONTINUE;
        }

        if (getSameState()) controller.markNeedsReload();
        switch (getState()) {
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
            case 14 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.armor_off"));
            case 15 -> controller.setAnimation(builder.playAndHold("animation.silverchariot.counter_miss"));
        }
        controller.setAnimationSpeed(this.getMode() == 2 ? 1.5 : 1);
        return PlayState.CONTINUE;
    }
}
