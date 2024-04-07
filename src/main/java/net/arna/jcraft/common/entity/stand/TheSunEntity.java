package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.shared.NoOpMove;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.function.Consumer;

public final class TheSunEntity extends StandEntity<TheSunEntity, TheSunEntity.State> {
    private static final TrackedData<Boolean> PASSIVE;
    private final int desiredHeight = 32;
    private Vec3d desiredPosition;

    private static final NoOpMove<TheSunEntity> TOGGLE_PASSIVE = new NoOpMove<TheSunEntity>(20, 0, 0)
            .withInitAction((attacker, user, ctx) -> attacker.togglePassive())
            .withInfo(
                    Text.of("Toggle Passive"),
                    Text.empty()
            );

    static {
        PASSIVE = DataTracker.registerData(TheSunEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public TheSunEntity(World worldIn) {
        super(StandType.THE_SUN, worldIn);

        auraColors = new Vec3f[]{
                new Vec3f(1.0f, 0.8f, 4.0f),
                new Vec3f(1.0f, 1.0f, 0.0f),
                new Vec3f(0.4f, 0.8f, 1.0f),
                new Vec3f(0.6f, 0.1f, 0.8f)
        };

        setAlphaOverride(1.0f);
    }

    @Override
    protected void registerMoves(MoveMap<TheSunEntity, State> moves) {


        moves.register(MoveType.UTILITY, TOGGLE_PASSIVE, null);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(PASSIVE, false);
    }

    private void togglePassive() {
        dataTracker.set(PASSIVE, !dataTracker.get(PASSIVE));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.isOutOfWorld();
    }

    @Override
    public void tryBlock() {}

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) return;

        LivingEntity user = getUser();
        if (user == null) return;

        if (!isFree()) {
            setFree(true);
            setFreePos(new Vec3f(getPos()));
        }

        if (desiredPosition == null) {
            Direction gravity = GravityChangerAPI.getGravityDirection(user);
            desiredPosition = user.getPos().add(Vec3d.of(gravity.getVector().multiply(desiredHeight)));
        } else {
            //moveControl.moveTo(desiredPosition.x, desiredPosition.y, desiredPosition.z, 0.2);
        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    @NonNull
    public TheSunEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<TheSunEntity> {
        IDLE(builder -> builder.loop("animation.sun.idle")),
        ;
        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(TheSunEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.sun.summon";
    }

    @Override
    public State getBlockState() {
        return null;
    }
}
