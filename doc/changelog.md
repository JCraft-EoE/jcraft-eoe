# Changelog
## General
* **Added Hamon**
* **Cosplay has been moved to its own mod**
* "Kill Vampirism" has been renamed to "Heal On Kill" to reduce the confusion
* updated Azurelib version to latest
### Blocks & Items
* removed JCraft items being added to vanilla creative tabs because that lead to a doubling in the search
* removed items without any use from the creative tabs; they are still accessible via `/give`
* removed Steel Ball recipe
* added Rewind Mock Item
  * once a Mandom Rewind is over, can be used to be resolved
  * gets resolved to either air or its original form, depending if the block it spawned from was reset or not
* get GO or DO particles depending if stand user players or stand user mobs are nearby
### NPCs & Stands
* improved combat AI
* Whitesnake
  * Poison Spew cooldowns increased by 4s
  * Poison Spew Projectile no longer interrupts moves
  * can now steal stands for real
* Mandom
  * now also resets the air (bubbles) of entities
  * now also rewinds blocks (no dupes totally sure yes yes)
  * particle color changed according to skin and shader added
* added Monks (`hamon_spec_user`) and Tonpetty (see Hamon section)
* D4C clones don't drop XP anymore if summoned by players
* entities resurrected by Vampirism don't drop XP or loot anymore
* Metallica now has some sweet mosh particles
* slight changes to Horus' hitbox
* Brawler
  * will fight Training Dummies on sight
  * taking away his dummy will aggro him
* Anubis Spec User now drops Anubis on death
### Structures
* added the monastery
* added Anubis temple
* added proper placement tags for all JCraft structures, i.e. you can now choose their biomes via datapacks
### Configs
* added different ways how long the Move UI should be displayed, including Always and Never (default as it was, client side)
* Mandom can affect blocks or not (default it does, server config)
* Whitesnake can steal stands from players or not (default it doesn't, server config)
### Compatibility
* made the mod compatible with FTB Chunks
### Commands
* `/spec reset @s` resets your spec as if you just first obtained it
* `/stand about` now isn't global anymore when you have no stand summoned
* added a notification to actually use `/stand about` when first stand is summoned
### Tags
* new tag `jcraft:bloodless_entities` for entities that cannot be bloodsucked by Vampires
* new tag `jcraft:ironless_entities` for entities that cannot be ironsucked by Metallica
### Bug Fixes
* Anvil cannot consume multiple Cinderella masks at once anymore
* Cinderella Enchantments are no longer additive but behave like other enchantments now
* fixed STW's desummon animation
* fixed idle and blocking animations not playing sometimes
* fixed Gold Experience's Snake not animating movement
* Brawler spec user no longer attacks villagers
* fixed Stone Mask not spawning in Vampire Lairs
* Training Dummy can no longer be abused by Vampires and Metallica users
* Leash of Training Dummy now drops if it is picked up
* fixed rare crash with inhale attack
* Road Roller can no longer get stands
* added names to Brawler and Anubis Spec User
* Horus Frostwalker now respects Stand Griefing rule
* GE Berry Bush attack now respects Stand Griefing rule
## Hamon
* first spec to have progression (see Commands section to skip those)
* [insert information about the moves]
## Cosplay
* everything except red hat has been moved to its own mod, JJBA Cosplay
* your cosplay shouldn't get lost **IF** you install the other mod together with 0.18.0
* IF the cosplay mod is installed, Stand user mobs will spawn with cosplay on
* for more news on cosplay see changelog of JJBA Cosplay
## Known Bugs
* …




## TODO (SOME UPDATE) :D
* Spin
* Throwing
* MR barrage fire :)
* Timestop should stop stand anims
* CRAZY DIAMOND, Hermit Purple, Yellow Temperance
* Actually use effect keyframes in animations
