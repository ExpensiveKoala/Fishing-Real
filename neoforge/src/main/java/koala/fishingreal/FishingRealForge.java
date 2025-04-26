package koala.fishingreal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        NeoForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, FishingRealForge::onItemFished);
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