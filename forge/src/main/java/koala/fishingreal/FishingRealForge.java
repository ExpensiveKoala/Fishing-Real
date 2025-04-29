package koala.fishingreal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, FishingRealForge::onItemFished);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    public static void onServerReloadListeners(AddReloadListenerEvent event) {
        FishingReal.onRegisterReloadListeners((id, listener) -> event.addListener(listener));
    }

    public static void onItemFished(ItemFishedEvent event) {
        for (ItemStack itemStack : event.getDrops()) {
            Entity convertedEntity = FishingReal.convertItemStack(itemStack, event.getEntity(), event.getHookEntity().position());
            if (convertedEntity != null) {
                for (int i = 0; i < itemStack.getCount(); i++) {
                    FishingReal.fishUpEntity(convertedEntity, event.getHookEntity(), itemStack, event.getEntity());
                }
                // Effectively remove the item from the loot pool
                itemStack.setCount(0);
            }
        }
    }
}