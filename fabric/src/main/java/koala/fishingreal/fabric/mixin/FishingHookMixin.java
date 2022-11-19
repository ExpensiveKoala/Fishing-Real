package koala.fishingreal.fabric.mixin;

import koala.fishingreal.FishingReal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

    @Final
    @Shadow
    private int luck;

    public FishingHookMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    public abstract Player getPlayerOwner();

    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext$Builder;<init>(Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    public void fishingreal_retrieve(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Player angler = getPlayerOwner();
        if (angler == null) {
            return;
        }
        LootContext.Builder builder = (new LootContext.Builder((ServerLevel) angler.level)).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, stack).withParameter(LootContextParams.THIS_ENTITY, this).withRandom(this.level.random).withLuck((float) this.luck + angler.getLuck());
        LootTable lootTable = angler.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
        List<ItemStack> drops = lootTable.getRandomItems(builder.create(LootContextParamSets.FISHING));

        if (FishingReal.retrieve(angler, drops, (FishingHook) (Object) this)) {
            if (!angler.isCreative()) {
                cir.setReturnValue(1);
            } else {
                cir.setReturnValue(0);
            }
        }
    }
}
