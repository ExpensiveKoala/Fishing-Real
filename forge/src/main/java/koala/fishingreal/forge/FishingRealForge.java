package koala.fishingreal.forge;

import koala.fishingreal.FishingReal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
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
        boolean useMixinInstead = event.getHookEntity().getClass() == FishingHook.class;
        try {
            useMixinInstead = event.getHookEntity().getClass().getMethod("m_37156_").getDeclaringClass() == FishingHook.class;
        } catch (Exception ignored) {}

        // Skip running when we know the mixin works. Still safe otherwise, just improves compat for other mixins.
        if (!useMixinInstead) {
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
}