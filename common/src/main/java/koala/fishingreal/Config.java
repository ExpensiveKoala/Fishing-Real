package koala.fishingreal;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Config CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    
    public final ForgeConfigSpec.BooleanValue enableCatchInteraction;
    public final ForgeConfigSpec.BooleanValue limitInteractionToWaterBucket;
    
    private Config(ForgeConfigSpec.Builder builder) {
        enableCatchInteraction = builder
          .comment("Whether to enable interacting with the caught entity")
          .comment("This usually means bucketing the fish if you are holding a water bucket")
          .define("enableCatchInteraction", false);
        limitInteractionToWaterBucket = builder
          .comment("Whether to limit the interaction to items in the c:buckets/water item tag.")
          .comment("Does nothing if enableCatchInteraction is disabled...")
          .define("limitInteractionToWaterBucket", true);
    }
    
    static {
        Pair<Config, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Config::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
