package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

public class FishingReal {
    public static final FishingManager FISHING_MANAGER = new FishingManager();

    public static final String MOD_ID = "fishingreal";

    public static boolean doItemStacksMatchIgnoreNBT(ItemStack stack1, ItemStack stack2) {
        return stack1.is(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    public static void onRegisterReloadListeners(BiConsumer<ResourceLocation, PreparableReloadListener> registry) {
        registry.accept(new ResourceLocation(MOD_ID, "fishing"), FISHING_MANAGER);
    }

    public static Entity convertItemStack(ItemStack itemstack, Player player) {
        if (player != null && player.level instanceof ServerLevel serverLevel) {
            FishingConversion.FishingResult result = FishingReal.FISHING_MANAGER.getConversionResultFromStack(itemstack);
            if(result != null) {
                Entity resultEntity = result.entity().create(player.level);
                result.tag().ifPresent(resultEntity::load);
                if (result.randomizeNbt() && resultEntity instanceof Mob resultMob) {
                    resultMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null, null);
                }
                return resultEntity;
            }
        }
        return null;
    }

    public static Entity convertItemEntity(Entity fishedEntity, Player player) {
        if (player != null && fishedEntity instanceof ItemEntity itemEntity) {
            Entity resultEntity = convertItemStack(itemEntity.getItem(), player);
            if (resultEntity != null) {
                resultEntity.moveTo(itemEntity.position());
                resultEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(1.2, 1.2, 1.2));
                return resultEntity;
            }
        }
        return fishedEntity;
    }
}