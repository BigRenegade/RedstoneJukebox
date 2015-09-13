package sidben.redstonejukebox.network;

import sidben.redstonejukebox.ModRedstoneJukebox;
import sidben.redstonejukebox.init.MyItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;


/**
 * Represents the full list of record trades for a specific villager.
 *
 */
public class RecordTradingFullListMessage implements IMessage
{

    // ---------------------------------------------
    // Fields
    // ---------------------------------------------
    private static final byte TYPE_SELLING = 1;
    private static final byte TYPE_BUYING = 2;

    private MerchantRecipeList tradeList;


    
    // ---------------------------------------------
    // Methods
    // ---------------------------------------------

    public RecordTradingFullListMessage() {}

    public RecordTradingFullListMessage(MerchantRecipeList list) {
        this.tradeList = list;
    }
    
    
    
    // Reads the packet
    @SuppressWarnings("unchecked")
    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.tradeList = new MerchantRecipeList();
        int listSize = buf.readShort();
        
        // TODO: check what happens if the ByteBuf can't read anymore
        
        // Loop to parse all trades
        for (short i=0; i < listSize; i++) {
            byte tradeType = buf.readByte();                // Trade type
            short recordIndex = buf.readShort();            // Record the villager is buying / selling
            short emeraldPrice = buf.readShort();           // Price in emeralds
            
            if (recordIndex > -1 && emeraldPrice > 0) {
                MerchantRecipe recipe = null;
                ItemStack emptyDisc = new ItemStack(MyItems.recordBlank, 1);
                ItemStack musicDisc = new ItemStack(ModRedstoneJukebox.instance.getGenericHelper().getRecordFromCollection(recordIndex), 1);
                ItemStack emeralds = new ItemStack(Items.emerald, emeraldPrice);

                // Create the trade
                if (tradeType == TYPE_BUYING) {
                    recipe = new MerchantRecipe(musicDisc, emeralds);
                }
                else if (tradeType == TYPE_SELLING) {
                    recipe = new MerchantRecipe(emptyDisc, emeralds, musicDisc);
                }
                
                // Adds to the list
                this.tradeList.add(recipe);
            }
        }
    }

    // Write the packet
    @Override
    public void toBytes(ByteBuf buf)
    {
        // TODO: add trade uses
        buf.writeShort(this.tradeList.size());
        
        MerchantRecipe recipe;
        for (Object obj : this.tradeList) {
            recipe = (MerchantRecipe) obj;
            
            ItemStack slotBuy1 = recipe.getItemToBuy();
            ItemStack slotBuy2 = recipe.getSecondItemToBuy();
            ItemStack slotSell = recipe.getItemToSell();

            // Checks the recipe type
            if (slotSell.getItem() == Items.emerald) {
                int recordIndex = ModRedstoneJukebox.instance.getGenericHelper().getVanillaRecordIndex(slotBuy1);
                if (recordIndex > -1) {
                    // Villager is buying records
                    buf.writeByte(TYPE_BUYING);             // Trade type
                    buf.writeShort(recordIndex);            // Record the villager is buying
                    buf.writeShort(slotSell.stackSize);     // Price in emeralds
                }                

            } else {
                int recordIndex = ModRedstoneJukebox.instance.getGenericHelper().getVanillaRecordIndex(slotSell);
                if (recordIndex > -1) {
                    // Villager is selling records
                    buf.writeByte(TYPE_SELLING);            // Trade type
                    buf.writeShort(recordIndex);            // Record the villager is selling
                    buf.writeShort(slotBuy2.stackSize);     // Price in emeralds
                }
                
            }

        }
    }
    
    
    
    
    public void updateClientSideRecordStore()
    {
        ModRedstoneJukebox.instance.getRecordStoreHelper().clientSideCurrentStore = this.tradeList;
    }

    
}
