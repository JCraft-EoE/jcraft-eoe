package net.arna.jcraft.common.component.world;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import lombok.Data;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface ShockwaveHandlerComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    void addShockwave(double x, double y, double z, float pitch, float yaw, float scale);

    default void addShockwave(double x, double y, double z, float pitch, float yaw) {
        addShockwave(x, y, z, pitch, yaw, 1.0f);
    }

    default void addShockwave(Vec3d pos, float pitch, float yaw, float scale) {
        addShockwave(pos.x, pos.y, pos.z, pitch, yaw, scale);
    }

    default void addShockwave(Vec3d pos, float pitch, float yaw) {
        addShockwave(pos.x, pos.y, pos.z, pitch, yaw);
    }

    default void addShockwave(Vec3d pos, Vec3d rotation, float scale) {
        Vec2f polarRot = JUtils.rotationVectorToPolar(rotation);
        addShockwave(pos.x, pos.y, pos.z, polarRot.x, polarRot.y, scale);
    }

    default void addShockwave(Vec3d pos, Vec3d rotation) {
        Vec2f polarRot = JUtils.rotationVectorToPolar(rotation);
        addShockwave(pos.x, pos.y, pos.z, polarRot.x, polarRot.y);
    }

    List<Shockwave> getShockwaves();

    @Data
    class Shockwave {
        public static final int MAX_AGE = 6;
        public final double x, y, z;
        public final BlockPos blockPos;
        public final float pitch, yaw, scale;
        private int age;

        public Shockwave(double x, double y, double z, float pitch, float yaw, float scale, int age) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockPos = BlockPos.ofFloored(x, y, z);
            this.pitch = pitch;
            this.yaw = yaw;
            this.scale = scale;
            this.age = age;
        }

        public Shockwave(double x, double y, double z, float pitch, float yaw, float scale) {
            this(x, y, z, pitch, yaw, scale, 0);
        }

        public void tick() {
            age++;
        }

        // Currently just the age, but this might change.
        public int getFrame() {
            return Math.min(age, MAX_AGE - 1);
        }
    }
}
