package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModSoundRegister {

    public static SoundEvent registerSound(Identifier id) {
        JCraft.LOGGER.info("Registering sound: " + id.toString());
        SoundEvent event = new SoundEvent(id);
        Registry.register(Registry.SOUND_EVENT, id, event);
        return event;
    }

    // Generic
    public static final Identifier STANDSUMMON = new Identifier("jcraft:standsummon");
    public static SoundEvent STAND_SUMMON;

    public static final Identifier DESUMMON = new Identifier("jcraft:desummon");
    public static SoundEvent STAND_DESUMMON;

    public static final Identifier STANDBLOCK = new Identifier("jcraft:standblock");
    public static SoundEvent STAND_BLOCK;

    public static final Identifier COMBOBREAK = new Identifier("jcraft:combobreak");
    public static SoundEvent COMBO_BREAK;

    public static final Identifier COOLDOWNCANCEL = new Identifier("jcraft:cooldowncancel");
    public static SoundEvent COOLDOWN_CANCEL;

    public static final Identifier IMPACT1 = new Identifier("jcraft:impact1");
    public static SoundEvent IMPACT_1;
    public static final Identifier IMPACT2 = new Identifier("jcraft:impact2");
    public static SoundEvent IMPACT_2;
    public static final Identifier IMPACT3 = new Identifier("jcraft:impact3");
    public static SoundEvent IMPACT_3;
    public static final Identifier IMPACT4 = new Identifier("jcraft:impact4");
    public static SoundEvent IMPACT_4;
    public static final Identifier IMPACT5 = new Identifier("jcraft:impact5");
    public static SoundEvent IMPACT_5;

    public static final Identifier TIMESKIP = new Identifier("jcraft:timeskip");
    public static SoundEvent TIME_SKIP;

    // Star Platinum
    public static final Identifier SPBARRAGE = new Identifier("jcraft:spbarrage");
    public static SoundEvent STAR_PLATINUM_BARRAGE;

    public static final Identifier SPADVBARRAGE = new Identifier("jcraft:spadvbarrage");
    public static SoundEvent STAR_PLATINUM_ADVANCING_BARRAGE;

    public static final Identifier SPTW = new Identifier("jcraft:sptw");
    public static SoundEvent STAR_PLATINUM_THE_WORLD;

    public static final Identifier SPKICK = new Identifier("jcraft:spkick");
    public static SoundEvent STAR_PLATINUM_KICK;

    public static final Identifier STARBREAKER = new Identifier("jcraft:starbreaker");
    public static SoundEvent STAR_BREAKER;

    public static final Identifier STARFINGER = new Identifier("jcraft:starfinger");
    public static SoundEvent STAR_FINGER;

    //King Crimson
    public static final Identifier KCSUMMON = new Identifier("jcraft:kcsummon");
    public static SoundEvent KC_SUMMON;

    public static final Identifier KCDUALCHOP = new Identifier("jcraft:kcdualchop");
    public static SoundEvent KC_DUAL_CHOP;

    public static final Identifier KCDONUT = new Identifier("jcraft:kcdonut");
    public static SoundEvent KC_DONUT;

    public static final Identifier KCBARRAGE = new Identifier("jcraft:kcbarrage");
    public static SoundEvent KC_BARRAGE;

    public static final Identifier KCHEAVY = new Identifier("jcraft:kcheavy");
    public static SoundEvent KC_HEAVY;

    public static final Identifier KCHEAVY2 = new Identifier("jcraft:kcheavy2");
    public static SoundEvent KC_HEAVY2;

    public static final Identifier EYECHOP = new Identifier("jcraft:eyechop");
    public static SoundEvent EYE_CHOP;

    public static final Identifier TETP = new Identifier("jcraft:tetp");
    public static SoundEvent TE_TP;

    public static final Identifier TIMEERASE = new Identifier("jcraft:timeerase");
    public static SoundEvent TIME_ERASE;

    public static final Identifier TIMEERASE_EXIT = new Identifier("jcraft:kcteexit");
    public static SoundEvent TIME_ERASE_EXIT;

    //The World
    public static final Identifier TWSUMMON = new Identifier("jcraft:twsummon");
    public static SoundEvent TW_SUMMON;

    public static final Identifier TWBARRAGE = new Identifier("jcraft:twbarrage");
    public static SoundEvent TW_BARRAGE;

    public static final Identifier TWTS = new Identifier("jcraft:twts");
    public static SoundEvent TW_TS;

    public static final Identifier TWCHARGE = new Identifier("jcraft:twcharge");
    public static SoundEvent TW_CHARGE;

    public static final Identifier TWCHARGEHIT = new Identifier("jcraft:twchargehit");
    public static SoundEvent TW_CHARGE_HIT;

    public static final Identifier TWDONUT = new Identifier("jcraft:twdonut");
    public static SoundEvent TW_DONUT;

    public static final Identifier TWDONUTHIT = new Identifier("jcraft:twdonuthit");
    public static SoundEvent TW_DONUT_HIT;

    public static final Identifier TWKICK = new Identifier("jcraft:twkick");
    public static SoundEvent TW_KICK;

    public static final Identifier TWKICKHIT = new Identifier("jcraft:twkickhit");
    public static SoundEvent TW_KICK_HIT;

    public static final Identifier TWCOUNTER = new Identifier("jcraft:twcounter");
    public static SoundEvent TW_COUNTER;

    public static final Identifier MUDADA = new Identifier("jcraft:mudada");
    public static SoundEvent MUDA_DA;

    //Dirty Deeds Done Dirt Cheap
    public static final Identifier D4CSUMMON = new Identifier("jcraft:d4csummon");
    public static SoundEvent D4C_SUMMON;

    public static final Identifier D4CLIGHT = new Identifier("jcraft:d4clight");
    public static SoundEvent D4C_LIGHT;

    public static final Identifier D4CHEAVY = new Identifier("jcraft:d4cheavy");
    public static SoundEvent D4C_HEAVY;

    public static final Identifier D4CBARRAGE = new Identifier("jcraft:d4cbarrage");
    public static SoundEvent D4C_BARRAGE;

    public static final Identifier D4CDIMHOP = new Identifier("jcraft:d4cdimhop");
    public static SoundEvent D4C_DIMHOP;

    public static final Identifier REVOLVERFIRE = new Identifier("jcraft:revolverfire");
    public static SoundEvent REVOLVER_FIRE;

    public static final Identifier D4CTHROW = new Identifier("jcraft:d4cthrow");
    public static SoundEvent D4C_THROW;

    public static final Identifier D4CCOUNTER = new Identifier("jcraft:d4ccounter");
    public static SoundEvent D4C_COUNTER;

    //Cream
    public static final Identifier CREAMCONSUME = new Identifier("jcraft:creamconsume");
    public static SoundEvent CREAM_CONSUME;

    public static final Identifier CREAMCHARGE = new Identifier("jcraft:creamcharge");
    public static SoundEvent CREAM_CHARGE;

    public static final Identifier CREAMCOMBO = new Identifier("jcraft:creamcombo");
    public static SoundEvent CREAM_COMBO;

    public static final Identifier CREAMHEAVY = new Identifier("jcraft:creamheavy");
    public static SoundEvent CREAM_HEAVY;

    public static final Identifier CREAMGRAB = new Identifier("jcraft:creamgrab");
    public static SoundEvent CREAM_GRAB;

    public static final Identifier CREAMSMASH = new Identifier("jcraft:creamsmash");
    public static SoundEvent CREAM_SMASH;

    public static final Identifier CREAMENTER = new Identifier("jcraft:creamenter");
    public static SoundEvent CREAM_ENTER;

    public static final Identifier CREAMEXIT = new Identifier("jcraft:creamexit");
    public static SoundEvent CREAM_EXIT;

    //Killer Queen
    public static final Identifier KQHEAVY = new Identifier("jcraft:kqheavy");
    public static SoundEvent KQ_HEAVY;

    public static final Identifier KQBARRAGE = new Identifier("jcraft:kqbarrage");
    public static SoundEvent KQ_BARRAGE;

    public static final Identifier KQDETONATE = new Identifier("jcraft:kqdetonate");
    public static SoundEvent KQ_DETONATE;

    public static final Identifier KQUPPERCUT = new Identifier("jcraft:kquppercut");
    public static SoundEvent KQ_UPPERCUT;

    //Killer Queen: Bites The Dust
    public static final Identifier KQBTDELBOW = new Identifier("jcraft:kqbtdelbow");
    public static SoundEvent KQBTD_ELBOW;

    public static final Identifier KQBTDSUMMON = new Identifier("jcraft:kqbtdsummon");
    public static SoundEvent KQBTD_SUMMON;

    //Whitesnake
    public static final Identifier WSBARRAGE = new Identifier("jcraft:wsbarrage");
    public static SoundEvent WS_BARRAGE;

    public static final Identifier WSLEGCRUSH = new Identifier("jcraft:wslegcrush");
    public static SoundEvent WS_LEGCRUSH;

    public static final Identifier WSDONUT = new Identifier("jcraft:wsdonut");
    public static SoundEvent WS_DONUT;

    public static final Identifier WSDISK = new Identifier("jcraft:wsdisk");
    public static SoundEvent WS_DISK;

    public static final Identifier WSGUN = new Identifier("jcraft:wsgun");
    public static SoundEvent WS_GUN;

    //Silver Chariot
    public static final Identifier SCSUMMON = new Identifier("jcraft:scsummon");
    public static SoundEvent SC_SUMMON;

    public static final Identifier SCBARRAGE = new Identifier("jcraft:scbarrage");
    public static SoundEvent SC_BARRAGE;

    public static final Identifier SCCHARGE = new Identifier("jcraft:sccharge");
    public static SoundEvent SC_CHARGE;

    public static final Identifier SCHEAVY = new Identifier("jcraft:scheavy");
    public static SoundEvent SC_HEAVY;

    public static final Identifier SCSPIN = new Identifier("jcraft:scspin");
    public static SoundEvent SC_SPIN;

    //Magicians Red

    //Golden Experience
    public static final Identifier GESUMMON = new Identifier("jcraft:gesummon");
    public static SoundEvent GE_SUMMON;

    public static final Identifier GEBARRAGE = new Identifier("jcraft:gebarrage");
    public static SoundEvent GE_BARRAGE;

    public static final Identifier GEHEAL = new Identifier("jcraft:geheal");
    public static SoundEvent GE_HEAL;

    public static final Identifier GETREE = new Identifier("jcraft:getree");
    public static SoundEvent GE_TREE;

    //Golden Experience: Requiem
    public static final Identifier GERSUMMON = new Identifier("jcraft:gersummon");
    public static SoundEvent GER_SUMMON;
    public static final Identifier GERHEAVY = new Identifier("jcraft:gerheavy");
    public static SoundEvent GER_HEAVY;
    public static final Identifier GERLASER = new Identifier("jcraft:gerlaser");
    public static SoundEvent GER_LASER;
    public static final Identifier GERKICKBARRAGE = new Identifier("jcraft:gerkickbarrage");
    public static SoundEvent GER_KICKBARRAGE;
    public static final Identifier GERSETUP = new Identifier("jcraft:gersetup");
    public static SoundEvent GER_SETUP;

    //The Fool
    public static final Identifier FOOLBARK1 = new Identifier("jcraft:foolbark1");
    public static SoundEvent FOOL_BARK1;
    public static final Identifier FOOLBARK2 = new Identifier("jcraft:foolbark2");
    public static SoundEvent FOOL_BARK2;

    public static final Identifier FOOLLAUNCH = new Identifier("jcraft:foollaunch");
    public static SoundEvent FOOL_LAUNCH;

    public static final Identifier FOOLCHARGE = new Identifier("jcraft:foolcharge");
    public static SoundEvent FOOL_CHARGE;

    //C-Moon
    public static final Identifier CMOONGRAVPUNCH = new Identifier("jcraft:cmoongravpunch");
    public static SoundEvent CMOON_GRAVPUNCH;

    public static final Identifier CMOONGRAVPUNCHHIT = new Identifier("jcraft:cmoongravpunchhit");
    public static SoundEvent CMOON_GRAVPUNCHHIT;

    public static final Identifier CMOONGROUNDSLAM = new Identifier("jcraft:cmoongroundslam");
    public static SoundEvent CMOON_GROUNDSLAM;

    public static final Identifier CMOONGRAVSHIFT = new Identifier("jcraft:cmoongravshift");
    public static SoundEvent CMOON_GRAVSHIFT;

    public static final Identifier CMOONDONUT = new Identifier("jcraft:cmoondonut");
    public static SoundEvent CMOON_DONUT;

    //Made in Heaven
    public static final Identifier MIHZOOM = new Identifier("jcraft:mihzoom");
    public static SoundEvent MIH_ZOOM;

    public static final Identifier MIHJUDGEMENT = new Identifier("jcraft:mihjudgement");
    public static SoundEvent MIH_JUDGEMENT;

    public static final Identifier MIHTACCEL = new Identifier("jcraft:mihtaccel");
    public static SoundEvent MIH_TACCEL;

    public static final Identifier MIHFURYCHOP = new Identifier("jcraft:mihfurychop");
    public static SoundEvent MIH_FURYCHOP;

    public static final Identifier MIHSPEEDSLICE = new Identifier("jcraft:mihspeedslice");
    public static SoundEvent MIH_SPEEDSLICE;

    //The World: Over Heaven
    public static final Identifier TWOHSUMMON = new Identifier("jcraft:twohsummon");
    public static SoundEvent TWOH_SUMMON;

    public static final Identifier TWOHSHOOT = new Identifier("jcraft:twohshoot");
    public static SoundEvent TWOH_SHOOT;

    public static final Identifier TWOHTS = new Identifier("jcraft:twohts");
    public static SoundEvent TWOH_TS;

    public static final Identifier TWOHHEAVY = new Identifier("jcraft:twohheavy");
    public static SoundEvent TWOH_HEAVY;

    public static final Identifier TWOHSMITE = new Identifier("jcraft:twohsmite");
    public static SoundEvent TWOH_SMITE;

    public static final Identifier TWOHOVERWRITE = new Identifier("jcraft:twohoverwrite");
    public static SoundEvent TWOH_OVERWRITE;

    public static final Identifier TWOHKNIFETHROW = new Identifier("jcraft:twohowarida");
    public static SoundEvent TWOH_KNIFETHROW;

    public static final Identifier TWOHAIRKNIVES = new Identifier("jcraft:twohairknives");
    public static SoundEvent TWOH_AIRKNIVES;

    public static void registerSounds() {
        STAND_SUMMON = registerSound(STANDSUMMON);
        STAND_DESUMMON = registerSound(DESUMMON);
        STAND_BLOCK = registerSound(STANDBLOCK);

        COMBO_BREAK = registerSound(COMBOBREAK);
        COOLDOWN_CANCEL = registerSound(COOLDOWNCANCEL);

        IMPACT_1 = registerSound(IMPACT1);
        IMPACT_2 = registerSound(IMPACT2);
        IMPACT_3 = registerSound(IMPACT3);
        IMPACT_4 = registerSound(IMPACT4);
        IMPACT_5 = registerSound(IMPACT5);

        TIME_SKIP = registerSound(TIMESKIP);

        STAR_PLATINUM_BARRAGE = registerSound(SPBARRAGE);
        STAR_PLATINUM_ADVANCING_BARRAGE = registerSound(SPADVBARRAGE);
        STAR_PLATINUM_THE_WORLD = registerSound(SPTW);
        STAR_PLATINUM_KICK = registerSound(SPKICK);
        STAR_BREAKER = registerSound(STARBREAKER);
        STAR_FINGER = registerSound(STARFINGER);

        KC_SUMMON = registerSound(KCSUMMON);
        KC_DUAL_CHOP = registerSound(KCDUALCHOP);
        KC_DONUT = registerSound(KCDONUT);
        KC_BARRAGE = registerSound(KCBARRAGE);
        KC_HEAVY = registerSound(KCHEAVY);
        KC_HEAVY2 = registerSound(KCHEAVY2);
        EYE_CHOP = registerSound(EYECHOP);
        TE_TP = registerSound(TETP);
        TIME_ERASE = registerSound(TIMEERASE);
        TIME_ERASE_EXIT = registerSound(TIMEERASE_EXIT);

        TW_SUMMON = registerSound(TWSUMMON);
        TW_BARRAGE = registerSound(TWBARRAGE);
        TW_TS = registerSound(TWTS);
        TW_CHARGE = registerSound(TWCHARGE);
        TW_CHARGE_HIT = registerSound(TWCHARGEHIT);
        TW_DONUT = registerSound(TWDONUT);
        TW_DONUT_HIT = registerSound(TWDONUTHIT);
        TW_KICK = registerSound(TWKICK);
        TW_KICK_HIT = registerSound(TWKICKHIT);
        TW_COUNTER = registerSound(TWCOUNTER);
        MUDA_DA = registerSound(MUDADA);

        D4C_SUMMON = registerSound(D4CSUMMON);
        D4C_LIGHT = registerSound(D4CLIGHT);
        D4C_HEAVY = registerSound(D4CHEAVY);
        D4C_BARRAGE = registerSound(D4CBARRAGE);
        D4C_DIMHOP = registerSound(D4CDIMHOP);
        REVOLVER_FIRE = registerSound(REVOLVERFIRE);
        D4C_THROW = registerSound(D4CTHROW);
        D4C_COUNTER = registerSound(D4CCOUNTER);

        CREAM_CONSUME = registerSound(CREAMCONSUME);
        CREAM_CHARGE = registerSound(CREAMCHARGE);
        CREAM_COMBO = registerSound(CREAMCOMBO);
        CREAM_HEAVY = registerSound(CREAMHEAVY);
        CREAM_GRAB = registerSound(CREAMGRAB);
        CREAM_SMASH = registerSound(CREAMSMASH);
        CREAM_ENTER = registerSound(CREAMENTER);
        CREAM_EXIT = registerSound(CREAMEXIT);

        KQ_HEAVY = registerSound(KQHEAVY);
        KQ_BARRAGE = registerSound(KQBARRAGE);
        KQ_DETONATE = registerSound(KQDETONATE);
        KQ_UPPERCUT = registerSound(KQUPPERCUT);

        KQBTD_ELBOW = registerSound(KQBTDELBOW);
        KQBTD_SUMMON = registerSound(KQBTDSUMMON);

        WS_BARRAGE = registerSound(WSBARRAGE);
        WS_LEGCRUSH = registerSound(WSLEGCRUSH);
        WS_DONUT = registerSound(WSDONUT);
        WS_DISK = registerSound(WSDISK);
        WS_GUN = registerSound(WSGUN);

        FOOL_BARK1 = registerSound(FOOLBARK1);
        FOOL_BARK2 = registerSound(FOOLBARK2);
        FOOL_LAUNCH = registerSound(FOOLLAUNCH);
        FOOL_CHARGE = registerSound(FOOLCHARGE);

        GE_SUMMON = registerSound(GESUMMON);
        GE_BARRAGE = registerSound(GEBARRAGE);
        GE_HEAL = registerSound(GEHEAL);
        GE_TREE = registerSound(GETREE);

        GER_SUMMON = registerSound(GERSUMMON);
        GER_HEAVY = registerSound(GERHEAVY);
        GER_LASER = registerSound(GERLASER);
        GER_KICKBARRAGE = registerSound(GERKICKBARRAGE);
        GER_SETUP = registerSound(GERSETUP);

        SC_SUMMON = registerSound(SCSUMMON);
        SC_BARRAGE = registerSound(SCBARRAGE);
        SC_CHARGE = registerSound(SCCHARGE);
        SC_HEAVY = registerSound(SCHEAVY);
        SC_SPIN = registerSound(SCSPIN);

        CMOON_GRAVPUNCH = registerSound(CMOONGRAVPUNCH);
        CMOON_GRAVPUNCHHIT = registerSound(CMOONGRAVPUNCHHIT);
        CMOON_GROUNDSLAM = registerSound(CMOONGROUNDSLAM);
        CMOON_GRAVSHIFT = registerSound(CMOONGRAVSHIFT);
        CMOON_DONUT = registerSound(CMOONDONUT);

        MIH_ZOOM = registerSound(MIHZOOM);
        MIH_JUDGEMENT = registerSound(MIHJUDGEMENT);
        MIH_TACCEL = registerSound(MIHTACCEL);
        MIH_FURYCHOP = registerSound(MIHFURYCHOP);
        MIH_SPEEDSLICE = registerSound(MIHSPEEDSLICE);

        TWOH_SUMMON = registerSound(TWOHSUMMON);
        TWOH_SHOOT = registerSound(TWOHSHOOT);
        TWOH_TS = registerSound(TWOHTS);
        TWOH_HEAVY = registerSound(TWOHHEAVY);
        TWOH_SMITE = registerSound(TWOHSMITE);
        TWOH_OVERWRITE = registerSound(TWOHOVERWRITE);
        TWOH_KNIFETHROW = registerSound(TWOHKNIFETHROW);
        TWOH_AIRKNIVES = registerSound(TWOHAIRKNIVES);
    }
}
