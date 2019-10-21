package koala.fishingreal.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import koala.fishingreal.FishingReal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JsonUtils {
	public static ItemStack deserializeItemStack(JsonObject object) {
		String id = object.get("item").getAsString();
		int count = object.has("count") ? object.get("count").getAsInt() : 1;
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
		return new ItemStack(item, count);
	}
	
	public static JsonObject serializeItemStack(ItemStack stack) {
		JsonObject object = new JsonObject();
		object.addProperty("item", stack.getItem().getRegistryName().toString());
		if(stack.getCount() > 1) {
			object.addProperty("count", stack.getCount());
		}
		return object;
	}
	
	public static CompoundNBT deserializeCompoundNBT(JsonObject object) {
		try {
			return JsonToNBT.getTagFromJson(object.toString());
		} catch (CommandSyntaxException e) {
			FishingReal.LOGGER.error("Parsing error loading json as NBT {}", object.toString());
		}
		return new CompoundNBT();
	}
	
	public static JsonObject serializeCompoundNBT(CompoundNBT nbt) {
		nbt = nbt.copy();
		JsonObject object = new JsonObject();
		object.addProperty("id", nbt.getString("id"));
		nbt.remove("id");
		if(nbt.size() > 0) {
			object.add("nbt", new Gson().fromJson(nbt.toString(), JsonObject.class));
		}
		return object;
	}
}
