package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface JSoundRegister {

    static SoundEvent registerSound(String id) {
        //JCraft.LOGGER.info("Registering sound: " + id);
        SoundEvent event = new SoundEvent(new Identifier(JCraft.MOD_ID, id));
        Registry.register(Registry.SOUND_EVENT, id, event);
        return event;
    }

    // Generic
    SoundEvent STAND_SUMMON = registerSound("standsummon");
    SoundEvent STAND_DESUMMON = registerSound("desummon");
    SoundEvent STAND_BLOCK = registerSound("standblock");
    SoundEvent COMBO_BREAK = registerSound("combobreak");
    SoundEvent COOLDOWN_CANCEL = registerSound("cooldowncancel");
    SoundEvent IMPACT_1 = registerSound("impact1");
    SoundEvent IMPACT_2 = registerSound("impact2");
    SoundEvent IMPACT_3 = registerSound("impact3");
    SoundEvent IMPACT_4 = registerSound("impact4");
    SoundEvent IMPACT_5 = registerSound("impact5");
    SoundEvent TIME_SKIP = registerSound("timeskip");
    SoundEvent COIN_TOSS = registerSound("cointoss");

    // Star Platinum
    SoundEvent STAR_PLATINUM_SUMMON = registerSound("spsummon");
    SoundEvent STAR_PLATINUM_TIMESKIP = registerSound("sptimeskip");
    SoundEvent STAR_PLATINUM_BARRAGE = registerSound("spbarrage");
    SoundEvent STAR_PLATINUM_ADVANCING_BARRAGE = registerSound("spadvbarrage");
    SoundEvent STAR_PLATINUM_THE_WORLD = registerSound("sptw");
    SoundEvent STAR_PLATINUM_KICK = registerSound("spkick");
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

    //Killer Queen: Bites The Dust
    SoundEvent KQBTD_ELBOW = registerSound("kqbtdelbow");
    SoundEvent KQBTD_SUMMON = registerSound("kqbtdsummon");

    //Whitesnake
    SoundEvent WS_SUMMON = registerSound("wssummon");
    SoundEvent WS_BARRAGE = registerSound("wsbarrage");
    SoundEvent WS_LEGCRUSH = registerSound("wslegcrush");
    SoundEvent WS_DONUT = registerSound("wsdonut");
    SoundEvent WS_DISK = registerSound("wsdisk");
    SoundEvent WS_GUN = registerSound("wsgun");

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

    //Golden Experience: Requiem
    SoundEvent GER_SUMMON = registerSound("gersummon");
    SoundEvent GER_HEAVY = registerSound("gerheavy");
    SoundEvent GER_LASER = registerSound("gerlaser");
    SoundEvent GER_SLOW_LASER = registerSound("gerslowlaser");
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
    SoundEvent CMOON_GRAVPUNCH = registerSound("cmoongravpunch");
    SoundEvent CMOON_GRAVPUNCHHIT = registerSound("cmoongravpunchhit");
    SoundEvent CMOON_GROUNDSLAM = registerSound("cmoongroundslam");
    SoundEvent CMOON_GRAVSHIFT = registerSound("cmoongravshift");
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
    SoundEvent TWOH_SMITE = registerSound("twohsmite");
    SoundEvent TWOH_CHARGEOVERWRITE = registerSound("twohchargeoverwrite");
    SoundEvent TWOH_CHARGE = registerSound("twohcharge");
    SoundEvent TWOH_OVERWRITE = registerSound("twohoverwrite");
    SoundEvent TWOH_KNIFETHROW = registerSound("twohowarida");
    SoundEvent TWOH_AIRKNIVES = registerSound("twohairknives");

    static void registerSounds() {

    }
}
