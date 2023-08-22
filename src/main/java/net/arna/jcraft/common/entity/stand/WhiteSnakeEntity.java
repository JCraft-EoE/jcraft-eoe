package net.arna.jcraft.common.entity.stand;

import com.google.common.collect.Lists;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.attack.core.old.AttackType;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.EffectInflictingAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class WhiteSnakeEntity extends StandEntity<WhiteSnakeEntity, WhiteSnakeEntity.State> {
    public static final SimpleAttack<WhiteSnakeEntity> LIGHT = SimpleAttack.<WhiteSnakeEntity>lightAttack(7, 14, 5f, 12, 0.75f, 0.75f, 0.2f)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final SimpleAttack<WhiteSnakeEntity> DONUT = new SimpleAttack<WhiteSnakeEntity>(280, 17, 36, 10, 28, 2, 0, 1, 0)
            .withSound(JSoundRegistry.WS_DONUT)
            .withImpactSound(JSoundRegistry.TW_DONUT_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Donut"), Text.literal("slow combo starter/extender"));
    public static final BarrageAttack<WhiteSnakeEntity> BARRAGE = new BarrageAttack<WhiteSnakeEntity>(340, 0, 60, 0.75f, 1, 20, 2, 0.25f, 0, 3)
            .withSound(JSoundRegistry.WS_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_3)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final EffectInflictingAttack<WhiteSnakeEntity> STAND_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(600, 22, 34, 1, 8f, 20, 2, 0, 0, List.of(new StatusEffectInstance(JStatusRegistry.STANDLESS, 160, 0)))
            .withSound(JSoundRegistry.WS_STAND_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Stand Disk"), Text.literal("uninterruptable, removes enemy stand for 8s"));
    public static final SimpleAttack<WhiteSnakeEntity> LEG_CRUSHER = new SimpleAttack<WhiteSnakeEntity>(400, 16, 22, 7, 32, 1.75f, 0.25f, 0.75f, 0.2f)
            .withSound(JSoundRegistry.WS_LEGCRUSH)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(Text.literal("Leg Crusher"), Text.literal("high stun, medium windup"))
    public static final EffectInflictingAttack<WhiteSnakeEntity> MEMORY_DISC = new EffectInflictingAttack<WhiteSnakeEntity>(600, 22, 34, 1, 7f, 20, 2, 0, 0,
            List.of(
                    new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0),
                    new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 0)
            ))
            .withSound(JSoundRegistry.WS_MEMORY_DISC)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withInfo(Text.literal("Memory Disk"), Text.literal("uninterruptable, mining fatigue & weakness for 30s"));
    public static final Attack chargedspew = new Attack(7, 30, 0.75f, 26, 20, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .setInfo("Charged Spew", "fires 5, slower acid balls");
    public static final Attack poisonspew = new Attack(5, 20, 0.75f, 14, 10, 2, 0f, 0, AttackType.BOX)
            .setUB(true)
            .crouchingVariation(chargedspew)
            .setInfo("Poison Spew", "fires an acid projectile that slows enemies and persists on the surface it hits for 5s");
    public static final Attack meltyourheart = new Attack(8, 40, 1f, 50, 40, 2, 3f, 1.0f, AttackType.BOX, 1f, 0, 0, JSoundRegistry.IMPACT_2)
            .hyperArmor()
            .setUB(true)
            .setLaunch()
            .setInfo("Melt your Heart", "remote-only and armored, expels a sphere of poison");

    public WhiteSnakeEntity(World worldIn) {
        super(StandType.WHITE_SNAKE, worldIn, JSoundRegistry.WS_SUMMON);
        idleRotation = 220f;

        description = "All Range DISABLER";

        pros = List.of(
                "coverage on all ranges",
                "high versatility",
                "accessible win condition"
        );

        cons = List.of(
                "no mobility options",
                "slow pokes",
                "below average damage"
        );

        freespace =
                """
                        BNBs:
                            -the el mayo (optimal damage with disk moves)
                            Memory Disk>M1>Barrage>Leg Crusher>Stand Disk>M1
                                        
                            -the gazebo (optimal damage without disk)
                            M1>Barrage>Leg Crusher>Donut>M1
                            
                            -the protein shake (sets up mixups)
                            M1>Barrage>Leg Crusher>Charged Spew""";

        moves = Lists.newArrayList(light, donut, barrage, memorydisk, standdisk, legcrusher, poisonspew,
                new Attack().setInfo("Pilot Mode", ""));

        super.initialize();
    }

    @Override
    public void initMove(MoveType type) {
        if (type != MoveType.UTILITY) super.initMove(type);

        if (!canAttack() || !hasUser()) return;
        CooldownsComponent cooldowns = JComponents.getCooldowns(getUserOrThrow());
        if (cooldowns.getCooldown(CooldownType.UTILITY) > 0) return;

        boolean newRemote = !getRemote();
        setRemote(newRemote);

        // TODO: update ultimate when going into/out of pilot mode
        //if (newRemote) moves.set(4, meltyourheart);
        //else moves.set(4, standdisk);

        cooldowns.setCooldown(CooldownType.UTILITY, 20);
    }

    @Override
    protected void registerMoves(MoveMap<WhiteSnakeEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT);
        moves.register(MoveType.HEAVY, DONUT, State.DONUT);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, MEMORY_DISC, State.DISC);
        moves.register(MoveType.SPECIAL2, LEG_CRUSHER, State.LEG_CRUSHER);
        moves.register(MoveType.SPECIAL3, POISON_SPEW, State.ACID_SPEW).withCrouchingVariant(State.ACID_SPEW_CHARGED);
        moves.register(MoveType.ULTIMATE, STAND_DISC, State.DISC);

        //todo: register pilot mode, but only as info
        //moves.register(MoveType.UTIL, )
    }

    @Override
    public void initUlt() {
        if (!canAttack()) return;
        if (getRemote() && handleMove(meltyourheart, CooldownType.STAND_ULTIMATE, State.MELT_YOUR_HEART))
            playSound(JSoundRegistry.WS_MYH, 1, 1);
        else if (handleMove(standdisk, CooldownType.STAND_ULTIMATE, State.DISC))
            playSound(JSoundRegistry.WS_STAND_DISC, 1, 1);
    }

    @Override
    public void initSpecial3() {
        if (!canAttack() || !hasUser()) return;

        if (getUserOrThrow().isSneaking())
            handleMove(chargedspew, CooldownType.STAND_SP3, State.ACID_SPEW_CHARGED);
        else handleMove(poisonspew, CooldownType.STAND_SP3, State.ACID_SPEW);
    }


    /*
            case(5)->
    { // Poison Spew
        WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
        acidProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.33F, 0);
        acidProjectile.setPosition(getEyePos());
        world.spawnEntity(acidProjectile);
    }

            case(7)->
    { // Charged Spew
        for (int i = 0; i < 5; i++) {
            WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
            acidProjectile.setVelocity(user, user.getPitch(), user.getYaw() - 75F + i * 37.5F, 0, 0.66F, 0);
            acidProjectile.setPosition(getEyePos());
            world.spawnEntity(acidProjectile);
        }
    }
            case(8)->
    { // Melt your Heart
        for (int i = 0; i < 10; i++) {
            float yaw = i * 36F - 180F + i * 3.6F;
            for (int j = 0; j < 10; j++) {
                WSAcidProjectile acidProjectile = new WSAcidProjectile(world, user);
                acidProjectile.markMeltYourHeart();
                acidProjectile.setVelocity(user, j * 36F - 180F, yaw, 0, 0.66F, 0);
                acidProjectile.setPosition(getEyePos());
                world.spawnEntity(acidProjectile);
            }
        }
    }
     */

    @Override
    public void tick() {
        super.tick();

        if (!getRemote() || world.isClient) return;

        double f = getRemoteForwardInput();
        double s = getRemoteSideInput();
        boolean jump = getRemoteJumpInput();

        Vec3d pos = getPos();

        // 3 ticks of inertia, helping movement be fluid as well as dealing with packet drops
        if (lastRemoteInputTime - age > 4) updateRemoteInputs(0, 0, false);
        Vec3d rotVec = new Vec3d(getRotationVector().x, 0, getRotationVector().z).normalize();

        double dragMult = getMoveStun() > 0 ? 0.2 : 0.4;
        double moveSpeed = 0.24;
        //HitResult groundCheck = this.world.raycast(new RaycastContext(getEyePos(), pos.add(0, -1.0E-5F, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        boolean onGround = isOnGround();

        if (getState() == State.IDLE) { // Replace idle anim
            if (s > 0) setStateNoReset(onGround ? State.RIGHT : State.RIGHT_DASH);
            if (s < 0) setStateNoReset(onGround ? State.LEFT : State.LEFT_DASH);
            if (f < 0) setStateNoReset(onGround ? State.BACKWARD : State.BACKWARD_DASH);
            if (f > 0) setStateNoReset(onGround ? State.FORWARD : State.FORWARD_DASH);
        }

        if (onGround) { // If grounded
            if (jump && getMoveStun() < 1) {
                remoteSpeed = new Vec3d(remoteSpeed.x, 0.25, remoteSpeed.z);
                setRemoteJumpInput(false);
            }
        } else {
            //JCraft.LOGGER.info("Airborne");
            moveSpeed = 0.024;
            remoteSpeed = remoteSpeed.add(0, -9.81 / 200, 0); // Account for gravity
            dragMult = 0.4;
        }

        remoteSpeed = remoteSpeed
                .add(rotVec.multiply(f * moveSpeed)) // Forward movement
                .add(rotVec.rotateY(1.5707963f).multiply(s * moveSpeed)); // Side movement

        remoteSpeed = remoteSpeed.multiply(dragMult, 1, dragMult);

        if (pos.add(remoteSpeed).squaredDistanceTo(getUserOrThrow().getPos()) > 400)
            remoteSpeed.multiply(-1);

        addVelocity(remoteSpeed.x, remoteSpeed.y, remoteSpeed.z);
        velocityDirty = true;
        velocityModified = true;
    }

    @Override
    protected WhiteSnakeEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<WhiteSnakeEntity> {
        IDLE(builder -> builder.loop("animation.whitesnake.idle")),
        LIGHT(builder -> builder.playAndHold("animation.whitesnake.light")),
        BLOCK(builder -> builder.loop("animation.whitesnake.block")),
        DONUT(builder -> builder.playAndHold("animation.whitesnake.donut")),
        BARRAGE(builder -> builder.loop("animation.whitesnake.barrage")),
        LEG_CRUSHER(builder -> builder.playAndHold("animation.whitesnake.legcrusher")),
        ACID_SPEW(builder -> builder.playAndHold("animation.whitesnake.acidspew")),
        ACID_SPEW_CHARGED(builder -> builder.playAndHold("animation.whitesnake.acidspew_charged")),
        DISC(builder -> builder.playAndHold("animation.whitesnake.disc")),

        FORWARD(builder -> builder.loop("animation.whitesnake.forw")),
        BACKWARD(builder -> builder.loop("animation.whitesnake.back")),
        LEFT(builder -> builder.loop("animation.whitesnake.left")),
        RIGHT(builder -> builder.loop("animation.whitesnake.right")),
        FORWARD_DASH(builder -> builder.loop("animation.whitesnake.fdash")),
        BACKWARD_DASH(builder -> builder.loop("animation.whitesnake.bdash")),
        LEFT_DASH(builder -> builder.loop("animation.whitesnake.ldash")),
        RIGHT_DASH(builder -> builder.loop("animation.whitesnake.rdash")),

        MELT_YOUR_HEART(builder -> builder.playAndHold("animation.whitesnake.meltyourheart"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(WhiteSnakeEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @Nullable String getSummonAnimation() {
        return "animation.whitesnake.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
