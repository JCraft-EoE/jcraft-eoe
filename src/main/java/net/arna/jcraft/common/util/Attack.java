package net.arna.jcraft.common.util;

import net.minecraft.sound.SoundEvent;

import java.util.List;

public final class Attack {
    //TODO: ADD MULTI HITBOX MOVES
    public int id = -1; // Unique ID
    public int cooldown = 0; // Ability cooldown
    public float attackDist = 1f; // Distance your stand is at while attacking
    public int moveStun = 0; // Duration you can't use another move
    public int initTime = 0; // Time during movestun the move initiates
    public double hitboxSize = 0; // Hitbox size in meters cubed
    public float damage = 0; // Damage in half hearts
    public float knockback = 0.1f; // Knockback in meters/tick, 20m/s
    public AttackType attackType; // Attack type
    public float stun = 0f; // How long is the opponent stunned for after being hit
    public float offset = 0; // Hitbox Y offset (inverted)
    public int interval = 1; // For barrages; attack interval | For charges; hit state ID

    public SoundEvent impactSound;
    public List<Integer> attackTimes;

    public boolean hasArmor = false; // For (un)interruptable attacks

    public Attack setArmor(boolean armor) {
        this.hasArmor = armor;
        return this;
    }

    public boolean lift = true; // If set to true, attack will attempt to keep the victim in air on hit

    public Attack setLift(boolean lift) {
        this.lift = lift;
        return this;
    }

    public int hitspark = 1; // Particle that appears on hit

    public Attack setHitspark(int hitspark) {
        this.hitspark = hitspark;
        return this;
    }

    public int stunType = 1; // 1 - HITSTUN, 2 - BLOCKSTUN, 3 - NO MOVEMENT PENALTY

    public Attack setStunType(int st) {
        this.stunType = st;
        return this;
    }

    public boolean overrideStun = false; // If set to true, attack will override current stun on victim
    public Attack setStunOverride(boolean o) {
        this.overrideStun = o;
        return this;
    }

    public Attack setLaunch() { // Shorthand
        this.stunType = 3;
        this.overrideStun = true;
        return this;
    }

    public boolean unblockable = false;
    public boolean ubEffectsOnly = false;

    /**
     * Marks the attack as unblockable
     * @param ubEO is the effect of the attack the only unblockable feature?
     */
    public Attack setUB(boolean ubEO) {
        this.unblockable = true;
        this.ubEffectsOnly = ubEO;
        return this;
    }

    public boolean canBackstab = true;
    public Attack disableBackstab() {
        this.canBackstab = false;
        return this;
    }

    // Info
    public AttackQueue button;
    public String name = "UNNAMED";
    public String description = "UNDESCRIBED";

    public Attack setInfo(String name, String desc) {
        this.name = name;
        this.description = desc;
        return this;
    }

    public Attack setInfo(String name, String desc, AttackQueue b) {
        this.name = name;
        this.description = desc;
        this.button = b;
        return this;
    }

    // Spec-exclusive
    public String animation;

    public Attack setAnimation(String anim) {
        this.animation = anim;
        return this;
    }

    // AI Flags, mostly
    public boolean isRanged = false;

    public Attack setRanged(boolean ranged) {
        this.isRanged = ranged;
        return this;
    }

    public Attack setDamage(float damage) {
        this.damage = damage;
        return this;
    }

    public MobilityType mobilityType = null;

    public Attack setMobility(MobilityType mobility) {
        this.mobilityType = mobility;
        return this;
    }

    public Attack followup;

    public Attack setFollowup(Attack f) {
        this.followup = f;
        return this;
    }

    public boolean hasFollowup() {
        return this.followup != null;
    }

    public int realInitTime() {
        return this.attackType == AttackType.MULTIHIT ? attackTimes.get(0) : initTime;
    }

    public static Attack copyOf(Attack attack) {
        Attack attackCopy = new Attack();
        attackCopy.id = attack.id;
        attackCopy.cooldown = attack.cooldown;
        attackCopy.attackDist = attack.attackDist;
        attackCopy.moveStun = attack.moveStun;
        attackCopy.initTime = attack.initTime;
        attackCopy.hitboxSize = attack.hitboxSize;
        attackCopy.damage = attack.damage;
        attackCopy.knockback = attack.knockback;
        attackCopy.attackType = attack.attackType;
        attackCopy.stun = attack.stun;
        attackCopy.offset = attack.offset;
        attackCopy.interval = attack.interval;
        attackCopy.impactSound = attack.impactSound;
        attackCopy.attackTimes = attack.attackTimes;
        attackCopy.hasArmor = attack.hasArmor;
        attackCopy.lift = attack.lift;
        attackCopy.hitspark = attack.hitspark;
        attackCopy.stunType = attack.stunType;
        attackCopy.overrideStun = attack.overrideStun;
        attackCopy.animation = attack.animation;
        attackCopy.canBackstab = attack.canBackstab;
        return attackCopy;
    }

    // For non-physicals
    public Attack() {
    }

    public Attack(int id, int cooldown, int moveStun, int initTime, float stun, AttackType attackType) {
        this.id = id;
        this.cooldown = cooldown;
        this.moveStun = moveStun; // Anim duration
        this.initTime = initTime; // TS init
        this.stun = stun; // TS duration
        this.attackType = attackType;
    }

    public Attack(int id, int cooldown, int moveStun, int initTime, float stun, float attackDist, AttackType attackType) {
        this.id = id;
        this.cooldown = cooldown;
        this.moveStun = moveStun;
        this.initTime = initTime;
        this.stun = stun;
        this.attackDist = attackDist;
        this.attackType = attackType;
    }

    // For knockback moves
    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType) {
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
    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun) {
        this.id = id;
        this.cooldown = cooldown;
        this.attackDist = attackDist; // CHARGE: max. attack dist
        this.moveStun = moveStun; // CHARGE: After initTime has passed, the remaining moveStun is how many ticks it takes for the stand to finish it's charge
        this.initTime = initTime;
        this.hitboxSize = hitboxSize;
        this.damage = damage;
        this.knockback = knockback;
        this.attackType = attackType;
        this.stun = stun;
    }

    // For stun moves
    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset) {
        this.id = id;
        this.cooldown = cooldown; //
        this.attackDist = attackDist; // CHARGE: max. attack dist
        this.moveStun = moveStun; // CHARGE: After initTime has passed, the remaining moveStun is how many ticks it takes for the stand to finish it's charge
        this.initTime = initTime; //
        this.hitboxSize = hitboxSize; //
        this.damage = damage; //
        this.knockback = knockback; //
        this.attackType = attackType; //
        this.stun = stun; //
        this.offset = offset; //
    }

    // For barrages
    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, int intervalOrHitAnim) {
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
        this.interval = intervalOrHitAnim;
    }

    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, int interval, SoundEvent impactSound) {
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
        this.interval = interval;
        this.impactSound = impactSound;
    }

    // For multi-hits that aren't barrages
    public Attack(int id, int cooldown, float attackDist, int moveStun, int initTime, double hitboxSize, float damage, float knockback, AttackType attackType, float stun, float offset, List<Integer> attackTimes, SoundEvent impactSound) {
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
        this.attackTimes = attackTimes;
        this.impactSound = impactSound;
    }
}