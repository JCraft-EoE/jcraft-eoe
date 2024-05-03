package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap.*;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JDimensionRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;
import java.util.function.Consumer;

public class D4CEntity extends StandEntity<D4CEntity, D4CEntity.State> {
    public static final ItemPlaceMove ITEM_PLACE = new ItemPlaceMove(JCraft.LIGHT_COOLDOWN, 8, 12, 0.75f)
            .withAnim(State.ITEM_PLACE)
            .withInfo(
                    Text.literal("Item Place"),
                    Text.literal("places an item from an alternate universe on the ground, attracts other such items"));
    public static final SimpleAttack<D4CEntity> LIGHT_FOLLOWUP = new SimpleAttack<D4CEntity>(
            0, 9, 14, 0.75f, 7f, 8, 1.75f, 1.25f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Text.literal("Deadly Blow"),
                    Text.literal("combo finisher, more blockstun than other light followups"));
    public static final SimpleAttack<D4CEntity> CHOP = new SimpleAttack<D4CEntity>(JCraft.LIGHT_COOLDOWN,
            9, 15, 0.75f, 5f, 20, 1.5f, 0.25f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(ITEM_PLACE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitAnimation(HitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Text.literal("Chop"),
                    Text.literal("relatively quick combo starter"));
    public static final MainBarrageAttack<D4CEntity> BARRAGE = new MainBarrageAttack<D4CEntity>(240, 0,
            40, 0.75f, 0.8f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.getHardness())
            .withSound(JSoundRegistry.D4C_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, high stun"));
    public static final SimpleAttack<D4CEntity> CHARGE = new SimpleAttack<D4CEntity>(200, 14, 25,
            1f, 8f, 12, 2f, 1.5f, -0.2f)
            .withInitAction(D4CEntity::doCharge)
            .withSound(JSoundRegistry.D4C_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Text.literal("Charge"),
                    Text.literal("user & stand charge forward, uninterruptible launcher"));
    public static final DimensionalHopMove DIM_HOP = new DimensionalHopMove(1200, 40, 60,
            1f, 0f, 0, 1.75f, 0f, 0f)
            .withSound(JSoundRegistry.D4C_DIMHOP)
            .withInfo(
                    Text.literal("Dimensional Hop"),
                    Text.literal("travels to a random dimension at exact coordinates, " +
                    "if user was hit in the last 30s, he is forced back, certified death button"));
    public static final GiveGunMove GIVE_GUN = new GiveGunMove(280, 10, 14, 0.75f)
            .withSound(JSoundRegistry.D4C_THROW)
            .withInitAction(D4CEntity::equipRevolver)
            .withInfo(
                    Text.literal("Summon Gun"),
                    Text.literal("gives the user a revolver"));
    public static final SimpleAttack<D4CEntity> GRAB_HIT_FINAL = new SimpleAttack<D4CEntity>(0, 26,
            34, 0.75f, 4f, 9, 2f, 1.2f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty());
    public static final SimpleMultiHitAttack<D4CEntity> GRAB_HIT = new SimpleMultiHitAttack<D4CEntity>(0,
            34, 0.75f, 4f, 10, 2f, 0f, 0f, IntSet.of(11, 17, 26))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // Play sound regardless of whether something hit.
            .withAction((attacker, user, ctx, targets) -> attacker.playSound(JSoundRegistry.REVOLVER_FIRE, 1, 1))
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(17, GRAB_HIT_FINAL)
            .withInfo(
                    Text.literal("Grab (Final Hit)"),
                    Text.empty());
    public static final D4CGrabAttack GRAB = new D4CGrabAttack(280, 12, 21, 0.75f,
            0f, 40, 1.5f, 0f, 0f, GRAB_HIT, State.THROW_HIT, 25, 1)
            .withCrouchingVariant(GIVE_GUN)
            .withSound(JSoundRegistry.D4C_THROW)
            .withInitAction(D4CEntity::equipRevolver)
            .withInfo(
                    Text.literal("Grab"),
                    Text.literal("unblockable, combo finisher"));
    public static final D4CCounterAttack COUNTER = new D4CCounterAttack(400, 5, 35, 0.75f)
            .withInfo(
                    Text.literal("Counter"),
                    Text.literal("0.25s startup, 1.5s duration, high damage, knocks back when hit"));
    public static final CloneSpawnMove CLONE_SPAWN = new CloneSpawnMove(400, 40, 50, 1f)
            .withSound(JSoundRegistry.D4C_DIMHOP)
            .withInfo(
                    Text.literal("Dimensional Clone"),
                    Text.literal("""
                    summons an unlimited number of servants, crouch and interact to give/take items, press a special button to change their weapon
                    Servant types:
                    DEFAULT - Iron Sword
                    SPECIAL 1 - Wooden Axe
                    SPECIAL 2 - Bow
                    SPECIAL 3 - None"""));
    public static final FlagMove FLAG = new FlagMove(280, 10, 60, 0f)
            .withSound(JSoundRegistry.D4C_UTILITY)
            .withInfo(
                    Text.literal("Dimensional Phase"),
                    Text.literal("hides in a flag in an un-stunnable, floating state"));

    public D4CEntity(World worldIn) {
        super(StandType.D4C, worldIn, JSoundRegistry.D4C_SUMMON, true);

        idleRotation = -45f;

        description = "All Range, Multipurpose TRICKSTER";

        pros = List.of(
                "good combo tools",
                "counter",
                "extensive setups",
                "good pressure"
        );

        cons = List.of(
                "optimal setups and combos require preparation",
                "slower than average"
        );

        freespace =
                """
                        BNBs:
                            -the lazy zoner
                            M1>Barrage>M1>Grab/Charge
                            
                            -the western
                            M1>Summon Gun>Barrage>M1~stand.OFF>M2>M2>M2>~s.ON+M1>Charge""";

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.5f, 0.7f),
                new Vector3f(0.5f, 0.8f, 0.9f),
                new Vector3f(0.4f, 0.4f, 1.0f),
                new Vector3f(1.0f, 0.5f, 0.2f)
        };
    }

    @Override
    protected void registerMoves(MoveMap<D4CEntity, State> moves) {
        moves.registerImmediate(MoveType.LIGHT, CHOP, State.LIGHT);

        moves.register(MoveType.HEAVY, CHARGE, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, CLONE_SPAWN, State.DIM_HOP);
        moves.register(MoveType.SPECIAL2, GRAB, State.THROW).withCrouchingVariant(State.GIVE_GUN);
        moves.register(MoveType.SPECIAL3, COUNTER, State.COUNTER);
        moves.register(MoveType.ULTIMATE, DIM_HOP, State.DIM_HOP);

        moves.register(MoveType.UTILITY, FLAG, State.FLAG);
    }

    private static void doCharge(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        if (!user.isOnGround()) return;
        JUtils.addVelocity(user, attacker.getRotationVector().multiply(0.75).add(0.0, 0.15, 0.0));
    }

    private static void equipRevolver(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.equipStack(EquipmentSlot.MAINHAND, JObjectRegistry.FV_REVOLVER.getDefaultStack());
    }

    @Override
    public boolean initMove(MoveType type) {
        switch (type) {
            case SPECIAL1 -> getMoveContext().set(CloneSpawnMove.CLONE_TYPE, CloneSpawnMove.CloneType.AXE);
            case SPECIAL2 -> getMoveContext().set(CloneSpawnMove.CLONE_TYPE, CloneSpawnMove.CloneType.BOW);
            case SPECIAL3 -> getMoveContext().set(CloneSpawnMove.CLONE_TYPE, CloneSpawnMove.CloneType.EMPTY);
            case ULTIMATE -> {
                if (curMove != null && curMove.getOriginalMove() == DIM_HOP) {
                    setMoveStun(0);
                    curMove = null;
                }

                if (getWorld().getRegistryKey().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
                    setMove(DIM_HOP, State.DIM_HOP);
                    playSound(JSoundRegistry.D4C_DIMHOP, 1, 1);
                    return true;
                }
            }
            case LIGHT -> {
                if (curMove != null && curMove.getMoveType() == MoveType.LIGHT && getMoveStun() < curMove.getWindupPoint()) {
                    AbstractMove<?, ? super D4CEntity> followup = curMove.getFollowup();
                    if (followup != null) {
                        setMove(followup, (State) followup.getAnimation());
                        return true;
                    }
                }
            }
        }

        return super.initMove(type);
    }

    @Override
    public void queueMove(MoveInputType type) {
        if (curMove != null && curMove.getOriginalMove() == CLONE_SPAWN)
            return;
        super.queueMove(type);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getState() == State.FLAG) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x + 0.5, y + 0.5, z + 0.5, x - 0.5, y, z - 0.5);
        }
        return super.calculateBoundingBox();
    }

    /* -- OLD GUN THROW CODE
                Vec3d rotVec = this.getRotationVector();
                Vec3d eyePos = this.getEyePos();

                ItemEntity revolver1 = new ItemEntity(EntityType.ITEM, world);
                revolver1.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver1.setPickupDelay(100);
                revolver1.setPosition(eyePos.add(rotVec.rotateY(90)));
                revolver1.setVelocity(rotVec.rotateY(95).multiply(1.5));

                ItemEntity revolver2 = new ItemEntity(EntityType.ITEM, world);
                revolver2.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver2.setPickupDelay(100);
                revolver2.setPosition(eyePos.add(rotVec.rotateY(-90)));
                revolver2.setVelocity(rotVec.rotateY(-95).multiply(1.5));

                world.spawnEntity(revolver1);
                world.spawnEntity(revolver2);
    */

    @Override
    @NonNull
    public D4CEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<D4CEntity> {
        IDLE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.d4c.idle"))),
        LIGHT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.light"))),
        BLOCK(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.d4c.block"))),
        HEAVY(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.heavy"))),
        BARRAGE(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.d4c.barrage"))),
        DIM_HOP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.dimhop"))),
        THROW(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.throw"))),
        THROW_HIT(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.throwhit"))),
        COUNTER(builder -> builder.setAnimation(RawAnimation.begin().thenLoop("animation.d4c.counter"))),
        COUNTER_MISS(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.counter_miss"))),
        GIVE_GUN(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.givegun"))),
        FLAG(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.flag"))),
        ITEM_PLACE(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.itemplace"))),
        LIGHT_FOLLOWUP(builder -> builder.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.d4c.light_followup")));

        private final Consumer<AnimationState> animator;

        State(Consumer<AnimationState> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(D4CEntity attacker, AnimationState state) {
            animator.accept(state);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.d4c.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
