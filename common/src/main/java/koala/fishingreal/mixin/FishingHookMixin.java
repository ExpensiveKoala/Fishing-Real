package koala.fishingreal.mixin;

import koala.fishingreal.FishingConversion;
import koala.fishingreal.FishingReal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {
    @Shadow @Nullable public abstract Player getPlayerOwner();

    public FishingHookMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public Entity replaceHookedItems(Entity original) {
        Player player = getPlayerOwner();
        if (player != null && original instanceof ItemEntity itemEntity) {
            FishingConversion conversion = FishingReal.FISHING_MANAGER.getConversionFromStack(itemEntity.getItem());
            if (conversion != null && conversion.result().entity() != null) {
                FishingConversion.FishingResult result = conversion.result();
                Entity entity = result.entity().create(player.level);
                if (entity != null) {
                    result.tag().ifPresent(entity::load);
                    if (level instanceof ServerLevel serverLevel) {
                        entity.moveTo(original.position());
                        entity.setDeltaMovement(original.getDeltaMovement().multiply(1.2, 1.1, 1.2));
                        if (result.randomizeNbt() && entity instanceof Mob mob) {
                            mob.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null, null);
                        }
                        return entity;
                    }
                }
            }
        }
        return original;
    }
}
