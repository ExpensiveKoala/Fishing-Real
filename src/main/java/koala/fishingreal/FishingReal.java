package koala.fishingreal;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
		PlayerEntity angler = event.getPlayer();
		FishingBobberEntity hook = event.getHookEntity();
		List<ItemStack> drops = event.getDrops();
		for (ItemStack stack : drops) {
			CompoundNBT nbt = FISHING_MANAGER.matchWithStack(stack);
			if (nbt != null) {
				EntityType.func_220335_a(nbt, angler.getEntityWorld(), (entity -> {
					//spawn with velocity to fling towards player
					World w = angler.getEntityWorld();
					if (w instanceof ServerWorld) {
						ServerWorld world = (ServerWorld) w;
						entity.setLocationAndAngles(hook.getPositionVec().getX(), hook.getPositionVec().getY(), hook.getPositionVec().getZ(), hook.rotationYaw, hook.rotationPitch);
						double dX = angler.getPositionVec().getX() - hook.getPositionVec().getX();
						double dY = angler.getPositionVec().getY() - hook.getPositionVec().getY();
						double dZ = angler.getPositionVec().getZ() - hook.getPositionVec().getZ();
						double mult = 0.12;
						entity.setMotion(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);
						
						world.addEntity(new ExperienceOrbEntity(angler.world, angler.getPositionVec().getX(), angler.getPositionVec().getY() + 0.5D, angler.getPositionVec().getZ() + 0.5D, angler.world.getRandom().nextInt(6) + 1));
						
						if (stack.getItem().isIn(ItemTags.FISHES)) {
							angler.addStat(Stats.FISH_CAUGHT, 1);
						}
						
						if (angler instanceof ServerPlayerEntity) {
							CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) angler, angler.getActiveItemStack(), hook, Arrays.asList(stack));
						}
						
						if (FISHING_MANAGER.getConversionFromStack(stack).isRandomizeNBT()) {
							if (entity instanceof MobEntity) {
								((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(angler.func_233580_cy_()), SpawnReason.NATURAL, null, null);
							}
						}
						
						return !world.summonEntity(entity) ? null : entity;
					} else return null;
					
				}));
				if (!angler.isCreative()) {
					event.damageRodBy(1);
				}
				event.setCanceled(true);
			}
		}
	}
}
