package net.arna.jcraft.common.entity.stand;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.aerosmith.BombDropAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.ItemDropAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.MuzzleHitscanAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.PatrolMove;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AerosmithEntity extends StandEntity<AerosmithEntity, AerosmithEntity.State> {
    public static final MoveSet<AerosmithEntity, AerosmithEntity.State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.AEROSMITH,
            AerosmithEntity::registerDefaultMoves, AerosmithEntity.State.class);

    public static final EntityDataAccessor<Float> OVERHEAT = SynchedEntityData.defineId(AerosmithEntity.class, EntityDataSerializers.FLOAT);
    public static final float OVERHEAT_MAX = 15f;

    // TODO Arna balance this
    public static final MuzzleHitscanAttack BULLET = new MuzzleHitscanAttack(
            1, 1, 2, 0f, 1f, 0, 0f, 30f, 10f, 1/6f, 0.01f)
            .withSound(JSoundRegistry.AS_SHOOT)
            .withHitSpark(JParticleType.HIT_SPARK_1)
            .withShootSpark(JParticleType.LEMON)
            .withStunType(StunType.WINDED);
    // TODO Arna description

    // TODO Arna balance this
    public static final ItemDropAttack ITEM_DROP = new ItemDropAttack(
            200, 1, 100, 0f, 30f);
    // TODO Arna description

    // TODO Arna balance this
    public static final BombDropAttack<AerosmithEntity> BOMB_DROP = new BombDropAttack<AerosmithEntity>(
            200, 1, 100, 0f, 30f)
            .withCrouchingVariant(ITEM_DROP);
    // TODO Arna description

    // TODO Arna balance this
    public static final PatrolMove<AerosmithEntity> PATROL = new PatrolMove<>(
            200, 1, 100, 0f, 30f, 10f, 0.5f);
    // TODO Arna description

    public static final StandData DATA = StandData.builder()
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.aerosmith"))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Vento Aureo"))
                    .skinName(Component.literal("Interceptor"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.AS_SUMMON).withAnimDuration(48))
            .build();

    private CommonMiscComponent miscComponent;
    private int overheatTick;
    @Getter
    @Setter
    @Nullable
    private ItemStack holdItem;

    public AerosmithEntity(final Level world) {
        super(JStandTypeRegistry.AEROSMITH.get(), world);
//        setYDistanceOffset(10f); // TODO for patrol mode
        setYDistanceOffset(1.2f);
        setNoGravity(true);
    }

    @Override
    public boolean remoteControllable() {
        return false;
    }

    @Override
    public @NonNull AerosmithEntity getThis() {
        return this;
    }

    private static void registerDefaultMoves(final @NonNull MoveMap<AerosmithEntity, AerosmithEntity.State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, BULLET, State.LIGHT);
        moves.registerImmediate(MoveClass.HEAVY, BOMB_DROP, State.IDLE);
        moves.registerImmediate(MoveClass.UTILITY, PATROL, State.IDLE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getCurrentMove() instanceof MuzzleHitscanAttack) && ++overheatTick % 5 == 0) {
            addOverheat(-0.4f);
        }
    }

    @Override
    public void setUser(@Nullable final LivingEntity user) {
        super.setUser(user);
        if (user == null) {
            return;
        }
        miscComponent = JComponentPlatformUtils.getMiscData(getUser());
        if (miscComponent == null) {
            return;
        }
        setOverheat(miscComponent.getAerosmithOverheat());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        float startValue = miscComponent == null ? 0f : miscComponent.getAerosmithOverheat();
        entityData.define(OVERHEAT, startValue);
    }

    public float getOverheat() {
        return entityData.get(OVERHEAT);
    }

    public void setOverheat(final float overheat) {
        entityData.set(OVERHEAT, overheat);
        if (miscComponent == null) {
            return;
        }
        miscComponent.setAerosmithOverheat(overheat);
    }

    public void addOverheat(float amount) {
        setOverheat(Mth.clamp(entityData.get(OVERHEAT) + amount, 0f, OVERHEAT_MAX));
    }

    @Override
    public void setRemote(final boolean r) {
        super.setRemote(r);
        setAlphaOverride(r ? 1f : -1f);
    }

    @Override
    public void desummon() {
        if (!level().isClientSide() && holdItem != null) {
            Containers.dropItemStack(level(), getX(), getY(), getZ(), holdItem);
        }
        super.desummon();
    }

    public enum State implements StandAnimationState<AerosmithEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP)),
        LIGHT(AzCommand.create(JCraft.BASE_CONTROLLER, "light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "block", AzPlayBehaviors.LOOP))
        ;

        private final AzCommand animator;

        State(final @NonNull AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(final @NonNull AerosmithEntity attacker) {
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
