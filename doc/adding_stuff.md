# Adding stuff
## Adding a Block
1. Add the block to `JBlockRegistry`.
2. Add its item variant to `JItemRegistry` using the same ID.
3. Add an English translation of the block _and_ its item variant to `en_us.json`.
4. Add the model to `JModelProvider`.
5. If the block drops, add it to `JLootTableProviders.BlockLoot`.
6. If the block mining requires a specific tool or mining level, add these to `JTagProviders.JBlockTags`.
7. If additional block or item tags apply, add them in `JTagProviders` as well.
8. If the block is involved in recipes, add them to `JRecipeProvider`.
9. If getting the block is worth an advancement, add it to `JAdvancementProvider`.
10. Run datagen.
11. Add the block to `JCreativeMenuTabRegistry` to the JCraft tab and possibly to other tabs as well.
12. Add the texture(s) for the block in `textures/block`.
13. Test your addition.

## Adding a Damage Type
1. Declare the ResourceKey using `JDamageSources.createDamageType()`.
2. Add a .JSON file to `resources/data/jcraft/damage_type` of the same name as the damage type.
3. Test your addition.

## Adding a Spawn Egg
1. Add the spawn egg with its two colors to `JItemRegistry`.
2. Add an English translation of the spawn egg to `en_us.json`.
3. Add the model to `JModelProvider`.
4. Run datagen.
5. Add the spawn egg to `JCreativeMenuTabRegistry` to the JCraft tab and the spawn egg tabs as well.
6. Test your addition.

## Adding an Item
1. Add the item to `JItemRegistry`.
2. Add an English translation of the item to `en_us.json`.
3. Add the model to `JModelProvider`.
4. If item tags apply, add them in `JTagProviders.JItemTags` as well. 
5. If the item is involved in recipes, add them to `JRecipeProvider`. 
6. If getting the item is worth an advancement, add it to `JAdvancementProvider`. 
7. Run datagen. 
8. Add the item to `JCreativeMenuTabRegistry` to the JCraft tab and possibly to other tabs as well. 
9. Add the texture(s) for the block in `textures/item`. 
10. Test your addition.

## Adding a Stand
1. Create the class `MyEntity` (replacing `My` with its name of course), subclassing `StandEntity`.
2. Create an inner `enum` called `State` that implements `StandAnimationState` with at least two entries: `IDLE` and `BLOCK`. You should add an enum constructor that takes an AzCommand and caches it, e.g. as `animator`. The parameter for e.g. the idle state could be `AzCommand.create(JCraft.BASE_CONTROLLER, "animation.my.idle", AzPlayBehaviors.LOOP)`. Also implement `playAnimation(final @NonNull MyEntity attacker)` via `animator.sendForEntity(attacker)` or a custom implementation.
3. Implement `getThis()` (just returning `this`), `getStateValues()` (returning `State.values()`) and `getBlockState()` (returning `State.BLOCK`).
4. Create a default moveset. If you're making an add-on, add this moveset to your moveset data provider; this is not needed if you're forking this mod.
5. Add a constructor that only takes a `Level` as a parameter. Ignore the missing type for the `super` call right now.
6. Add the type of the stand to `JEntityTypeRegistry` (for addons, this is `EntityTypeRegistry`). The dimension parameter is the hitbox of the stand in blocks.
7. Register the stand attributes in `JEntityTypeRegistry#registerAttributes` (for addons, this is `EntityTypeRegistry#registerAttributes`).
8. Add an entry in `JStandTypeRegistry` (or `StandTypeRegistry` for addons).
9. Use this new `StandType` entry in the `super` constructor of `MyEntity`.
10. Add a static field `DATA` of type `StandData` to your stand and fill it.
11. If needed, create the class `MyRenderer` extending `StandEntityRenderer`.
12. Add `MyRenderer` to `JEntityRendererRegister` (or `EntityRendererRegister` for addons); if you haven't created the class add something like `context -> new StandEntityRenderer<>(context, JStandTypeRegistry.MY.get())` to it.
13. Add the different skin PNGs, `my.geo.json` and `my.animation.json` from the `my.bbmodel` file from our modelers.
14. Let someone take care of the animations.
15. Add an English translation of the stand and its description to `en_us.json`.
16. If the stand is obtainable in survival, add it to the list of obtainables in `JAdvancementProvider`.
17. Run datagen.
18. Test your addition.

