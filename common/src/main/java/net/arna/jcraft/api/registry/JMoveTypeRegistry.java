package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.common.attack.moves.aerosmith.BombDropAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.ItemDropAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.MuzzleHitscanAttack;
import net.arna.jcraft.common.attack.moves.aerosmith.PatrolMove;
import net.arna.jcraft.common.attack.moves.anubis.*;
import net.arna.jcraft.common.attack.moves.cmoon.*;
import net.arna.jcraft.common.attack.moves.cream.*;
import net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap.*;
import net.arna.jcraft.common.attack.moves.goldexperience.BerryBushAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.LifeGiverAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.OverclockAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.TreeAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.requiem.*;
import net.arna.jcraft.common.attack.moves.hamon.*;
import net.arna.jcraft.common.attack.moves.hierophantgreen.EmeraldSplashAttack;
import net.arna.jcraft.common.attack.moves.hierophantgreen.NetSetMove;
import net.arna.jcraft.common.attack.moves.horus.*;
import net.arna.jcraft.common.attack.moves.killerqueen.*;
import net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust.*;
import net.arna.jcraft.common.attack.moves.kingcrimson.*;
import net.arna.jcraft.common.attack.moves.madeinheaven.*;
import net.arna.jcraft.common.attack.moves.magiciansred.*;
import net.arna.jcraft.common.attack.moves.mandom.CountdownMove;
import net.arna.jcraft.common.attack.moves.mandom.RewindMove;
import net.arna.jcraft.common.attack.moves.metallica.*;
import net.arna.jcraft.common.attack.moves.purplehaze.*;
import net.arna.jcraft.common.attack.moves.purplehaze.distortion.DistortionMove;
import net.arna.jcraft.common.attack.moves.shadowtheworld.ImpalingThrustAttack;
import net.arna.jcraft.common.attack.moves.shadowtheworld.STWChargeAttack;
import net.arna.jcraft.common.attack.moves.shadowtheworld.STWCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.silverchariot.*;
import net.arna.jcraft.common.attack.moves.starplatinum.BlockBreakingAttack;
import net.arna.jcraft.common.attack.moves.starplatinum.InhaleAttack;
import net.arna.jcraft.common.attack.moves.starplatinum.theworld.SPTWGroundSlamAttack;
import net.arna.jcraft.common.attack.moves.starplatinum.theworld.TimeStrikeAttack;
import net.arna.jcraft.common.attack.moves.thefool.*;
import net.arna.jcraft.common.attack.moves.thehand.*;
import net.arna.jcraft.common.attack.moves.thesun.FireMeteorAttack;
import net.arna.jcraft.common.attack.moves.thesun.FireSunBeamAttack;
import net.arna.jcraft.common.attack.moves.thesun.MeteorShowerAttack;
import net.arna.jcraft.common.attack.moves.theworld.FeignBarrageCounterAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWChargeAttack;
import net.arna.jcraft.common.attack.moves.theworld.TWDonutAttack;
import net.arna.jcraft.common.attack.moves.theworld.overheaven.*;
import net.arna.jcraft.common.attack.moves.vampire.*;
import net.arna.jcraft.common.attack.moves.whitesnake.ChargedSpewAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.GiveStandAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.MeltYourHeartAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.PoisonSpewAttack;
import net.arna.jcraft.common.attack.moves.whitesnake.StealStandAttack;

