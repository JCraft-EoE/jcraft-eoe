# Changelog
## General
* **Added Hamon**
* **Added Stand Throwing**
* **Added Block Breaker System**
* **Added Aerosmith**
* **Cosplay has been moved to its own mod**
* "Kill Vampirism" has been renamed to "Heal On Kill" to reduce the confusion
* updated Azurelib version to latest
### Blocks & Items
* removed JCraft items being added to vanilla creative tabs because that led to a doubling in the search
* removed items without any use from the creative tabs; they are still accessible via `/give`
* removed Steel Ball recipe
* added Rewind Mock Item
  * once a Mandom Rewind is over, can be used to be resolved
  * gets resolved to either air or its original form, depending if the block it spawned from was reset or not
* added Matches and Gas Can (both can be crafted)
  * Road Roller Recipe now requires Gas Can
* added recipes for Bullet and Peacemaker
* added proper map colors for JCraft blocks
* halved blood bottle gain/depletion rate
* get GO or DO particles depending if stand user players or stand user mobs are nearby
### NPCs & Stands
* Aerosmith
  * new Stand obtainable via usual methods like Stand Arrow 
* improved combat AI
* Whitesnake
  * Poison Spew cooldowns increased by 4s
  * Poison Spew Projectile no longer interrupts moves
  * can now steal stands for real
* Mandom
  * now also resets the air (bubbles) of entities
  * now also rewinds blocks (no dupes totally sure yes yes)
  * particle color changed according to skin and shader added
* Sun respects build perms better now
* added Monks (`hamon_spec_user`) and Tonpetty (see Hamon section)
* D4C clones don't drop XP anymore if summoned by players
* King Crimson
  * clones are now weaker and disappear more consistently
  * entities in Time Erase are not being affected by Acid Splatters, potions and effects anymore
  * Time Erase stops if you are using items of any kind or Stand Throwing
* KQ bubble more transparent with distance, doubled cooldown
* entities resurrected by Vampirism don't drop XP or loot anymore
* Metallica 
  * now has some sweet mosh particles
  * magnetic field rips out ferrous items from the ground when created
  * now has its own HUD icons
* slight changes to Horus' and SHA's hitbox
* Brawler
  * will fight Training Dummies on sight
  * taking away his dummy will aggro him
  * drops his Boxing Gloves on death
* Anubis Spec User now drops Anubis on death
* Drowned and Spec Users might now spawn with Stands by default
* disabled item usage while Stand blocking
* sounds heard by Stands are now redirected to the user
### Effects
* new Flammable effect
  * obtained by being dowsed in gasoline
### Structures
* added the Monastery
* added Anubis Temple
* improved Vampire Lair and fixed the broken loot tables
* added proper placement tags for all JCraft structures, i.e. you can now choose their biomes via datapacks
* added Boxing Ring to villages
* Cartographer now sells maps to JCraft structures 
### Game Rules
* added game rule `dropStandAsDisc` (off by default) that makes Stand Users drop their Stand as a disc on death
### Configs
* added different ways how long the Move UI should be displayed, including Always and Never (default as it was, client side)
* Mandom can affect blocks or not (default it does, server config)
* Whitesnake can steal stands from players or not (default it doesn't, server config)
### Compatibility
* made the mod compatible with FTB Chunks
### Commands
* `/spec reset @s` resets your spec as if you just first obtained it
* `/stand about` now isn't global anymore when you have no stand summoned
* added a notification to actually use `/stand about` when first stand is summoned, same for `/stand about`
* added `/jwiki` command
### Tags
* new tag `jcraft:bloodless_entities` for entities that cannot be bloodsucked by Vampires
* new tag `jcraft:ironless_entities` for entities that cannot be ironsucked by Metallica
* new tag `jcraft:unaffected_by_epitaph` for entites that don't show up on Epitaph
* new tag `jcraft:doesnt_breathe` for entities not detectable by Aerosmith's BreathXRay
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
* Stands can no longer catch on fire (#95)
* fixed The Hand rendering (#48)
* fixed Stand Meteor not having an entity name (#108)
* fixed an Armor dupe bug with The Fool
* fixed Spec not clearing on Client when removed
* fixed WS Acid Splatter affecting Creative players
* fixed an invulnerability bug with Cream
* fixed Lithium incompatibility
* fixed shot Stand Arrows being invisible from certain angles (#88)
* fixed Cream Ult being slowed down by blocks and improved its flight
* fixed HG Ult icon not displaying
* fixed the fish bucket bug
## Hamon
* first spec to have progression (see Commands section to skip those)
* Tonpetti is a Neutral Boss Mob
## Stand Throwing
* Humanoid Stands can now throw items the player is holding
* depending on the item and what is hit (block or entity) different things might happen
## Block Breaker System
* barrage mining has been removed, all Stand/Spec attacks now do damage to blocks
## Cosplay
* everything except red hat has been moved to its own mod, JJBA Cosplay
* your cosplay shouldn't get lost **IF** you install the other mod together with 0.18.0
* IF the cosplay mod is installed, Stand user mobs will spawn with cosplay on
* for more news on cosplay see changelog of JJBA Cosplay
## Known Bugs
* see https://github.com/JCraft-EoE/jcraft-eoe/issues?q=is%3Aissue%20state%3Aopen%20type%3ABug




## TODO (SOME UPDATE) :D
* Spin
* MR barrage fire :)
* Timestop should stop stand anims
* CRAZY DIAMOND, Hermit Purple, Yellow Temperance
* Actually use effect keyframes in animations
