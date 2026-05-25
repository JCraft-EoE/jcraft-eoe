package net.arna.jcraft.common.entity;

import dev.architectury.extensions.network.EntitySpawnExtension;
import dev.architectury.networking.NetworkManager;
import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A purely cosmetic entity that renders the carbon dioxide radar model on the head of
 * Aerosmith's user. It is invisible and fully non-interactive; visibility is tied to
 * whether {@link AerosmithEntity}'s breath-xray move is currently active.
 *
 * <p>Lifecycle: spawned by {@link AerosmithEntity} on its first server tick. Discards
 * itself if the user disappears or no longer has Aerosmith as their active stand.
 */
public class CarbonDioxideRadarEntity extends Entity implements EntitySpawnExtension {

    private static final EntityDataAccessor<Integer> USER_ID =
            SynchedEntityData.defineId(CarbonDioxideRadarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RADAR_ACTIVE =
            SynchedEntityData.defineId(CarbonDioxideRadarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(CarbonDioxideRadarEntity.class, EntityDataSerializers.INT);

    /**
     * Plays the summon animation exactly once, then loops the idle propeller animation.
     * Dispatched every client tick while the radar is active — AzAnimationController
     * dedupes identical sequences ({@code currentSequence.equals(sequence)}), so only
     * the first dispatch actually starts the summon; subsequent ones are no-ops while
     * the sequence is still playing or looping idle.
     */
    private static final AzCommand SUMMON_THEN_IDLE = AzCommand.controllerBuilder()
            .playSequence(
                    JCraft.BASE_CONTROLLER,
                    sequenceBuilder -> sequenceBuilder
                            .queue("summon", props -> props.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE))
                            .queue("idle",   props -> props.withPlayBehavior(AzPlayBehaviors.LOOP))
            )
            .build();

    /**
     * The "off" sequence — just idle on a loop. Dispatched while the radar is inactive
     * so the animation controller's {@code currentSequence} differs from
     * {@link #SUMMON_THEN_IDLE}; that's what makes the next activation actually restart
     * the summon rather than getting suppressed by the equality dedupe.
     *
     * <p>The radar is invisible while inactive, so whatever this plays isn't seen — it
     * only exists to reset controller state for the next activation.
     */
    private static final AzCommand IDLE_ONLY =
            AzCommand.create(JCraft.BASE_CONTROLLER, "idle", AzPlayBehaviors.LOOP);

    /**
     * Cached user reference so a transient {@code level().getEntity(id)} miss
     * (e.g. the user briefly out of tracking range) doesn't drop the binding
     * mid-tick. Cleared when the cached entity is removed or its ID no longer
     * matches USER_ID (e.g. respawn).
     */
    @Nullable
    private LivingEntity cachedUser;

    public CarbonDioxideRadarEntity(final Level level) {
        super(JEntityTypeRegistry.CO2_RADAR.get(), level);
        noPhysics = true;
        noCulling = true; // skip frustum culling — model origin is inside the player
        setNoGravity(true);
        setInvisible(true); // start invisible; becomes visible once radar activates
    }

    // Render if the user is being rendererd
    @Override
    public boolean shouldRenderAtSqrDistance(final double distanceSq) {
        return getUser() != null && getUser().shouldRenderAtSqrDistance(distanceSq);
    }

    // -------------------------------------------------------------------------
    // Synced data
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData() {
        entityData.define(USER_ID, -1);
        entityData.define(RADAR_ACTIVE, false);
        entityData.define(SKIN, 0);
    }

    public int getUserId() {
        return entityData.get(USER_ID);
    }

    public boolean isRadarActive() {
        return entityData.get(RADAR_ACTIVE);
    }

    public int getSkin() {
        return entityData.get(SKIN);
    }

    public void setUser(final @NonNull LivingEntity user) {
        entityData.set(USER_ID, user.getId());
    }

    @Nullable
    public LivingEntity getUser() {
        final int id = getUserId();
        if (id < 0) return null;

        if (cachedUser != null && !cachedUser.isRemoved() && cachedUser.getId() == id) {
            return cachedUser;
        }

        final Entity entity = level().getEntity(id);
        if (entity instanceof LivingEntity le) {
            cachedUser = le;
            return le;
        }
        return null;
    }

    @Override
    public boolean isInvisible() {
        // When the radar move is off the entity should be completely hidden.
        // Falling back to super preserves normal invisibility (e.g. spectator mode).
        return !isRadarActive() || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(final @NonNull Player player) {
        // When the radar is off, fully invisible to everyone so AzureLib sets alpha 0
        // rather than the 38% "teammate" semi-transparency.
        return !isRadarActive();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            // Drive the propeller animation. While active, run summon→idle (summon plays
            // once on the first dispatch, then idle loops forever); while inactive, run
            // idle-only so the *next* activation sees a different sequence and triggers
            // a fresh summon. See SUMMON_THEN_IDLE javadoc for the dedupe logic.
            if (isRadarActive()) {
                SUMMON_THEN_IDLE.sendForEntity(this);
            } else {
                IDLE_ONLY.sendForEntity(this);
            }

            // Client-side position/rotation tracking: pull straight from the user every
            // tick instead of waiting for server→client move packets. This is what makes
            // the position 50 ms fresh.
            final LivingEntity user = getUser();
            if (user != null) {
                xo = getX();
                yo = getY();
                zo = getZ();
                yRotO = getYRot();
                xRotO = getXRot();

                setPos(user.getX(), user.getEyeY(), user.getZ());
                setYRot(user.yHeadRot);
                setXRot(user.getXRot());
            }
            return;
        }

        // Server-side: track user, sync stand data, manage lifetime.
        final LivingEntity user = getUser();
        if (user == null || user.isRemoved()) {
            discard();
            return;
        }

        final StandEntity<?, ?> stand = JUtils.getStand(user);
        if (!(stand instanceof AerosmithEntity aerosmith)) {
            discard();
            return;
        }

        // Follow the user's eye position so the entity's lighting/culling/AABB stays in
        // a sensible place; the renderer overrides the visual position with sub-tick
        // precision anyway. (Same xOld/yOld preservation as the client branch so any
        // observer interpolating purely from server data still gets a smooth result.)
        xo = getX();
        yo = getY();
        zo = getZ();
        yRotO = getYRot();
        xRotO = getXRot();

        setPos(user.getX(), user.getEyeY(), user.getZ());
        setYRot(user.yHeadRot);
        setXRot(user.getXRot());

        // Sync skin and radar-active state.
        entityData.set(SKIN, aerosmith.getSkin());
        final boolean active = aerosmith.getBreathXrayMove().isActive() && aerosmith.isRemote();
        entityData.set(RADAR_ACTIVE, active);
        setInvisible(!active);
    }

    // -------------------------------------------------------------------------
    // Entity boilerplate
    // -------------------------------------------------------------------------

    /** This entity is transient — no meaningful state to persist. */
    @Override
    protected void readAdditionalSaveData(final @NonNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(final @NonNull CompoundTag tag) {}

    /** Do not persist across level restarts — AerosmithEntity will respawn us. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public @NonNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkManager.createAddEntityPacket(this);
    }

    // -------------------------------------------------------------------------
    // EntitySpawnExtension — write USER_ID into the spawn packet itself so the
    // client knows the user from frame zero, instead of waiting a tick or two
    // for the separate synced-data packet. Closes the spawn race that left the
    // radar parked at the user's foot position with no rotation lerp/alpha.
    // -------------------------------------------------------------------------

    @Override
    public void saveAdditionalSpawnData(final @NonNull FriendlyByteBuf buf) {
        buf.writeVarInt(getUserId());
    }

    @Override
    public void loadAdditionalSpawnData(final @NonNull FriendlyByteBuf buf) {
        entityData.set(USER_ID, buf.readVarInt());
    }
}
