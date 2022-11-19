package koala.fishingreal.mixin;

import koala.fishingreal.FishingConversion;
import koala.fishingreal.FishingReal;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.*;
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

        for (ItemStack drop : drops) {
            FishingConversion conversion = FishingReal.FISHING_MANAGER.getConversionFromStack(drop);
            if (conversion != null && conversion.result().entity() != null) {
                FishingConversion.FishingResult result = conversion.result();
                Entity entity = result.entity().create(angler.level);
                if (entity != null) {
                    result.tag().ifPresent(entity::load);
                    if (angler.level instanceof ServerLevel serverLevel) {
                        entity.moveTo(this.position().x(), this.position().y(), this.position().z(), this.xRotO, this.yRotO);
                        double dX = angler.position().x() - this.position().x();
                        double dY = angler.position().y() - this.position().y();
                        double dZ = angler.position().z() - this.position().z();
                        double mult = 0.12;
                        entity.setDeltaMovement(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);

                        serverLevel.addFreshEntity(new ExperienceOrb(angler.level, angler.position().x(), angler.position().y() + 0.5D, angler.position().z() + 0.5D, angler.level.getRandom().nextInt(6) + 1));

                        if (drop.is(ItemTags.FISHES)) {
                            angler.awardStat(Stats.FISH_CAUGHT, 1);
                        }

                        if (angler instanceof ServerPlayer serverPlayer) {
                            CriteriaTriggers.FISHING_ROD_HOOKED.trigger(serverPlayer, angler.getUseItem(), (FishingHook) (Object) this, List.of(drop));
                        }

                        if (result.randomizeNbt() && entity instanceof Mob mob) {
                            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(angler.blockPosition()), MobSpawnType.NATURAL, null, null);
                        }
                        serverLevel.addFreshEntity(entity);
                    }
                    if (!angler.isCreative()) {
                        stack.hurtAndBreak(1, angler, (player) -> player.broadcastBreakEvent(angler.getUsedItemHand()));
                    }
                    this.discard();
                    cir.setReturnValue(1);
                }
            }
        }

    }
}