## Adding an Entity (Type)
1. Create the class `MyEntity` (replacing `My` with its name of course), subclassing `Entity` or one of its subclasses (like `PathAwareEntity`).
2. Add the interface `GeoEntity` to `MyEntity`.
3. Add the line `private final AnimatableInstanceCache geoCache = AzureLibUtil.createInstanceCache(this);` to `MyEntity` and implement the interface getter accordingly.
4. Add the method `public static AttributeSupplier.Builder createMyAttributes()` to `MyEntity` and return the stats for your entity, e.g. use `LivingEntity.createLivingAttributes()`.
5. Add a constructor that only takes a `Level` as a parameter. Ignore the missing type for the `super` call right now.
6. Add the type of the entity to `JEntityTypeRegistry`. The dimension parameter is the hitbox of the entity in blocks.
7. Register the entity attributes in `JEntityTypeRegistry#registerAttributes`.
8. Use the newly created entity type in the `super` constructor of `MyEntity`.
9. Create the class `MyModel` extending `GeoModel`. Model resource is `JCraft.id("geo/my.geo.json")`, texture resource is `JCraft.id("textures/entity/my.png")` and animation resource is `JCraft.id("animations/my.animation.json")`.
10. Create the class `MyRenderer` extending `GeoEntityRenderer`.
11. Add `MyRenderer` to `JEntityRendererRegister`.
12. Add `my.png`, `my.geo.json` and `my.animation.json` from the `my.bbmodel` file from our modelers.
13. Maybe add a spawn egg (see above how).
14. Add an English translation of the entity to `en_us.json`.
15. If the entity can have stands, add the **serverside** line `JEnemies.add(this);` to the constructor of `MyEntity`.
16. Test your addition.

## Adding a Biome
1. Add the biome to `JBiomeRegistry`.
2. Add the biome with its properties in its own method to `JBiomeProvider`.
3. Add the biome to `JBiomeProvider#bootstrap(…)`.
4. Run datagen.
### to a custom dimension
5. Add the biome to the custom dimension.
6. Test your addition.
### to a vanilla dimension
5. Add material rules for the biome if needed in `MaterialRulesFabric` and `MaterialRulesForge`.
6. Add the biome generation parameters to `OverworldRegionFabric` and `OverworldRegionForge` (or the equivalents for Nether or End).
7. Test your addition.

## Adding a Feature
1. Add the feature to `JConfiguredFeatureRegistry` (suffix: `"_cf"`).
2. Add the feature to `JConfiguredFeatureProvider`.
3. Add the feature to `JPlacedFeatureRegistry` (suffix: `"_pf`").
4. Add the feature to `JPlacedFeatureProvider`.
5. Add the feature to biomes in `JBiomeProvider`.
6. Run datagen.
7. Test your addition.

## Adding a stat
1. Add the stat to `JStatRegistry`.
2. Use `Player#awardStat(…)` on server-side wherever appropriate.
3. Add an English translation of the stat to `en_us.json`.
4. Test your addition.

## Adding a move
1. Create a move class (preferably in some subpackage in `net.arna.jcraft.attack.moves` in the `common` project) that derives from some base class (see `net.arna.jcraft.attack.moves.base`).
   The most basic moves should at least derive from `AbstractMove`.
2. If the move is not abstract or is abstract and adds any field that should be serialized, make sure to add an (abstract) type class in it that extends from the type class of the super class as well.
3. Register the move's type in `JMoveTypeRegistry`. Preferably ensure the type has a `public static final INSTANCE` field, although this should not make a difference.

## Adding move actions and conditions
1. Create a class deriving from MoveAction/MoveCondition and implement it.
2. Register the action/condition in `MoveSetLoader`.

## Persisting something in the world
1. Add `CommonMyComponent` (replacing `My` with whatever is appropriate of course) interface to `net.arna.jcraft.common.component.world` and add the method signatures you need.
2. Add `CommonMyComponentImpl` class to `net.arna.jcraft.common.component.impl.world` and implement the methods.
3. Add a constructor with a `Level world` parameter to `CommonMyComponentImpl`.
4. Add methods `public void readFromNbt(CompoundTag tag)` and `public void writeToNbt(CompoundTag tag)` to `CommonMyComponentImpl` and implement them.
5. Add a method `@ExpectPlatform public static CommonMyComponent getMyComponent(Level world)` like the others in `JComponentPlatformUtils`. This is common done.
6. Add `MyComponent` interface to `net.arna.jcraft.fabric.common.component.world`, inheriting from `CommonMyComponent`, `dev.onyxstudios.cca.api.v3.component.Component` and others if needed.
7. Add `MyComponentImpl` to `net.arna.jcraft.fabric.common.component.impl.world`, inheriting from `CommonMyComponentImpl` and implementing `MyComponent`.
8. Register the component in `JComponents` and in `fabric.mod.json`. 
9. Add the component to `net.arna.jcraft.platform.fabric.JComponentPlatformUtilsImpl`. This is Fabric done.
10. Add `MyCapability` to `net.arna.jcraft.forge.capability.impl.world`, inheriting from `CommonMyComponentImpl` and implementing `JCapability`.
11. Add the component to `net.arna.jcraft.platform.forge.JComponentPlatformUtilsImpl`. This is Forge done.