public interface JMoveTypeRegistry {
    DeferredRegister<MoveType<?>> MOVE_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.MOVE_TYPE_REGISTRY_KEY);

    RegistrySupplier<MoveType<?>> ANUBIS_LOW_KICK_ATTACK = register("anubis_low_kick_attack", LowKickAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> ANUBIS_REKKA_3_ATTACK = register("anubis_rekka_3_attack", Rekka3Attack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> ANUBIS_SIMPLE_ANUBIS_ATTACK = register("anubis_simple_anubis_attack", SimpleAnubisAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> ANUBIS_SIMPLE_ANUBIS_MULTI_HIT_ATTACK = register("anubis_simple_anubis_multi_hit_attack", SimpleAnubisMultiHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> ANUBIS_UNSHEATHING_ATTACK = register("anubis_unsheathing_attack", UnsheathingAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> CMOON_GRAVITATIONAL_HOP_MOVE = register("cmoon_gravitational_hop_move", GravitationalHopMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CMOON_GRAVITY_SHIFT_MOVE = register("cmoon_gravity_shift_move", GravityShiftMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CMOON_GRAVITY_SHIFT_PULSE_MOVE = register("cmoon_gravity_shift_pulse_move", GravityShiftPulseMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CMOON_GRAV_PUNCH_ATTACK = register("cmoon_grav_punch_attack", GravPunchAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CMOON_GROUND_SLAM_ATTACK = register("cmoon_ground_slam_attack", CGroundSlamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CMOON_LAUNCH_ATTACK = register("cmoon_launch_attack", LaunchAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> CREAM_BALL_CHARGE_ATTACK = register("cream_ball_charge_attack", BallChargeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_BALL_MODE_ENTER_MOVE = register("cream_ball_mode_enter_move", BallModeEnterMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_BALL_MODE_EXIT_MOVE = register("cream_ball_mode_exit_move", BallModeExitMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_CONSUME_ATTACK = register("cream_consume_attack", ConsumeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_CREAM_COMBO_ATTACK = register("cream_cream_combo_attack", CreamComboAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_DESTROY_ATTACK = register("cream_destroy_attack", DestroyAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_DETACH_CHARGE_MOVE = register("cream_detach_charge_move", DetachChargeMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> CREAM_SURPRISE_MOVE = register("cream_surprise_move", SurpriseMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_CLONE_SPAWN_MOVE = register("dirtydeedsdonedirtcheap_clone_spawn_move", CloneSpawnMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_D_4C_COUNTER_ATTACK = register("dirtydeedsdonedirtcheap_d_4c_counter_attack", D4CCounterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_D_4C_GRAB_ATTACK = register("dirtydeedsdonedirtcheap_d_4c_grab_attack", D4CGrabAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_DIMENSIONAL_HOP_MOVE = register("dirtydeedsdonedirtcheap_dimensional_hop_move", DimensionalHopMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_FLAG_MOVE = register("dirtydeedsdonedirtcheap_flag_move", FlagMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_GIVE_GUN_MOVE = register("dirtydeedsdonedirtcheap_give_gun_move", GiveGunMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> DIRTYDEEDSDONEDIRTCHEAP_ITEM_PLACE_MOVE = register("dirtydeedsdonedirtcheap_item_place_move", ItemPlaceMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_BERRY_BUSH_ATTACK = register("goldexperience_berry_bush_attack", BerryBushAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_LIFE_GIVER_ATTACK = register("goldexperience_life_giver_attack", LifeGiverAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_OVERCLOCK_ATTACK = register("goldexperience_overclock_attack", OverclockAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_TREE_ATTACK = register("goldexperience_tree_attack", TreeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_REQUIEM_FLIGHT_MOVE = register("goldexperience_requiem_flight_move", FlightMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_REQUIEM_LIFE_BEAM_ATTACK = register("goldexperience_requiem_life_beam_attack", LifeBeamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_REQUIEM_NULLIFICATION_ATTACK = register("goldexperience_requiem_nullification_attack", NullificationAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_REQUIEM_OVERHEAD_KICK_ATTACK = register("goldexperience_requiem_overhead_kick_attack", OverheadKickAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> GOLDEXPERIENCE_REQUIEM_RETURN_TO_ZERO_MOVE = register("goldexperience_requiem_return_to_zero_move", ReturnToZeroMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> HIEROPHANTGREEN_EMERALD_SPLASH_ATTACK = register("hierophantgreen_emerald_splash_attack", EmeraldSplashAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HIEROPHANTGREEN_NET_SET_MOVE = register("hierophantgreen_net_set_move", NetSetMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> HORUS_CHASING_FREEZE_ATTACK = register("horus_chasing_freeze_attack", ChasingFreezeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_DETONATE_ATTACK = register("horus_detonate_attack", HorusDetonateAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_HORUS_BARRAGE_ATTACK = register("horus_horus_barrage_attack", HorusBarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_HORUS_DIVEKICK_ATTACK = register("horus_horus_divekick_attack", HorusDivekickAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_ICE_LANCE_ATTACK = register("horus_ice_lance_attack", IceLanceAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_ICICLE_FIRE_ATTACK = register("horus_icicle_fire_attack", IcicleFireAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_PERFECT_FREEZE_ATTACK = register("horus_perfect_freeze_attack", PerfectFreezeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_SCATTER_ATTACK = register("horus_scatter_attack", ScatterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HORUS_STOMP_ATTACK = register("horus_stomp_attack", StompAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> KILLERQUEEN_BOMB_PLANT_ATTACK = register("killerqueen_bomb_plant_attack", BombPlantAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_COIN_TOSS_MOVE = register("killerqueen_coin_toss_move", CoinTossMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_DETONATE_ATTACK = register("killerqueen_detonate_attack", KQDetonateAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_EXPLOSIVE_DASH_ATTACK = register("killerqueen_explosive_dash_attack", ExplosiveDashAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_KQ_GRAB_ATTACK = register("killerqueen_kq_grab_attack", KQGrabAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_KQ_GRAB_HIT_ATTACK = register("killerqueen_kq_grab_hit_attack", KQGrabHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_SHEER_HEART_ATTACK_ATTACK = register("killerqueen_sheer_heart_attack_attack", SheerHeartAttackAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_BTD_DETONATE_ATTACK = register("killerqueen_bitesthedust_btd_detonate_attack", BTDDetonateAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_BTD_GRAB_HIT_ATTACK = register("killerqueen_bitesthedust_btd_grab_hit_attack", BTDGrabHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_BTD_PLANT_ATTACK = register("killerqueen_bitesthedust_btd_plant_attack", BTDPlantAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_BUBBLE_ATTACK = register("killerqueen_bitesthedust_bubble_attack", BubbleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_BUBBLE_COUNTER_ATTACK = register("killerqueen_bitesthedust_bubble_counter_attack", BubbleCounterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KILLERQUEEN_BITESTHEDUST_ELBOW_ATTACK = register("killerqueen_bitesthedust_elbow_attack", ElbowAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> KINGCRIMSON_BLOOD_THROW_ATTACK = register("kingcrimson_blood_throw_attack", BloodThrowAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KINGCRIMSON_EPITAPH_ATTACK = register("kingcrimson_epitaph_attack", EpitaphAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KINGCRIMSON_KC_DONUT_ATTACK = register("kingcrimson_kc_donut_attack", KCDonutAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KINGCRIMSON_PREDICTION_MOVE = register("kingcrimson_prediction_move", PredictionMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> KINGCRIMSON_TIME_ERASE_MOVE = register("kingcrimson_time_erase_move", TimeEraseMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> MADEINHEAVEN_CIRCLE_ATTACK = register("madeinheaven_circle_attack", CircleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MADEINHEAVEN_FURY_CHOP_ATTACK = register("madeinheaven_fury_chop_attack", FuryChopAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MADEINHEAVEN_JUDGEMENT_ATTACK = register("madeinheaven_judgement_attack", JudgementAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MADEINHEAVEN_SPEED_SLICE_ATTACK = register("madeinheaven_speed_slice_attack", SpeedSliceAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MADEINHEAVEN_TIME_ACCELERATION_MOVE = register("madeinheaven_time_acceleration_move", TimeAccelerationMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> MAGICIANSRED_CROSSFIRE_ATTACK = register("magiciansred_crossfire_attack", CrossfireAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_CROSSFIRE_HURRICANE_ATTACK = register("magiciansred_crossfire_hurricane_attack", CrossfireHurricaneAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_CROSSFIRE_VARIATION_ATTACK = register("magiciansred_crossfire_variation_attack", CrossfireVariationAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_FLAMETHROWER_ATTACK = register("magiciansred_flamethrower_attack", FlamethrowerAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_LIFE_DETECTOR_ATTACK = register("magiciansred_life_detector_attack", LifeDetectorAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_RED_BIND_ATTACK = register("magiciansred_red_bind_attack", RedBindAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MAGICIANSRED_REDIRECT_ATTACK = register("magiciansred_redirect_attack", RedirectAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> METALLICA_GIVE_SCALPEL_MOVE = register("metallica_give_scalpel_move", GiveScalpelMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_BISECT_ATTACK = register("metallica_bisect_attack", BisectAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_BISECT_CHARGE_MOVE = register("metallica_bisect_charge_move", BisectChargeMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_FAN_TOSS_ATTACK = register("metallica_fan_toss_attack", FanTossAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_HARVEST_MOVE = register("metallica_harvest_move", HarvestMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_INTERNAL_ATTACK = register("metallica_internal_attack", InternalAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_INVISIBILITY_MOVE = register("metallica_invisibility_move", InvisibilityMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_PRECISE_TOSS_ATACK = register("metallica_precise_toss_atack", PreciseTossAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_CREATE_MAGNETIC_FIELD_MOVE = register("metallica_create_magnetic_field_move", CreateMagneticFieldMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_EXPLODE_MAGNETIC_FIELD_MOVE = register("metallica_explode_magnetic_field_move", ExplodeMagneticFieldMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_RAZOR_COUGH_ATTACK = register("metallica_razor_cough_attack", RazorCoughAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> METALLICA_REMOTE_SCALPEL_MOVE = register("metallica_remote_scalpel_move", RemoteScalpelMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> PURPLEHAZE_BACKHAND_ATTACK = register("purplehaze_backhand_attack", BackhandAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_FULL_RELEASE_ATTACK = register("purplehaze_full_release_attack", FullReleaseAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_GROUND_SLAM_ATTACK = register("purplehaze_ground_slam_attack", PHGroundSlamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_LAUNCH_CAPSULE_ATTACK = register("purplehaze_launch_capsule_attack", LaunchCapsuleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_LAUNCH_CAPSULES_ATTACK = register("purplehaze_launch_capsules_attack", LaunchCapsulesAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_PLAY_MOVE = register("purplehaze_play_move", PlayMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_REKKA_ATTACK = register("purplehaze_rekka_attack", PHRekkaAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> PURPLEHAZE_DISTORTION_DISTORTION_MOVE = register("purplehaze_distortion_distortion_move", DistortionMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> SHADOWTHEWORLD_CHARGE_ATTACK = register("shadowtheworld_charge_attack", STWChargeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHADOWTHEWORLD_COUNTER_ATTACK = register("shadowtheworld_counter_attack", STWCounterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHADOWTHEWORLD_IMPALING_THRUST_ATTACK = register("shadowtheworld_impaling_thrust_attack", ImpalingThrustAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> SHARED_BARRAGE_ATTACK = register("shared_barrage_attack", BarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_CHARGE_BARRAGE_ATTACK = register("shared_charge_barrage_attack", ChargeBarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_COUNTER_MISS_MOVE = register("shared_counter_miss_move", CounterMissMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_GRAB_ATTACK = register("shared_grab_attack", GrabAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_HEAL_MOVE = register("shared_heal_move", HealMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_JUMP_MOVE = register("shared_jump_move", JumpMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_KNOCKDOWN_ATTACK = register("shared_knockdown_attack", KnockdownAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_KNOCKDOWN_BARRAGE_ATTACK = register("shared_knockdown_barrage_attack", KnockdownBarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_KNOCKDOWN_MULTI_HIT_ATTACK = register("shared_knockdown_multi_hit_attack", KnockdownMultiHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_MAIN_BARRAGE_ATTACK = register("shared_main_barrage_attack", MainBarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_NO_OP_MOVE = register("shared_no_op_move", NoOpMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_PILOT_MODE_MOVE = register("shared_pilot_mode_move", PilotModeMove.Type.INSTANCE);
//    RegistrySupplier<MoveType<?>> SHARED_REKKA_ATTACK = register("shared_rekka_attack", RekkaAttack.Type.INSTANCE); // unused
    RegistrySupplier<MoveType<?>> SHARED_SIMPLE_ATTACK = register("shared_simple_attack", SimpleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_MOVEMENT_SLOWING_SIMPLE_ATTACK = register("shared_movement_slowing_simple_attack", MovementSlowingSimpleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_SIMPLE_HOLDABLE_MOVE = register("shared_simple_holdable_move", SimpleHoldableMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_SIMPLE_MULTI_HIT_ATTACK = register("shared_simple_multi_hit_attack", SimpleMultiHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_SIMPLE_UPPERCUT_ATTACK = register("shared_simple_uppercut_attack", SimpleUppercutAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_TIME_SKIP_MOVE = register("shared_time_skip_move", TimeSkipMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_TIME_STOP_MOVE = register("shared_time_stop_move", TimeStopMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_RESTORATION_ATTACK = register("shared_restoration_attack", RestorationAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_STANDBY_ON_MOVE = register("shared_standby_on_move", StandbyActivationMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_STANDBY_OFF_MOVE = register("shared_standby_off_move", StandbyDeactivationMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_TOSS_CHARGE_MOVE = register("shared_toss_charge_move", TossChargeMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_TOSS_MOVE = register("shared_toss_move", TossMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SHARED_SIMPLE_HITSCAN_ATTACK = register("shared_simple_hitscan_attack", SimpleHitscanAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> SILVERCHARIOT_ARMOR_OFF_ATTACK = register("silverchariot_armor_off_attack", ArmorOffAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_CIRCLE_SLASH_ATTACK = register("silverchariot_circle_slash_attack", CircleSlashAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_CLEAVE_ATTACK = register("silverchariot_cleave_attack", CleaveAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_GOD_OF_DEATH_ATTACK = register("silverchariot_god_of_death_attack", GodOfDeathAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_GOD_OF_DEATH_HIT_ATTACK = register("silverchariot_god_of_death_hit_attack", GodOfDeathHitAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_LAST_SHOT_ATTACK = register("silverchariot_last_shot_attack", LastShotAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_RAY_DART_ATTACK = register("silverchariot_ray_dart_attack", RayDartAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_SC_CHARGE_ATTACK = register("silverchariot_sc_charge_attack", SCChargeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_SC_COUNTER_ATTACK = register("silverchariot_sc_counter_attack", SCCounterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> SILVERCHARIOT_SPIN_BARRAGE_ATTACK = register("silverchariot_spin_barrage_attack", SpinBarrageAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> STARPLATINUM_BLOCK_BREAKING_ATTACK = register("starplatinum_block_breaking_attack", BlockBreakingAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> STARPLATINUM_INHALE_ATTACK = register("starplatinum_inhale_attack", InhaleAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> STARPLATINUM_THEWORLD_GROUND_SLAM_ATTACK = register("starplatinum_theworld_ground_slam_attack", SPTWGroundSlamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> STARPLATINUM_THEWORLD_TIME_STRIKE_ATTACK = register("starplatinum_theworld_time_strike_attack", TimeStrikeAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> THEFOOL_AIR_BARRAGE_ATTACK = register("thefool_air_barrage_attack", AirBarrageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_GLIDE_MOVE = register("thefool_glide_move", GlideMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_POUND_ATTACK = register("thefool_pound_attack", PoundAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_SAND_CLONE_MOVE = register("thefool_sand_clone_move", SandCloneMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_SANDSTORM_ATTACK = register("thefool_sandstorm_attack", SandstormAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_SAND_TORNADO_MOVE = register("thefool_sand_tornado_move", SandTornadoMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_SAND_WAVE_ATTACK = register("thefool_sand_wave_attack", SandWaveAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_SLAM_ATTACK = register("thefool_slam_attack", SlamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_CHARGE_ATTACK = register("thefool_charge_attack", TFChargeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_COMBO_ATTACK = register("thefool_combo_attack", TFComboAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEFOOL_LAUNCH_ATTACK = register("thefool_launch_attack", TFLaunchAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> THEHAND_ERASE_ATTACK = register("thehand_erase_attack", SimpleEraseAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEHAND_ERASE_GROUND_ATTACK = register("thehand_erase_ground_attack", EraseGroundAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEHAND_ERASE_SPACE_ATTACK = register("thehand_erase_space_attack", EraseSpaceAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEHAND_RAGE_ATTACK = register("thehand_rage_attack", RageAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEHAND_STOMP2 = register("thehand_stomp2", Stomp2Attack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> THESUN_FIRE_METEOR_ATTACK = register("thesun_fire_meteor_attack", FireMeteorAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THESUN_FIRE_SUN_BEAM_ATTACK = register("thesun_fire_sun_beam_attack", FireSunBeamAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THESUN_METEOR_SHOWER_ATTACK = register("thesun_meteor_shower_attack", MeteorShowerAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> THEWORLD_FEIGN_BARRAGE_COUNTER_ATTACK = register("theworld_feign_barrage_counter_attack", FeignBarrageCounterAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_CHARGE_ATTACK = register("theworld_charge_attack", TWChargeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_DONUT_ATTACK = register("theworld_donut_attack", TWDonutAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_AERIAL_DIVINE_FINISHER_ATTACK = register("theworld_overheaven_aerial_divine_finisher_attack", AerialDivineFinisherAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_CHARGE_OVERWRITE_MOVE = register("theworld_overheaven_charge_overwrite_move", ChargeOverwriteMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_DIVINE_FINISHER_ATTACK = register("theworld_overheaven_divine_finisher_attack", DivineFinisherAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_LUNGE_ATTACK = register("theworld_overheaven_lunge_attack", LungeAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_OVERWRITE_ATTACK = register("theworld_overheaven_overwrite_attack", OverwriteAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_SINGULARITY_ATTACK = register("theworld_overheaven_singularity_attack", SingularityAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> THEWORLD_OVERHEAVEN_SMITE_ATTACK = register("theworld_overheaven_smite_attack", SmiteAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> VAMPIRE_BLOOD_SUCK_ATTACK = register("vampire_blood_suck_attack", BloodSuckAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> VAMPIRE_BLOOD_SUCK_HITS_ATTACK = register("vampire_blood_suck_hits_attack", BloodSuckHitsAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> VAMPIRE_NIGHT_VISION_MOVE = register("vampire_night_vision_move", NightVisionMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> VAMPIRE_REVIVE_MOVE = register("vampire_revive_move", ReviveMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> VAMPIRE_SPACE_RIPPER_ATTACK = register("vampire_space_ripper_attack", SpaceRipperAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> HAMON_CHARGE_MOVE = register("hamon_charge_move", ChargeHamonMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HAMON_RIPPLE_ATTACK = register("hamon_ripple_attack", RippleAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HAMON_SENDO_ATTACK = register("hamon_sendo_attack", SendoAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HAMON_ZOOM_PUNCH_ATTACK = register("hamon_zoom_punch_attack", ZoomPunchAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> HAMON_IMPROVISER_ATTACK = register("hamon_improviser_attack", ImproviserAttack.Type.INSTANCE);
    //RegistrySupplier<MoveType<?>> HAMON_IMPROVISER_MOVE = register("hamon_improviser_move", ImproviserMove.Type.INSTANCE);


    RegistrySupplier<MoveType<?>> WHITESNAKE_CHARGED_SPEW_ATTACK = register("whitesnake_charged_spew_attack", ChargedSpewAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> WHITESNAKE_GIVE_STAND_ATTACK = register("whitesnake_give_stand_attack", GiveStandAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> WHITESNAKE_MELT_YOUR_HEART_ATTACK = register("whitesnake_melt_your_heart_attack", MeltYourHeartAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> WHITESNAKE_POISON_SPEW_ATTACK = register("whitesnake_poison_spew_attack", PoisonSpewAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> WHITESNAKE_STEAL_STAND_ATTACK = register("whitesnake_steal_stand_attack", StealStandAttack.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> MANDOM_COUNTDOWN_MOVE = register("mandom_countdown_move", CountdownMove.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> MANDOM_REWIND_MOVE = register("mandom_rewind_move", RewindMove.Type.INSTANCE);

    RegistrySupplier<MoveType<?>> AEROSMITH_BULLET_ATTACK = register("aerosmith_bullet_attack", MuzzleHitscanAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> AEROSMITH_BOMB_DROP_ATTACK = register("aerosmith_bomb_drop_attack", BombDropAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> AEROSMITH_ITEM_DROP_ATTACK = register("aerosmith_item_drop_attack", ItemDropAttack.Type.INSTANCE);
    RegistrySupplier<MoveType<?>> AEROSMITH_PATROL_MOVE = register("aerosmith_patrol_move", PatrolMove.Type.INSTANCE);



    private static RegistrySupplier<MoveType<?>> register(String id, MoveType<?> type) {
        return MOVE_TYPE_REGISTRY.register(id, () -> type);
    }
}
