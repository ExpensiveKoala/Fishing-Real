package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;

public class FishingReal {
    public static final FishingManager FISHING_MANAGER = new FishingManager();

    public static final String MOD_ID = "fishingreal";
    
    private static TagKey<Item> WATER_BUCKET = TagKey.create(Registries.ITEM, new ResourceLocation("c", "water_buckets"));

    public static boolean doItemStacksMatchIgnoreNBT(ItemStack stack1, ItemStack stack2) {
        return stack1.is(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    public static void onRegisterReloadListeners(BiConsumer<ResourceLocation, PreparableReloadListener> registry) {
        registry.accept(new ResourceLocation(MOD_ID, "fishing"), FISHING_MANAGER);
    }

    public static Entity convertItemStack(ItemStack itemstack, Player player, Vec3 position) {
        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            FishingConversion.FishingResult result = FishingReal.FISHING_MANAGER.getConversionResultFromStack(itemstack);
            if(result != null) {
                Entity resultEntity = result.entity().create(player.level());
                result.tag().ifPresent(resultEntity::load);
                resultEntity.moveTo(position);
                if (result.randomizeNbt() && resultEntity instanceof Mob resultMob) {
                    resultMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null, null);
                }
                return resultEntity;
            }
        }
        return null;
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
        
        if(Config.CONFIG.enableCatchInteraction.get()) {
            attemptBucket(entity, player);
        }
    }
    
    private static void attemptBucket(Entity entity, Player player) {
        InteractionResult interactionResult = InteractionResult.PASS;
        
        boolean restrictToWaterBucket = Config.CONFIG.limitInteractionToWaterBucket.get();
        boolean isWaterBucketMain = player.getMainHandItem().is(WATER_BUCKET);
        boolean isWaterBucketOff = player.getOffhandItem().is(WATER_BUCKET);
        
        if (!restrictToWaterBucket || isWaterBucketOff) {
            interactionResult = player.interactOn(entity, InteractionHand.OFF_HAND);
        }
        
        if (!interactionResult.consumesAction()) {
            if (!restrictToWaterBucket || isWaterBucketMain) {
                player.interactOn(entity, InteractionHand.MAIN_HAND);
            }
        }
    }
}