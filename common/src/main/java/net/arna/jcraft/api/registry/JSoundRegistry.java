package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.minecraft.sounds.SoundEvent;

import static net.arna.jcraft.JCraft.SOUNDS;

public interface JSoundRegistry {

    static RegistrySupplier<SoundEvent> register(String name) {
        var event = SoundEvent.createVariableRangeEvent(JCraft.id(name));
        return SOUNDS.register(event.getLocation().getPath(), () -> event);
    }


    // Generic

    RegistrySupplier<SoundEvent> STAND_SUMMON = register("standsummon");
    RegistrySupplier<SoundEvent> STAND_DESUMMON = register("desummon");
    RegistrySupplier<SoundEvent> STAND_BLOCK = register("standblock");
    RegistrySupplier<SoundEvent> STAND_PUSHBLOCK = register("standpushblock");
    RegistrySupplier<SoundEvent> BACKSTAB = register("backstab");
    RegistrySupplier<SoundEvent> ARMORED_HIT = register("armoredhit");
    RegistrySupplier<SoundEvent> COMBO_BREAK = register("combobreak");
    RegistrySupplier<SoundEvent> COOLDOWN_CANCEL = register("cooldowncancel");
    RegistrySupplier<SoundEvent> IMPACT_1 = register("impact1");
    RegistrySupplier<SoundEvent> IMPACT_2 = register("impact2");
    RegistrySupplier<SoundEvent> IMPACT_3 = register("impact3");
    RegistrySupplier<SoundEvent> IMPACT_4 = register("impact4");
    RegistrySupplier<SoundEvent> IMPACT_5 = register("impact5");
    RegistrySupplier<SoundEvent> IMPACT_6 = register("impact6");
    RegistrySupplier<SoundEvent> IMPACT_7 = register("impact7");
    RegistrySupplier<SoundEvent> IMPACT_8 = register("impact8");
    RegistrySupplier<SoundEvent> IMPACT_9 = register("impact9");
    RegistrySupplier<SoundEvent> IMPACT_10 = register("impact10");
    RegistrySupplier<SoundEvent> IMPACT_11 = register("impact11");
    RegistrySupplier<SoundEvent> IMPACT_12 = register("impact12");
    RegistrySupplier<SoundEvent> TIME_SKIP = register("timeskip");
    RegistrySupplier<SoundEvent> COIN_TOSS = register("cointoss");
    RegistrySupplier<SoundEvent> TOSS = register("throw");
    RegistrySupplier<SoundEvent> JANGLE = register("jangle");

    // Star Platinum
    RegistrySupplier<SoundEvent> STAR_PLATINUM_SUMMON = register("spsummon");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_TIMESKIP = register("sptimeskip");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_BARRAGE = register("spbarrage");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_ADVANCING_BARRAGE = register("spadvbarrage");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_LUNGING_BARRAGE = register("spadvbarrageshort");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_THE_WORLD = register("sptw");
    RegistrySupplier<SoundEvent> STAR_PLATINUM_KNEE = register("spknee");
    RegistrySupplier<SoundEvent> STAR_BREAKER = register("starbreaker");
    RegistrySupplier<SoundEvent> STAR_FINGER = register("starfinger");
    RegistrySupplier<SoundEvent> INHALE_LOOP = register("spinhaleloop");

    //King Crimson
    RegistrySupplier<SoundEvent> KC_SUMMON = register("kcsummon");
    RegistrySupplier<SoundEvent> KC_DUAL_CHOP = register("kcdualchop");
    RegistrySupplier<SoundEvent> KC_DONUT = register("kcdonut");
    RegistrySupplier<SoundEvent> KC_BARRAGE = register("kcbarrage");
    RegistrySupplier<SoundEvent> KC_HEAVY = register("kcheavy");
    RegistrySupplier<SoundEvent> KC_HEAVY2 = register("kcheavy2");
    RegistrySupplier<SoundEvent> KC_EYE_CHOP = register("kceyechop");
    RegistrySupplier<SoundEvent> KC_EPITAPH = register("kcepitaph");
    RegistrySupplier<SoundEvent> KC_RAGE = register("kcrage");
    RegistrySupplier<SoundEvent> TE_TP = register("tetp");
    RegistrySupplier<SoundEvent> TIME_ERASE = register("timeerase");
    RegistrySupplier<SoundEvent> TIME_ERASE_EXIT = register("kcteexit");

