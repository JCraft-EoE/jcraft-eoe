package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.LungeAction;
import net.arna.jcraft.common.attack.actions.PlaySoundAction;
import net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap.*;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Dirty_Deeds_Done_Dirt_Cheap">Dirty Deeds Done Dirt Cheap</a>.
 * @see JStandTypeRegistry#D4C
 * @see net.arna.jcraft.client.renderer.entity.stands.D4CRenderer D4CRenderer
 * @see CloneSpawnMove
 * @see D4CCounterAttack
 * @see D4CGrabAttack
 * @see DimensionalHopMove
 * @see FlagMove
 * @see GiveGunMove
 * @see ItemPlaceMove
 */
public class D4CEntity extends StandEntity<D4CEntity, D4CEntity.State> {
    public static final MoveSet<D4CEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.D4C, D4CEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-45f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.d4c"))
                    .proCount(4)
                    .conCount(2)
                    .freeSpace(Component.literal("""
                        BNBs: (outdated)
                            -the lazy zoner
                            Light>Barrage>Light>Grab/Charge
                        
                            -the western
                            Light>Summon Gun>Barrage>Light~stand.OFF>M2>M2>M2>~s.ON+Light>Charge"""))
                    .skinName(Component.literal("Jojoveller"))
                    .skinName(Component.literal("Teaser"))
                    .skinName(Component.literal("Spangled"))
                    .build())
            .summonData(SummonData.builder()
                    .sound(JSoundRegistry.D4C_SUMMON)
                    .playGenericSound(true)
                    .build())
            .build();

    public static final ItemPlaceMove ITEM_PLACE = new ItemPlaceMove(JCraft.LIGHT_COOLDOWN, 8, 12, 0.75f)
            .withAnim(State.ITEM_PLACE)
            .withInfo(
                    Component.literal("Item Place"),
                    Component.literal("places an item from an alternate universe on the ground, which attracts other such items"));
    public static final SimpleAttack<D4CEntity> LIGHT_FOLLOWUP = new SimpleAttack<D4CEntity>(0,
            9, 14, 0.75f, 7f, 8, 1.75f, 1.25f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Deadly Blow"),
                    Component.literal("combo finisher, more blockstun than other light followups"));
    public static final SimpleAttack<D4CEntity> CHOP = new SimpleAttack<D4CEntity>(JCraft.LIGHT_COOLDOWN,
            9, 15, 0.75f, 5f, 20, 1.5f, 0.25f, -0.1f)
            .noLoopPrevention()
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(ITEM_PLACE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Chop"),
                    Component.literal("relatively quick combo starter"));
    public static final MainBarrageAttack<D4CEntity> BARRAGE = new MainBarrageAttack<D4CEntity>(240, 0,
            40, 0.75f, 0.8f, 30, 2f, 0.25f, 0f, 3, Blocks.DEEPSLATE.defaultDestroyTime())
            .withSound(JSoundRegistry.D4C_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun"));
    public static final SimpleAttack<D4CEntity> CHARGE = new SimpleAttack<D4CEntity>(100, 14, 25,
            1f, 8f, 12, 2f, 1.5f, -0.2f)
            .withInitAction(LungeAction.lunge(0.75f, 0.15f).onGround())
            .withSound(JSoundRegistry.D4C_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Charge"),
                    Component.literal("user & stand charge forward, uninterruptible launcher"));
    public static final DimensionalHopMove DIM_HOP = new DimensionalHopMove(1200, 40, 60,
            1f, 0f, 0, 1.75f, 0f, 0f, 300)
            .withSound(JSoundRegistry.D4C_DIMHOP)
            .withInfo(
                    Component.literal("Dimensional Hop"),
                    Component.literal("travels to a random dimension at exact coordinates, " +
                            "if user was hit in the last 30s, he is forced back, certified death button"));
    public static final GiveGunMove GIVE_GUN = new GiveGunMove(280, 10, 14, 0.75f)
            .withSound(JSoundRegistry.D4C_THROW)
            .withInfo(
                    Component.literal("Summon Gun"),
                    Component.literal("gives the user a revolver"));
    public static final SimpleAttack<D4CEntity> GRAB_HIT_FINAL = new SimpleAttack<D4CEntity>(0, 26,
            34, 0.75f, 4f, 9, 2f, 1.2f, 0f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withLaunch()
            .withInfo(
                    Component.literal("Grab (Final Hit)"),
                    Component.empty());
    public static final SimpleMultiHitAttack<D4CEntity> GRAB_HIT = new SimpleMultiHitAttack<D4CEntity>(0,
            34, 0.75f, 4f, 10, 2f, 0f, 0f, IntSet.of(11, 17, 26))
            .withImpactSound(JSoundRegistry.IMPACT_1)
            // Play sound regardless of whether something hit.
            .withAction(PlaySoundAction.playSound(JSoundRegistry.REVOLVER_FIRE))
            .withStunType(StunType.UNBURSTABLE)
            .withFinisher(17, GRAB_HIT_FINAL)
            .withInfo(
                    Component.literal("Grab (Final Hit)"),
                    Component.empty());
    public static final D4CGrabAttack GRAB = new D4CGrabAttack(280, 12, 21, 0.75f,
            0f, 40, 1.5f, 0f, 0f, GRAB_HIT, 25, 1)
            .withCrouchingVariant(GIVE_GUN)
            .withSound(JSoundRegistry.D4C_THROW)
            .withInfo(
                    Component.literal("Grab"),
                    Component.literal("unblockable, combo finisher"));
    public static final D4CCounterAttack COUNTER = new D4CCounterAttack(300, 5, 35, 0.75f)
            .withInfo(
                    Component.literal("Counter"),
                    Component.literal("0.25s startup, 1.5s duration, high damage, knocks back when hit"));
    public static final CloneSpawnMove CLONE_SPAWN = new CloneSpawnMove(300, 40, 50, 1f)
            .withSound(JSoundRegistry.D4C_DIMHOP)
            .withInfo(
                    Component.literal("Dimensional Clone"),
                    Component.literal("""
                            summons an unlimited number of servants, crouch and interact to give/take items, press a special button to change their weapon
                            Servant types:
                            DEFAULT - Iron Sword
                            SPECIAL 1 - Wooden Axe
                            SPECIAL 2 - Bow
                            SPECIAL 3 - None"""));
    public static final FlagMove FLAG = new FlagMove(200, 10, 60, 0f)
            .withSound(JSoundRegistry.D4C_UTILITY)
            .withInfo(
                    Component.literal("Dimensional Phase"),
                    Component.literal("hides in a flag in an un-stunnable, floating state"));

    public D4CEntity(Level worldIn) {
        super(JStandTypeRegistry.D4C.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(0.9f, 0.5f, 0.7f),
                new Vector3f(0.5f, 0.8f, 0.9f),
                new Vector3f(0.4f, 0.4f, 1.0f),
                new Vector3f(1.0f, 0.5f, 0.2f)
        };
    }

    private static void registerMoves(MoveMap<D4CEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, CHOP, State.LIGHT);

        moves.register(MoveClass.HEAVY, CHARGE, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, CLONE_SPAWN, State.DIM_HOP);
        moves.register(MoveClass.SPECIAL2, GRAB, State.THROW).withCrouchingVariant(State.GIVE_GUN);
        moves.register(MoveClass.SPECIAL3, COUNTER, State.COUNTER);
        moves.register(MoveClass.ULTIMATE, DIM_HOP, State.DIM_HOP);

        moves.register(MoveClass.UTILITY, FLAG, State.FLAG);
    }

    public void equipRevolver() {
        setItemSlot(EquipmentSlot.MAINHAND, JItemRegistry.FV_REVOLVER.get().getDefaultInstance());
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case ULTIMATE -> {
                if (getCurrentMove() instanceof DimensionalHopMove) {
                    setMoveStun(0);
                    setCurrentMove(null);
                }
            }
            case LIGHT -> {
                if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
            }
        }

        return super.initMove(moveClass);
    }

    @Override
    protected @NonNull AABB makeBoundingBox() {
        if (getState() == State.FLAG) {
            final double x = getX(), y = getY(), z = getZ();
            return new AABB(x + 0.5, y + 0.5, z + 0.5, x - 0.5, y, z - 0.5);
        }
        return super.makeBoundingBox();
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
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.d4c.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.d4c.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.barrage", AzPlayBehaviors.LOOP)),
        DIM_HOP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.dimhop", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THROW(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.throw", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        THROW_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.throwhit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        COUNTER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.counter", AzPlayBehaviors.LOOP)),
        COUNTER_MISS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.counter_miss", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GIVE_GUN(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.givegun", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        FLAG(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.flag", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_PLACE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.itemplace", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.d4c.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(D4CEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
