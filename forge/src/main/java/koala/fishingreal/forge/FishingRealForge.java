package koala.fishingreal.forge;

import koala.fishingreal.FishingReal;
import koala.fishingreal.forge.compat.AquacultureCompat;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, FishingRealForge::onItemFished);
    }

    public static void onServerReloadListeners(AddReloadListenerEvent event) {
        FishingReal.onRegisterReloadListeners((id, listener) -> event.addListener(listener));
    }
    
    /**
     * Rip of FishingHook#Retrieve logic for use where direct entity conversion can't occur
     */
    public static void fishUpEntity(Entity entity, FishingHook hook, ItemStack stack, Player player) {
        double dX = player.getX() - hook.getX();
        double dY = player.getY() - hook.getY();
        double dZ = player.getZ() - hook.getZ();
        double m = 0.12;
        entity.setPos(hook.getX(), hook.getY(), hook.getZ());
        entity.setDeltaMovement(dX * m, dY * m + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.08, dZ * m);
        player.level.addFreshEntity(entity);
        player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, player.level.random.nextInt(6) + 1));
        if (stack.is(ItemTags.FISHES)) {
            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger(serverPlayer, player.getUseItem(), hook, List.of(stack));
        }
    }

    public static void onItemFished(ItemFishedEvent event) {
        for (ItemStack itemStack : event.getDrops()) {
            Entity convertedEntity = FishingReal.convertItemStack(itemStack, event.getEntity());
            if (convertedEntity != null) {
                fishUpEntity(convertedEntity, event.getHookEntity(), itemStack, event.getEntity());
                if (!event.getEntity().isCreative()) {
                    event.damageRodBy(1);
                }
                if(ModList.get().isLoaded("aquaculture")) {
                    AquacultureCompat.onItemFished(event);
                }
                event.setCanceled(true);
            }
        }
    }
}