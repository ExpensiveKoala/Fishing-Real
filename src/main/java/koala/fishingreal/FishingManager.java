package koala.fishingreal;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import koala.fishingreal.util.StackUtils;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishingManager extends JsonReloadListener {
	private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(FishingConversion.class, new FishingConversion.Serializer()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	private List<FishingConversion> conversions = ImmutableList.of();
	
	public FishingManager() {super(GSON_INSTANCE,"fishing");}
	
	public List<FishingConversion> getConversions() {
		return conversions;
	}
	
	public FishingConversion getConversionFromStack(ItemStack stack) {
		for(FishingConversion conv : conversions) {
			if(StackUtils.doItemStacksMatchIgnoreNBT(stack, conv.getStack())) {
				return conv;
			}
		}
		return null;
	}
	
	public CompoundNBT matchWithStack(ItemStack stack) {
		FishingConversion conv = getConversionFromStack(stack);
		if(conv != null) {
			return conv.target;
		}
		return null;
	}
	

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		List<FishingConversion> output = new ArrayList<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			ResourceLocation resourcelocation = entry.getKey();
			if (resourcelocation.getPath().startsWith("_")) continue;
			
			try {
				FishingConversion conversion = GSON_INSTANCE.fromJson(entry.getValue(), FishingConversion.class);
				output.removeIf(conv -> StackUtils.doItemStacksMatchIgnoreNBT(conversion.getStack(), conv.getStack()));
				output.add(conversion);
			} catch (IllegalArgumentException | JsonParseException jsonparseexception) {
				LOGGER.error("Parsing error loading fishing conversion {}", resourcelocation, jsonparseexception);
			}
		}
		conversions = ImmutableList.copyOf(output);
		LOGGER.info("Loaded {} fish conversions", conversions.size());
	}
}
