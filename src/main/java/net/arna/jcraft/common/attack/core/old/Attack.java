package net.arna.jcraft.common.attack.core.old;

import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.VariationType;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Attack {
    public int id = -1; // Unique ID
    public double cooldown = 0; // Ability cooldown
    public float attackDist = 1f; // Distance your stand is at while attacking
    public int moveStun = 0; // Duration you can't use another move
    public int initTime = 0; // Time during movestun the move initiates
    public double hitboxSize = 0; // Hitbox size in meters cubed
    public float damage = 0; // Damage in half hearts
    public float knockback = 0.1f; // Knockback in meters/tick, 20m/s
    public AttackType attackType; // Attack type
    public float stun = 0f; // How long is the opponent stunned for after being hit
    public float offset = 0; // Hitbox Y offset (inverted)
    public SoundEvent impactSound;
    public final List<Integer> attackTimes = new ArrayList<>();

    public byte interval = 1; // For barrages; attack interval | For charges; hit state ID

    public static final Attack unusable = new Attack(-1, 32767, 32767, 32767, 0, AttackType.BOX).setInfo("NONE", "NONE");

    public byte armor = 0; // For (un)interruptable attacks
    public boolean lift = true; // If set to true, attack will attempt to keep the victim in air on hit
    public StunType stunType = StunType.BURSTABLE; // 1 - HITSTUN, 2 - BLOCKSTUN, 3 - NO MOVEMENT PENALTY
    public boolean overrideStun = false; // If set to true, attack will override current stun on victim
    public boolean unblockable = false;
    public boolean ubEffectsOnly = false;
    public boolean canBackstab = true;

    // Info
    public MoveQueue button;
    public String name = "UNNAMED";
    public String description = "";

    // Spec-exclusive
    public String animation;

    // AI Flags, mostly
    public boolean isRanged = false;

    public List<HitBoxData> extraHitboxes = new ArrayList<>();
    public MobilityType mobilityType = null;
    public boolean overrideBlockstun = false;
    public int blockstun;
    public Attack followup;
    public Map<VariationType, Attack> variations = new LinkedHashMap<>();
    public boolean isFollowup, isCrouchingVariation, isAerialVariation = false;

    // For non-physicals
    public Attack() {
    }

    public Attack(int id, double cooldown, int moveStun, int initTime) {
        this.id = id;
        this.cooldown = cooldown;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.attackType = AttackType.BOX;
    }

    public Attack(int id, double cooldown, int moveStun, int initTime, float stun, AttackType attackType) {
        this.id = id;
        this.cooldown = cooldown;
        this.moveStun = moveStun; // Anim duration
        this.initTime = initTime; // TS init
        this.stun = stun; // TS duration
        this.attackType = attackType;
    }

    public Attack(int id, double cooldown, int moveStun, int initTime, float stun, float attackDist, AttackType attackType) {
        this.id = id;
        this.cooldown = cooldown;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.stun = stun;
        this.attackDist = attackDist;
        this.attackType = attackType;
    }

    // For knockback moves
    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
    }

    // Stun moves (simplified)
    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist; // CHARGE: max. attack dist
        this.moveStun = moveStun; // CHARGE: After initTime has passed, the remaining moveStun is how many ticks it takes for the stand to finish its charge
        this.initTime = initTime;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
        this.stun = stun;
    }

    // For stun moves
    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset) {
        this.id = id;
        this.cooldown = cooldown; //
        this.attackDist = attackDist; // CHARGE: max. attack dist
        this.moveStun = moveStun; // CHARGE: After initTime has passed, the remaining moveStun is how many ticks it takes for the stand to finish its charge
        this.initTime = initTime; //
        this.hitboxSize = hitboxSize; //
        this.damage = damage; //
        this.knockback = knockback; //
        this.attackType = attackType; //
        this.stun = stun; //
        this.offset = offset; //
    }

    // For barrages
    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, int intervalOrHitAnim) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
        this.stun = stun;
        this.offset = offset;
        this.interval = (byte) intervalOrHitAnim;
    }

    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, int interval, SoundEvent impactSound) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
        this.stun = stun;
        this.offset = offset;
        this.interval = (byte) interval;
        this.impactSound = impactSound;
    }

    public static Attack barrageAttack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, float stun, float offset, int interval) {
        return barrageAttack(id, cooldown, attackDist, moveStun, initTime, hitboxSize, damage, knockback, stun, offset, interval, null);
    }
    public static Attack barrageAttack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, float stun, float offset, int interval, @Nullable SoundEvent impactSound) {
        return new Attack(id, cooldown, attackDist, moveStun, initTime, hitboxSize, damage, knockback, AttackType.BARRAGE, stun, offset, interval, impactSound);
    }

    // For multi-hits that aren't barrages
    public Attack(int id, double cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, List<Integer> attackTimes, SoundEvent impactSound) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist;
        this.moveStun = moveStun;
        this.initTime = attackTimes.isEmpty() ? initTime : attackTimes.get(0);
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
        this.stun = stun;
        this.offset = offset;
        this.attackTimes.addAll(attackTimes);
        this.impactSound = impactSound;
    }

    public Attack setDist(float attackDist) {
        this.attackDist = attackDist;
        return this;
    }

    /**
     * Marks an attack as unstoppable
     */
    public Attack hyperArmor() {
        this.armor = Byte.MAX_VALUE;
        return this;
    }

    /**
     * Gives an attack an amount of hits it can withstand before being stopped (max 127)
     */
    public Attack armorPoints(byte armor) {
        this.armor = armor;
        return this;
    }

    /**
     * Assigns whether the attack will attempt to lift the victim on hit
     */
    public Attack setLift(boolean lift) {
        this.lift = lift;
        return this;
    }

    public int hitspark = 1; // Particle that appears on hit

    /**
     * Assigns the particle the attack will generate on hit
     *
     * @param hitspark id of particle (see: net.arna.jcraft.client.net.ClientPacketHandler)
     */
    public Attack setHitspark(int hitspark) {
        this.hitspark = hitspark;
        return this;
    }

    public Attack setStunType(StunType stunType) {
        this.stunType = stunType;
        return this;
    }

    public Attack setStunOverride(boolean o) {
        this.overrideStun = o;
        return this;
    }

    public String stunTypeName() {
        switch (this.stunType) {
            default -> {
                return "Unknown";
            }
            case UNBURSTABLE -> {
                return "Unburstable";
            }
            case BURSTABLE -> {
                return "Burstable";
            }
            case BLOCK -> {
                return "Block";
            }
            case LAUNCH -> {
                return "Launch";
            }
        }
    }

    public Attack setGrab() {
        return this.setStunType(StunType.UNBURSTABLE).setStunOverride(true).setUB(false);
    }

    /**
     * Marks attack as a launcher
     */
    public Attack setLaunch() { // Shorthand
        this.stunType = StunType.LAUNCH;
        this.overrideStun = true;
        return this;
    }

    /**
     * Marks the attack as unblockable
     *
     * @param effectsOnly is the effect of the attack the only unblockable feature?
     */
    public Attack setUB(boolean effectsOnly) {
        this.unblockable = true;
        this.ubEffectsOnly = effectsOnly;
        return this;
    }

    /**
     * @return attack with backstabs disabled
     */
    public Attack disableBackstab() {
        this.canBackstab = false;
        return this;
    }

    /**
     * Assigns information to attack
     *
     * @param name name of attack
     * @param desc description
     */
    public Attack setInfo(String name, String desc) {
        this.name = name;
        this.description = desc;
        return this;
    }

    /**
     * Assigns information to attack
     *
     * @param name name of attack
     * @param desc description
     * @param b    which type of button the attack corresponds to
     */
    public Attack setInfo(String name, String desc, MoveQueue b) {
        this.name = name;
        this.description = desc;
        this.button = b;
        return this;
    }

    /**
     * Assigns {@link net.minecraft.entity.player.PlayerEntity} animation id to attack, used for spec moves
     *
     * @param anim string id for animation
     */
    public Attack setAnimation(String anim) {
        this.animation = anim;
        return this;
    }

    /**
     * Marks attack as ranged or not
     */
    public Attack setRanged(boolean ranged) {
        this.isRanged = ranged;
        return this;
    }

    public Attack setDamage(float damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Adds new hitboxes to the attack
     *
     * @param list hitboxes to add
     */
    public Attack appendHitboxes(Collection<? extends HitBoxData> list) {
        extraHitboxes.addAll(list);
        return this;
    }

    /**
     * Adds a new hitbox to the attack
     *
     * @param data hitbox to add
     */
    public Attack appendHitbox(HitBoxData data) {
        extraHitboxes.add(data);
        return this;
    }

    /**
     * Assigns a specified mobility type to the attack
     *
     * @param mobility type of mobility
     */
    public Attack setMobility(MobilityType mobility) {
        this.mobilityType = mobility;
        return this;
    }

    /**
     * Disables automatic blockstun calculation, and forces baseDamageLogic to use the specified value
     */
    public Attack setBlockstun(int blockstun) {
        overrideBlockstun = true;
        this.blockstun = blockstun;
        return this;
    }

    /**
     * @return effective blockstun (in ticks) for attack (4 + damage OR assigned blockstun)
     */
    public int getEffectiveBlockstun() {
        if (overrideBlockstun)
            return blockstun;
        return (int) (4 + damage);
    }

    /**
     * Assigns a followup to the attack
     */
    public Attack setFollowup(Attack followup) {
        this.followup = followup;
        followup.isFollowup = true;
        return this;
    }

    /**
     * @return whether the attack has a followup
     */
    public boolean hasFollowup() {
        return this.followup != null;
    }

    /**
     * Assigns an attack as the aerial variation to this attack
     */
    public Attack aerialVariation(Attack air) {
        this.variations.put(VariationType.AERIAL, air);
        air.isAerialVariation = true;
        return this;
    }
    public @Nullable Attack getAerialVariation() {
        if (!this.variations.containsKey(VariationType.AERIAL)) return null;
        return this.variations.get(VariationType.AERIAL);
    }
    public @NotNull Attack getAerialVariationOrThrow() {
        return variations.get(VariationType.AERIAL);
    }

    /**
     * Assigns an attack as the crouching variation to this attack
     */
    public Attack crouchingVariation(Attack cr) {
        this.variations.put(VariationType.CROUCHING, cr);
        cr.isCrouchingVariation = true;
        return this;
    }
    public @Nullable Attack getCrouchingVariation() {
        if (!this.variations.containsKey(VariationType.CROUCHING)) return null;
        return this.variations.get(VariationType.CROUCHING);
    }
    public @NotNull Attack getCrouchingVariationOrThrow() {
        return variations.get(VariationType.CROUCHING);
    }

    public int realInitTime() {
        return this.attackType == AttackType.MULTIHIT ? attackTimes.get(0) : initTime;
    }

    public static Attack copyOf(Attack attack) {
        Attack attackCopy = new Attack(
                attack.id,
                attack.cooldown,
                attack.attackDist,
                attack.moveStun,
                attack.initTime,
                attack.hitboxSize,
                attack.damage,
                attack.knockback,
                attack.attackType,
                attack.stun,
                attack.offset,
                attack.interval,
                attack.impactSound
        );

        if (!attack.attackTimes.isEmpty())
            attackCopy.attackTimes.addAll(attack.attackTimes);
        if (!attack.extraHitboxes.isEmpty())
            attackCopy.extraHitboxes.addAll(attack.extraHitboxes);

        attackCopy.armor = attack.armor;
        attackCopy.lift = attack.lift;
        attackCopy.hitspark = attack.hitspark;

        attackCopy.stunType = attack.stunType;
        attackCopy.overrideStun = attack.overrideStun;

        attackCopy.animation = attack.animation;
        attackCopy.canBackstab = attack.canBackstab;

        attackCopy.blockstun = attack.blockstun;
        attackCopy.overrideBlockstun = attack.overrideBlockstun;

        attackCopy.name = attack.name;
        attackCopy.description = attack.description;
        attackCopy.button = attack.button;
        return attackCopy;
    }

    public boolean isCharge() {
        return attackType == AttackType.CHARGE || attackType == AttackType.CHARGEBARRAGE;
    }

    public boolean isBarrage() {
        return attackType == AttackType.BARRAGE || attackType == AttackType.CHARGEBARRAGE;
    }
}
