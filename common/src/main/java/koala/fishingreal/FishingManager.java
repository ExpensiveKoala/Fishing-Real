package koala.fishingreal;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishingManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger();
	private List<FishingConversion> conversions = ImmutableList.of();

	public FishingManager() {
		super(GSON,"fishing");
	}
	
	public FishingConversion getConversionFromStack(ItemStack stack) {
		for(FishingConversion conv : conversions) {
			if(FishingReal.doItemStacksMatchIgnoreNBT(stack, conv.stack())) {
				return conv;
			}
		}
		return null;
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		List<FishingConversion> output = new ArrayList<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
			ResourceLocation resourcelocation = entry.getKey();
			if (resourcelocation.getPath().startsWith("_")) continue;
			
			try {
				FishingConversion.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.result()
					.ifPresent(conversion -> {
						output.removeIf(conv -> FishingReal.doItemStacksMatchIgnoreNBT(conversion.stack(), conv.stack()));
						output.add(conversion);
					});
			} catch (IllegalArgumentException | JsonParseException jsonparseexception) {
				LOGGER.error("Parsing error loading fishing conversion {}", resourcelocation, jsonparseexception);
			}
		}
		conversions = ImmutableList.copyOf(output);
		LOGGER.info("Loaded {} fish conversions", conversions.size());
	}
}
