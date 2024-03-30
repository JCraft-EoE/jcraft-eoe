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
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;

import java.util.Set;

public class SurpriseMove extends AbstractMove<SurpriseMove, CreamEntity> {
    public static final MoveVariable<Vec3f> OUT_POS = new MoveVariable<>(Vec3f.class);
    public static final MoveVariable<Vec3f> OUT_DIR = new MoveVariable<>(Vec3f.class);

    public SurpriseMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void onInitiate(CreamEntity attacker) {
        super.onInitiate(attacker);

        // OUT_POS are set in .withInitAction() in CreamEntity.java

        attacker.setFree(true);
        attacker.setFreePos(new Vec3f(attacker.getUserOrThrow().getPos()));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setCharging(true);

        // OUT_DIR is set in .withAction() in CreamEntity.java

        ctx.get(OUT_POS).subtract(ctx.get(OUT_DIR));
        Vec3f outPos = ctx.get(OUT_POS);
        attacker.setPosition(new Vec3d(outPos.getX(), outPos.getY(), outPos.getZ()));
        attacker.setFreePos(outPos);

        attacker.setVoidTime(getWindupPoint());

        attacker.playSound(JSoundRegistry.IMPACT_5, 1, 0.75f);

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(OUT_POS, new Vec3f());
        ctx.register(OUT_DIR, new Vec3f());
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
