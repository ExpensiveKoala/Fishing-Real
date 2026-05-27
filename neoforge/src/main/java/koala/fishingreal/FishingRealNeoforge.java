package koala.fishingreal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@Mod(FishingReal.MOD_ID)
public class FishingRealNeoforge {
    public FishingRealNeoforge(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(FishingRealNeoforge::onDataPackRegistry);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, FishingRealNeoforge::onItemFished);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(FishingReal.FISHING_CONVERSION_REGISTRY_KEY, FishingConversion.CODEC, FishingConversion.CODEC);
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