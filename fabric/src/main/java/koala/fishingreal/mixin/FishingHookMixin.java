package koala.fishingreal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import koala.fishingreal.FishingReal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    
    @Inject(method = "retrieve", at = @At(value = "INVOKE_ASSIGN", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    public void onFishCaught(ItemStack rod, CallbackInfoReturnable<Integer> cir, @Local(name = "owner") Player owner, @Local(name = "items") List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            Entity convertedEntity = FishingReal.convertItemStack(itemStack, owner, ((FishingHook)(Object)this).position());
            if (convertedEntity != null) {
                for (int i = 0; i < itemStack.getCount(); i++) {
                    FishingReal.fishUpEntity(convertedEntity, (FishingHook)(Object)this, itemStack, owner);
                }
                // Effectively remove the item from the loot pool
                itemStack.setCount(0);
            }
        }
    }
}
