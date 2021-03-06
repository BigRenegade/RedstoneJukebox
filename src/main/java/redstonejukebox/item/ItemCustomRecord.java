package redstonejukebox.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import redstonejukebox.proxy.ProxyClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemCustomRecord extends ItemRecord
{

    public static String NBT_RECORD_INFO_ID = "RecordId";



    // --------------------------------------------------------------------
    // Constructors
    // --------------------------------------------------------------------
    public ItemCustomRecord() {
        super("custom_record", null);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("custom_record");
        this.setRegistryName("custom_record");
        this.setCreativeTab(CreativeTabs.MISC);

        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }



    // --------------------------------------------------------------------
    // Textures and Rendering
    // --------------------------------------------------------------------




    // ----------------------------------------------------
    // Item name and flavor text
    // ----------------------------------------------------

    /**
     * Allows items to add custom lines of information to the mouse-over description
     */
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add("NOT IMPLEMENTED");
    }



    // ----------------------------------------------------
    // Custom record info
    // ----------------------------------------------------

    public int getRecordInfoId(ItemStack stack)
    {
        int recordInfoId = -1;
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        
        if (nbttagcompound != null && nbttagcompound.hasKey(NBT_RECORD_INFO_ID)) {
            recordInfoId = nbttagcompound.getInteger(NBT_RECORD_INFO_ID);
        }
        return recordInfoId;
    }

}
