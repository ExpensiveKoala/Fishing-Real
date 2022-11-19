package koala.fishingreal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public class FishingReal
{
	public static final FishingManager FISHING_MANAGER = new FishingManager();

	public static final String MOD_ID = "fishingreal";

	public static void init() {
		
	}

	public static boolean doItemStacksMatchIgnoreNBT(ItemStack stack1, ItemStack stack2) {
		return stack1.is(stack2.getItem()) && stack1.getCount() == stack2.getCount();
	}

	public static void onRegisterReloadListeners(BiConsumer<ResourceLocation, PreparableReloadListener> registry) {
		registry.accept(new ResourceLocation(MOD_ID + "fishing"), FISHING_MANAGER);
	}
}