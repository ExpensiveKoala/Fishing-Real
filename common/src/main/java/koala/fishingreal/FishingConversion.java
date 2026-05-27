package koala.fishingreal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record FishingConversion(FishingInput input, FishingResult result) {

	public static final Codec<FishingConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FishingInput.CODEC.fieldOf("input").forGetter(FishingConversion::input),
			FishingResult.CODEC.fieldOf("result").forGetter(FishingConversion::result)
	).apply(instance, FishingConversion::new));

	public record FishingInput(Item item, int count) {
		public static final Codec<FishingInput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(FishingInput::item),
				Codec.intRange(1, 64).fieldOf("count").orElse(1).forGetter(FishingInput::count)
		).apply(instance, FishingInput::new));
	}

	public record FishingResult(EntityType<?> entity, Optional<CompoundTag> tag) {
		public static final Codec<FishingResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter(FishingResult::entity),
				CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FishingResult::tag)
		).apply(instance, FishingResult::new));

		public boolean randomizeNbt() {
			return tag.isEmpty();
		}
	}
	
}
