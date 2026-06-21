# Changelog
## General
### Stands & Specs
* Disabled damage scaling for bosses and horses
* Throwing now force-feeds potions
* Throwing items are now transparent while being hold by Stand
* Breaking Blocks now only triggers when nothing else was hit
* Aerosmith
  * now heats up blocks, i.e. makes them work temporarily like Magma Blocks
  * BreathXRay now respects Hypoxia levels
  * only displays landing gear when starting/landing
  * improved the Ult mechanics
  * added glowing to AS when it is out of sight for the user
  * getting stunned while AS is on Autopilot influences AS now
  * added particles to show which entity is targeted by AS
* Made In Heaven
  * Sprinting while Stand is out
    * gives Speed 70
    * creates Hunger (configurable)
    * gives Water Walking and Dolphin's Grace
    * gives Exhaustion
  * improved Hit Detection for MIH Cr Sp 3
* Added description for Hamon Charge in `/spec about`
* Missing with Stands/Specs now makes miss sounds (whiff)
### Effects
* Exhaustion I to IV, works against regeneration from 25% to 100%
### Structures
* made Vampire Lairs rarer
### Configs
* `mih_sprint_hunger_multiplier` how much Hunger is created by MIH sprinting
* Vampires taking extra damage by Hamon Users is now on by default
* `show_all_cosplay` has been removed, as it doesn't serve any purpose anymore
* Added tooltips to all server config options
* `block_breakage_multiplier` to scale the effectiveness of Block Breaking
* Server config is now part of the API (for add-ons)
### Tags
* `jcraft:bosses` describes what constitutes bosses for JCraft purposes (for now Ender Dragon, Wither, Warden, Elder Guardian and Tonpetty) 
* `jcraft:ignores_damage_scaling` describes which entities ignore Stand/Spec damage scaling (for now bosses and horses)
### Bug Fixes
* Fixed missing translation for Anubis Special 2 and Hamon Charge
* Fixed AS bomb double damage
* Fixed AS detecting and following Spectators
* Fixed AS getting stuck shooting on Multiplayer
* Fixed PH Poison damage attribution
* Fixed bound sounds lingering when spamming
* Fixed Spec moves holding when Stand is out
* Fixed Stand throwing into claims
* Fixed D4C explosions not respecting claims
* Fixed an absurd jump boost by using The Fool with Elytra
* Disabled Pick Block only for stands with a Toss Move
* Fixed an NPE

## Known Bugs
* see https://github.com/JCraft-EoE/jcraft-eoe/issues?q=is%3Aissue%20state%3Aopen%20type%3ABug




## TODO (SOME UPDATE) :D
* Spin
* MR barrage fire :)
* Timestop should stop stand anims
* CRAZY DIAMOND, Hermit Purple, Yellow Temperance
* Actually use effect keyframes in animations
