# SlashBlade Item Architecture

## Purpose

This document defines the target item architecture for SlashBlade-DHJ-Edition.
It is intended to solve the current identity and compatibility issues caused by
many blades sharing the same registry item while still preserving the content
authoring flexibility of the original SlashBlade design.

This is a target architecture document, not a description of the current code.

## Current Problems

The current implementation relies on a small number of registered `Item`
instances plus many NBT-backed `ItemStack` prototypes.

This causes several practical issues:

- JEI often sees multiple named blades as the same item and links the wrong recipes.
- Quest mods may either treat all `slashbladeNamed` items as the same thing or
  fail to match them once mutable NBT changes.
- Addons depend on NBT identity fields such as `CurrentItemName`, which are not
  strong enough as a cross-mod identity contract.
- Recipe, anvil, and upgrade logic must manually copy many NBT fields, which is
  easy to break when a new state field is added.
- Compatibility logic becomes scattered between core code, named blade loaders,
  recipes, and addon-specific workarounds.

## Design Goals

The target item architecture should satisfy the following goals:

- Stable external identity for important blades.
- Clear separation between item identity and mutable blade state.
- Good JEI, task mod, and scripting compatibility.
- Support for both built-in blades and addon/custom blades.
- A legacy path for old addons that still use prototype registration.
- Minimal duplication of combat and rendering logic.

## Core Principles

### 1. Identity and state must be separated

The answer to "what blade is this?" must not depend on mutable progression NBT.

- Identity:
  registry name, stable blade id, definition id
- State:
  proud soul, kill count, repair count, owner, charge state, energy, temporary flags

### 2. Logic reuse is good, shared identity is not

Many blades may use the same implementation class, but they should not all share
the same registry item identity.

### 3. Dynamic content and fixed content should not use the same path

Built-in named blades and major compat blades should use stable registered items.
Truly dynamic blades should use a dedicated dynamic path.

### 4. Legacy compatibility must be explicit

Old APIs may remain, but they should be clearly marked as legacy and internally
bridged into the new architecture.

## Target Item Layers

The item system should be split into four layers.

### Layer 1: Registry Identity Layer

This layer defines what gets registered into Forge item registries.

Recommended categories:

- `ItemProudSoul`
  blade souls, ingots, spheres, crystals, trapezohedrons, and similar materials
- `ItemSlashBladeBase`
  progression blades such as wood, bamboo, white sheath, and other core non-named blades
- `ItemSlashBladeNamed`
  fixed named blades with their own registry entries
- `ItemSlashBladeWrapper`
  wrapper and sheath-like bridge items
- `ItemSlashBladeDynamic`
  optional fallback item for config-generated or legacy prototype blades

Rules:

- Every built-in named blade should have its own registry name.
- Every major compat blade should preferably have its own registry name.
- Only dynamic or legacy-generated blades should share a common dynamic item.

Examples of acceptable identity:

- `slashblade:yamato`
- `slashblade:tagayasan`
- `slashblade:agito_rust`
- `slashblade:wrapper_bamboomod_katana`
- `slashblade:dynamic_named_blade`

## Blade Definition Layer

This layer defines immutable blade data.

Recommended `BladeDefinition` content:

- `bladeId`
- translation key
- model path
- texture path
- base attack
- max damage
- standby render type
- default special attack
- default sword traits
- repair material definition
- rarity
- compat tags

Rules:

- A definition is immutable once loaded.
- A blade definition should be retrievable without reading mutable player state.
- Recipes and integrations should be able to resolve a blade definition directly.

Recommended sources of definitions:

- built-in Java registration
- JSON or data-driven definitions for future extensibility
- compat module registration
- legacy prototype bridge conversion

## Blade State Layer

This layer stores mutable progression and runtime state.

Recommended state groups:

- `progress`
  proud soul, kill count, repair count, refine data
- `ownership`
  owner UUID, permissions, seal unlock state
- `combat`
  combo sequence, charge state, target entity id, attack amplifier
- `special`
  special effect levels, summoned sword color, empowered state
- `runtime`
  temporary flags that can safely be regenerated

Recommended NBT layout:

```text
tag
|- BladeId
|- DefinitionVersion
|- Progress
|  |- ProudSoul
|  |- KillCount
|  |- RepairCount
|- Ownership
|  |- Owner
|- Combat
|  |- Combo
|  |- Charge
|- Special
|  |- Effects
|  |- Energy
|- Legacy
```

Rules:

- `BladeId` is the stable logical identity for the item.
- Mutable fields must never be used as the sole identity key.
- Temporary runtime fields should be minimized and clearly separated.
- Read helpers should avoid silently mutating NBT unless initialization is required.

## Class Responsibilities

Recommended high-level class split:

- `ItemSlashBladeBase`
  shared combat behavior, durability behavior, common rendering hooks
- `ItemSlashBladeNamed`
  fixed named blade identity, named definition lookup
- `ItemSlashBladeWrapper`
  wrapper-specific inner-item behavior
