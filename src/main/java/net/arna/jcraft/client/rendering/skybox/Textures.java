package net.arna.jcraft.client.rendering.skybox;

import com.google.common.collect.Lists;
import net.minecraft.util.Identifier;

import java.util.List;

public class Textures {
    private final List<Texture> textureList = Lists.newArrayList();

    public Textures(Texture north, Texture south, Texture east, Texture west, Texture up, Texture down) {
        this.textureList.add(down);
        this.textureList.add(north);
        this.textureList.add(south);
        this.textureList.add(up);
        this.textureList.add(east);
        this.textureList.add(west);
    }

    public Texture byId(int i) {
        return this.textureList.get(i);
    }

    public static class Texture {
        private final Identifier textureId;
        private final float minU;
        private final float minV;
        private final float maxU;
        private final float maxV;

        public Texture(Identifier textureId, float minU, float minV, float maxU, float maxV) {
            this.textureId = textureId;
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
        }

        public Texture(Identifier textureId) {
            this(textureId, 0.0F, 0.0F, 1.0F, 1.0F);
        }

        public Identifier getTextureId() {
            return this.textureId;
        }

        public float getMinU() {
            return this.minU;
        }

        public float getMaxU() {
            return this.maxU;
        }

        public float getMinV() {
            return this.minV;
        }

        public float getMaxV() {
            return this.maxV;
        }

        public Texture withUV(float minU, float minV, float maxU, float maxV) {
            return new Texture(this.getTextureId(), minU, minV, maxU, maxV);
        }
    }
}
