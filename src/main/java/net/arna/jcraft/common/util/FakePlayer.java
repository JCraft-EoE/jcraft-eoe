package net.arna.jcraft.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;

import java.util.UUID;

public class FakePlayer extends ServerPlayerEntity {
    private static final GameProfile FAKE_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("jcraft".getBytes()), "[JCraft]");

    public FakePlayer(ServerWorld world) {
        super(world.getServer(), world, FAKE_PROFILE);
        this.networkHandler = new ServerPlayNetworkHandler(world.getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), this);
    }

    @Override
    public void tick() {
    }

    @Override
    public void increaseStat(Stat<?> stat, int incrementer) {
    }

    @Override
    public void incrementStat(Stat<?> stat) {
    }

    @Override
    public void sendMessage(Text message, boolean actionBar) {
        super.sendMessage(message, actionBar);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
}