    //The World
    RegistrySupplier<SoundEvent> TW_SUMMON = register("twsummon");
    RegistrySupplier<SoundEvent> TW_BARRAGE = register("twbarrage");
    RegistrySupplier<SoundEvent> TW_TS = register("twtimestop");
    RegistrySupplier<SoundEvent> TW_TS_CLEAN = register("twtimestop_clean");
    RegistrySupplier<SoundEvent> TW_CHARGE = register("twcharge");
    RegistrySupplier<SoundEvent> TW_CHARGE_HIT = register("twchargehit");
    RegistrySupplier<SoundEvent> TW_DONUT = register("twdonut");
    RegistrySupplier<SoundEvent> TW_DONUT_HIT = register("twdonuthit");
    RegistrySupplier<SoundEvent> TW_KICK = register("twkick");
    RegistrySupplier<SoundEvent> TW_KICK_HIT = register("twkickhit");
    RegistrySupplier<SoundEvent> TW_COUNTER = register("twcounter");
    RegistrySupplier<SoundEvent> MUDA_DA = register("mudada");

    RegistrySupplier<SoundEvent> STW_ZAP = register("stwzap");
    RegistrySupplier<SoundEvent> STW_WARBLE = register("stwwarble");
    RegistrySupplier<SoundEvent> STW_TS = register("stwtimestop");
    RegistrySupplier<SoundEvent> STW_LAUGH = register("stwlaugh");

    //Dirty Deeds Done Dirt Cheap
    RegistrySupplier<SoundEvent> D4C_SUMMON = register("d4csummon");
    RegistrySupplier<SoundEvent> D4C_LIGHT = register("d4clight");
    RegistrySupplier<SoundEvent> D4C_HEAVY = register("d4cheavy");
    RegistrySupplier<SoundEvent> D4C_BARRAGE = register("d4cbarrage");
    RegistrySupplier<SoundEvent> D4C_DIMHOP = register("d4cdimhop");
    RegistrySupplier<SoundEvent> REVOLVER_FIRE = register("revolverfire");
    RegistrySupplier<SoundEvent> D4C_THROW = register("d4cthrow");
    RegistrySupplier<SoundEvent> D4C_COUNTER = register("d4ccounter");
    RegistrySupplier<SoundEvent> D4C_UTILITY = register("d4cutility");
    RegistrySupplier<SoundEvent> D4C_ALT_UNIVERSE_AMBIENCE = register("altuniverseambience");

    //Cream
    RegistrySupplier<SoundEvent> CREAM_SUMMON = register("creamsummon");
    RegistrySupplier<SoundEvent> CREAM_CONSUME = register("creamconsume");
    RegistrySupplier<SoundEvent> CREAM_CHARGE = register("creamcharge");
    RegistrySupplier<SoundEvent> CREAM_COMBO = register("creamcombo");
    RegistrySupplier<SoundEvent> CREAM_HEAVY = register("creamheavy");
    RegistrySupplier<SoundEvent> CREAM_GRAB = register("creamgrab");
    RegistrySupplier<SoundEvent> CREAM_SMASH = register("creamsmash");
    RegistrySupplier<SoundEvent> CREAM_ENTER = register("creamenter");
    RegistrySupplier<SoundEvent> CREAM_EXIT = register("creamexit");
    RegistrySupplier<SoundEvent> CREAM_OVERHEAD = register("creamoverhead");
    RegistrySupplier<SoundEvent> CREAM_BALLDASH = register("creamballdash");

    //Killer Queen
    RegistrySupplier<SoundEvent> KQ_HEAVY = register("kqheavy");
    RegistrySupplier<SoundEvent> KQ_BARRAGE = register("kqbarrage");
    RegistrySupplier<SoundEvent> KQ_DETONATE = register("kqdetonate");
    RegistrySupplier<SoundEvent> KQ_UPPERCUT = register("kquppercut");
    RegistrySupplier<SoundEvent> KQ_EXPLODE = register("kqexplode");
    RegistrySupplier<SoundEvent> SHA_TREAD = register("shatread");

