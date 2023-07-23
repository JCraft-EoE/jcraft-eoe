package net.arna.jcraft.common.gravity.config;

// TODO probably incorporate this into JCraft config.
// Some variables can get a static value. E.g. resetGravityOnRespawn will likely always need to be true.
//@Config(name ="gravity_api")
public class GravityChangerConfig {
    //    @ConfigEntry.Gui.PrefixText
//    @ConfigEntry.Gui.Excluded
    private boolean client;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean keepWorldLook = false;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public int rotationTime = 500;

    //    @ConfigEntry.Gui.PrefixText
//    @ConfigEntry.Gui.Excluded
    private boolean server;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean worldVelocity = false;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean resetGravityOnDimensionChange = true;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean resetGravityOnRespawn = true;

    //    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean voidDamageAboveWorld = false;
}
