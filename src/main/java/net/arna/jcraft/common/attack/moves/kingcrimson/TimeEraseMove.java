package net.arna.jcraft.common.attack.moves.kingcrimson;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.core.ctx.WeakMoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;

import java.util.Set;

@Getter
public class TimeEraseMove extends AbstractMove<TimeEraseMove, KingCrimsonEntity> {
    public static final MoveVariable<MobEntity> DOPPELGANGER = new WeakMoveVariable<>(MobEntity.class);
    private final int erasureDuration;

    public TimeEraseMove(int cooldown, int windup, int duration, float moveDistance, int erasureDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.erasureDuration = erasureDuration;
    }

    @Override
    public void onInitiate(KingCrimsonEntity attacker) {
        super.onInitiate(attacker);

        if (attacker.getUser() instanceof ServerPlayerEntity player)
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(JSoundRegistry.TIME_ERASE), SoundCategory.PLAYERS,
                    attacker.getX(), attacker.getY(), attacker.getZ(), 1, 1, 0));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KingCrimsonEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setTETime(erasureDuration);

        attacker.curMove = null;
        MobEntity doppelganger = null;

        if (user instanceof ServerPlayerEntity player) {
            // Shader handling
            ShaderActivationPacket.send(player, attacker, 0, 120, ShaderActivationPacket.Type.CRIMSON);

            PlayerCloneEntity clone = new PlayerCloneEntity(attacker.getWorld());
            clone.setShouldRenderForMaster(false);
            clone.disableDrops();
            clone.disableItemExchange();

            // Copy properties
            clone.setMaster(player);

            doppelganger = clone;
        } else if (user instanceof MobEntity mob)
            doppelganger = JUtils.mobCloneOf(mob);

        ctx.set(DOPPELGANGER, doppelganger);
        if (doppelganger == null) return Set.of();

        // Copy rotation
        doppelganger.copyPositionAndRotation(user);
        doppelganger.setHeadYaw(user.getHeadYaw());
        doppelganger.setBodyYaw(user.getBodyYaw());

        // Copy equipment
        doppelganger.equipStack(EquipmentSlot.MAINHAND, user.getMainHandStack().copy());
        doppelganger.equipStack(EquipmentSlot.OFFHAND, user.getOffHandStack().copy());
        doppelganger.equipStack(EquipmentSlot.HEAD, user.getEquippedStack(EquipmentSlot.HEAD).copy());
        doppelganger.equipStack(EquipmentSlot.CHEST, user.getEquippedStack(EquipmentSlot.CHEST).copy());
        doppelganger.equipStack(EquipmentSlot.LEGS, user.getEquippedStack(EquipmentSlot.LEGS).copy());
        doppelganger.equipStack(EquipmentSlot.FEET, user.getEquippedStack(EquipmentSlot.FEET).copy());

        // Copy health and make immortal
        doppelganger.setHealth(user.getHealth());
        doppelganger.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 32767, 9, true, false));

        // Set and summon King Crimson replica, make it block forever
        summonFakeKC(attacker);

        // Look at enemy
        doppelganger.setTarget(user.getAttacker());

        attacker.getWorld().spawnEntity(doppelganger);

        return Set.of();
    }

    private void summonFakeKC(KingCrimsonEntity attacker) {
        MobEntity doppelganger = attacker.getMoveContext().get(DOPPELGANGER);
        StandComponent standData = JComponents.getStandData(doppelganger);
        standData.setTypeAndSkin(attacker.getStandType(), attacker.getSkin());

        StandEntity<?, ?> clone = JCraft.summon(attacker.getWorld(), doppelganger);
        if (clone == null) return;

        clone.blocking = true;
        clone.setMoveStun(32767);
        clone.setSilent(true);
    }

    public void tickTimeErase(KingCrimsonEntity attacker) {
        if (!attacker.hasUser()) return;

        LivingEntity user = attacker.getUserOrThrow();
        int teTime = attacker.getTETime();
        if (teTime > 0) {
            attacker.setTETime(--teTime);

            if (attacker.blocking || attacker.curMove != null && attacker.getMoveStun() < attacker.curMove.getWindupPoint() * 2 / 3)
                attacker.cancelTE();

            // Invulnerability and invisibility
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 9, true, false));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, true, false));
            // Inability to be stunned
            user.removeStatusEffect(JStatusRegistry.DAZED);
            // Inability to be hit (by projectiles)
            Box noBox = new Box(0, 0, 0, 0, 0, 0);
            user.setBoundingBox(noBox);
            user.noClip = true;

            if (teTime <= 0) {
                // Play exit noise
                if (user instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(JSoundRegistry.TIME_ERASE_EXIT),
                            SoundCategory.PLAYERS, attacker.getX(), attacker.getY(), attacker.getZ(), 1, 1, 0));

                /* Return targets to position
                for (TimeEraseData timeEraseData : timeEraseInfo) {
                    Vec3d tePos = timeEraseData.getPosition();
                    timeEraseData.getEntity().teleport(tePos.x, tePos.y, tePos.z);
                }
                 */
            }
        }

        MobEntity doppelganger = attacker.getMoveContext().get(TimeEraseMove.DOPPELGANGER);
        if (teTime <= 0 && doppelganger != null) // Doppelgänger disappears at the end of Time Erase
            doppelganger.discard();

        attacker.setSilent(teTime > 0);

        if (user.hasCustomName()) user.setCustomNameVisible(teTime <= 0);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(DOPPELGANGER);
    }

    @Override
    protected @NonNull TimeEraseMove getThis() {
        return this;
    }

    @Override
    public @NonNull TimeEraseMove copy() {
        return copyExtras(new TimeEraseMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getErasureDuration()));
    }
}
