package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

import java.util.List;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        NeoForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, FishingRealForge::onItemFished);
    }

    public static void onServerReloadListeners(AddReloadListenerEvent event) {
        FishingReal.onRegisterReloadListeners((id, listener) -> event.addListener(listener));
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

    public static void onItemFished(ItemFishedEvent event) {
        for (ItemStack itemStack : event.getDrops()) {
            Entity convertedEntity = FishingReal.convertItemStack(itemStack, event.getEntity(), event.getHookEntity().position());
            if (convertedEntity != null) {
                for (int i = 0; i < itemStack.getCount(); i++) {
                    fishUpEntity(convertedEntity, event.getHookEntity(), itemStack, event.getEntity());
                }
                // Effectively remove the item from the loot pool
                itemStack.setCount(0);
            }
        }
    }
}