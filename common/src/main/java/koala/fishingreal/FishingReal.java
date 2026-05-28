package koala.fishingreal;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class FishingReal {

    public static final String MOD_ID = "fishingreal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Registry<FishingConversion>> FISHING_CONVERSION_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("fishing"));
    
    private static TagKey<Item> WATER_BUCKET = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "buckets/water"));
    private static TagKey<Item> FISHING_ROD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/fishing_rod"));

    public static Optional<Registry<FishingConversion>> getFishingConversionRegistry(RegistryAccess registryAccess) {
        return registryAccess.lookup(FISHING_CONVERSION_REGISTRY_KEY);
    };

    public static boolean doItemStacksMatchIgnoreNBT(ItemStack itemStack, Item conversionResult, int count) {
        return itemStack.is(conversionResult) && itemStack.getCount() == count;
    }

    public static FishingConversion.FishingResult getConversionResultFromStack(RegistryAccess registryAccess, ItemStack stack) {
        Optional<Registry<FishingConversion>> optionalRegistry = getFishingConversionRegistry(registryAccess);
        if(optionalRegistry.isPresent()) {
            Registry<FishingConversion> registry = optionalRegistry.get();
            for(FishingConversion conv : registry.stream().toList()) {
                if(FishingReal.doItemStacksMatchIgnoreNBT(stack, conv.input().item(), conv.input().count()) && conv.result() != null) {
                    return conv.result();
                }
            }
        }
        return null;
    }

    public static Entity convertItemStack(ItemStack itemstack, Player player, Vec3 position) {
        if (player != null) {
            FishingConversion.FishingResult result = getConversionResultFromStack(player.level().registryAccess(), itemstack);
            if(result != null) {
                Entity resultEntity = result.entity().create(player.level(), EntitySpawnReason.NATURAL);
                result.tag().ifPresent((tag) -> {
                    ValueInput valueInput = TagValueInput.create(new ProblemReporter.ScopedCollector(LOGGER), player.level().registryAccess(), tag);
                    resultEntity.load(valueInput);
                    EntityType.loadPassengersRecursive(resultEntity, valueInput, player.level(), EntitySpawnReason.NATURAL, EntityProcessor.NOP);
                });
                resultEntity.setPos(position);
                if (result.randomizeNbt() && resultEntity instanceof Mob resultMob && player.level() instanceof ServerLevel serverLevel) {
                    resultMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), EntitySpawnReason.NATURAL, null);
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
        entity.getSelfAndPassengers().forEach(e -> e.setPos(hook.getX(), hook.getY(), hook.getZ()));
        entity.setDeltaMovement(dX * strength, dY * strength + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * verticalStrength, dZ * strength);
        if(player.level() instanceof ServerLevel serverLevel) {
            serverLevel.tryAddFreshEntityWithPassengers(entity);
        }

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
            interactionResult = player.interactOn(entity, InteractionHand.OFF_HAND, player.position());
        }
        
        if (!interactionResult.consumesAction()) {
            if (!restrictToWaterBucket || isWaterBucketMain) {
                player.interactOn(entity, InteractionHand.MAIN_HAND, player.position());
            }
        }
    }
}