package net.arna.jcraft.common.ai;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

public class AttackerBrainInfo {
    public enum State {
        IDLE,
        // Approaching target
        APPROACH,
        // Hitting blocking target
        PRESSURE,
        // Actively hitting target
        COMBOING,
        // Repositioning, soft defense
        DISENGAGE,
        // Zoning
        KEEPAWAY,
        // Blocking
        DEFENSE,
        // Hit
        COMBOED,
    }

    @Getter
    private final CombatInstantContext combatCtx;

    /**
     * The level of combat competency of this individual. 0 means disabled.
     */
    @Getter
    private final int aiLevel;
    @Getter
    private State state = State.IDLE;
    @Getter @Setter
    private int ticksSinceStateChange = 0;
    @Getter @Setter
    private int reactionTime = 0;
    @Getter @Setter
    private Vec3 awayPos = null;
    @Getter @Setter
    private int desiredStandOffTime = 0;
    @Setter
    private int desiredNoAttackTime = 0;
    public boolean desiresNoAttack() {
        return desiredNoAttackTime > 0;
    }

    public void setState(@NonNull State state) {
        if (this.state != state) this.ticksSinceStateChange = 0;
        this.state = state;
    }

    public void tick() {
        ticksSinceStateChange++;
        desiredStandOffTime--;
        desiredNoAttackTime--;
    }

    public AttackerBrainInfo(int aiLevel) {
        this.aiLevel = aiLevel;
        this.combatCtx = new CombatInstantContext();
    }
}
