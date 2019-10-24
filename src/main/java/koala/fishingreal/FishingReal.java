package koala.fishingreal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@Mod("fishingreal")
public class FishingReal {
	
	public static final Logger LOGGER = LogManager.getLogger("FishingReal");
	
	public static final FishingManager FISHING_MANAGER = new FishingManager();
	
	public FishingReal() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onServerStarting(FMLServerAboutToStartEvent event) {
		event.getServer().getResourceManager().addReloadListener(FISHING_MANAGER);
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
						entity.setLocationAndAngles(hook.posX, hook.posY, hook.posZ, hook.rotationYaw, hook.rotationPitch);
						double dX = angler.posX - hook.posX;
						double dY = angler.posY - hook.posY;
						double dZ = angler.posZ - hook.posZ;
						double mult = 0.12;
						entity.setMotion(dX * mult, dY * mult + Math.sqrt(Math.sqrt(dX * dX + dY * dY + dZ * dZ)) * 0.14D, dZ * mult);
						
						world.addEntity(new ExperienceOrbEntity(angler.world, angler.posX, angler.posY + 0.5D, angler.posZ + 0.5D, angler.world.getRandom().nextInt(6) + 1));
						
						if (stack.getItem().isIn(ItemTags.FISHES)) {
							angler.addStat(Stats.FISH_CAUGHT, 1);
						}
						
						if (angler instanceof ServerPlayerEntity) {
							CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) angler, angler.getActiveItemStack(), hook, Arrays.asList(stack));
						}
						
						if (FISHING_MANAGER.getConversionFromStack(stack).isRandomizeNBT()) {
							if (entity instanceof MobEntity) {
								((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(angler.getPosition()), SpawnReason.NATURAL, null, null);
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
