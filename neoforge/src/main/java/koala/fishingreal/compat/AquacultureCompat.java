package koala.fishingreal.compat;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.init.AquaLootTables;
import com.teammetallurgy.aquaculture.init.AquaSounds;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import koala.fishingreal.FishingReal;
import koala.fishingreal.FishingRealForge;
import koala.fishingreal.mixin.compat.AquaFishingBobberEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

// https://github.com/TeamMetallurgy/Aquaculture/blob/master/src/main/java/com/teammetallurgy/aquaculture/entity/AquaFishingBobberEntity.java
public class AquacultureCompat {
    
    public static void onItemFished(ItemFishedEvent event) {
        if (event.getHookEntity() instanceof AquaFishingBobberEntity) {
            AquaFishingBobberEntity hook = (AquaFishingBobberEntity) event.getHookEntity();
            if(hook.level().isClientSide()) {
                return;
            }
            // Handle double hook
            if (hook.hasHook() && hook.getHook().getDoubleCatchChance() > 0) {
                if (hook.level().random.nextDouble() <= hook.getHook().getDoubleCatchChance()) {
                    LootParams lootParams = new LootParams.Builder((ServerLevel) hook.level()).withParameter(LootContextParams.ORIGIN, hook.position()).withParameter(LootContextParams.TOOL, event.getEntity().getUseItem()).withParameter(LootContextParams.THIS_ENTITY, hook).withParameter(LootContextParams.ATTACKING_ENTITY, event.getEntity()).withParameter(LootContextParams.THIS_ENTITY, hook).withLuck((float)((AquaFishingBobberEntityAccessor)hook).getLuck() + event.getEntity().getLuck()).create(LootContextParamSets.FISHING);
                    List<ItemStack> doubleLoot = getLoot(hook, lootParams, (ServerLevel) hook.level());
                    if (!doubleLoot.isEmpty()) {
                        for (ItemStack stack : doubleLoot) {
                            Entity convertedEntity = FishingReal.convertItemStack(stack, event.getEntity(), hook.position());
                            FishingRealForge.fishUpEntity(convertedEntity == null ? new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), stack) {
                                @Override
                                public boolean displayFireAnimation() {
                                    return false;
                                }
                                
                                @Override
                                public void lavaHurt() {
                                }
                                
                                @Override
                                public boolean isInvulnerableTo(@Nonnull DamageSource source) {
                                    BlockPos spawnPos = BlockPos.containing(hook.getX(), hook.getY(), hook.getZ());
                                    return hook.isLavaHookInLava(hook, level(), spawnPos) || super.isInvulnerableTo(source);
                                }
                            } : convertedEntity, hook, stack, event.getEntity());
                        }
                    }
                }
            }
            
            // Handle bait consumption
            if (!event.getEntity().isCreative()) {
                ItemStackHandler rodHandler = AquaFishingRodItem.getHandler(((AquaFishingBobberEntityAccessor)hook).getFishingRod());
                ItemStack bait = rodHandler.getStackInSlot(1);
                if (!bait.isEmpty()) {
                    bait.hurtAndBreak(1, (ServerLevel) hook.level(), null, item -> {
                        bait.shrink(1);
                        hook.playSound(AquaSounds.BOBBER_BAIT_BREAK.get(), 0.7F, 0.2F);
                    });
                    rodHandler.setStackInSlot(1, bait);
                }
            }
        }
    }
    
    private static List<ItemStack> getLoot(AquaFishingBobberEntity hook, LootParams lootParams, ServerLevel level) {
        ResourceKey<LootTable> lootTableLocation;
        if (hook.isLavaHookInLava(hook, level, hook.blockPosition())) {
            if (level.getLevel().dimensionType().hasCeiling()) {
                lootTableLocation = AquaLootTables.NETHER_FISHING;
            } else {
                lootTableLocation = AquaLootTables.LAVA_FISHING;
            }
        } else {
            lootTableLocation = BuiltInLootTables.FISHING;
        }
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableLocation);
        return lootTable.getRandomItems(lootParams);
    }
}
