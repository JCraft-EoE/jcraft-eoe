package net.arna.jcraft.common.attack.moves.cream;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

import java.util.Set;

public class SurpriseMove extends AbstractMove<SurpriseMove, CreamEntity> {
    public static final MoveVariable<Vector3f> OUT_POS = new MoveVariable<>(Vector3f.class);
    public static final MoveVariable<Vector3f> OUT_DIR = new MoveVariable<>(Vector3f.class);

    public SurpriseMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void onInitiate(CreamEntity attacker) {
        super.onInitiate(attacker);

        // OUT_POS are set in .withInitAction() in CreamEntity.java

        attacker.setFree(true);
        attacker.setFreePos(attacker.getUserOrThrow().getPos().toVector3f());
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setCharging(true);

        // OUT_DIR is set in .withAction() in CreamEntity.java

        ctx.get(OUT_POS).sub(ctx.get(OUT_DIR));
        var outPos = ctx.get(OUT_POS);
        attacker.setPosition(new Vec3d(outPos.x(), outPos.y(), outPos.z()));
        attacker.setFreePos(outPos);

        attacker.setVoidTime(getWindupPoint());

        attacker.playSound(JSoundRegistry.IMPACT_5, 1, 0.75f);

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(OUT_POS, new Vector3f());
        ctx.register(OUT_DIR, new Vector3f());
    }

    @Override
    protected @NonNull SurpriseMove getThis() {
        return this;
    }

    @Override
    public @NonNull SurpriseMove copy() {
        return copyExtras(new SurpriseMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
