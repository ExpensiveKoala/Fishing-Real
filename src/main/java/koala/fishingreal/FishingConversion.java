package koala.fishingreal;

import com.google.gson.*;
import koala.fishingreal.util.JsonUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;

public class FishingConversion {
	
	protected ItemStack stack;
	protected CompoundTag target;
	protected boolean randomizeNBT;
	
	public FishingConversion(ItemStack stack, CompoundTag target, boolean randomizeNBT) {
		this.stack = stack;
		this.target = target;
		this.randomizeNBT = randomizeNBT;
	}
	
	@Override
	public String toString() {
		return "FishingConversion{" +
		  "stack=" + stack +
		  ", target=" + target +
		  ", randomizeNBT=" + randomizeNBT +
		  '}';
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public CompoundTag getTarget() {
		return target;
	}
	
	public boolean isRandomizeNBT() {
		return randomizeNBT;
	}
	
	public static class Serializer implements JsonDeserializer<FishingConversion>, JsonSerializer<FishingConversion> {
		
		@Override
		public FishingConversion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			ItemStack stack = JsonUtils.deserializeItemStack(obj.get("input").getAsJsonObject());
			JsonObject result = obj.get("result").getAsJsonObject();
			String id = result.get("id").getAsString();
			CompoundTag nbt = null;
			if(result.has("nbt")) {
				nbt = JsonUtils.deserializeCompoundNBT(result.get("nbt").getAsJsonObject());
			}
			CompoundTag target = new CompoundTag();
			target.putString("id", id);
			if(nbt != null) {
				target.merge(nbt);
			}
			return new FishingConversion(stack, target, nbt == null);
		}
		
		@Override
		public JsonElement serialize(FishingConversion src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.add("input", JsonUtils.serializeItemStack(src.stack));
			obj.add("result", JsonUtils.serializeCompoundNBT(src.target));
			return obj;
		}
	}
	
}
