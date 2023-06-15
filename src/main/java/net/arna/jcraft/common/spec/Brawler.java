package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class Brawler extends JCraftSpec {
    public static final Attack heavy = new Attack(0, 17, 1f, 21, 10, 1.5, 6f, 0.8f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.upct")
            .setHitspark(2)
            .setArmor(true);
    public static final Attack low = new Attack(1, 20, 1f, 18, 11, 1.5, 5f, 0.6f, AttackType.BOX, 0.80f, 1, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.low");
    public static final Attack combo = new Attack(2, 22, 1f, 26, 0, 1.5, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(5, 10, 19), JSoundRegister.IMPACT_2)
            .setAnimation("br.3hit");
    public static final Attack gut = new Attack(3, 20, 1f, 18, 11, 1.5, 6f, 0.8f, AttackType.BOX, 0.80f, 0, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.gut")
            .setHitspark(2);

    @Override
    public void InitHeavyAttack(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, heavy, JCraft.heavyCD);
    }

    @Override
    public void InitBarrage(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, combo, JCraft.barrageCD);
    }

    @Override
    public void InitSpecial1(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, gut, JCraft.s1CD);
    }

    @Override
    public void InitSpecial2(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, low, JCraft.s2CD);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> hurt) {
        if (attack == low) {
            for (LivingEntity ent : hurt)
                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 25, 0, true, true));
        }
    }
}
