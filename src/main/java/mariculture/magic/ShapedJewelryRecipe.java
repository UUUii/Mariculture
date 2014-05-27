package mariculture.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ShapedJewelryRecipe implements IRecipe {
	// Added in for future ease of change, but hard coded for now.
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;

	private ItemStack output = null;
	private Object[] input = null;
	private int width = 0;
	private int height = 0;
	private boolean mirrored = true;

	public ShapedJewelryRecipe(Block result, Object... recipe) {
		this(new ItemStack(result), recipe);
	}

	public ShapedJewelryRecipe(Item result, Object... recipe) {
		this(new ItemStack(result), recipe);
	}

	public ShapedJewelryRecipe(ItemStack result, Object... recipe) {
		output = result.copy();

		String shape = "";
		int idx = 0;

		if (recipe[idx] instanceof Boolean) {
			mirrored = (Boolean) recipe[idx];
			if (recipe[idx + 1] instanceof Object[]) {
				recipe = (Object[]) recipe[idx + 1];
			} else {
				idx = 1;
			}
		}

		if (recipe[idx] instanceof String[]) {
			String[] parts = ((String[]) recipe[idx++]);

			for (String s : parts) {
				width = s.length();
				shape += s;
			}

			height = parts.length;
		} else {
			while (recipe[idx] instanceof String) {
				String s = (String) recipe[idx++];
				shape += s;
				width = s.length();
				height++;
			}
		}

		if (width * height != shape.length()) {
			String ret = "Invalid shaped ore recipe: ";
			for (Object tmp : recipe) {
				ret += tmp + ", ";
			}
			ret += output;
			throw new RuntimeException(ret);
		}

		HashMap<Character, Object> itemMap = new HashMap<Character, Object>();

		for (; idx < recipe.length; idx += 2) {
			Character chr = (Character) recipe[idx];
			Object in = recipe[idx + 1];

			if (in instanceof ItemStack) {
				itemMap.put(chr, ((ItemStack) in).copy());
			} else if (in instanceof Item) {
				itemMap.put(chr, new ItemStack((Item) in));
			} else if (in instanceof Block) {
				itemMap.put(chr, new ItemStack((Block) in, 1, OreDictionary.WILDCARD_VALUE));
			} else if (in instanceof String) {
				itemMap.put(chr, OreDictionary.getOres((String) in));
			} else {
				String ret = "Invalid shaped ore recipe: ";
				for (Object tmp : recipe) {
					ret += tmp + ", ";
				}
				ret += output;
				throw new RuntimeException(ret);
			}
		}

		input = new Object[width * height];
		int x = 0;
		for (char chr : shape.toCharArray()) {
			input[x++] = itemMap.get(chr);
		}
	}

	 ShapedJewelryRecipe(ShapedRecipes recipe, Map<ItemStack, String> replacements) {
		output = recipe.getRecipeOutput();
		width = recipe.recipeWidth;
		height = recipe.recipeHeight;

		input = new Object[recipe.recipeItems.length];

		for (int i = 0; i < input.length; i++) {
			ItemStack ingred = recipe.recipeItems[i];

			if (ingred == null)
				continue;

			input[i] = recipe.recipeItems[i];

			for (Entry<ItemStack, String> replace : replacements.entrySet()) {
				if (OreDictionary.itemMatches(replace.getKey(), ingred, true)) {
					input[i] = OreDictionary.getOres(replace.getValue());
					break;
				}
			}
		}
	}
	
	public static class LinkedInteger {
		int enchant;
		int level;
		public LinkedInteger(int enchant, int level) {
			this.enchant = enchant;
			this.level = level;
		}
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craft) {
		ItemStack ret = output.copy();
		if(!ret.hasTagCompound()) {
			ret.setTagCompound(new NBTTagCompound());
		}
		
		ArrayList<LinkedInteger> cache = new ArrayList();
		for(int j = 0; j < craft.getSizeInventory(); j++) {
			ItemStack stack = craft.getStackInSlot(j);
			if(stack != null) {
				LinkedHashMap<Integer, Integer> maps = (LinkedHashMap<Integer, Integer>) EnchantmentHelper.getEnchantments(stack);
				for(Entry<Integer, Integer> i: maps.entrySet()) {
					Enchantment enchant = Enchantment.enchantmentsList[i.getKey()];
					cache.add(new LinkedInteger(enchant.effectId, EnchantmentHelper.getEnchantmentLevel(enchant.effectId, stack)));
				}
			}
		}
		
		if(cache.size() > 0) {
			Collections.shuffle(cache);
			int[] level = new int[cache.size()];
			int[] enchant = new int[cache.size()];
			for(int i = 0; i < cache.size(); i++) {
				enchant[i] = cache.get(i).enchant;
				level[i] = cache.get(i).level;
			}
			
			ret.stackTagCompound.setIntArray("EnchantmentList", enchant);
			ret.stackTagCompound.setIntArray("EnchantmentLevels", level);
		}
		
		return ret;
	}

	@Override
	public int getRecipeSize() {
		return input.length;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return output;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		for (int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++) {
			for (int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y) {
				if (checkMatch(inv, x, y, false)) {
					return true;
				}

				if (mirrored && checkMatch(inv, x, y, true)) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
		for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
			for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
				int subX = x - startX;
				int subY = y - startY;
				Object target = null;

				if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
					if (mirror) {
						target = input[width - subX - 1 + subY * width];
					} else {
						target = input[subX + subY * width];
					}
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);

				if (target instanceof ItemStack) {
					if (!checkItemEquals((ItemStack) target, slot)) {
						return false;
					}
				} else if (target instanceof ArrayList) {
					boolean matched = false;

					for (ItemStack item : (ArrayList<ItemStack>) target) {
						matched = matched || checkItemEquals(item, slot);
					}

					if (!matched) {
						return false;
					}
				} else if (target == null && slot != null) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkItemEquals(ItemStack target, ItemStack input) {
		if (input == null && target != null || input != null && target == null) {
			return false;
		}
		return (target.getItem() == input.getItem() && (target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage()));
	}

	public ShapedJewelryRecipe setMirrored(boolean mirror) {
		mirrored = mirror;
		return this;
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should
	 * never manipulate the values in this array as it will effect the recipe
	 * itself.
	 * 
	 * @return The recipes input vales.
	 */
	public Object[] getInput() {
		return this.input;
	}
}