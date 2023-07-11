package koala.fishingreal.forge.mixin.compat;

import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AquaFishingBobberEntity.class)
public interface AquaFishingBobberEntityAccessor {

    @Accessor(remap = false)
    int getLuck();
    
    @Accessor(remap = false)
    ItemStack getFishingRod();
}
