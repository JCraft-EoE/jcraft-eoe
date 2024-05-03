package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public interface JSoundRegistry {

    static SoundEvent registerSound(String id) {
        //JCraft.LOGGER.info("Registering sound: " + id); // Probably unnecessary
        SoundEvent event = SoundEvent.of(JCraft.id(id));
        Registry.register(Registries.SOUND_EVENT, event.getId(), event);
        return event;
    }

    // Generic
    SoundEvent STAND_SUMMON = registerSound("standsummon");
    SoundEvent STAND_DESUMMON = registerSound("desummon");
    SoundEvent STAND_BLOCK = registerSound("standblock");
    SoundEvent BACKSTAB = registerSound("backstab");
    SoundEvent ARMORED_HIT = registerSound("armoredhit");
    SoundEvent COMBO_BREAK = registerSound("combobreak");
    SoundEvent COOLDOWN_CANCEL = registerSound("cooldowncancel");
    SoundEvent IMPACT_1 = registerSound("impact1");
    SoundEvent IMPACT_2 = registerSound("impact2");
    SoundEvent IMPACT_3 = registerSound("impact3");
    SoundEvent IMPACT_4 = registerSound("impact4");
    SoundEvent IMPACT_5 = registerSound("impact5");
    SoundEvent IMPACT_6 = registerSound("impact6");
    SoundEvent IMPACT_7 = registerSound("impact7");
    SoundEvent IMPACT_8 = registerSound("impact8");
    SoundEvent IMPACT_9 = registerSound("impact9");
    SoundEvent IMPACT_10 = registerSound("impact10");
    SoundEvent IMPACT_11 = registerSound("impact11");
    SoundEvent IMPACT_12 = registerSound("impact12");
    SoundEvent TIME_SKIP = registerSound("timeskip");
    SoundEvent COIN_TOSS = registerSound("cointoss");

    // Star Platinum
    SoundEvent STAR_PLATINUM_SUMMON = registerSound("spsummon");
    SoundEvent STAR_PLATINUM_TIMESKIP = registerSound("sptimeskip");
    SoundEvent STAR_PLATINUM_BARRAGE = registerSound("spbarrage");
    SoundEvent STAR_PLATINUM_ADVANCING_BARRAGE = registerSound("spadvbarrage");
    SoundEvent STAR_PLATINUM_LUNGING_BARRAGE = registerSound("spadvbarrageshort");
    SoundEvent STAR_PLATINUM_THE_WORLD = registerSound("sptw");
    SoundEvent STAR_PLATINUM_KNEE = registerSound("spknee");
    SoundEvent STAR_BREAKER = registerSound("starbreaker");
    SoundEvent STAR_FINGER = registerSound("starfinger");

    //King Crimson
    SoundEvent KC_SUMMON = registerSound("kcsummon");
    SoundEvent KC_DUAL_CHOP = registerSound("kcdualchop");
    SoundEvent KC_DONUT = registerSound("kcdonut");
    SoundEvent KC_BARRAGE = registerSound("kcbarrage");
    SoundEvent KC_HEAVY = registerSound("kcheavy");
    SoundEvent KC_HEAVY2 = registerSound("kcheavy2");
    SoundEvent KC_EYE_CHOP = registerSound("kceyechop");
    SoundEvent KC_EPITAPH = registerSound("kcepitaph");
    SoundEvent KC_RAGE = registerSound("kcrage");
    SoundEvent TE_TP = registerSound("tetp");
    SoundEvent TIME_ERASE = registerSound("timeerase");
    SoundEvent TIME_ERASE_EXIT = registerSound("kcteexit");

    //The World
    SoundEvent TW_SUMMON = registerSound("twsummon");
    SoundEvent TW_BARRAGE = registerSound("twbarrage");
    SoundEvent TW_TS = registerSound("twtimestop");
    SoundEvent TW_TS_CLEAN = registerSound("twtimestop_clean");
    SoundEvent TW_CHARGE = registerSound("twcharge");
    SoundEvent TW_CHARGE_HIT = registerSound("twchargehit");
    SoundEvent TW_DONUT = registerSound("twdonut");
    SoundEvent TW_DONUT_HIT = registerSound("twdonuthit");
    SoundEvent TW_KICK = registerSound("twkick");
    SoundEvent TW_KICK_HIT = registerSound("twkickhit");
    SoundEvent TW_COUNTER = registerSound("twcounter");
    SoundEvent MUDA_DA = registerSound("mudada");

    //Dirty Deeds Done Dirt Cheap
    SoundEvent D4C_SUMMON = registerSound("d4csummon");
    SoundEvent D4C_LIGHT = registerSound("d4clight");
    SoundEvent D4C_HEAVY = registerSound("d4cheavy");
    SoundEvent D4C_BARRAGE = registerSound("d4cbarrage");
    SoundEvent D4C_DIMHOP = registerSound("d4cdimhop");
    SoundEvent REVOLVER_FIRE = registerSound("revolverfire");
    SoundEvent D4C_THROW = registerSound("d4cthrow");
    SoundEvent D4C_COUNTER = registerSound("d4ccounter");
    SoundEvent D4C_UTILITY = registerSound("d4cutility");
    SoundEvent D4C_ALT_UNIVERSE_AMBIENCE = registerSound("altuniverseambience");

    //Cream
    SoundEvent CREAM_SUMMON = registerSound("creamsummon");
    SoundEvent CREAM_CONSUME = registerSound("creamconsume");
    SoundEvent CREAM_CHARGE = registerSound("creamcharge");
    SoundEvent CREAM_COMBO = registerSound("creamcombo");
    SoundEvent CREAM_HEAVY = registerSound("creamheavy");
    SoundEvent CREAM_GRAB = registerSound("creamgrab");
    SoundEvent CREAM_SMASH = registerSound("creamsmash");
    SoundEvent CREAM_ENTER = registerSound("creamenter");
    SoundEvent CREAM_EXIT = registerSound("creamexit");
    SoundEvent CREAM_OVERHEAD = registerSound("creamoverhead");
    SoundEvent CREAM_BALLDASH = registerSound("creamballdash");

    //Killer Queen
    SoundEvent KQ_HEAVY = registerSound("kqheavy");
    SoundEvent KQ_BARRAGE = registerSound("kqbarrage");
    SoundEvent KQ_DETONATE = registerSound("kqdetonate");
    SoundEvent KQ_UPPERCUT = registerSound("kquppercut");
    SoundEvent KQ_EXPLODE = registerSound("kqexplode");
    SoundEvent SHA_TREAD = registerSound("shatread");

    //Killer Queen: Bites The Dust
    SoundEvent KQBTD_ELBOW = registerSound("kqbtdelbow");
    SoundEvent KQBTD_SUMMON = registerSound("kqbtdsummon");

    //Whitesnake
    SoundEvent WS_SUMMON = registerSound("wssummon");
    SoundEvent WS_BARRAGE = registerSound("wsbarrage");
    SoundEvent WS_LEGCRUSH = registerSound("wslegcrush");
    SoundEvent WS_DONUT = registerSound("wsdonut");
    SoundEvent WS_MEMORY_DISC = registerSound("wsmemorydisc");
    SoundEvent WS_STAND_DISC = registerSound("wsstanddisc");
    SoundEvent WS_GUN = registerSound("wsgun");
    SoundEvent WS_MYH = registerSound("wsmeltyourheart");

    //Magician's Red
    SoundEvent MR_SUMMON = registerSound("mrsummon");
    SoundEvent MR_BARRAGE = registerSound("mrbarrage");
    SoundEvent MR_CROSSFIRE = registerSound("mrcrossfire");
    SoundEvent MR_DETECTOR = registerSound("mrdetector");
    SoundEvent MR_HEAVY = registerSound("mrheavy");
    SoundEvent MR_REDIRECT = registerSound("mrredirect");
    SoundEvent MR_ULT = registerSound("mrult");
    SoundEvent MR_REDBIND = registerSound("mrredbind");

    //Silver Chariot
    SoundEvent SC_SUMMON = registerSound("scsummon");
    SoundEvent SC_BARRAGE = registerSound("scbarrage");
    SoundEvent SC_CHARGE = registerSound("sccharge");
    SoundEvent SC_HEAVY = registerSound("scheavy");
    SoundEvent SC_SPIN = registerSound("scspin");
    SoundEvent SC_CLEAVE = registerSound("sccleave");
    SoundEvent SC_ARMOROFF = registerSound("scarmoroff");
    SoundEvent SC_POKE = registerSound("scpoke");

    //Golden Experience
    SoundEvent GE_SUMMON = registerSound("gesummon");
    SoundEvent GE_BARRAGE = registerSound("gebarrage");
    SoundEvent GE_HEAL = registerSound("geheal");
    SoundEvent GE_TREE = registerSound("getree");
    SoundEvent GE_REKKA1 = registerSound("gerekka1");
    SoundEvent GE_REKKA2 = registerSound("gerekka2");
    SoundEvent GE_REKKA3 = registerSound("gerekka3");

    //Hierophant Green
    SoundEvent HG_SUMMON = registerSound("hgsummon");
    SoundEvent HG_BARRAGE = registerSound("hgbarrage");
    SoundEvent HG_CROUCH_LIGHT = registerSound("hgcrouchlight");
    SoundEvent HG_LIGHT_FOLLOWUP = registerSound("hglightfollowup");
    SoundEvent HG_SPLASH = registerSound("hgsplash");
    SoundEvent HG_EXTEND = registerSound("hgextend");
    SoundEvent HG_NET_SET = registerSound("hgnetset");

    //Golden Experience: Requiem
    SoundEvent GER_SUMMON = registerSound("gersummon");
    SoundEvent GER_HEAVY = registerSound("gerheavy");
    SoundEvent GER_LASER = registerSound("gerlaser");
    SoundEvent GER_LASER_FIRE = registerSound("gerlaserfire");
    SoundEvent GER_KICKBARRAGE = registerSound("gerkickbarrage");
    SoundEvent GER_SETUP = registerSound("gersetup");
    SoundEvent GER_FLY = registerSound("gerfly");
    SoundEvent GER_RTZ = registerSound("gerrtz");

    //The Fool
    SoundEvent FOOL_BARK1 = registerSound("foolbark1");
    SoundEvent FOOL_BARK2 = registerSound("foolbark2");
    SoundEvent FOOL_LAUNCH = registerSound("foollaunch");
    SoundEvent FOOL_CHARGE = registerSound("foolcharge");
    SoundEvent FOOL_ULT = registerSound("foolultimate");
    SoundEvent FOOL_GLIDE = registerSound("foolglide");

    //C-Moon
    SoundEvent CMOON_SUMMON = registerSound("cmoonsummon");
    SoundEvent CMOON_BARRAGE = registerSound("cmoonbarrage");
    SoundEvent CMOON_GRAV_PUNCH = registerSound("cmoongravpunch");
    SoundEvent CMOON_GRAV_PUNCH_HIT = registerSound("cmoongravpunchhit");
    SoundEvent CMOON_GROUNDSLAM = registerSound("cmoongroundslam");
    SoundEvent CMOON_GRAVSHIFT = registerSound("cmoongravshift");
    SoundEvent CMOON_GRAVSHIFT_DIRECTIONAL = registerSound("cmoondirectionalshift");
    SoundEvent CMOON_DONUT = registerSound("cmoondonut");
    SoundEvent CMOON_GROUNDSHOOT = registerSound("cmoongroundshoot");
    SoundEvent CMOON_BLOCKLAUNCH = registerSound("blocklaunch");
    SoundEvent CMOON_BLOCKHALT = registerSound("blockhalt");

    //Made in Heaven
    SoundEvent MIH_SUMMON = registerSound("mihsummon");
    SoundEvent MIH_BARRAGE = registerSound("mihbarrage");
    SoundEvent MIH_ZOOM = registerSound("mihzoom");
    SoundEvent MIH_JUDGEMENT = registerSound("mihjudgement");
    SoundEvent MIH_TACCEL = registerSound("mihtaccel");
    SoundEvent MIH_FURYCHOP = registerSound("mihfurychop");
    SoundEvent MIH_SPEEDSLICE = registerSound("mihspeedslice");
    SoundEvent MIH_LEGCRUSHER = registerSound("mihlegcrusher");
    SoundEvent MIH_CIRCLE = registerSound("mihcircle");

    //The World: Over Heaven
    SoundEvent TWOH_SUMMON = registerSound("twohsummon");
    SoundEvent TWOH_BARRAGE = registerSound("twohbarrage");
    SoundEvent TWOH_SHOOT = registerSound("twohshoot");
    SoundEvent TWOH_TIMESKIP = registerSound("twohtimeskip");
    SoundEvent TWOH_TS = registerSound("twohtimestop");
    SoundEvent TWOH_HEAVY = registerSound("twohheavy");
    SoundEvent TWOH_SINGULARITY = registerSound("twohsingularity");
    SoundEvent TWOH_SMITE = registerSound("twohsmite");
    SoundEvent TWOH_CHARGE_OVERWRITE = registerSound("twohchargeoverwrite");
    SoundEvent TWOH_CHARGE = registerSound("twohcharge");
    SoundEvent TWOH_OVERWRITE = registerSound("twohoverwrite");
    SoundEvent TWOH_KNIFETHROW = registerSound("twohowarida");
    SoundEvent TWOH_AIRKNIVES = registerSound("twohairknives");

    // Star Platinum: The World
    SoundEvent SPTW_GRAB = registerSound("sptwgrab");
    SoundEvent SPTW_GRABHIT = registerSound("sptwgrabhit");
    SoundEvent SPTW_UPPERCUT = registerSound("sptwuppercut");
    SoundEvent SPTW_BACKHAND = registerSound("sptwbackhand");

    // Purple Haze
    SoundEvent PH_SUMMON = registerSound("phsummon");
    SoundEvent PH_BARRAGE = registerSound("phbarrage");
    SoundEvent PH_GRAB_HIT = registerSound("phgrabhit");
    SoundEvent PH_REKKA1 = registerSound("phrekka1");
    SoundEvent PH_REKKA2 = registerSound("phrekka2");
    SoundEvent PH_REKKA3 = registerSound("phrekka3");
    SoundEvent PH_CAPSULE1 = registerSound("phcapsule1");
    SoundEvent PH_CAPSULE2 = registerSound("phcapsule2");
    SoundEvent PH_GROUNDSLAM = registerSound("phgroundslam");
    SoundEvent PH_ULTIMATE = registerSound("phultimate");

    // The Sun
    SoundEvent SUN_SUMMON = registerSound("sunsummon");
    SoundEvent SUN_SHOWER = registerSound("sunshower");
    SoundEvent SUN_BEAM_RAY = registerSound("sunbeamray");
    SoundEvent SUN_METEOR_FIRE = registerSound("sunmeteorfire");
    SoundEvent SUN_IDLE = registerSound("sunidle");

    //// SPECS
    // Brawler

    // Anubis
    SoundEvent ANUBIS_SLASH = registerSound("anubisslash");
    SoundEvent ANUBIS_POMMEL = registerSound("anubispommel");
    SoundEvent ANUBIS_SHEATHE = registerSound("anubissheathe");
    SoundEvent ANUBIS_UNSHEATHE = registerSound("anubisunsheathe");
    SoundEvent ANUBIS_SPECCHANGE = registerSound("anubisspecchange");
    SoundEvent ANUBIS_REKKA2 = registerSound("anubisrekka2");
    SoundEvent ANUBIS_REKKA3 = registerSound("anubisrekka3"); //todo: 3 hit rekka sound for anubis

    // Vampire
    SoundEvent VAMPIRE_LASER = registerSound("vampirelaser");
    SoundEvent VAMPIRE_LASER_FIRE = registerSound("vampirelaserfire");
    SoundEvent VAMPIRE_GRAB_HIT = registerSound("vampiregrabhit");
    SoundEvent VAMPIRE_SUCK = registerSound("vampiresuck");
    SoundEvent VAMPIRE_REANIMATE = registerSound("vampirereanimate");

    //// OTHER
    SoundEvent BULLET_RICOCHET = registerSound("bulletricochet");
    SoundEvent BULLET_PENETRATE = registerSound("bulletpenetrate");

    static void registerSounds() {

    }
}
