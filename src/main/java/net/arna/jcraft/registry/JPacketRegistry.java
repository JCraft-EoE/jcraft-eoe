package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.util.Identifier;

public interface JPacketRegistry {
    //todo: probably shorten these because strings suck ASS for network efficiency
    Identifier S2C_SERVER_CHANNEL_FEEDBACK = JCraft.id("sfchannel");
    Identifier S2C_PLAYER_ANIMATION = JCraft.id("anim_packet");
    Identifier S2C_SHADER_ACTIVATION = JCraft.id("shader_packet");
    Identifier S2C_SHADER_DEACTIVATION = JCraft.id("shader_deact_packet");
    Identifier S2C_TIME_ACCELERATION_STATE = JCraft.id("time_accel_state");
    Identifier S2C_EPITAPH_STATE = JCraft.id("epitaph_state");
    Identifier S2C_TIME_ERASE_PREDICTION_STATE = JCraft.id("te_prediction_state");
    Identifier S2C_SERVER_CONFIG = JCraft.id("server_config");
    Identifier S2C_J_EXPLOSION = JCraft.id("explosion");
    Identifier S2C_COMBO_COUNTER = JCraft.id("combo_counter");
    Identifier S2C_TIME_STOP = JCraft.id("time_stop");
    Identifier S2C_SPLATTER = JCraft.id("splatter");
    Identifier S2C_STAND_HURT = JCraft.id("stand_hurt");

    Identifier C2S_STAND_BLOCK = JCraft.id("stand_block");
    Identifier C2S_COOLDOWN_CANCEL = JCraft.id("cooldown_cancel");
    Identifier C2S_PLAYER_INPUT = JCraft.id("player_input");
    Identifier C2S_REMOTE_STAND_INTERACT = JCraft.id("remote_stand_interact");
    Identifier C2S_PREDICTION_TRIGGER = JCraft.id("prediction_trigger");
}
