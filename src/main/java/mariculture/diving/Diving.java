package mariculture.diving;

import java.util.HashMap;

import mariculture.core.Core;
import mariculture.core.helpers.RecipeHelper;
import mariculture.core.helpers.RegistryHelper;
import mariculture.core.lib.CraftingMeta;
import mariculture.core.lib.Dye;
import mariculture.core.lib.ItemIds;
import mariculture.core.lib.Modules.RegistrationModule;
import mariculture.core.lib.MultiMeta;
import mariculture.core.lib.RenderIds;
import net.minecraft.block.Block;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

public class Diving extends RegistrationModule {
	public static Item divingHelmet;
	public static Item divingTop;
	public static Item divingPants;
	public static Item divingBoots;
	public static Item scubaMask;
	public static Item scubaTank;
	public static Item scubaSuit;
	public static Item swimfin;
	public static Item snorkel;

	private static EnumArmorMaterial armorSCUBA = EnumHelper.addArmorMaterial("SCUBA", 15, new int[] { 0, 0, 1, 0 }, 0);
	private static EnumArmorMaterial armorDIVING = EnumHelper.addArmorMaterial("DIVING", 20, new int[] { 1, 0, 2, 1 }, 0);
	private static EnumArmorMaterial armorSnorkel = EnumHelper.addArmorMaterial("SNORKEL", 10, new int[] { 0, 0, 0, 0 }, 0);

	@Override
	public void registerHandlers() {
		MinecraftForge.EVENT_BUS.register(new ArmorEventHandler());
	}

	@Override
	public void registerBlocks() {
		GameRegistry.registerTileEntity(TileAirCompressor.class, "tileEntityAirCompressor");		
		MinecraftForge.setBlockHarvestLevel(Core.multi, MultiMeta.COMPRESSOR_BASE, "pickaxe", 1);
		MinecraftForge.setBlockHarvestLevel(Core.multi, MultiMeta.COMPRESSOR_TOP, "pickaxe", 1);
		MinecraftForge.setBlockHarvestLevel(Core.multi, MultiMeta.PRESSURE_VESSEL, "pickaxe", 1);
	}

	@Override
	public void registerItems() {
		divingHelmet = (new ItemArmorDiving(ItemIds.divingHelmet, armorDIVING, RenderIds.DIVING, 0)).setUnlocalizedName("divingHelmet");
		divingTop = (new ItemArmorDiving(ItemIds.divingTop, armorDIVING, RenderIds.DIVING, 1)).setUnlocalizedName("divingTop");
		divingPants = (new ItemArmorDiving(ItemIds.divingPants, armorDIVING, RenderIds.DIVING, 2)).setUnlocalizedName("divingPants");
		divingBoots = (new ItemArmorDiving(ItemIds.divingBoots, armorDIVING, RenderIds.DIVING, 3)).setUnlocalizedName("divingBoots");

		scubaMask = (new ItemArmorScuba(ItemIds.scubaMask, armorSCUBA, RenderIds.SCUBA, 0)).setUnlocalizedName("scubaMask");
		scubaTank = (new ItemArmorScuba(ItemIds.scubaTank, armorSCUBA, RenderIds.SCUBA, 1)).setUnlocalizedName("scubaTank").setNoRepair();
		scubaSuit = (new ItemArmorScuba(ItemIds.scubaSuit, armorSCUBA, RenderIds.SCUBA, 2)).setUnlocalizedName("scubaSuit");
		swimfin = (new ItemArmorScuba(ItemIds.swimfin, armorSCUBA, RenderIds.SCUBA, 3)).setUnlocalizedName("swimfin");
		
		snorkel = (new ItemArmorSnorkel(ItemIds.snorkel, armorSnorkel, RenderIds.SNORKEL, 0)).setUnlocalizedName("snorkel");
		
		RegistryHelper.register(new Object[]{ divingHelmet, divingTop, divingPants, divingBoots, 
												scubaMask, scubaTank, scubaSuit, swimfin, snorkel });
	}
	
	@Override
	public void registerOther() {
		return;
	}
	
