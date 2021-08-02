package koala.fishingreal.util;


import net.minecraft.world.item.ItemStack;

public class StackUtils {
	public static boolean doItemStacksMatchIgnoreNBT(ItemStack stack1, ItemStack stack2) {
		return stack1.getItem() == stack2.getItem() && stack1.getCount() == stack2.getCount();
	}
}
