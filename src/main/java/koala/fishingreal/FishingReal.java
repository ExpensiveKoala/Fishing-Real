package koala.fishingreal;

import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("fishingreal")
public class FishingReal {
	
	public static final Logger LOGGER = LogManager.getLogger("FishingReal");
	
	public static final FishingManager FISHING_MANAGER = new FishingManager();
	
	public FishingReal() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onServerStarting(AddReloadListenerEvent event) {
		event.addListener(FISHING_MANAGER);
	}
	
	@SubscribeEvent
	public void itemFished(ItemFishedEvent event) {
		Player angler = event.getPlayer();
		FishingHook hook = event.getHookEntity();
		List<ItemStack> drops = event.getDrops();
		for (ItemStack stack : drops) {
			CompoundTag nbt = FISHING_MANAGER.matchWithStack(stack);
			if (nbt != null) {
				EntityType.loadEntityRecursive(nbt, angler.level, (entity -> {
					//spawn with velocity to fling towards player
					Level l = angler.level;
					if (l instanceof ServerLevel) {
						ServerLevel level = (ServerLevel) l;
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
						
						if (FISHING_MANAGER.getConversionFromStack(stack).isRandomizeNBT()) {
							if (entity instanceof Mob) {
								((Mob) entity).finalizeSpawn(level, level.getCurrentDifficultyAt(angler.blockPosition()), MobSpawnType.NATURAL, null, null);
							}
						}
						
						return !level.addFreshEntity(entity) ? null : entity;
					} else {
						return null;
					}
					
				}));
				if (!angler.isCreative()) {
					event.damageRodBy(1);
				}
				event.setCanceled(true);
			}
		}
	}
}
