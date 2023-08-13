package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.HitBoxData;
import net.arna.jcraft.common.attack.StunType;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.projectile.RapierProjectile;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//todo: make crouching with SC increase attackDist
public class SilverChariotEntity extends StandEntity<SilverChariotEntity, SilverChariotEntity.State> {
    public static final Attack lastshot = new Attack(12, 7, 1f, 15, 12, 0, 0f, 0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Last Shot", "Chariot fires his rapier, which can bounce 5 times off walls, nerfs all hitboxes and damage by 25% until returned");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.65f, 9, 5, 1.75, 5f, 0.75f, AttackType.BOX, 0.55f, -0.1f, 0)
            .crouchingVariation(lastshot)
            .setInfo("Stab", "quick combo starter, links into Spinning Blade while armor is off");
    public static final Attack barrage = new Attack(2, 17, 0.65f, 60, 0, 2.25, 0.9f, 0.1f, AttackType.BARRAGE, 1.25f, 0, 3)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack heavy = new Attack(1, 15, 0.65f, 28, 20, 2.0, 8f, 1.5f, AttackType.BOX, 0.5f)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Impaling Thrust", "slow, uninterruptable launcher");
    //todo: spin barrage deflecting projectiles, launch stun
    public static final Attack spinbarrage = new Attack(3, 25, 0.65f, 24, 7, 2, 1f, 0.1f, AttackType.BARRAGE, 0.50f, 0, 2)
            .setInfo("Spinning Blade", "fast reliable combo starter/extender, low stun");
    public static final Attack pcharge = new Attack(4, 13, 0.65f, 25, 13, 1.75, 5f, 0.25f, AttackType.BOX, 0.75f, -0.2f, 0)
            .setRanged(true)
            .setMobility(MobilityType.DASH)
            .setBlockstun(17)
            .setInfo("Ray Dart", "Silver Chariot and the user charge forward, combo finisher");
    public static final Attack cleave = new Attack(5, 20, 0.75f, 21, 12, 2.5, 9f, 0.8f, AttackType.BOX, 1f, 0, 0)
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Cleave", "Silver Chariot detaches from the user, delivering an uninterruptable, combo-starting slice");
    public static final Attack charge = new Attack(6, 18, 8f, 19, 5, 1.5, 5f, 0.25f, AttackType.CHARGE, 0.85f, 0, State.P_CHARGE_HIT.ordinal())
            .setRanged(true)
            .disableBackstab()
            .setInfo("Shooting Star", "Silver Chariot detaches from the user and charges in the looked direction, combo starter/extender");
    public static final Attack counter = new Attack(7, 32, 0.5f, 34, 4, 0, 0, 0, AttackType.COUNTER)
            .setInfo("Counter", "0.2s windup, 1.5s duration, stuns when hit");
    public static final Attack pbeatdown = new Attack(8, 50, 0.65f, 28, 23, 1.75, 4f, 0f, AttackType.BOX, 2, 0, 0)
            .setHitspark(-4)
            .setStunType(StunType.UNBURSTABLE)
            .setInfo("God of Death", "high-damage beatdown, 1.5s stun on whiff, cannot be combo broken");
    public static final Attack mainbeatdown = new Attack(9, 0, 0.65f, 59, 0, 2.0, 4.5f, 0.75f, AttackType.MULTIHIT, 1.6f, 0, List.of(13, 23), JSoundRegistry.IMPACT_1)
            .setStunType(StunType.UNBURSTABLE)
            .setInfo("God of Death (Hit)", "");
    public static final Attack beatdownfinish = new Attack(10, 0, 0.65f, 59, 0, 2.5, 6f, 1.25f, AttackType.MULTIHIT, 1, 0, List.of(54), JSoundRegistry.TW_KICK_HIT)
            .setLaunch()
            .setHitspark(2)
            .setInfo("God of Death (Final Hit)", "");
    public static final Attack armoroff = new Attack(11, 60, 0.65f, 15, 6, 1.75, 4f, 0.75f, AttackType.BOX, 0.35f, 0f, 0)
            .setLaunch()
            .setInfo("Armor Off", "25s of faster moves");
    public static final Attack circleslash = new Attack(14, 0, 0.65f, 20, 2, 1.75, 5, 0, AttackType.BOX, 1, 0, 0, JSoundRegistry.IMPACT_1)
            .setLaunch()
            .appendHitbox( new HitBoxData(-0.65, 0, 2) )
            .setInfo("Circle Slash (Hit)", "");
    public final Attack circlecharge = new Attack(13, 17, 0.65f, 100, 101, 0, 0, 0, AttackType.BOX)
            .setFollowup(circleslash)
            .armorPoints((byte) 2)
            .setInfo("Circle Slash", "charges for a minimum of 1 second, tap again to release, 2 armor points");

    private int armorTime;

    private void setNormalDesc() {
        description = "Close Range RUSHDOWN";

        freespace =
                """
                        BNBs:
                            (Armor ON) M1>Barrage>M1>Cleave>Spinning Blade>Shooting Star>M1
                            (Armor ON) Shooting Star>M1>Barrage>Impaling Thrust
                            (Armor OFF) Shooting Star>M1>Spinning Blade>Barrage>M1>Cleave>Impaling Thrust
                            (Armor OFF) M1>Spinning Blade>Barrage>Shooting Star>Cleave>M1
                            (Armor OFF) Impaling Thrust>dash>Barrage>...
                        """;

        moves = List.of(light, heavy, barrage, spinbarrage, armoroff, charge, cleave, circlecharge);

        // A little redundant for Silver Chariot when it mode switches a few times, but the overhead is negligible
        markAllAttackButtons();
        gatherAllAttacks();
    }

    private void setPossessedDesc() {
        description = "Mid Range TRICKSTER";

        freespace =
                """
                        BNBs:
                            (M1>)Charge~Barrage>M1>Spin
                            (M1>)Charge~Barrage>God of Death""";

        // Possessed moveset
        moves = List.of(light, heavy, barrage, spinbarrage, pbeatdown, pcharge, counter, circlecharge);

        // A little redundant for Silver Chariot when it mode switches a few times, but the overhead is negligible
        markAllAttackButtons();
        gatherAllAttacks();
    }

    public SilverChariotEntity(World worldIn) {
        super(StandType.SILVER_CHARIOT, worldIn, JSoundRegistry.SC_SUMMON);
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

        super.initialize();
    }

    private static final TrackedData<Boolean> HAS_RAPIER;
    private static final TrackedData<Integer> MODE;

    static {
        MODE = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HAS_RAPIER = DataTracker.registerData(SilverChariotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public int getMode() {
        return dataTracker.get(MODE);
    }

    public void setMode(int m) {
        dataTracker.set(MODE, m);
    }

    public boolean hasRapier() {
        return dataTracker.get(HAS_RAPIER);
    }

    public void setHasRapier(boolean hasRapier) {
        dataTracker.set(HAS_RAPIER, hasRapier);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(HAS_RAPIER, true);
        dataTracker.startTracking(MODE, 1);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking())
            handleAttack(lastshot, CooldownType.STAND_LIGHT, State.LAST_SHOT);
        else if (handleAttack(light, CooldownType.STAND_LIGHT, State.STAB))
            playSound(JSoundRegistry.SC_POKE, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack()) return;
        if (handleAttack(heavy, CooldownType.STAND_HEAVY, State.HEAVY))
            playSound(JSoundRegistry.SC_HEAVY, 1, 1);
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, CooldownType.STAND_BARRAGE, State.BARRAGE))
            playSound(JSoundRegistry.SC_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(spinbarrage, CooldownType.STAND_SP1, State.SPIN))
            playSound(JSoundRegistry.SC_SPIN, 1, 1);
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (this.getMode() == 3)
            handleAttack(pbeatdown, CooldownType.STAND_ULT, State.BEAT_DOWN_START);
            //playSound(ModSoundRegister.PSC_BEATDOWN,1, 1);
        else if (handleAttack(armoroff, CooldownType.STAND_ULT, State.ARMOR_OFF))
            playSound(JSoundRegistry.SC_ARMOROFF, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack() || !hasUser()) return;
        LivingEntity user = getUserOrThrow();
        if (getMode() == 3) {
            if (handleAttack(pcharge, CooldownType.STAND_SP2, State.CHARGE)) {
                //playSound(ModSoundRegister.PSC_CHARGE,1, 1);
                if (user.isOnGround()) {
                    user.setVelocity(user.getVelocity().add(getRotationVector().multiply(0.85)).add(0.0, 0.15, 0.0));
                    user.velocityModified = true;
                }
                playSound(JSoundRegistry.SC_CHARGE, 1, 1);

            }
        } else if (handleAttack(charge, CooldownType.STAND_SP2, State.P_CHARGE)) {
            lookDirY = (float) user.getRotationVector().y;
            lookDirY *= MathHelper.abs(lookDirY);
        }
    }

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;
        if (getMode() == 3) {
            handleAttack(counter, CooldownType.STAND_SP3, State.COUNTER);
            //playSound(ModSoundRegister.PSC_CHARGE,1, 1);
        } else {
            if (handleAttack(cleave, CooldownType.STAND_SP3, State.CLEAVE)) {
                setFreePos(new Vec3f(getUserOrThrow().getPos().add(getUserOrThrow().getRotationVector().multiply(1.5))));
                setFree(true);
                playSound(JSoundRegistry.SC_CLEAVE, 1, 1);
            }
        }
    }

    private Attack chargedSlash;
    @Override
    public void initUtil() {
        if (curAttack != null && curAttack.id == circlecharge.id && getMoveStun() <= 80)
            setAttack(chargedSlash, State.CIRCLE_SLASH);
        if (!canAttack()) return;
        handleAttack(circlecharge, CooldownType.UTIL, State.CIRCLE_CHARGE);
        chargedSlash = Attack.copyOf(circleslash);
    }

    @Override
    public boolean handleAttack(Attack attack, CooldownType cooldownType, State animState) {
        if (!hasUser()) return false;

        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        int cooldown = cooldowns.getCooldown(cooldownType);

        if (cooldown > 0) return false;

        Attack attackRef = Attack.copyOf(attack);
        if (getMode() == 2) {
            attackRef.initTime = (int) (attackRef.initTime * 0.67);
            attackRef.moveStun = (int) (attackRef.moveStun * 0.67);
        }
        if (!hasRapier()) {
            attackRef.hitboxSize *= 0.75;
            attackRef.damage = (float) (attackRef.damage * 0.75);
        }
        setAttack(attackRef, animState);

        cooldowns.setCooldown(cooldownType, (int) (attack.cooldown * 20));
        return true;
    }

    private float lookDirY = 0.0F;

    @Override
    public void specialAttack(Attack attack, Set<LivingEntity> entities) {
        switch (attack.id) {
            case (8) -> {
                if (entities.isEmpty()) stun(getUser(), 30, 1);
                else setAttack(mainbeatdown, State.BEAT_DOWN);
            }
            case (9) -> {
                if (getMoveStun() == 36) curAttack = beatdownfinish;
            }
            case (11) -> {
                setMode(2);
                armorTime = 500;
            }
            case (12) -> {
                if (!hasRapier() || !hasUser()) return;

                LivingEntity user = getUserOrThrow();
                RapierProjectile rapier = new RapierProjectile(getWorld(), user, this);
                rapier.setVelocity(this, user.getPitch(), user.getYaw(), 0, 2, 1);
                rapier.setSkin(
                        getMode() != 1 ?
                                -getMode() + 1 // Modes 2 and 3 output -1 and -2
                                : getSkin()
                );
                world.spawnEntity(rapier);
                setHasRapier(false);
                //playSound();
            }
            case (14) -> {
                if (!hasUser()) return;

                Vec3d pos = getUserOrThrow().getPos();
                double launchMult = attack.damage / 5; // damage [6.5 to 11]

                for (LivingEntity living : entities) {
                    Vec3d launchVec = living.getPos().subtract(pos).normalize().multiply(launchMult);
                    living.addVelocity(launchVec.x, launchVec.y + 0.2, launchVec.z);

                    living.velocityModified = true;
                    if (living instanceof ServerPlayerEntity serverPlayer)
                        serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
                }
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);
        if (!(entity instanceof LivingEntity ent)) return;

        stun(ent, 30, 0);
        StandEntity<?, ?> stand = JUtils.getStand(ent);
        if (stand != null) stand.cancelAttack();
    }

    private static final Attack counterMiss = new Attack(8, 0, 20, 21, 0.5f, AttackType.BOX);

    @Override
    public void whiffCounter() {
        setAttack(counterMiss, State.COUNTER_MISS);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser()) return;
        LivingEntity user = getUserOrThrow();
        int mode = getMode();

        if (world.isClient) {
            if (mode == 3)
                for (int i = 0; i < 16; i++)
                    world.addParticle(
                            ParticleTypes.ASH,
                            getX() + random.nextDouble() - 0.5, getY() + random.nextDouble() * 0.25 + 0.5, getZ() + random.nextDouble() - 0.5,
                            0.0, 0.0, 0.0
                    );

            return;
        }

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

        if (curAttack != null) {
            if (curAttack.id == charge.id) {
                Vec3f chargePos = getFreePos();
                chargePos.add(0, lookDirY, 0);
                setFreePos(chargePos);
            }
            if (curAttack.id == circlecharge.id) {
                if (chargedSlash == null) chargedSlash = Attack.copyOf(circleslash); // Fallback for when the server restarts inconveniently
                if (getMoveStun() % 20 == 0)
                    chargedSlash.damage += 1.5f;
            }
        }
    }

    // Animation code
    public enum State implements StandAnimationState<SilverChariotEntity> {
        IDLE((silverChariot, builder) -> builder.loop("animation.silverchariot.idle" + switch (silverChariot.getMode()) {
            case 1 -> "";
            case 2 -> "_armorless";
            case 3 -> "_possessed";
            default -> throw new IllegalStateException("Unexpected value: " + silverChariot.getMode());
        })),
        STAB(builder -> builder.playAndHold("animation.silverchariot.stab")),
        BLOCK(builder -> builder.loop("animation.silverchariot.block")),
        HEAVY(builder -> builder.playAndHold("animation.silverchariot.heavy")),
        BARRAGE(builder -> builder.loop("animation.silverchariot.barrage")),
        SPIN(builder -> builder.loop("animation.silverchariot.spin")),
        CHARGE(builder -> builder.loop("animation.silverchariot.charge")),
        P_CHARGE(builder -> builder.loop("animation.silverchariot.pcharge")),
        P_CHARGE_HIT(builder -> builder.playAndHold("animation.silverchariot.pchargehit")),
        COUNTER(builder -> builder.loop("animation.silverchariot.counter")),
        BEAT_DOWN_START(builder -> builder.playAndHold("animation.silverchariot.beatdownstart")),
        BEAT_DOWN(builder -> builder.playAndHold("animation.silverchariot.beatdown")),
        CLEAVE(builder -> builder.playAndHold("animation.silverchariot.cleave")),
        ARMOR_OFF(builder -> builder.playAndHold("animation.silverchariot.armor_off")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.silverchariot.counter_miss")),
        LAST_SHOT(builder -> builder.playAndHold("animation.silverchariot.lastshot")),
        CIRCLE_CHARGE(builder -> builder.playAndHold("animation.silverchariot.circle_charge")),
        CIRCLE_SLASH(builder -> builder.playAndHold("animation.silverchariot.circle_slash"));

        private final BiConsumer<SilverChariotEntity, AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this((silverChariot, builder) -> animator.accept(builder));
        }

        State(BiConsumer<SilverChariotEntity, AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(SilverChariotEntity stand, AnimationBuilder builder) {
            animator.accept(stand, builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.silverchariot.summon" + (getMode() == 3 ? "_possessed" : "");
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
