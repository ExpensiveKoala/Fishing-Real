package koala.fishingreal;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.neoforged.fml.config.ModConfig;

public class FishingRealFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(FishingReal.FISHING_CONVERSION_REGISTRY_KEY, FishingConversion.CODEC);

        ConfigRegistry.INSTANCE.register(FishingReal.MOD_ID, ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }
}