package sidben.redstonejukebox;


import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import sidben.redstonejukebox.client.PlayerEventHandler;
import sidben.redstonejukebox.client.SoundEventHandler;
import sidben.redstonejukebox.common.BlockRedstoneJukebox;
import sidben.redstonejukebox.common.CommandPlayBgMusic;
import sidben.redstonejukebox.common.CommandPlayRecord;
import sidben.redstonejukebox.common.CommandPlayRecordAt;
import sidben.redstonejukebox.common.CommonProxy;
import sidben.redstonejukebox.common.CustomRecordHelper;
import sidben.redstonejukebox.common.ItemBlankRecord;
import sidben.redstonejukebox.common.ItemCustomRecord;
import sidben.redstonejukebox.common.TileEntityRedstoneJukebox;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;



@Mod(modid="SidbenRedstoneJukebox", name="Redstone Jukebox", version="1.2")
@NetworkMod(clientSideRequired=true, serverSideRequired=false, channels = {"chRSJukebox"}, packetHandler = sidben.redstonejukebox.common.PacketHandler.class)
public class ModRedstoneJukebox {

	
    // The instance of your mod that Forge uses.
	// Obs: MUST BE THE VALUE OF MODIF ABOVE!!1!11!!one!
	@Instance("SidbenRedstoneJukebox")
	public static ModRedstoneJukebox instance;
	
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="sidben.redstonejukebox.client.ClientProxy", serverSide="sidben.redstonejukebox.CommonProxy")
	public static CommonProxy proxy;

	
	// Channels
	public static final String jukeboxChannel = "chRSJukebox";		// OBS TODO: move this to a "config" class, along with the ModID
	
	
	// Models IDs
	public static int redstoneJukeboxModelID;

	
	// Textures and Icons paths
	public static String blankRecordIcon = "redstonejukebox:blank_record";
	public static String customRecordIconArray = "redstonejukebox:custom_record_";
	public static String jukeboxDiscIcon = "redstonejukebox:redstone_jukebox_disc";
	public static String jukeboxTopIcon = "redstonejukebox:redstone_jukebox_top";
	public static String jukeboxBottomIcon = "redstonejukebox:redstone_jukebox_bottom";
	public static String jukeboxSideOnIcon = "redstonejukebox:redstone_jukebox_on";
	public static String jukeboxSideOffIcon = "redstonejukebox:redstone_jukebox_off";

	public static final ResourceLocation redstoneJukeboxGui = new ResourceLocation("redstonejukebox", "/textures/gui/redstonejukebox-gui.png");
	public static final ResourceLocation recordTradeGui = new ResourceLocation("redstonejukebox", "/textures/gui/recordtrading-gui.png");

	
	// GUI IDs
	public static int redstoneJukeboxGuiID = 0;
	public static int recordTradingGuiID = 1;

	
	// Blocks and Items IDs
	public static int redstoneJukeboxIdleID;
	public static int redstoneJukeboxActiveID;
	public static int blankRecordItemID;
	public static int customRecordItemID;
	

    // Blocks and Items
	public static Item recordBlank;
	public static Item customRecord;
	public static Block redstoneJukebox;
	public static Block redstoneJukeboxActive; 
	
	
	// Global variables
	public final static String sourceName = "streaming";	// music discs are called "streaming" 
	public final static int maxCustomRecords = 32;			// Limit of custom records accepted
	public final static int maxCustomRecordIcon = 77;		// Limit of icon IDs for the records. This is stored on the metadata of the item. Start at zero.
	public final static int maxStores = 16;					// Number of "record stores" available. Each "store" is a random selection of records for trade.
	public final static int maxOffers = 8;					// Maximum number of record offers a villager have
	public static String customRecordsFolder = "jukebox";  	// Folder where this mod will look for custom records. Must be inside the 'Mods' folder.
	public static Vec3 lastSoundSource;						// holds the position of the last sound source (used so only 1 redstone jukebox is active at a time)
	public final static int maxExtraVolume = 128;			// Maximum amount of extra range for the custom jukebox

	
	// Config variables
	public static int customRecordOffersMin;				// Minimal of custom records a villager can offer
	public static int customRecordOffersMax;				// Maximum of custom records a villager can offer
	public static int customRecordPriceMin;					// Minimal value of custom records in emeralds
	public static int customRecordPriceMax;					// Maximum value of custom records in emeralds
	
		
	

	
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
        // Config file (ref: http://www.minecraftforge.net/wiki/How_to_make_an_advanced_configuration_file)
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        // Custom records config
    	String customRecordCategory = "custom_records";

    	
        try 
        {
        	// loading the configuration from its file
        	config.load();
        	
        	
        	// Load blocks and items IDs
        	this.redstoneJukeboxIdleID 		= config.getBlock("redstoneJukeboxIdleID", 520).getInt(520);
        	this.redstoneJukeboxActiveID 	= config.getBlock("redstoneJukeboxActiveID", 521).getInt(521);
        	this.blankRecordItemID 			= config.getItem(config.CATEGORY_ITEM, "blankRecordItemID", 7200).getInt(7200);
        	this.customRecordItemID 		= config.getItem(config.CATEGORY_ITEM, "customRecordItemID", 7201).getInt(7201);

        	// Merchant config
        	this.customRecordOffersMin		= config.get(customRecordCategory, "customRecordOffersMin", 2).getInt(2);
        	this.customRecordOffersMax		= config.get(customRecordCategory, "customRecordOffersMax", 4).getInt(4);
        	this.customRecordPriceMin		= config.get(customRecordCategory, "customRecordPriceMin", 5).getInt(5);
        	this.customRecordPriceMax		= config.get(customRecordCategory, "customRecordPriceMax", 9).getInt(9);
        	
        	// Extra validation on the merchant config (min and max values)
        	if (this.customRecordOffersMin < 1) this.customRecordOffersMin = 1;
        	if (this.customRecordOffersMax < this.customRecordOffersMin) this.customRecordOffersMax = this.customRecordOffersMin;
        	if (this.customRecordOffersMax > this.maxOffers) this.customRecordOffersMax = this.maxOffers;
        	if (this.customRecordOffersMin < 1) this.customRecordPriceMin = 1;
        	if (this.customRecordPriceMax < this.customRecordPriceMin) this.customRecordPriceMax = this.customRecordPriceMin;
        	
        			

        	// Load the custom records
        	CustomRecordHelper.LoadCustomRecordsConfig(config, customRecordCategory);
        	

        	// Custom records config help
        	String br 				= System.getProperty("line.separator");
        	String helpComment		= "";

        	helpComment				+= "How to add a custom record config" + br;
        	helpComment				+= "------------------------------------------------------------------" + br;
        	helpComment				+= "For each custom record, add a line below like this:" + br;
        	helpComment				+= br;
        	helpComment				+= "S:record###=ICON_ID;SONG_FILE;SONG_NAME" + br;
        	helpComment				+= "    ###       = A number between 000 and 0" + (maxCustomRecords - 1) + ". Do not repeat numbers. The numbers don't need to be in order." + br;
        	helpComment				+= "    ICON_ID   = The icon of the this record. Must be a number between 1 and " + maxCustomRecordIcon +  "." + br;
        	helpComment				+= "    SONG_FILE = The name of the song file that should be on the 'mods/jukebox' folder. Only OGG files are accepted." + br;
        	helpComment				+= "    SONG_NAME = The title of the song. Plain text, avoid using unicode characters. Max of 64 characters.";
        	helpComment				+= br;
        	helpComment				+= "Extra notes:" + br;
        	helpComment				+= "  - if the game can't find the song file, the record won't be added;" + br;
        	helpComment				+= "  - if the config line is incorrect, the record won't be added;" + br;
        	
        	config.addCustomCategoryComment(customRecordCategory, helpComment);

			//--DEBUG--// 
			/*
			System.out.println("Loading RedstoneJukebox config:");
			System.out.println("	Record List size: " + CustomRecordHelper.getRecordList().size());
			*/

        } 
        catch (Exception e) 
        {
        	FMLCommonHandler.instance().getFMLLogger().log(Level.SEVERE, "Error loading the configuration of the Redstone Jukebox Mod. Error message: " + e.getMessage() + " / " + e.toString());
        } 
        finally 
        {
        	// saving the configuration to its file
        	config.save();
        }
        
        
        
        // Register my custom sound handler
		SoundEventHandler soundEventHandler = new SoundEventHandler();
		MinecraftForge.EVENT_BUS.register(soundEventHandler);
		
		// Register my custom player event handler
		PlayerEventHandler playerEventHandler = new PlayerEventHandler();
		MinecraftForge.EVENT_BUS.register(playerEventHandler);
		
		// resets the sound source reference
		ModRedstoneJukebox.lastSoundSource = Vec3.createVectorHelper((double)0, (double)-1, (double)0);

	
	
		// Blocks and Items
		recordBlank = new ItemBlankRecord(ModRedstoneJukebox.blankRecordItemID, CreativeTabs.tabMisc, blankRecordIcon).func_111206_d("record_blank");
		customRecord = new ItemCustomRecord(ModRedstoneJukebox.customRecordItemID, "customRecord").func_111206_d("record_custom");
		redstoneJukebox = new BlockRedstoneJukebox(ModRedstoneJukebox.redstoneJukeboxIdleID, false).setHardness(2.0F).setResistance(10.0F).setUnlocalizedName("sidbenRedstoneJukebox").setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabRedstone).func_111022_d("redstone_jukebox_off");
		redstoneJukeboxActive = new BlockRedstoneJukebox(ModRedstoneJukebox.redstoneJukeboxActiveID, true).setHardness(2.0F).setResistance(10.0F).setUnlocalizedName("sidbenRedstoneJukebox").setStepSound(Block.soundStoneFootstep).setLightValue(0.75F).func_111022_d("redstone_jukebox_on");

		
		
		// Blocks
		GameRegistry.registerBlock(redstoneJukebox, "sidbenRedstoneJukebox");


		// Tile Entities
		GameRegistry.registerTileEntity(TileEntityRedstoneJukebox.class, "RedstoneJukeboxPlaylist");
		
		
		// GUIs
		NetworkRegistry.instance().registerGuiHandler(this, this.proxy);

		
		// Names
		LanguageRegistry.addName(recordBlank, "Blank Record");
		LanguageRegistry.addName(customRecord, "Music Disc");
		LanguageRegistry.addName(redstoneJukebox, "Redstone Jukebox");
		
		
		
		proxy.registerRenderers();

		
	}
	
	
	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {


		// Crafting Recipes
		ItemStack recordStack0 = new ItemStack(recordBlank, 1);
		ItemStack recordStack1 = new ItemStack(Item.record11);
		ItemStack recordStack2 = new ItemStack(Item.record13);
		ItemStack recordStack3 = new ItemStack(Item.recordCat);
		ItemStack recordStack4 = new ItemStack(Item.recordBlocks);
		ItemStack recordStack5 = new ItemStack(Item.recordChirp);
		ItemStack recordStack6 = new ItemStack(Item.recordFar);
		ItemStack recordStack7 = new ItemStack(Item.recordMall);
		ItemStack recordStack8 = new ItemStack(Item.recordMellohi);
		ItemStack recordStack9 = new ItemStack(Item.recordStal);
		ItemStack recordStack10 = new ItemStack(Item.recordStrad);
		ItemStack recordStack11 = new ItemStack(Item.recordWard);
		ItemStack recordStack12 = new ItemStack(Item.recordWait);

		ItemStack flintStack = new ItemStack(Item.flint);
		ItemStack redstoneStack = new ItemStack(Item.redstone);
		ItemStack redstoneTorchStack = new ItemStack(Block.torchRedstoneActive);
		ItemStack glassStack = new ItemStack(Block.glass);
		ItemStack woodStack = new ItemStack(Block.planks);
		ItemStack jukeboxStack = new ItemStack(Block.jukebox);
				
		
		// Recipe: Blank record
		GameRegistry.addShapelessRecipe(recordStack0, recordStack1, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack2, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack3, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack4, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack5, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack6, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack7, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack8, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack9, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack10, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack11, flintStack, redstoneStack);
		GameRegistry.addShapelessRecipe(recordStack0, recordStack12, flintStack, redstoneStack);
		// Load all custom records metadata as possible recipes. Maybe I can upgrade this to only consider metadata in use.
		for (int varCont = 0; varCont <= maxCustomRecordIcon; ++varCont)
		{
			GameRegistry.addShapelessRecipe(recordStack0, new ItemStack(customRecord, 1, varCont), flintStack, redstoneStack);
	    }

		// Recipe: Redstone Jukebox
		GameRegistry.addRecipe(new ItemStack(redstoneJukebox), "ggg", "tjt", "www", 'g', glassStack, 't', redstoneTorchStack, 'j', jukeboxStack, 'w', woodStack);

	}
	
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		// Custom Trades
		CustomRecordHelper.InitializeAllStores();		

	}

	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		// register custom commands
		event.registerServerCommand(new CommandPlayRecord());
		event.registerServerCommand(new CommandPlayRecordAt());
		event.registerServerCommand(new CommandPlayBgMusic());
	}

	
}