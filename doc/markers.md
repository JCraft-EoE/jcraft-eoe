# How to use Markers
## Overview
Most of the following classes/interfaces/records can be found in `net.arna.jcraft.common.marker`.

A `Marker` is a `Codec`-serializable object that has some kind of ID of type `I`. The other type parameter `M` is the type of the class, similar to how `IAttacker` works. It is WHAT is being saved.

A `MarkerType` doesn't need to be serializable. It has three type parameters: the id type `I`, the type of information to save `T` and the type of the marker `M`. A `MarkerType` decides HOW an instance of `T` is saved into a marker `M` and HOW it is loaded back in.

A `MarkerSavePredicate` decides IF an instance of `T` should be saved.

A `MarkerLoadPredicate` decides IF an instance of `M` should be loaded.

### The fundamental difference

A block in a world is the combination of a `BlockPos` and the `BlockState`. These two properties make up the `BlockMarker`. The `BlockMarkerType` is likewise also a very simple record, since saving and setting blocks is easy.

An entity can be identified by its UUID. It might have many fields that need to be saved, which we do via NBT. Therefore, the `EntityMarker` is also a simple record.

The `EntityMarkerType` is a different story though. Saving the necessary properties of entities (and loading them back in) is sophisticated (see below for more).

The fundamental difference is that blocks are easy to serialize, but it is difficult to decide which ones should be saved (because of the sheer amount of blocks and duping issues), while entities are sophisticated to save and load, but we can simply save all entities in an area, as there are comparatively few of them.

### EntityDataHandler
The `EntityDataHandler` is the core of the `EntityMarkerType`. It decides HOW things are saved and loaded via instances of `TriConsumer<ResourceLocation,Entity,CompoundTag>`.

The `BiPredicate` decides if a certain property should be saved/loaded in the first place.

The save consumer, or "extractor" takes in a resource location and the entity and saves the property identified by the resource location into the given compound tag.

The load consumer or "injector" takes the property identified by the resource location from the compound tag and writes it back into the entity.

### EntityMarkerType

Next to the `EntityDataHandler`, the `EntityMarkerType` also contains marker predicates to decide if an entity should be saved and loaded in the first place as well as a set of resource locations identifying WHICH properties should be saved/loaded.

### BlockMarkerMove
Hidden in `net.arna.jcraft.api.attack.moves`, `BlockMarkerMove` shows that a move can rewind blocks. Each individual move is stored in `BlockMarkerMove#MOVES` to be called upon by `JServerEvents#beforeBlockSet`.

### Helper interfaces

`Identifiers` is an interface supplying the resource location to identify entity properties.

`Predicates` contains useful instances of `MarkerSavePredicate`, `MarkerLoadPredicate` as well as `BiPredicate`s for the `EntityDataHandler`.

`Extractors` contains useful extractors and `Injectors` contains useful injectors. They can be registered in `JMarkerExtractorRegistry` and `JMarkerInjectorRegistry` respectively to be used for moves.

## Implementing a rewind move
### Block rewind
1. Implement the `BlockMarkerMove` interface if you want to rewind blocks. 
2. Add a variable `resolving` to your move class, with a Lombok `@Getter` and `@Setter`. This is very important as we don't want to change the list of affected blocks during the rewind execution. Also add a `BlockMarkerType`.
3. Implement `#addBlock` to your liking. Should probably immediately return `false` if `resolving`. For example implementation see `CountdownMove#addBlock(...)`.
4. In the `#perform` part of your move where the rewind is started it is recommended to clear the saved blocks.
5. In the `#perform` part of your move where the rewind is finished, add something like
```java
final ServerLevel level = (ServerLevel)attacker.level();
setResolving(true);
for (final BlockMarker marker : blockMarkers) {
    if (blockMarkerType.shouldLoad(marker, level)) {
        blockMarkerType.load(marker, level);
    }
}
```
### Entity Rewind
1. No interface required.
2. Add a collection of `EntityMarker`s to your move as well as an `EntityMarkerType`. To allow for extensibility, make the set of IDs, extractor and injector part of the move's parameters (cmp. `CountdownMove`).
3. In the `#perform` part of your move where the rewind is started save the affected entities like this:
```java
for (final Entity entity : toCapture) {
    if (entityMarkerType.shouldSave(entity.getUUID(), entity)) {
        timeEntityMarkers.add(entityMarkerType.save(entity.getUUID(), entity));
    }
}
```
4. In the `#perform` part of your move where the rewind is finished, add something like
```java
final ServerLevel level = (ServerLevel)attacker.level();
for (final EntityMarker marker : timeEntityMarkers) {
    if (entityMarkerType.shouldLoad(marker, level)) {
        entityMarkerType.load(marker, level);
    }
}
```