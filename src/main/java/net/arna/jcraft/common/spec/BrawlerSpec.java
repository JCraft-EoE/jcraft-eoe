package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackQueue;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class BrawlerSpec extends JCraftSpec {
    public static final Attack heavy = new Attack(0, 17, 1f, 21, 10, 1.5, 6f, 0.8f, AttackType.BOX, 0.75f, 0, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.upct")
            .setHitspark(2)
            .hyperArmor()
            .setInfo("Uppercut", "uninterruptable, medium speed");
    public static final Attack combo = new Attack(1, 22, 1f, 26, 0, 1.5, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(5, 10, 19), JSoundRegister.IMPACT_2)
            .setAnimation("br.3hit")
            .setInfo("Combo", "hits 3 times, combo starter/extender");
    public static final Attack gut = new Attack(2, 20, 1f, 18, 11, 1.5, 6f, 0.8f, AttackType.BOX, 0.80f, 0, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.gut")
            .setHitspark(2)
            .setInfo("Gut Punch", "good stun", AttackQueue.SPECIAL1);
    public static final Attack low = new Attack(3, 20, 1f, 18, 11, 1.5, 5f, 0.6f, AttackType.BOX, 0.80f, 1, 0, JSoundRegister.IMPACT_2)
            .setAnimation("br.low")
            .setInfo("Sweep", "knocks down", AttackQueue.SPECIAL2);

    // Info
    @Override
    public String getInternalName() {
        return "brawler";
    }
    @Override
    public String getDescription() {
        return "Close-range pressure and combo extension tool";
    }
    @Override
    public String getDetails() {
        return "Important hitconfirm in the form of (any stand move)~stand.OFF>Combo>stand.ON+(any stand move)";
    }
    @Override
    public List<Attack> getAttacks() {
        return List.of(heavy, combo, gut, low);
    }
    @Override
    public int getId() {
        return 1;
    }

    // Attacks
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
        if (attack.id == low.id)
            for (LivingEntity ent : hurt)
                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 25, 0, true, true));
    }
}
