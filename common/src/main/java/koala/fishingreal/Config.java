package koala.fishingreal;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Config CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    
    public final ModConfigSpec.BooleanValue enableCatchInteraction;
    public final ModConfigSpec.BooleanValue limitInteractionToWaterBucket;
    
    private Config(ModConfigSpec.Builder builder) {
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
        Pair<Config, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Config::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
