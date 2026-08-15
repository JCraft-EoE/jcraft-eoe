package net.arna.jcraft.api.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

public interface JPacketRegistry {

    // Shortened strings for the sake of saving bandwidth.
    // Further optimization would probably just be an int to char conversion + a static registration method.
    ResourceLocation S2C_SERVER_CHANNEL_FEEDBACK = JCraft.id("sfc");
    ResourceLocation S2C_PLAYER_ANIMATION = JCraft.id("anim");
    ResourceLocation S2C_SHADER_ACTIVATION = JCraft.id("s_act");
    ResourceLocation S2C_SHADER_DEACTIVATION = JCraft.id("s_dct");
    ResourceLocation S2C_TIME_ACCELERATION_STATE = JCraft.id("t_acl");
    ResourceLocation S2C_EPITAPH_STATE = JCraft.id("epth");
    ResourceLocation S2C_TIME_ERASE_PREDICTION_STATE = JCraft.id("te_prdct");
    ResourceLocation S2C_SERVER_CONFIG = JCraft.id("s_config");
    ResourceLocation S2C_J_EXPLOSION = JCraft.id("expl");
    ResourceLocation S2C_COMBO_COUNTER = JCraft.id("combo");
    ResourceLocation S2C_TIME_STOP = JCraft.id("ts");
    ResourceLocation S2C_SPLATTER = JCraft.id("splt");
    ResourceLocation S2C_STAND_HURT = JCraft.id("stnd_hurt");
    ResourceLocation S2C_PREDICTION_UPDATE = JCraft.id("prdct");
    ResourceLocation S2C_MAGNETIC_FIELD_PARTICLE = JCraft.id("mfp");
    ResourceLocation S2C_ATTACKER_DATA = JCraft.id("atk_data");
    ResourceLocation S2C_MANDOM_DATA = JCraft.id("mndm_data");
    ResourceLocation S2C_STONE_MASK_CLENCH = JCraft.id("msk_clch");
    ResourceLocation S2C_IPS_TRIGGERED = JCraft.id("ips");
    ResourceLocation S2C_DAMAGE_NUMBER = JCraft.id("dmg_no");
    ResourceLocation S2C_BLOCK_BREAKAGE = JCraft.id("bb");
    ResourceLocation S2C_BOUND_SOUND = JCraft.id("bs");

    ResourceLocation C2S_STAND_BLOCK = JCraft.id("stnd_blk");
    ResourceLocation C2S_COOLDOWN_CANCEL = JCraft.id("cdc");
    ResourceLocation C2S_PLAYER_INPUT = JCraft.id("plr_input");
    ResourceLocation C2S_PLAYER_INPUT_HOLD = JCraft.id("plr_input_h");
    ResourceLocation C2S_REMOTE_STAND_INTERACT = JCraft.id("rmt_stnd_act");
    ResourceLocation C2S_PREDICTION_TRIGGER = JCraft.id("prdct_trig");
    ResourceLocation C2S_MENU_CALL = JCraft.id("menu");
    ResourceLocation C2S_GUN_AIM = JCraft.id("gun_aim");

    ResourceLocation S2C_SYNC = JCraft.id("s2c_sync");
    ResourceLocation C2S_SYNC = JCraft.id("c2s_sync");
}
