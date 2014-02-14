package mariculture;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import mariculture.api.core.MaricultureRegistry;
import mariculture.api.core.MaricultureTab;
import mariculture.api.core.IAnvilHandler.RecipeAnvil;
import mariculture.core.CommonProxy;
import mariculture.core.Config;
import mariculture.core.RecipesSmelting;
import mariculture.core.guide.GuideHandler;
import mariculture.core.guide.GuideRegistry;
import mariculture.core.handlers.LogHandler;
import mariculture.core.helpers.RegistryHelper;
import mariculture.core.lib.Modules;
import mariculture.core.network.PacketHandler;
import mariculture.core.network.Packets;
import mariculture.plugins.Plugins;
import mariculture.plugins.compatibility.Compat;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "Mariculture", name = "Mariculture", version = "1.2.0i", dependencies="after:TConstruct;after:Railcraft;after:ExtrabiomesXL;after:Forestry;after:IC2;after:Thaumcraft;after:BiomesOPlenty;after:AWWayofTime")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "Mariculture" }, packetHandler = PacketHandler.class)
public class Mariculture {
	public static final String modid = "mariculture";

	@SidedProxy(clientSide = "mariculture.core.ClientProxy", serverSide = "mariculture.core.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance("Mariculture")
	public static Mariculture instance = new Mariculture();
	
	//Root folder
	public static File root;
	
	//Plugins
	public static Plugins plugins = new Plugins();
	public static enum Stage {
		PRE, INIT, POST;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		root = event.getModConfigurationDirectory();
		Config.init(root + "/mariculture/");
		LogHandler.init();	
		
		MaricultureTab.tabMariculture = new MaricultureTab("maricultureTab");
		MaricultureTab.tabFish = (Modules.fishery.isActive())? new MaricultureTab("fishTab"): null;
		MaricultureTab.tabJewelry = (Modules.magic.isActive())? new MaricultureTab("jewelryTab"): null;
		
		plugins.init();
		Modules.core.preInit();
		Modules.diving.preInit();
		Modules.factory.preInit();
		Modules.fishery.preInit();
		Modules.magic.preInit();
		Modules.sealife.preInit();
		Modules.transport.preInit();
		Modules.world.preInit();
		plugins.load(Stage.PRE);
		Compat.preInit();

		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		plugins.load(Stage.INIT);
		Modules.core.init();
		Modules.diving.init();
		Modules.factory.init();
		Modules.fishery.init();
		Modules.magic.init();
		Modules.sealife.init();
		Modules.transport.init();
		Modules.world.init();
		Compat.init();
		Packets.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		plugins.load(Stage.POST);
		proxy.initClient();
		proxy.loadBooks();
		RecipesSmelting.postAdd();
	}
}