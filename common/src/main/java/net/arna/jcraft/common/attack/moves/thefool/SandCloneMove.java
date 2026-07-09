package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Set;

public final class SandCloneMove extends AbstractMove<SandCloneMove, TheFoolEntity> {
    private WeakReference<Mob> sandClone = new WeakReference<>(null);

    public SandCloneMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<SandCloneMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final TheFoolEntity attacker) {
        if (attacker.hasUser()) {
            tickClone(attacker);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final TheFoolEntity attacker, final LivingEntity user) {
        final Vec3 pos = user.getEyePosition();

        // Display sand effect
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeShort(11);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(2);

        Collection<ServerPlayer> nearby = JUtils.around((ServerLevel) attacker.level(), pos, 128);

        for (ServerPlayer serverPlayer : nearby) {
            if (serverPlayer == user) continue;
            if (serverPlayer.closerThan(user, 4)) { // Blind players caught in the cloud
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
            }
        }

        ServerChannelFeedbackPacket.send(nearby, buf);
        Mob newClone = null;

        if (user.isShiftKeyDown()) {
            for (int i = 0; i < 32; i++) {
                double y = 0.4;
                double h = i * 3.1415 / 8;
                double hDiv = 5;
                if (i >= 16) {
                    y = 0.8;
                    hDiv = 9.5;
                }
                TheFoolEntity.createFoolishSand(attacker.level(), attacker, attacker.blockPosition(),
                        new Vec3(Math.sin(h) / hDiv, y, Math.cos(h) / hDiv));
            }
            return Set.of();
        }

        // Summon clone
        if (user instanceof ServerPlayer player) {
            final PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(attacker.level());
            playerCloneEntity.copyPosition(player);
            playerCloneEntity.setMaster(player);
            playerCloneEntity.markSand();

            playerCloneEntity.setItemSlot(EquipmentSlot.HEAD, user.getItemBySlot(EquipmentSlot.HEAD).copy());
            playerCloneEntity.setItemSlot(EquipmentSlot.CHEST, user.getItemBySlot(EquipmentSlot.CHEST).copy());
            playerCloneEntity.setItemSlot(EquipmentSlot.LEGS, user.getItemBySlot(EquipmentSlot.LEGS).copy());
            playerCloneEntity.setItemSlot(EquipmentSlot.FEET, user.getItemBySlot(EquipmentSlot.FEET).copy());

            playerCloneEntity.setAllowItemExchange(false);
            playerCloneEntity.disableDrops();

            setSandClone(newClone = playerCloneEntity);
        } else if (user instanceof Mob mob) {
            setSandClone(newClone = JUtils.mobCloneOf(mob));
        }

        if (newClone != null) {
            attacker.level().addFreshEntity(newClone);
        }

        return Set.of();
    }

    public void tickClone(final TheFoolEntity attacker) {
        final Mob sandClone = this.sandClone.get();
        if (sandClone != null && sandClone.tickCount > 200) {
            setSandClone(null);
        }
    }

    public void discardClone(final TheFoolEntity attacker) {
        Mob clone = sandClone.get();
        if (clone != null) {
            clone.discard();
        }
    }

    private void setSandClone(Mob clone) {
        Mob currentSandClone = sandClone.get();
        if (currentSandClone != null) {
            currentSandClone.kill();
        }
        sandClone = new WeakReference<>(clone);
        if (clone == null) {
            return;
        }
        JComponentPlatformUtils.getStandComponent(clone).setType(JStandTypeRegistry.NONE.get());
        applySandCloneModifiers(clone);
    }

    public static void applySandCloneModifiers(LivingEntity entity) {
        if (entity == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to invalid entity!");
            return;
        }
        final AttributeInstance maxHealthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to entity with no max health attribute!");
            return;
        }

        maxHealthAttribute.addPermanentModifier(
                new AttributeModifier("Sand Clone Max Health Modifier", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL)
        );
    }

    @Override
    protected @NonNull SandCloneMove getThis() {
        return this;
    }

    @Override
    public @NonNull SandCloneMove copy() {
        return copyExtras(new SandCloneMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<SandCloneMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SandCloneMove>, SandCloneMove> buildCodec(RecordCodecBuilder.Instance<SandCloneMove> instance) {
            return baseDefault(instance, SandCloneMove::new);
        }
    }
}
