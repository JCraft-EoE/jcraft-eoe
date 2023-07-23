package net.arna.jcraft.common.spec;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class AnubisSpec extends JCraftSpec {
    public static final Attack slash = new Attack(0, 17, 1f, 20, 9, 1.75, 6f, 0.9f, AttackType.BOX, 0.75f, 0, 0, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .setAnimation("an.slsh")
            .setHitspark(-4)
            .hyperArmor()
            .setInfo("Slash", "uninterruptable get-off-me tool");
    public static final Attack pommel = new Attack(1, 14, 1f, 8, 5, 1.25, 4f, 0.3f, AttackType.BOX, 0.35f, 0, 0, JSoundRegister.IMPACT_3)
            .setAnimation("an.pom")
            .setInfo("Pommel Strike", "speedy counterpoke, usable while sheathed");
    public static final Attack pommelIn = new Attack(1, 14, 1f, 8, 5, 1.25, 4f, 0.3f, AttackType.BOX, 0.35f, 0, 0, JSoundRegister.IMPACT_3)
            .setAnimation("an.pmi");
    public static final Attack rekkas2 = new Attack(2, 20, 1f, 26, 0, 1.75, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(8, 20), JSoundRegister.IMPACT_4)
            .setAnimation("an.2hit")
            .setInfo("Cleaving Strikes", "2 hits");
    public static final Attack rekkas3 = new Attack(3, 20, 1f, 40, 0, 1.75, 4f, 0.6f, AttackType.MULTIHIT, 0.75f, -0.1f, List.of(8, 20, 32), JSoundRegister.IMPACT_4)
            .setAnimation("an.3hit")
            .setInfo("Cleaving Strikes/Sweep", "3 hits, last knocks down/sweeps while sheathed");
    public static final Attack rekkafinisher = new Attack(4, 0, 1f, 40, 0, 2, 7f, 0.9f, AttackType.MULTIHIT, 0.75f, 0, List.of(32), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .setHitspark(-4)
            .setLaunch();
    public static final Attack sweep = new Attack(5, 16, 1.5f, 17, 10, 1.33, 7f, 0.3f, AttackType.BOX, 0.45f, 0, 0, JSoundRegister.IMPACT_3)
            .setAnimation("an.swp")
            .setInfo("Sweep", "");

    // Info
    @Override
    public String getInternalName() {
        return "anubis";
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

    @Override
    public int getId() {
        return 2;
    }

    // Attacks
    @Override
    public void initHeavyAttack(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (handleAttack(serverWorld, player.isHolding(JObjectRegistry.ANUBIS) ? pommel : pommelIn, JCraft.heavyCD))
            JUtils.serverPlaySound(JSoundRegister.ANUBIS_POMMEL, serverWorld, player.getPos());
    }

    /*
    @Override
    public void InitBarrage(ServerWorld serverWorld) {
        if (!canAttack()) return;
        handleAttack(serverWorld, barrage, JCraft.barrageCD);
    }
     */

    @Override
    public void initSpecial1(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (!player.isHolding(JObjectRegistry.ANUBIS)) return;
        if (handleAttack(serverWorld, slash, JCraft.s1CD))
            JUtils.serverPlaySound(JSoundRegister.ANUBIS_SLASH, serverWorld, player.getPos());
    }

    @Override
    public void initSpecial2(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (!player.isHolding(JObjectRegistry.ANUBIS)) return;
        if (handleAttack(serverWorld, rekkas2, JCraft.s2CD))
            JUtils.serverPlaySound(JSoundRegister.ANUBIS_REKKA2, serverWorld, player.getPos());
    }

    @Override
    public void initSpecial3(ServerWorld serverWorld) {
        if (!canAttack()) return;
        if (player.isHolding(JObjectRegistry.ANUBIS) && handleAttack(serverWorld, rekkas3, JCraft.s2CD)) {
            JUtils.serverPlaySound(JSoundRegister.ANUBIS_REKKA3, serverWorld, player.getPos());
        } else {
            handleAttack(serverWorld, sweep, JCraft.s3CD);
            player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.SLOWNESS, sweep.moveStun, 2, true, false)
            );
        }
    }

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> hurt) {
        if (attack.id == 3 && moveStun == 20)
            curAttack = rekkafinisher;
        if (attack.id >= 4) // Rekka finisher or Sweep
            for (LivingEntity ent : hurt)
                if (!JUtils.isBlocking(ent))
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 35, 0, true, true));
    }
}
