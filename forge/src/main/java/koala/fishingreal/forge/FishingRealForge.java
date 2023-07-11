package koala.fishingreal.forge;

import koala.fishingreal.FishingReal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::onItemFished);
    }

    public static void onServerReloadListeners(AddReloadListenerEvent event) {
        FishingReal.onRegisterReloadListeners((id, listener) -> event.addListener(listener));
    }

    public static void onItemFished(ItemFishedEvent event) {
        for (ItemStack itemStack : event.getDrops()) {
            Entity convertedEntity = FishingReal.convertItemStack(itemStack, event.getEntity());
            if (convertedEntity != null) {
                FishingReal.fishUpEntity(convertedEntity, event.getHookEntity(), itemStack, event.getEntity());
                if (!event.getEntity().isCreative()) {
                    event.damageRodBy(1);
                }
                event.setCanceled(true);
            }
        }
    }
}