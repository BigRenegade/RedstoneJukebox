package sidben.redstonejukebox;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sidben.redstonejukebox.handler.ConfigurationHandler;
import sidben.redstonejukebox.helper.MusicHelper;
import sidben.redstonejukebox.helper.RecordInfoManager;
import sidben.redstonejukebox.helper.RecordStoreHelper;
import sidben.redstonejukebox.init.MyCommands;
import sidben.redstonejukebox.proxy.IProxy;
import sidben.redstonejukebox.reference.Reference;


@Mod(modid = Reference.ModID, name = Reference.ModName, version = Reference.ModVersion, guiFactory = Reference.GuiFactoryClass)
public class ModRedstoneJukebox
{


    // The instance of your mod that Forge uses.
    @Mod.Instance(Reference.ModID)
    public static ModRedstoneJukebox   instance;


    @SidedProxy(clientSide = Reference.ClientProxyClass, serverSide = Reference.ServerProxyClass)
    public static IProxy               proxy;


    // Used to send information between client / server
    public static SimpleNetworkWrapper NetworkWrapper;


    // Global variables
    public final static int            maxExtraVolume       = 128;        // Maximum amount of extra range for the custom jukebox


    // Helper classes
    private RecordInfoManager          recordInfoManager;
    private RecordStoreHelper          recordStoreHelper;
    @SideOnly(Side.CLIENT)
    private MusicHelper                musicHelper;



    // GUI IDs
    public static int                  redstoneJukeboxGuiID = 0;
    public static int                  recordTradingGuiID   = 1;

    // Models IDs
    public static int                  redstoneJukeboxModelID;



    // TODO: Custom records


    /**
     * Returns a singleton instance of the record info class.
     */
    public RecordInfoManager getRecordInfoManager()
    {
        return recordInfoManager;
    }

    /**
     * Returns a singleton instance of the music helper class.
     */
    @SideOnly(Side.CLIENT)
    public MusicHelper getMusicHelper()
    {
        return musicHelper;
    }

    @SideOnly(Side.CLIENT)
    public void setMusicHelper(MusicHelper helper)
    {
        musicHelper = helper;
    }

    /**
     * Returns a singleton instance of the record store helper class.
     */
    public RecordStoreHelper getRecordStoreHelper()
    {
        return recordStoreHelper;
    }



    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        // Loads config
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

        // Sided pre-initialization
        proxy.pre_initialize();
    }


    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        // GUIs
        NetworkRegistry.INSTANCE.registerGuiHandler(this, ModRedstoneJukebox.proxy);        // REMINDER: do not put this on the proxy class you dummy

        // Sided initializations
        proxy.initialize();

        // Helper classes single instances
        recordInfoManager = new RecordInfoManager();
        recordStoreHelper = new RecordStoreHelper();
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Sided post-initialization
        proxy.post_initialize();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Custom commands
        MyCommands.register(event);
    }

}