    //Killer Queen: Bites The Dust
    RegistrySupplier<SoundEvent> KQBTD_ELBOW = register("kqbtdelbow");
    RegistrySupplier<SoundEvent> KQBTD_SUMMON = register("kqbtdsummon");

    //Whitesnake
    RegistrySupplier<SoundEvent> WS_SUMMON = register("wssummon");
    RegistrySupplier<SoundEvent> WS_BARRAGE = register("wsbarrage");
    RegistrySupplier<SoundEvent> WS_LEGCRUSH = register("wslegcrush");
    RegistrySupplier<SoundEvent> WS_DONUT = register("wsdonut");
    RegistrySupplier<SoundEvent> WS_MEMORY_DISC = register("wsmemorydisc");
    RegistrySupplier<SoundEvent> WS_STAND_DISC = register("wsstanddisc");
    RegistrySupplier<SoundEvent> WS_GUN = register("wsgun");
    RegistrySupplier<SoundEvent> WS_MYH = register("wsmeltyourheart");

    //Magician's Red
    RegistrySupplier<SoundEvent> MR_SUMMON = register("mrsummon");
    RegistrySupplier<SoundEvent> MR_BARRAGE = register("mrbarrage");
    RegistrySupplier<SoundEvent> MR_CROSSFIRE = register("mrcrossfire");
    RegistrySupplier<SoundEvent> MR_DETECTOR = register("mrdetector");
    RegistrySupplier<SoundEvent> MR_HEAVY = register("mrheavy");
    RegistrySupplier<SoundEvent> MR_REDIRECT = register("mrredirect");
    RegistrySupplier<SoundEvent> MR_ULT = register("mrult");
    RegistrySupplier<SoundEvent> MR_REDBIND = register("mrredbind");

    //Silver Chariot
    RegistrySupplier<SoundEvent> SC_SUMMON = register("scsummon");
    RegistrySupplier<SoundEvent> SC_BARRAGE = register("scbarrage");
    RegistrySupplier<SoundEvent> SC_CHARGE = register("sccharge");
    RegistrySupplier<SoundEvent> SC_HEAVY = register("scheavy");
    RegistrySupplier<SoundEvent> SC_SPIN = register("scspin");
    RegistrySupplier<SoundEvent> SC_CLEAVE = register("sccleave");
    RegistrySupplier<SoundEvent> SC_ARMOROFF = register("scarmoroff");
    RegistrySupplier<SoundEvent> SC_POKE = register("scpoke");

    //Golden Experience
    RegistrySupplier<SoundEvent> GE_SUMMON = register("gesummon");
    RegistrySupplier<SoundEvent> GE_BARRAGE = register("gebarrage");
    RegistrySupplier<SoundEvent> GE_HEAL = register("geheal");
    RegistrySupplier<SoundEvent> GE_TREE = register("getree");
    RegistrySupplier<SoundEvent> GE_REKKA1 = register("gerekka1");
    RegistrySupplier<SoundEvent> GE_REKKA2 = register("gerekka2");
    RegistrySupplier<SoundEvent> GE_REKKA3 = register("gerekka3");

    //Hierophant Green
    RegistrySupplier<SoundEvent> HG_SUMMON = register("hgsummon");
    RegistrySupplier<SoundEvent> HG_BARRAGE = register("hgbarrage");
    RegistrySupplier<SoundEvent> HG_CROUCH_LIGHT = register("hgcrouchlight");
    RegistrySupplier<SoundEvent> HG_LIGHT_FOLLOWUP = register("hglightfollowup");
    RegistrySupplier<SoundEvent> HG_SPLASH = register("hgsplash");
    RegistrySupplier<SoundEvent> HG_EXTEND = register("hgextend");
    RegistrySupplier<SoundEvent> HG_NET_SET = register("hgnetset");

