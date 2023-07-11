package koala.fishingreal.fabric.mixin;

import koala.fishingreal.FishingReal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow @Nullable public abstract Player getPlayerOwner();
    @ModifyArg(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", ordinal = 0))
    public Entity replaceHookedItems(Entity original) {
        return FishingReal.convertItemEntity(original, getPlayerOwner());
    }
}
