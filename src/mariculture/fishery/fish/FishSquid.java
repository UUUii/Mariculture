package mariculture.fishery.fish;

import java.util.Random;

import mariculture.api.core.EnumBiomeType;
import mariculture.api.fishery.EnumRodQuality;
import mariculture.api.fishery.Fishing;
import mariculture.api.fishery.fish.EnumFishGroup;
import mariculture.api.fishery.fish.FishSpecies;
import mariculture.core.Core;
import mariculture.core.lib.Dye;
import mariculture.core.lib.MaterialsMeta;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FishSquid extends FishSpecies {
	public FishSquid(int id) {
		super(id);
	}

	@Override
	public EnumFishGroup getGroup() {
		return EnumFishGroup.JELLY;
	}

	@Override
	public int getLifeSpan() {
		return 20;
	}

	@Override
	public int getFertility() {
		return 40;
	}

	@Override
	public boolean isDominant() {
		return true;
	}

	@Override
	public ItemStack getProduct(Random rand) {
		if (rand.nextInt(4) == 1) {
			return new ItemStack(Item.dyePowder, 1, Dye.INK);
		}

		return (rand.nextInt(141) == 0) ? new ItemStack(Core.materials, 1, MaterialsMeta.DROP_WATER) : null;
	}

	@Override
	public boolean canCatch(Random rand, World world, int x, int y, int z, EnumRodQuality quality) {
		if (world.provider.isSurfaceWorld() && rand.nextInt(8) == 0) {
			if (Fishing.fishHelper.biomeMatches(world.getWorldChunkManager().getBiomeGenAt(x, z), new EnumBiomeType[] {
					EnumBiomeType.OCEAN, EnumBiomeType.FROZEN_OCEAN })) {
				return true;
			}
		}

		return false;
	}
	
	@Override
	public double getFishOilVolume() {
		return 0;
	}

	@Override
	public int[] getChestGenChance() {
		return null;
	}
	
	@Override
	public int getFishMealSize() {
		return 0;
	}
}