    //Golden Experience: Requiem
    RegistrySupplier<SoundEvent> GER_SUMMON = register("gersummon");
    RegistrySupplier<SoundEvent> GER_HEAVY = register("gerheavy");
    RegistrySupplier<SoundEvent> GER_LASER = register("gerlaser");
    RegistrySupplier<SoundEvent> GER_LASER_FIRE = register("gerlaserfire");
    RegistrySupplier<SoundEvent> GER_KICKBARRAGE = register("gerkickbarrage");
    RegistrySupplier<SoundEvent> GER_SETUP = register("gersetup");
    RegistrySupplier<SoundEvent> GER_FLY = register("gerfly");
    RegistrySupplier<SoundEvent> GER_RTZ = register("gerrtz");

    //The Fool
    RegistrySupplier<SoundEvent> FOOL_BARK1 = register("foolbark1");
    RegistrySupplier<SoundEvent> FOOL_BARK2 = register("foolbark2");
    RegistrySupplier<SoundEvent> FOOL_LAUNCH = register("foollaunch");
    RegistrySupplier<SoundEvent> FOOL_CHARGE = register("foolcharge");
    RegistrySupplier<SoundEvent> FOOL_ULT = register("foolultimate");
    RegistrySupplier<SoundEvent> FOOL_GLIDE = register("foolglide");

    //C-Moon
    RegistrySupplier<SoundEvent> CMOON_SUMMON = register("cmoonsummon");
    RegistrySupplier<SoundEvent> CMOON_BARRAGE = register("cmoonbarrage");
    RegistrySupplier<SoundEvent> CMOON_GRAV_PUNCH = register("cmoongravpunch");
    RegistrySupplier<SoundEvent> CMOON_GRAV_PUNCH_HIT = register("cmoongravpunchhit");
    RegistrySupplier<SoundEvent> CMOON_GROUNDSLAM = register("cmoongroundslam");
    RegistrySupplier<SoundEvent> CMOON_GRAVSHIFT = register("cmoongravshift");
    RegistrySupplier<SoundEvent> CMOON_GRAVSHIFT_DIRECTIONAL = register("cmoondirectionalshift");
    RegistrySupplier<SoundEvent> CMOON_DONUT = register("cmoondonut");
    RegistrySupplier<SoundEvent> CMOON_GROUNDSHOOT = register("cmoongroundshoot");
    RegistrySupplier<SoundEvent> CMOON_BLOCKLAUNCH = register("blocklaunch");
    RegistrySupplier<SoundEvent> CMOON_BLOCKHALT = register("blockhalt");

    //Made in Heaven
    RegistrySupplier<SoundEvent> MIH_SUMMON = register("mihsummon");
    RegistrySupplier<SoundEvent> MIH_BARRAGE = register("mihbarrage");
    RegistrySupplier<SoundEvent> MIH_ZOOM = register("mihzoom");
    RegistrySupplier<SoundEvent> MIH_JUDGEMENT = register("mihjudgement");
    RegistrySupplier<SoundEvent> MIH_TACCEL = register("mihtaccel");
    RegistrySupplier<SoundEvent> MIH_FURYCHOP = register("mihfurychop");
    RegistrySupplier<SoundEvent> MIH_SPEEDSLICE = register("mihspeedslice");
    RegistrySupplier<SoundEvent> MIH_LEGCRUSHER = register("mihlegcrusher");
    RegistrySupplier<SoundEvent> MIH_CIRCLE = register("mihcircle");

    //The World: Over Heaven
    RegistrySupplier<SoundEvent> TWOH_SUMMON = register("twohsummon");
    RegistrySupplier<SoundEvent> TWOH_BARRAGE = register("twohbarrage");
    RegistrySupplier<SoundEvent> TWOH_SHOOT = register("twohshoot");
    RegistrySupplier<SoundEvent> TWOH_TIMESKIP = register("twohtimeskip");
    RegistrySupplier<SoundEvent> TWOH_TS = register("twohtimestop");
    RegistrySupplier<SoundEvent> TWOH_HEAVY = register("twohheavy");
    RegistrySupplier<SoundEvent> TWOH_SINGULARITY = register("twohsingularity");
    RegistrySupplier<SoundEvent> TWOH_SMITE = register("twohsmite");
    RegistrySupplier<SoundEvent> TWOH_CHARGE_OVERWRITE = register("twohchargeoverwrite");
    RegistrySupplier<SoundEvent> TWOH_CHARGE = register("twohcharge");
    RegistrySupplier<SoundEvent> TWOH_OVERWRITE = register("twohoverwrite");
    RegistrySupplier<SoundEvent> TWOH_KNIFETHROW = register("twohairknives");
    RegistrySupplier<SoundEvent> TWOH_KNIFESUMMON = register("twohknives");

