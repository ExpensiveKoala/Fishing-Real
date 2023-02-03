package koala.fishingreal.forge;

import koala.fishingreal.FishingReal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(FishingReal.MOD_ID)
public class FishingRealForge {
    public FishingRealForge() {
        FishingReal.init();
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::onServerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(FishingRealForge::itemFished);
    }

    public static void onServerReloadListeners(AddReloadListenerEvent event) {
        FishingReal.onRegisterReloadListeners((id, listener) -> event.addListener(listener));
    }

    public static void itemFished(ItemFishedEvent event) {
        if (FishingReal.retrieve(event.getEntity(), event.getDrops(), event.getHookEntity())) {
            if (!event.getEntity().isCreative()) {
                event.damageRodBy(1);
            }
            event.setCanceled(true);
        }
    }
}