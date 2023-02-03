package koala.fishingreal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record FishingConversion(ItemStack stack, FishingResult result) {

	public static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Registry.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem),
			Codec.intRange(1, 64).fieldOf("count").orElse(1).forGetter(ItemStack::getCount)
	).apply(instance, ItemStack::new));

	public static final Codec<FishingConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ITEM_STACK_CODEC.fieldOf("input").forGetter(FishingConversion::stack),
		FishingResult.CODEC.fieldOf("result").forGetter(FishingConversion::result)
	).apply(instance, FishingConversion::new));

	public record FishingResult(EntityType<?> entity, Optional<CompoundTag> tag) {

		public static final Codec<FishingResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Registry.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter(FishingResult::entity),
				CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FishingResult::tag)
		).apply(instance, FishingResult::new));

		public boolean randomizeNbt() {
			return tag.isEmpty();
		}
	}
	
}