- `ItemSlashBladeDynamic`
  fallback item for dynamic, config, or legacy blades
- `BladeDefinition`
  immutable blade metadata
- `BladeState`
  state accessors and upgrade/migration helpers
- `BladeIdentity`
  identity resolution helpers for registry name, blade id, and legacy aliases
- `BladeDefinitionRegistry`
  centralized definition registry
- `BladeStateCodec`
  read/write and migration helpers for state NBT

## Recommended Item Categories

The mod should distinguish the following item groups clearly.

### 1. Material Items

Examples:

- proud soul
- ingot blade soul
- sphere blade soul
- crystal blade soul
- tiny blade soul
- trapezohedron blade soul

Properties:

- stable registry item
- little or no mutable blade state
- can remain in a metadata or subtype-based design if desired

### 2. Core Progression Blades

Examples:

- wood blade
- bamboo blade
- silver bamboo blade
- white sheath
- unnamed slashblade base

Properties:

- stable registry item
- progression state allowed
- recipes should target real registry entries

### 3. Fixed Named Blades

Examples:

- yamato
- tagayasan
- agito
- muramasa variants
- fox and sange/yasha lines

Properties:

- one registered item per important blade variant
- may share the same implementation class
- should expose a stable registry identity to JEI and quest mods

### 4. Wrapper and Bridge Blades

Examples:

- wrapper scabbard
- compat wrapper variants

Properties:

- special behavior delegated to wrapped content
- identity should still be stable if the wrapped result is a curated built-in or compat blade

### 5. Dynamic and Custom Blades

Examples:

- config-generated blades
- datapack-style future blades
- legacy addon blades that still use prototype registration

Properties:

- may use a shared fallback registry item
- must still carry a stable `BladeId`
- should be treated as a second-class compatibility path, not the default for core content

## Identity Rules

The item system should resolve identity in the following order:

1. Real registry name for fixed registered blades.
2. Stable `BladeId` field for dynamic blades.
3. Legacy alias lookup from old fields such as `CurrentItemName`.

Rules:

- `CurrentItemName` should become a legacy alias field, not the primary identity field.
- `TrueItemName` should be treated as migration metadata, not the long-term public contract.
- External integrations should use a centralized helper instead of reading raw NBT keys directly.

## Recipe Architecture

Recipes should be split by what they care about.

### Identity recipes

These recipes care about which blade the item is.

Examples:

- awakening a specific broken blade into a specific named blade
- compat transformation recipes

They should match by:

- registry item when fixed
- stable `BladeId` when dynamic legacy content is involved

### State transfer recipes

These recipes care about preserving progression.

Examples:

- upgrade recipes
- awakening recipes
- repair evolution paths

They should copy state through a centralized state transfer helper instead of
manually copying arbitrary NBT fields in each recipe class.

### Material recipes

These recipes do not care about blade identity.

Examples:

- soul upgrades
- repair materials

They should operate on material item definitions only.

## Compatibility Architecture

Compatibility should be layered.

### Core compatibility

The core mod should expose:

- blade identity resolver
- blade definition registry
- blade state transfer helper
- legacy prototype registration bridge

### Compat module

A separate compat module may provide:

- addon blade registration
- JEI subtype and ingredient helpers
- task mod matching bridges
- scripted integration helpers
- addon-specific mixin patches where unavoidable

### Legacy addon bridge

Legacy addons using `registerCustomItemStack` should still work, but the core
should internally convert them into:

- a resolved `BladeDefinition`
- a stable `BladeId`
- a categorized dynamic or legacy blade entry

## Migration Strategy

The architecture should be introduced in phases.

### Phase 1

- Add `BladeId` and centralized identity helpers.
- Keep old prototype registration working.
- Teach JEI and matching logic to use stable identity helpers.

### Phase 2

- Move built-in named blades to dedicated registry items.
- Keep legacy aliases so old lookups still resolve.
- Centralize state transfer logic.

### Phase 3

- Add a proper early registration API for addons and compat modules.
- Reserve the dynamic shared item path for config or legacy content only.

### Phase 4

- Deprecate direct reliance on `CurrentItemName`.
- Migrate recipe and quest integration to definition-based matching.

## What Should Stay Shared

The following should remain shared across blade item classes:

- combat logic
- combo logic
- special attack dispatch
- rendering hooks
- tooltip helpers
- state access helpers

Shared logic is good. Shared registry identity is the real problem.

## What Should Not Stay Shared

The following should not remain hidden behind a single shared registered item
for built-in content:

- important named blade identity
- JEI-visible recipe outputs
- task-mod-visible reward targets
- curated compat blade outputs

## Final Recommendation

The target architecture for this mod should be:

- fixed blades use real registry identities
- dynamic blades use a dedicated fallback item plus stable `BladeId`
- blade definitions are immutable and centralized
- blade state is mutable and structured
- recipes use centralized state transfer helpers
- compatibility is handled through explicit bridges, not by relying on display-oriented NBT

This gives SlashBlade the flexibility of the old prototype model without
keeping the external compatibility weaknesses of the old identity model.