	@Override
	public void registerRecipes() {
		//Air Compressor Top
		RecipeHelper.addShapedRecipe(new ItemStack(Core.multi, 2, MultiMeta.COMPRESSOR_TOP), new Object[] {
			"  F", " PB", "III",
			Character.valueOf('I'), new ItemStack(Core.crafting, 1, CraftingMeta.ALUMINUM_SHEET),
			Character.valueOf('F'), new ItemStack(Core.crafting, 1, CraftingMeta.COOLER),
			Character.valueOf('B'), new ItemStack(Core.batteryTitanium, 1, OreDictionary.WILDCARD_VALUE),
			Character.valueOf('P'), Block.pistonBase
		});
		
		//Air Compressor Base
		RecipeHelper.addShapedRecipe(new ItemStack(Core.multi, 1, MultiMeta.COMPRESSOR_BASE), new Object[] {
			"ITT", "III", "W  ",
			Character.valueOf('I'), new ItemStack(Core.crafting, 1, CraftingMeta.ALUMINUM_SHEET),
			Character.valueOf('W'), new ItemStack(Core.crafting, 1, CraftingMeta.WHEEL),
			Character.valueOf('T'), "ingotTitanium"
		});
		
		//Snorkel
		RecipeHelper.addShapedRecipe(new ItemStack(snorkel), new Object[] {
			"  R", "LLR",
			Character.valueOf('R'), Item.reed,
			Character.valueOf('L'), new ItemStack(Core.crafting, 1, CraftingMeta.LENS_GLASS)
		});
		
		CraftingManager
				.getInstance()
				.getRecipeList()
				.add(new ShapedOreRecipe(new ItemStack(Diving.divingHelmet), true, new Object[] { "CCC", "CGC",
					Character.valueOf('C'), "ingotCopper", Character.valueOf('G'), "glass" }));

		GameRegistry.addRecipe(new ItemStack(Diving.divingTop), new Object[] { " C ", "C C", " C ", 
			Character.valueOf('C'), new ItemStack(Item.leather) });
		
		GameRegistry.addRecipe(new ItemStack(Diving.divingPants), new Object[] { "CCC", " C ", "CCC", 
				Character.valueOf('C'), new ItemStack(Item.leather) });
		
		CraftingManager
				.getInstance()
				.getRecipeList()
				.add(new ShapedOreRecipe(new ItemStack(Diving.divingBoots), true, new Object[] { "C C", "L L",
					Character.valueOf('C'), new ItemStack(Item.leather),
					Character.valueOf('L'), new ItemStack(Item.ingotIron) }));
		
		CraftingManager
				.getInstance()
				.getRecipeList()
				.add(new ShapedOreRecipe(Diving.scubaMask, true, new Object[] { "PD", "LL", 
						Character.valueOf('P'), "dyeBlack", 
						Character.valueOf('L'), new ItemStack(Core.crafting, 1, CraftingMeta.LENS), 
						Character.valueOf('D'), "dyeYellow" }));
		
		ItemStack tank = new ItemStack(Diving.scubaTank);
		tank.setItemDamage(tank.getMaxDamage() - 1);
		
		CraftingManager
				.getInstance()
				.getRecipeList()
				.add(new ShapedOreRecipe(tank, true, new Object[] { "DSD", "S S", "DSD",
					Character.valueOf('S'), new ItemStack(Core.crafting, 1, CraftingMeta.ALUMINUM_SHEET), 
					Character.valueOf('D'), "dyeYellow" }));

		GameRegistry.addRecipe(new ItemStack(Diving.scubaSuit), new Object[] { "NNN", " N ", "NNN", 
				Character.valueOf('N'), new ItemStack(Core.crafting, 1, CraftingMeta.NEOPRENE) });
		
		GameRegistry.addRecipe(new ItemStack(Diving.swimfin), new Object[] { "N N", "PDP", "PDP", 
				Character.valueOf('N'), new ItemStack(Core.crafting, 1, CraftingMeta.NEOPRENE), 
				Character.valueOf('P'), new ItemStack(Core.crafting, 1, CraftingMeta.PLASTIC), 
				Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, Dye.INK) });
	}
}
