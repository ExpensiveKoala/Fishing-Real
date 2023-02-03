package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class FishingReal {
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

    public static boolean retrieve(Player angler, List<ItemStack> stacks, FishingHook hook) {
        for (ItemStack stack : stacks) {
            FishingConversion conversion = FishingReal.FISHING_MANAGER.getConversionFromStack(stack);
            if (conversion != null && conversion.result().entity() != null) {
                FishingConversion.FishingResult result = conversion.result();
                Entity entity = result.entity().create(angler.level);
                if (entity != null) {
                    result.tag().ifPresent(entity::load);
                    Level l = angler.level;
                    if (l instanceof ServerLevel level) {
                        entity.moveTo(hook.position().x(), hook.position().y(), hook.position().z(), hook.xRotO, hook.yRotO);
                        double dX = angler.position().x() - hook.position().x();
                        double dY = angler.position().y() - hook.position().y();
                        double dZ = angler.position().z() - hook.position().z();
                        double mult = 0.12;
                        entity.setDeltaMovement(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);

                        level.addFreshEntity(new ExperienceOrb(angler.level, angler.position().x(), angler.position().y() + 0.5D, angler.position().z() + 0.5D, angler.level.getRandom().nextInt(6) + 1));

                        if (stack.is(ItemTags.FISHES)) {
                            angler.awardStat(Stats.FISH_CAUGHT, 1);
                        }

                        if (angler instanceof ServerPlayer) {
                            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) angler, angler.getUseItem(), hook, Arrays.asList(stack));
                        }

                        if (result.randomizeNbt() && entity instanceof Mob mob) {
                            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(angler.blockPosition()), MobSpawnType.NATURAL, null, null);
                        }
                        level.addFreshEntity(entity);
                    }
					hook.discard();
                    return true;
                }
            }
        }
        return false;
    }
}