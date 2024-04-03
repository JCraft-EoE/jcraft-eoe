package net.arna.jcraft.client.rendering.skybox;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;

public class SkyBoxManager implements ClientTickEvents.EndWorldTick {
    private static final SkyBoxManager INSTANCE = new SkyBoxManager();

    private JSkyBox currentSkyBox = null;
    private boolean enabled = true;

    @Override
    public void onEndTick(ClientWorld world) {

    }

    public static SkyBoxManager getInstance() {
        return INSTANCE;
    }

    public void clearSkyBox() {
        this.currentSkyBox = null;
    }

    public JSkyBox getCurrentSkybox() {
        return currentSkyBox;
    }

    public void setCurrentSkyBox(JSkyBox skyBox) {
        this.currentSkyBox = skyBox;
    }

    public void renderSkyBox(MatrixStack matrices, Matrix4f matrix4f, float tickDelta, Camera camera, boolean thickFog) {
        currentSkyBox.render(matrices, matrix4f, tickDelta, camera, thickFog);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