    // Star Platinum: The World
    RegistrySupplier<SoundEvent> SPTW_GRAB = register("sptwgrab");
    RegistrySupplier<SoundEvent> SPTW_GRABHIT = register("sptwgrabhit");
    RegistrySupplier<SoundEvent> SPTW_UPPERCUT = register("sptwuppercut");
    RegistrySupplier<SoundEvent> SPTW_BACKHAND = register("sptwbackhand");

    // Purple Haze
    RegistrySupplier<SoundEvent> PH_SUMMON = register("phsummon");
    RegistrySupplier<SoundEvent> PH_BARRAGE = register("phbarrage");
    RegistrySupplier<SoundEvent> PH_GRAB_HIT = register("phgrabhit");
    RegistrySupplier<SoundEvent> PH_REKKA1 = register("phrekka1");
    RegistrySupplier<SoundEvent> PH_REKKA2 = register("phrekka2");
    RegistrySupplier<SoundEvent> PH_REKKA3 = register("phrekka3");
    RegistrySupplier<SoundEvent> PH_CAPSULE1 = register("phcapsule1");
    RegistrySupplier<SoundEvent> PH_CAPSULE2 = register("phcapsule2");
    RegistrySupplier<SoundEvent> PH_GROUNDSLAM = register("phgroundslam");
    RegistrySupplier<SoundEvent> PH_ULTIMATE = register("phultimate");

    // The Sun
    RegistrySupplier<SoundEvent> SUN_SUMMON = register("sunsummon");
    RegistrySupplier<SoundEvent> SUN_SHOWER = register("sunshower");
    RegistrySupplier<SoundEvent> SUN_BEAM_RAY = register("sunbeamray");
    RegistrySupplier<SoundEvent> SUN_METEOR_FIRE = register("sunmeteorfire");
    RegistrySupplier<SoundEvent> SUN_IDLE = register("sunidle");

    // Horus
    RegistrySupplier<SoundEvent> HORUS_SUMMON = register("horussummon");
    RegistrySupplier<SoundEvent> HORUS_BARRAGE_FIRE = register("horusbarragefire");
    RegistrySupplier<SoundEvent> HORUS_SCATTER = register("horusscatter");
    RegistrySupplier<SoundEvent> HORUS_LANCE_CHARGE = register("horuslancecharge");
    RegistrySupplier<SoundEvent> HORUS_LANCE_THROW = register("horuslancethrow");
    RegistrySupplier<SoundEvent> HORUS_PlACE_CREEPING_ICE = register("horusplacecreepingice");
    RegistrySupplier<SoundEvent> HORUS_STOMP = register("horusstomp");
    RegistrySupplier<SoundEvent> HORUS_STOMP_SLAM = register("horusstompslam");

    // The Hand
    RegistrySupplier<SoundEvent> THE_HAND_SUMMON = register("thsummon");
    RegistrySupplier<SoundEvent> THE_HAND_KICK_BARRAGE = register("thkickbarrage");
    RegistrySupplier<SoundEvent> THE_HAND_SWIPE = register("thswipe");
    RegistrySupplier<SoundEvent> THE_HAND_SWIPE_QUICK = register("thswipequick");
    RegistrySupplier<SoundEvent> THE_HAND_GRAB = register("thgrab");
    RegistrySupplier<SoundEvent> THE_HAND_RAGE = register("thrage");

