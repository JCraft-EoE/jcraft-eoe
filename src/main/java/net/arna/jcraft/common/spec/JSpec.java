package net.arna.jcraft.common.spec;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.old.MoveQueue;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * Class that needs to be instantiated per-player to contain temporary data relating to their current state.
 * Used to handle stand-off attacks.
 */
public abstract class JSpec<A extends JSpec<A, S>, S extends Enum<S> & SpecAnimationState<A>> implements IAttacker<A, S> {
    private final MoveMap<A, S> moveMap = new MoveMap<>();
    @Getter
    private final MoveContext moveContext = new MoveContext();
    public PlayerEntity player;
    @Getter @Setter
    public int moveStun = 0;
    @Getter
    private S state;
    public AbstractMove<?, ? super A> curAttack;
    public AbstractMove<?, ? super A> previousAttack;
    public MoveQueue queuedAttack;
    public int armorPoints = 0;

    protected JSpec() {
        registerMoves(moveMap);
        moveMap.freeze();
        moveMap.forEach(entry -> entry.getMove().registerContextEntries(moveContext));
    }

    // TODO move these methods to SpecType
    public Text getTranslatableName() {
        return Text.translatable("spec.jcraft." + getInternalName());
    }

    public String getInternalName() {
        return "unnamed";
    }

    public String getDescription() {
        return "UNDESCRIBED";
    }

    public String getDetails() {
        return "UNFINISHED";
    }

    public int getId() {
        return 0;
    }

    @Override
    public LivingEntity getUser() {
        return player;
    }

    @Override
    public World getWorld() {
        return player.getWorld();
    }

    @Override
    public LivingEntity getBaseEntity() {
        return player;
    }

    @Override
    public DamageSource getDamageSource() {
        return DamageSource.player(player);
    }

    @Override
    public boolean hasUser() {
        return player != null;
    }

    @Override
    public LivingEntity getUserOrThrow() {
        return Objects.requireNonNull(player, "Player must not be null");
    }

    @Override
    public AbstractMove<?, ? super A> getCurrentMove() {
        return curAttack;
    }

    @Override
    public void setCurrentMove(AbstractMove<?, ? super A> move) {
        previousAttack = curAttack;
        curAttack = move;
    }

    @Override
    public void setState(S state) {
        PlayerLookup.world((ServerWorld) player.getWorld()).forEach(serverPlayer -> PlayerAnimPacket.sendSpec(
                player, serverPlayer, (this.state = state).getKey(getThis()), moveStun, 1f));
    }

    protected abstract void registerMoves(MoveMap<A, S> moves);

    public void initMove(MoveType type) {
        handleMove(type);
    }

    public boolean canAttack() {
        return moveStun <= 0 && !JUtils.isAffectedByTimeStop(player) && !player.hasStatusEffect(JStatusRegistry.DAZED);
    }

    public boolean handleMove(MoveType type) {
        return handleMove(type, 1f);
    }

    public boolean handleMove(MoveType type, float animationSpeed) {
        MoveMap.Entry<A, S> entry = moveMap.getFirstValidEntry(type, getThis());
        if (entry == null) return false;

        if (player.isSneaking() && entry.getCrouchingVariant() != null) entry = entry.getCrouchingVariant();
        return handleMove(entry.getMove(), entry.getCooldownType(), entry.getAnimState(), animationSpeed);
    }

    public boolean handleMove(AbstractMove<?, ? super A> attack, CooldownType cooldownType, S state) {
        return handleMove(attack, cooldownType, state, 1f);
    }

    public boolean handleMove(AbstractMove<?, ? super A> attack, CooldownType cooldownType, S state, float animationSpeed) {
        CooldownsComponent cooldowns = JComponents.getCooldowns(player);
        int cd = cooldowns.getCooldown(cooldownType);
        if (cd > 0) return false;
        cooldowns.setCooldown(cooldownType, attack.getCooldown());

        //JCraft.LOGGER.info("SERVER: Handling spec attack: " + attack + " in world: " + serverWorld);

        curAttack = attack.copy()
                .withDuration((int) (attack.getDuration() / animationSpeed))
                .withWindup((int) (attack.getWindup() / animationSpeed));

        if (curAttack instanceof AbstractMultiHitAttack<?,?> multiHitAttack)
            multiHitAttack.withHitMoments(IntSet.of(multiHitAttack.getHitMoments().intStream()
                    .map(i -> (int) (i / animationSpeed))
                    .toArray()));

        armorPoints = attack.getArmor();

        PlayerLookup.world((ServerWorld) player.getWorld()).forEach(serverPlayer -> PlayerAnimPacket.sendSpec(
                player, serverPlayer, (this.state = state).getKey(getThis()), moveStun, animationSpeed));
        return true;
    }

    public void cancelAttack() {
        curAttack = null;
        queuedAttack = null;
        moveStun = 0;

        if (player == null) return;
        // Cancel player animation if it exists
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(13);
        buf.writeInt(player.getId());
        ServerWorld serverWorld = (ServerWorld) player.getWorld();
        for (ServerPlayerEntity sendPlayer : serverWorld.getPlayers())
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
    }

    public boolean shouldSneak() {
        return false;
    }

    public void processAttackClient() {}

    public void tickSpec() {
        if (player.isSpectator()) return;

        World world = player.getWorld();

        if (world.isClient()) {
            //JCraft.LOGGER.info("CLIENT: Ticking spec " + this);

            if (moveStun > 0) {
                //JCraft.LOGGER.info("CLIENT: Movestun is " + moveStun);

                player.setSneaking(shouldSneak());

                // Process attack
                moveStun--;
                processAttackClient();
            }

            return;
        }

        //JCraft.LOGGER.info("SERVER: Ticking spec " + this);

        if (moveStun <= 0) {
            if (queuedAttack != null) {
                initMove(queuedAttack.getMoveType());
                queuedAttack = null;
            }

            if (curAttack != previousAttack && curAttack != null) previousAttack = curAttack;
            return;
        }

        //JCraft.LOGGER.info("SERVER: Movestun is " + moveStun);

        // Likely will be changed later, but at the moment this serves to prevent animations breaking
        player.setSneaking(shouldSneak());

        // Process attack
        AbstractMove<?, ? super A> attack = this.curAttack;
        moveStun--;
        if (attack != null) attack.tick(getThis());
    }

    protected abstract A getThis();
}
