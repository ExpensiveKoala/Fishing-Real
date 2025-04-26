package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;

public class FishingReal {
    public static final FishingManager FISHING_MANAGER = new FishingManager();

    public static final String MOD_ID = "fishingreal";

    public static boolean doItemStacksMatchIgnoreNBT(ItemStack stack1, ItemStack stack2) {
        return stack1.is(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    public static void onRegisterReloadListeners(BiConsumer<ResourceLocation, PreparableReloadListener> registry) {
        registry.accept(ResourceLocation.fromNamespaceAndPath(MOD_ID, "fishing"), FISHING_MANAGER);
    }

    public static Entity convertItemStack(ItemStack itemstack, Player player, Vec3 position) {
        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            FishingConversion.FishingResult result = FishingReal.FISHING_MANAGER.getConversionResultFromStack(itemstack);
            if(result != null) {
                Entity resultEntity = result.entity().create(player.level());
                result.tag().ifPresent(resultEntity::load);
                resultEntity.moveTo(position);
                if (result.randomizeNbt() && resultEntity instanceof Mob resultMob) {
                    resultMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null);
                }
                return resultEntity;
            }
        }
        return null;
    }

    public static Entity convertItemEntity(Entity fishedEntity, Player player) {
        if (player != null && fishedEntity instanceof ItemEntity itemEntity) {
            Entity resultEntity = convertItemStack(itemEntity.getItem(), player, itemEntity.position());
            if (resultEntity != null) {
                resultEntity.moveTo(itemEntity.position());
                resultEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(1.2, 1.2, 1.2));
                return resultEntity;
            }
        }
        return fishedEntity;
    }
    
    /**
     * Rip of FishingHook#Retrieve logic for spawning the new entity instead of the original ItemStack
     */
    public static void fishUpEntity(Entity entity, FishingHook hook, ItemStack stack, Player player) {
        double dX = player.getX() - hook.getX();
        double dY = player.getY() - hook.getY();
        double dZ = player.getZ() - hook.getZ();
        double strength = 0.12;
        double verticalStrength = 0.18;
        entity.setPos(hook.getX(), hook.getY(), hook.getZ());
        entity.setDeltaMovement(dX * strength, dY * strength + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * verticalStrength, dZ * strength);
        player.level().addFreshEntity(entity);
        
        // Stack is empty now, so we need to award stats and trigger CriteriaTriggers ourselves
        if (stack.is(ItemTags.FISHES)) {
            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger(serverPlayer, player.getUseItem(), hook, List.of(stack));
        }
    }
}