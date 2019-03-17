package redstonejukebox.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import redstonejukebox.ModRedstoneJukebox;
import redstonejukebox.client.gui.GuiRecordTrading;
import redstonejukebox.client.gui.GuiRedstoneJukebox;
import redstonejukebox.handler.SoundEventHandler;
import redstonejukebox.main.Features;
import redstonejukebox.main.ModConfig;
import redstonejukebox.main.Reference;
import redstonejukebox.tileentity.TileEntityRedstoneJukebox;
import redstonejukebox.util.LogHelper;
import redstonejukebox.util.MusicHelper;



public class ProxyClient extends ProxyCommon
{



    @Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().world;
    }



    @Override
    public void pre_initialize()
    {
        super.pre_initialize();

        Features.registerItemModels();
        Features.registerBlockModels();
    }



    @Override
    public void initialize()
    {
        super.initialize();

        MinecraftForge.EVENT_BUS.register(new SoundEventHandler());

        ModRedstoneJukebox.instance.setMusicHelper(new MusicHelper(Minecraft.getMinecraft()));
    }



    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        LogHelper.debug("Proxy.getClientGuiElement(%d, player, world, %d, %d, %d)", guiID, x, y, z);

        if (guiID == ModConfig.REDSTONE_JUKEBOX_GUI_ID) {
            final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityRedstoneJukebox) { return new GuiRedstoneJukebox(player.inventory, (TileEntityRedstoneJukebox) te); }
        }

        else if (guiID == ModConfig.RECORD_TRADING_GUI_ID) {
            // OBS: The X value can be used to store the EntityID - facepalm courtesy of http://www.minecraftforge.net/forum/index.php?topic=1671.0
            final Entity villager = world.getEntityByID(x);
            if (villager instanceof EntityVillager) { return new GuiRecordTrading(player.inventory, (EntityVillager) villager, world); }
        }

        return null;
    }



    public static String getResourceName(String name)		// TODO: find a better place for this
    {
        return Reference.ResourcesNamespace + ":" + name;
    }

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		
	}

	@Override
	public void init(FMLInitializationEvent event) {
		
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		
	}

	@Override
	public void serverStarting(FMLServerStartingEvent event) {}

	@Override
	public EntityPlayer getPlayerFromContext(MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft().player : ModRedstoneJukebox.proxy.getPlayerFromContext(ctx));
	}

	@Override
	public WorldClient getWorldFromContext(MessageContext ctx) {
		return (WorldClient) (ctx.side.isClient() ? Minecraft.getMinecraft().world : ModRedstoneJukebox.proxy.getWorldFromContext(ctx));
	}

	@Override
	public void addRunnableFromContext(MessageContext ctx, Runnable task) {
		if(ctx.side.isClient()) Minecraft.getMinecraft().addScheduledTask(task);
		else ModRedstoneJukebox.proxy.addRunnableFromContext(ctx, task);
	}

}
