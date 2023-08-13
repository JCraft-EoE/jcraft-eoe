package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract sealed class AbstractKillerQueenEntity<E extends AbstractKillerQueenEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits KillerQueenEntity, KQBTDEntity {
    public static final Attack low = new Attack(1, 0, 0.85f, 13, 8, 1.5, 4f, 0.5f, AttackType.BOX, 0.5f, 0.1f, 0, JSoundRegistry.IMPACT_6)
            .setInfo("Low Punch", "frametrap tool, low stun");
    public static final Attack detonate = new Attack(6, 1, 1, 6, 5, 0, 0f, 0.0f, AttackType.BOX)
            .setInfo("Detonate", "slight windup");

    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 19, 0, 1.5, 3f, 0.75f, AttackType.MULTIHIT, 1f, 0, List.of(6, 11), JSoundRegistry.IMPACT_4)
            .crouchingVariation(detonate)
            .setFollowup(low)
            .setInfo("Dual Punch", "combo starter, decent speed, has followup with more blockstun");
    public static final Attack barrage = Attack.barrageAttack(3, 17, 0.75f, 50, 0, 1.5, 1f, 0.1f, 1, 0, 3, JSoundRegistry.IMPACT_4)
            .setInfo("Barrage", "fast reliable combo starter/extender, medium stun");

    public static final Attack bombplant = new Attack(4, 30, 1, 20, 12, 1.5, 0f, 0.0f, AttackType.BOX, 0.45f)
            .setUB(true)
            .setBlockstun(8)
            .setInfo("Bomb Plant", "crouch to plant on the ground below you, stealthily");

    protected static final int bombplantCD = (int) (bombplant.cooldown * 20);

    protected ItemEntity coin;
    protected Entity bombEntity;
    protected Vec3d bombBlock;

    protected AbstractKillerQueenEntity(StandType type, World worldIn, @Nullable SoundEvent summonSound) {
        super(type, worldIn, summonSound, true);
        idleRotation = -30f;

        description = "Explosive SETPLAY";

        pros = List.of(
                "good stun",
                "excellent setups",
                "easy knockdowns",
                "good zoning"
        );

        cons = List.of(
                "limited pressure tools",
                "below-average speed",
                "limited combo tools"
        );

        freespace = """
                BNBs:
                    -Standard bomb plant confirm and SHA setup
                    M1>Barrage>Bomb plant>Detonate(>Sheer Heart Attack)
                    -Confirm while bomb plant is on cd
                    M1>Barrage>Heavy(>Sheer Heart Attack)""";


        //moves = List.of(light, heavy, barrage, bombplant, Attack.unusable, sha, new Attack().setRanged(true).setInfo("Coin Toss", "overrides current bomb with an aimable coin"), new Attack().setMobility(MobilityType.DASH).setInfo("Explosive Dash", "slight aoe damage, 3D movement tool"));
    }

    protected void explode(Entity user, Vec3d pos) {
        double y = pos.y + 2.2;
        Vec3d offsetPos = new Vec3d(pos.x, y, pos.z);
        ServerWorld serverWorld = (ServerWorld) world;

        JCraft.createParticle(serverWorld, pos.x, y, pos.z,-5);
        JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE, serverWorld, offsetPos, 96);

        DamageSource damageSource = JDamageSources.stand(this);

        Set<? extends LivingEntity> toExplode = JUtils.generateHitbox(world, offsetPos, 2.2, Set.of(user, this));

        for (LivingEntity living : toExplode) {
            Vec3d kbVec = offsetPos.subtract(living.getPos()).normalize();
            damageLogic(world, living, kbVec, 2, 3, true, 11f, true, 4, damageSource, user);
            living.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));
        }
    }

    public Vec3d getBombPos() {
        if (bombEntity != null)
            return bombEntity.getPos();
        if (bombBlock != null)
            return bombBlock;
        return null;
    }

    protected void detonate() {
        setAttack(detonate, getDetonateState());
        playSound(JSoundRegistry.KQ_DETONATE, 1, 1);
    }

    // Moveset
    @Override
    public void initLightAttack() {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        if (user.hasStatusEffect(JStatusRegistry.DAZED)) return;

        boolean idling = getMoveStun() < 1;
        if (curAttack != light) {
            if (idling) {
                if (user.isSneaking()) detonate();
                else handleAttack(light, CooldownType.STAND_LIGHT, getLightState());
            }
        } else if (getMoveStun() < 7) {
            if (user.isSneaking())
                detonate();
            else
                setAttack(low, getLowState());
        }
    }

    // All moves apart from light and util are different for both versions.
    @Override
    public abstract void initHeavyAttack();

    @Override
    public abstract void initBarrage();

    @Override
    public abstract void initSpecial1();

    @Override
    public abstract void initUlt();

    @Override
    public abstract void initSpecial2();

    @Override
    public abstract void initSpecial3();

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser()) return;

        LivingEntity user = getUserOrThrow();
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.UTIL) > 0) return;

        Vec3d lookVec = user.getRotationVector().multiply(0.9);
        world.createExplosion(user,
                user.getX() - lookVec.x,
                user.getY() + user.getHeight() / 2 - lookVec.y,
                user.getZ() - lookVec.z,
                1f, Explosion.DestructionType.NONE);

        user.setVelocity(user.getVelocity().add(lookVec));
        user.velocityModified = true;

        cooldowns.setCooldown(CooldownType.UTIL, 360); // 18s explosive dash cooldown
        playSound(JSoundRegistry.KQ_DETONATE, 1, 1);
    }

    @Override
    public abstract void specialAttack(Attack attack, Set<LivingEntity> entities);

    @Override
    public void desummon() {
        if (coin != null) coin.discard();
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(Attack attack, MobEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, Attack enemyAttack) {
        Vec3d bombPos = getBombPos();
        return bombPos != null && attack == detonate && target.squaredDistanceTo(bombPos) < 9.0D ?
                MoveSelectionResult.USE : MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (hasUser()) {
            LivingEntity user = getUser();
            if (!world.isClient && user instanceof ServerPlayerEntity playerEntity)
                displayBombParticles(playerEntity, this.bombBlock, this.bombEntity);
        }
    }

    protected void displayBombParticles(ServerPlayerEntity playerEntity, Vec3d bombBlock, Entity bombEntity) {
        boolean bombIsBlock = bombBlock != null;
        boolean bombExists = (bombEntity != null || bombIsBlock);

        double dX1 = 0;
        double dY1 = 0;
        double dZ1 = 0;
        double dX2 = 0;
        double dY2 = 0;
        double dZ2 = 0;

        Box bBox = null;

        if (bombEntity != null) { // If the bomb isn't a block
            dX1 = bombEntity.getX();
            dY1 = bombEntity.getY();
            dZ1 = bombEntity.getZ();

            bBox = bombEntity.getBoundingBox();

            dX2 = bBox.getXLength();
            dY2 = bBox.getYLength();
            dZ2 = bBox.getZLength();
        } else if (bombIsBlock) { // If the bomb is a block
            dX1 = bombBlock.getX();
            dY1 = bombBlock.getY();
            dZ1 = bombBlock.getZ();

            dX2 = dY2 = dZ2 = 1.41;
        }

        if (bombExists) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(4);

            buf.writeDouble(dX1);
            buf.writeDouble(dY1);
            buf.writeDouble(dZ1);

            buf.writeDouble(dX2);
            buf.writeDouble(dY2);
            buf.writeDouble(dZ2);

            boolean anyInRange = false;
            Vec3d bPos = getBombPos();
            Vec3d v1 = bPos.add(3, 3, 3);
            Vec3d v2 = bPos.add(-3, -3, -3);
            List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);
            if (bombEntity instanceof LivingEntity) list.remove(bombEntity);
            for (LivingEntity l : list)
                if (l.squaredDistanceTo(bPos) < 9) {
                    anyInRange = true;
                    break;
                }

            buf.writeBoolean(anyInRange);

            if ((bBox != null && bBox.getAverageSideLength() > 0) || bombIsBlock)
                ServerChannelFeedbackPacket.send(playerEntity, buf);
        }
    }

    // Animation code
    protected abstract S getDetonateState();
    protected abstract S getLightState();
    protected abstract S getLowState();
}
