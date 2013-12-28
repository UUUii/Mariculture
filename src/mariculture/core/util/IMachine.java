package mariculture.core.util;

import mariculture.core.gui.ContainerMariculture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IMachine extends IHasGUI {
	public void sendGUINetworkData(ContainerMariculture container, EntityPlayer player);
	public void getGUINetworkData(int id, int value);
	
	public ItemStack[] getInventory();
}
