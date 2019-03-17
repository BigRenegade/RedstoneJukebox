package redstonejukebox.handler;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.village.MerchantRecipeList;
import redstonejukebox.ModRedstoneJukebox;
import redstonejukebox.main.Features;
import redstonejukebox.main.ModConfig;
import redstonejukebox.network.NetworkManager;
import redstonejukebox.util.LogHelper;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class PlayerEventHandler
{


    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteract event)
    {

        /*
         * OBS: This method is called whenever a player interacts with something (right-click)
         */

    	// 1.9 OBS - this event is called twice, for main and off hand. Trying to open the GUI
    	// twice don't work, so to make it easy I only check the main hand.
    	
        // check if the player right-clicked a villager
        if (event.getTarget() instanceof EntityVillager && event.getHand().equals(EnumHand.MAIN_HAND)) {

            // if the player is holding a blank record, cancels the regular trade...
            final ItemStack item = event.getEntityPlayer().inventory.getCurrentItem();
            if (item != null && item.getItem() == Features.Items.BLANK_RECORD) {
                // ...and opens a custom trade screen
                event.setCanceled(true);

                if (!event.getTarget().world.isRemote) {
                    // Check if the villager have valid trades
                    MerchantRecipeList tradesList = null;
                    try {
                        tradesList = ModRedstoneJukebox.instance.getRecordStoreHelper().getStore(event.getTarget().getEntityId());
                    } catch (final Throwable ex) {
                        LogHelper.error("Error loading the custom trades lists for villager ID " + event.getTarget().getEntityId());
                        LogHelper.error(ex);
                    }
                    if (tradesList == null) {
                        tradesList = new MerchantRecipeList();
                    }


                    // --- Debug ---
                    if (ModConfig.debugNetworkRecordTrading) {
                        LogHelper.info("PlayerEventHandler.onEntityInteractEvent()");
                        LogHelper.info("    Villager ID: " + event.getTarget().getEntityId());
                        LogHelper.info("    Custom record trades: " + tradesList.size());
                    }



                    if (tradesList.size() > 0) {
                        // Sends the shop to the player
                        NetworkManager.sendRecordTradingFullListMessage(tradesList, event.getEntityPlayer());
                        
                        // Have trades, opens the GUI
                        ((EntityVillager) event.getTarget()).setCustomer(event.getEntityPlayer());
                        event.getEntityPlayer().openGui(ModRedstoneJukebox.instance, ModConfig.RECORD_TRADING_GUI_ID, event.getTarget().world, event.getTarget().getEntityId(), 0, 0);

                    } else {
                        // Don't have trades, play a sound
                        event.getTarget().playSound(SoundEvents.ENTITY_VILLAGER_NO, 1F, 1F);

                    }

                }


            } // if (item != null && item.getItem() == Features.Items.recordBlank)


        } // if (event.target instanceof EntityVillager)

    }
}
