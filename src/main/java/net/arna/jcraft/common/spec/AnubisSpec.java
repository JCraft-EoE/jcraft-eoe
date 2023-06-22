package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.AttackType;
import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class AnubisSpec extends JCraftSpec {
    public static final Attack slash = new Attack(0, 17, 1f, 20, 9, 1.75, 6f, 0.9f, AttackType.BOX, 0.75f, 0, 0, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .setAnimation("an.slsh")
            .setHitspark(-4)
            .setArmor(true)
            .setInfo("Slash", "uninterruptable get-off-me tool");
    public static final Attack pommel = new Attack(1, 14, 1f, 8, 5, 1.25, 4f, 0.3f, AttackType.BOX, 0.35f, 0, 0, JSoundRegister.IMPACT_3)
            .setAnimation("an.pom")
            .setInfo("Pommel Strike", "speedy counterpoke, usable even when sheathed");
    public static final Attack pommelIn = new Attack(1, 14, 1f, 8, 5, 1.25, 4f, 0.3f, AttackType.BOX, 0.35f, 0, 0, JSoundRegister.IMPACT_3)
            .setAnimation("an.pmi");
    public static final Attack rekkas2 = new Attack(2, 20, 1f, 26, 0, 1.75, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(8, 20), JSoundRegister.IMPACT_4)
            .setAnimation("an.2hit")
            .setInfo("Cleaving Strikes", "2 hits");
    public static final Attack rekkas3 = new Attack(3, 20, 1f, 40, 0, 1.75, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(8, 20, 32), JSoundRegister.IMPACT_4)
            .setAnimation("an.3hit")
            .setInfo("Cleaving Strikes", "3 hits, last knocks down");
    public static final Attack rekkafinisher = new Attack(4, 0, 1f, 40, 0, 2, 7f, 0.9f, AttackType.MULTIHIT, 0.75f, 0, List.of(32), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .setHitspark(-4)
            .setLaunch();

    // Info
    @Override
    public String getName() {
        return "Anubis";
    }
    @Override
    public String getDescription() {
        return "Counterpoking tool";
    }
    @Override
    public String getDetails() {
        return "untested";
    }
    @Override
    public List<Attack> getAttacks() {
        return List.of(pommel, Attack.unusable,
                slash,
                rekkas2,
                rekkas3
        );
    }

    // Attacks
    @Override
    public void InitHeavyAttack(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, player.isHolding(JObjectRegistry.ANUBIS) ? pommel : pommelIn, JCraft.heavyCD);
    }

    /*
    @Override
    public void InitBarrage(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, barrage, JCraft.barrageCD);
    }
     */

    @Override
    public void InitSpecial1(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (!player.isHolding(JObjectRegistry.ANUBIS)) return;
        handleAttack(serverWorld, slash, JCraft.s1CD);
    }

    @Override
    public void InitSpecial2(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (!player.isHolding(JObjectRegistry.ANUBIS)) return;
        handleAttack(serverWorld, rekkas2, JCraft.s2CD);
    }

    @Override
    public void InitSpecial3(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (!player.isHolding(JObjectRegistry.ANUBIS)) return;
        handleAttack(serverWorld, rekkas3, JCraft.s2CD);
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> hurt) {
        if (attack.id == 3 && moveStun == 20)
            curAttack = rekkafinisher;
        if (attack.id == 4)
            for (LivingEntity ent : hurt)
                if (!JCraftUtils.isBlocking(ent))
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, true));
    }
}
