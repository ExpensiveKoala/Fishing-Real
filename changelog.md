## 1.9.1
- Change project layout (Should be no change for end user). Using 1.21.1 now
- Fix deleting fishing loot when catching multiple items
- Add config for attempting to interact with fished-up-entity, this means you catch the fish in your bucket if you have it in your offhand. (Requires forge config api port for fabric)

## 1.9.0
- Port to 1.21

## 1.8.0
- Update to 1.20.4
- Drop Forge in favor of NeoForge

## 1.7.2
- Set position of fish before running nbt randomizing. This will fix certain mod entities who determine variant based on position.

## 1.7.1
- Update to 1.20.1

## 1.6
- Redid the way Fabric fish conversion is handled to add more support for custom fishing loot tables. I.E. Mods like Sandwichable
- Added Aquaculture 2 rod support. Double hook can actually hook two fish, and bait is properly consumed!
- Added support for stack size on forge. If you fish up an itemstack with more than 1 size, it will spawn that many fish