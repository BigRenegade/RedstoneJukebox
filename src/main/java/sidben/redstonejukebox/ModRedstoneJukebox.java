package sidben.redstonejukebox;

import net.minecraftforge.common.MinecraftForge;
import sidben.redstonejukebox.reference.Reference;
import sidben.redstonejukebox.proxy.CommonProxy;
import sidben.redstonejukebox.proxy.IProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;


@Mod(modid = Reference.ModID, name = Reference.ModName, version = Reference.ModVersion)
public class ModRedstoneJukebox
{

    
    // The instance of your mod that Forge uses.
    @Mod.Instance(Reference.ModID)
    public static ModRedstoneJukebox   instance;

    
    @SidedProxy(clientSide = Reference.ClientProxyClass, serverSide = Reference.ServerProxyClass)
    public static IProxy      proxy;


    // Used to send information between client / server
    public static SimpleNetworkWrapper NetworkWrapper;

    
    // Global variables
    public final static int          maxExtraVolume       = 128;        // Maximum amount of extra range for the custom jukebox

    
    
    
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
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
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Sided post-initialization
        proxy.post_initialize();
    }

    
    
}