    // Metallica
    RegistrySupplier<SoundEvent> METALLICA_SUMMON = register("msummon");
    RegistrySupplier<SoundEvent> METALLICA_BARRAGE = register("mbarrage");
    RegistrySupplier<SoundEvent> METALLICA_SCALPEL_SUMMON = register("mscalpelsummon");
    RegistrySupplier<SoundEvent> METALLICA_INVISIBILITY = register("minvisibility");
    RegistrySupplier<SoundEvent> METALLICA_RAZOR_VOMIT_PREPARE = register("mrazorvomitprepare");
    RegistrySupplier<SoundEvent> METALLICA_BLADE_SWIPE = register("mbladeswipe");

    // Mandom
    RegistrySupplier<SoundEvent> MANDOM_REWIND = register("mandomrewind");
    RegistrySupplier<SoundEvent> MANDOM_SUMMON = register("mandomsummon");
    RegistrySupplier<SoundEvent> MANDOM_COUNTDOWN = register("mandomcountdown");

    // Aerosmith
    RegistrySupplier<SoundEvent> AS_SHOOT = register("asshoot");
    RegistrySupplier<SoundEvent> AS_SUMMON = register("assummon");

    //// SPECS
    // Brawler

    // Anubis
    RegistrySupplier<SoundEvent> ANUBIS_SLASH = register("anubisslash");
    RegistrySupplier<SoundEvent> ANUBIS_POMMEL = register("anubispommel");
    RegistrySupplier<SoundEvent> ANUBIS_SHEATHE = register("anubissheathe");
    RegistrySupplier<SoundEvent> ANUBIS_UNSHEATHE = register("anubisunsheathe");
    RegistrySupplier<SoundEvent> ANUBIS_SPECCHANGE = register("anubisspecchange");
    RegistrySupplier<SoundEvent> ANUBIS_REKKA2 = register("anubisrekka2");
    RegistrySupplier<SoundEvent> ANUBIS_REKKA3 = register("anubisrekka3"); //todo: 3 hit rekka sound for anubis

    // Vampire
    RegistrySupplier<SoundEvent> VAMPIRE_LASER = register("vampirelaser");
    RegistrySupplier<SoundEvent> VAMPIRE_LASER_FIRE = register("vampirelaserfire");
    RegistrySupplier<SoundEvent> VAMPIRE_GRAB_HIT = register("vampiregrabhit");
    RegistrySupplier<SoundEvent> VAMPIRE_SUCK = register("vampiresuck");
    RegistrySupplier<SoundEvent> VAMPIRE_REANIMATE = register("vampirereanimate");
    RegistrySupplier<SoundEvent> VAMPIRE_SPEC_CHANGE = register("vampirespecchange");

    // Hamon

    RegistrySupplier<SoundEvent> HAMON_CRASH = register("hamoncrash");
    RegistrySupplier<SoundEvent> HAMON_ECHO = register("hamonecho");
    RegistrySupplier<SoundEvent> HAMON_RING = register("hamonring");
    RegistrySupplier<SoundEvent> HAMON_SURGE = register("hamonsurge");
    RegistrySupplier<SoundEvent> HAMON_SWOOSH = register("hamonswoosh");
    RegistrySupplier<SoundEvent> HAMON_CRACKLE_IMPACT = register("hamoncrackleimpact");
    RegistrySupplier<SoundEvent> HAMON_BREATHE = register("hamonbreathe");
    RegistrySupplier<SoundEvent> HAMON_CRACKLES = register("hamoncrackles");

    //// OTHER
    RegistrySupplier<SoundEvent> BULLET_RICOCHET = register("bulletricochet");
    RegistrySupplier<SoundEvent> BULLET_PENETRATE = register("bulletpenetrate");
    RegistrySupplier<SoundEvent> LOAD = register("reload");

    // ROAD ROLLER
    RegistrySupplier<SoundEvent> ROAD_ROLLER_HIT = register("rrhit");
    RegistrySupplier<SoundEvent> ROAD_ROLLER_SLAM = register("rrslam");
    RegistrySupplier<SoundEvent> ROAD_ROLLER_IGNITION = register("rrignition");
    RegistrySupplier<SoundEvent> ROAD_ROLLER_ACTIVE = register("rractive");

    static void init() {
        // intentionally left empty
    }
}
