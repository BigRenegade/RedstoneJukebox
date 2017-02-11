package sidben.redstonejukebox.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sidben.redstonejukebox.ModRedstoneJukebox;
import sidben.redstonejukebox.block.BlockRedstoneJukebox;
import sidben.redstonejukebox.handler.EventHandlerConfig;
import sidben.redstonejukebox.inventory.ContainerRedstoneJukebox;
import sidben.redstonejukebox.main.ModConfig;
import sidben.redstonejukebox.network.NetworkManager;
import sidben.redstonejukebox.proxy.ProxyClient;
import sidben.redstonejukebox.util.LogHelper;
import sidben.redstonejukebox.util.RecordInfo;



public class TileEntityRedstoneJukebox extends TileEntityLockable implements IInventory
{


    /*
     * LAG TEST
     * -------------------------------------------------------------------------------
     * When the game lags, the TileEntity counts slower, but it doesn't skip ticks.
     * That means the song will end but the counter keeps counting for several more seconds.
     * 
     * Maybe I should use the system clock to count, but I have to remember that when the
     * game is paused, the music stops and so should the counter.
     * 
     * The music itself doesn't lag at all, so system clock may be viable.
     */


    // --------------------------------------------------------------------
    // Constants and Variables
    // --------------------------------------------------------------------

    /** Items of this jukebox */
    private ItemStack[] jukeboxItems              = new ItemStack[8];

    /** Play mode: 0 = Simple (in order) / 1 = Shuffle */
    public short        paramPlayMode             = 0;

    /** Indicates if it should loop when reach the end of a playlist */
    public boolean      paramLoop                 = false;

    /** Array with the order in which the records will play (playlist). used for the shuffle option. */
    private byte[]      playOrder                 = new byte[8];


    /** Indicates if the block is being powered */
    private boolean     isBlockPowered            = false;

    /** Indicates if this jukebox started to play a playlist */
    private boolean     isPlaylistStarted         = false;

    /** Used to detect when the jukebox finished playing all records */
    private boolean     isPlaylistFinished        = false;

    /** Slot currently playing. This refers to the [playOrder] array, not the GUI inventory, so slot 0 is the first slot of the playOrder, not the jukebox */
    private int         currentIndex              = -1;

    /** Slot of the jukebox with the current playing record. */
    private byte        currentJukeboxPlaySlot    = -1;

    /** Amount of seconds that will be added to the song timer before playing the next record. Should help compensate latency on multiplayer */
    private static int  songInterval              = 0;

    /** Timer of the song being played */
    public int          songTimer                 = 0;


    /*
     * Flags to call some method without doing it recursively, I believe this will perform better.
     * 
     * Instead of making the 3 main Play Control methods call each other, I use this variables
     * to schedule the calls. Every method call is now performed by the tickJukebox loop, when needed.
     */
    private boolean     schedulePlayNextRecord    = false;
    private boolean     scheduleStartPlaying      = false;
    private boolean     scheduleStopPlaying       = false;


    private String      customName;


    private int         _jukeboxExtraVolumeCached = -1;


    // TODO: When placing the jukebox in a powered block, activate it (? maybe invalid, since the new Jukebox will be empty. Try using a Shift-Middle click NBT one)



    // --------------------------------------------------------------------
    // Inventory
    // --------------------------------------------------------------------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return this.jukeboxItems.length;
    }


    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.jukeboxItems[slot];
    }


    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        if (this.jukeboxItems[slot] != null) {
            if (this.jukeboxItems[slot].getCount() <= amount) {
                final ItemStack itemstack = this.jukeboxItems[slot];
                this.jukeboxItems[slot] = null;
                return itemstack;
            }

            final ItemStack itemstack1 = this.jukeboxItems[slot].splitStack(amount);

            if (this.jukeboxItems[slot].getCount() == 0) {
                this.jukeboxItems[slot] = null;
            }

            return itemstack1;
        } else {
            return null;
        }
    }
    
    
    
    /**
     * Removes a stack from the given slot and returns it.
     */
	@Override
	public ItemStack removeStackFromSlot(int slot) {
        if (this.jukeboxItems[slot] != null) {
            final ItemStack itemstack = this.jukeboxItems[slot];
            this.jukeboxItems[slot] = null;
            return itemstack;
        } else {
            return null;
        }
    }



    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.jukeboxItems[slot] = stack;

        if (stack != null && stack.getCount() > this.getInventoryStackLimit()) {
            // TODO: update stack.getCount() = this.getInventoryStackLimit();
        }
    }


    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.redstoneJukebox";
    }


    public void setInventoryName(String name)
    {
        this.customName = name;
    }


    @Override
    public boolean hasCustomName()
    {
        return this.customName != null && this.customName.length() > 0;
    }


    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
     * this more of a set than a get?*
     */
    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }


    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        }

        // Check if the player is too far
        return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }


    @Override
    public void openInventory(EntityPlayer player)
    {
    }


    @Override
    public void closeInventory(EntityPlayer player)
    {
    }


    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return ModRedstoneJukebox.instance.getRecordInfoManager().isRecord(stack);
    }



    // --------------------------------------------------------------------
    // NBT and network stuff
    // --------------------------------------------------------------------

    /**
     * Marks the block and the chuck as "dirty", forcing an update between Server/Client.
     */
    public void resync()		// TODO: check if this is needed, it think it only server to update rendering
    {
    	// this.world.notifyBlockUpdate(pos, state, state, 3);		// Disabled by now
        this.markDirty();
    }


    /**
     * Reads a tile entity from NBT.
     * 
     * OBS: This is the only info that is saved with the world.
     */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        final NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
        this.jukeboxItems = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            final NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            final byte byte0 = nbttagcompound.getByte("Slot");

            if (byte0 >= 0 && byte0 < this.jukeboxItems.length) {
                // TODO: update this.jukeboxItems[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }


        this.paramPlayMode = par1NBTTagCompound.getShort("PlayMode");
        this.paramLoop = par1NBTTagCompound.getBoolean("Loop");
        this.isBlockPowered = par1NBTTagCompound.getBoolean("Powered");
        this.isPlaylistStarted = par1NBTTagCompound.getBoolean("PlayStarted");
        this.isPlaylistFinished = par1NBTTagCompound.getBoolean("PlayFinished");
        this.playOrder = par1NBTTagCompound.getByteArray("PlaylistOrder");
        this.currentIndex = par1NBTTagCompound.getInteger("PlaylistIndex");

        if (this.playOrder == null || this.playOrder.length != 8) {
            this.playOrder = new byte[8];
        }
        if (this.currentIndex >= 0) {
            this.currentIndex--;        // Removes 1 from the index because on world load, the "PlayNextRecord" method will be triggered and that will add 1 to the index
        }

        if (par1NBTTagCompound.hasKey("CustomName", 8))     // OBS: Custom name is useless, since it don't appear on the GUI... But it's coded!
        {
            this.customName = par1NBTTagCompound.getString("CustomName");
        }
    }


    /**
     * Writes a tile entity to NBT.
     * 
     * OBS: This is the only info that will be saved with the world.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);

        par1NBTTagCompound.setShort("PlayMode", this.paramPlayMode);
        par1NBTTagCompound.setBoolean("Loop", this.paramLoop);
        par1NBTTagCompound.setBoolean("Powered", this.isBlockPowered);
        par1NBTTagCompound.setBoolean("PlayStarted", this.isPlaylistStarted);
        par1NBTTagCompound.setBoolean("PlayFinished", this.isPlaylistFinished);
        par1NBTTagCompound.setByteArray("PlaylistOrder", this.playOrder);
        par1NBTTagCompound.setInteger("PlaylistIndex", this.currentIndex);
        final NBTTagList nbttaglist = new NBTTagList();


        for (int i = 0; i < this.jukeboxItems.length; i++) {
            if (this.jukeboxItems[i] != null) {
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.jukeboxItems[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        par1NBTTagCompound.setTag("Items", nbttaglist);

        if (this.hasCustomName()) {
            par1NBTTagCompound.setString("CustomName", this.customName);
        }
        
        return par1NBTTagCompound; 
    }


    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. Called on client only.
     * 
     * @param net
     *            The NetworkManager the packet originated from
     * @param packet
     *            The data packet
     */
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, SPacketUpdateTileEntity packet)
    {
        // Read NBT packet from the server
        final NBTTagCompound tag = packet.getNbtCompound();

        // Main parameters
        this.paramPlayMode = tag.getShort("PlayMode");
        this.paramLoop = tag.getBoolean("Loop");
        this.currentJukeboxPlaySlot = tag.getByte("InvSlot");

        // --- Debug ---
        if (ModConfig.debugNetworkJukebox) {
            LogHelper.info("TileEntityRedstoneJukebox.onDataPacket()");
            LogHelper.info("    PlayMode: " + this.paramPlayMode);
            LogHelper.info("    Loop:     " + this.paramLoop);
            LogHelper.info("    Slot:     " + this.currentJukeboxPlaySlot);
            LogHelper.info("    Index:    " + this.currentIndex);
            LogHelper.info("    Coords:   " + this.pos);
        }
    }


    /**
     * Gathers data into a packet that is to be sent to the client. Called on server only.
     */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        // --- Debug ---
        if (ModConfig.debugNetworkJukebox) {
            LogHelper.info("TileEntityRedstoneJukebox.getDescriptionPacket()");
            LogHelper.info("    PlayMode: " + this.paramPlayMode);
            LogHelper.info("    Loop:     " + this.paramLoop);
            LogHelper.info("    Slot:     " + this.currentJukeboxPlaySlot);
            LogHelper.info("    Index:    " + this.currentIndex);
            LogHelper.info("    Coords:   " + this.pos);
        }

        // Send the NBT Packet to client
        final NBTTagCompound tag = new NBTTagCompound();

        // Main parameters
        tag.setShort("PlayMode", this.paramPlayMode);
        tag.setBoolean("Loop", this.paramLoop);
        tag.setByte("InvSlot", this.currentJukeboxPlaySlot);

        return new SPacketUpdateTileEntity(this.pos, 0, tag);
    }



    // --------------------------------------------------------------------
    // Events
    // --------------------------------------------------------------------


    /**
     * Process one tick of jukebox-related events. Kind of a custom updateEntity implementation that
     * should run once per second (20 ticks).
     */
    public void tickJukebox()
    {
        if (!this.world.isRemote) {

            // --- Debug ---
            if (ModConfig.debugJukeboxTick) {
                LogHelper.info("Jukebox.tickJukebox() @ " + this.pos + " isPlaying: " + this.isPlaying() + " - isBlockPowered: " + this.isBlockPowered + " - schdPlayNext: "
                        + this.schedulePlayNextRecord + " - schdStart: " + this.scheduleStartPlaying + " - schdStop: " + this.scheduleStopPlaying + " - playlistFinish: " + this.isPlaylistFinished
                        + " - songTimer: " + this.songTimer + " - gameTick: " + this.world.getMinecraftServer().getTickCounter());
            }


            /*
             * Special cases where the code must execute some scheduled methods.
             */
            if (this.schedulePlayNextRecord) {
                this.playNextRecord();
            } else if (this.scheduleStartPlaying) {
                this.startPlaying();
                this.playNextRecord();
            } else if (this.scheduleStopPlaying) {
                this.stopPlaying(true);
            }


            // Only process the method when the block is powered
            if (!this.isBlockPowered) {
                return;
            }


            if (!this.isPlaylistFinished) {
                // Still playing, check song timer
                if (this.songTimer > 0) {
                    // Still counting...
                    --this.songTimer;

                    // Check the jukebox slot, skip to the next record if the current slot is empty (most likely removed by player)
                    if (this.currentJukeboxPlaySlot > -1) {
                        final ItemStack record = this.jukeboxItems[this.currentJukeboxPlaySlot];
                        if (record == null) {
                            this.schedulePlayNextRecord = true;
                        }
                    }
                } else {
                    // Time to play the next record
                    this.schedulePlayNextRecord = true;
                }

            }


        } // world.isremote


    }



    // --------------------------------------------------------------------
    // This is where the groove starts :)
    // --------------------------------------------------------------------

    /**
     * Makes the jukebox start playing a sequence of records (playlist).
     * 
     * That consists in reseting some variables and setting the playlist order.
     */
    public void startPlaying()
    {
        // --- Debug ---
        if (ModConfig.debugJukeboxRecordPlay) {
            LogHelper.info("Jukebox.startPlaying() @ (" + this.pos + ") - Empty: " + this.isEmpty());
        }

        if (this.isEmpty()) {
            return;
        }

        this.isPlaylistFinished = false;
        this.isPlaylistStarted = true;
        this.setPlaylistOrder();
        this.scheduleStartPlaying = false;
        this.schedulePlayNextRecord = true;
    }


    /**
     * Makes the jukebox stop playing the current sequence (playlist).
     */
    public void stopPlaying(boolean updateNeighbors)
    {
        this.currentIndex = -1;
        this.currentJukeboxPlaySlot = -1;
        this.songTimer = 0;
        this.scheduleStopPlaying = false;

        // --- Debug ---
        if (ModConfig.debugJukeboxRecordPlay) {
            LogHelper.info("Jukebox.stopPlaying() @ (" + this.pos + ")");
        }

        // Send update to clients
        NetworkManager.sendJukeboxPlayRecordMessage(this, -1, (byte) -1, 0);

        if (updateNeighbors) {
            // To update comparators
            //TODO: update BlockRedstoneJukebox.updateJukeboxBlockState(this.isBlockPowered, this.world, this.pos);
        }
    }


    /**
     * Makes the jukebox play the next record of the current sequence (playlist).
     */
    private void playNextRecord()
    {
        // --- Debug ---
        if (ModConfig.debugJukeboxRecordPlay) {
            LogHelper.info("Jukebox.playNextRecord() @ (" + this.pos + ") - playlist index: " + this.currentIndex + " - current slot: "
                    + this.currentJukeboxPlaySlot + " - Empty: " + this.isEmpty());
        }

        if (this.isEmpty()) {
            return;
        }

        // Advances to the next slot
        ++this.currentIndex;
        this.schedulePlayNextRecord = false;



        if (this.currentIndex >= 0 && this.currentIndex < this.getSizeInventory()) {

            // Find the next valid record
            ItemStack nextRecordStack = null;

            for (int i = 0; i < this.getSizeInventory(); i++) {
                this.currentJukeboxPlaySlot = playOrder[this.currentIndex];
                nextRecordStack = this.jukeboxItems[this.currentJukeboxPlaySlot];

                // --- Debug ---
                if (ModConfig.debugJukeboxRecordPlay) {
                    LogHelper.info("    checking item #" + this.currentIndex + " on slot " + this.currentJukeboxPlaySlot + ": " + LogHelper.itemStackToString(nextRecordStack));
                }

                if (nextRecordStack == null && this.currentIndex + 1 < this.getSizeInventory()) {
                    ++this.currentIndex;
                } else {
                    break;
                }
            }


            int recordInfoId = -1;
            RecordInfo recordInfo = null;

            // TODO: maybe the song timer should be global


            // --- Debug ---
            if (ModConfig.debugJukeboxRecordPlay) {

                if (ModRedstoneJukebox.instance.getRecordInfoManager().isRecord(nextRecordStack)) {
                    final ItemRecord debugRecord = (ItemRecord) (nextRecordStack.getItem());
                    if (debugRecord.getSound() == null) {
                        LogHelper.info("    * recordName:       NULL");
                        LogHelper.info("    * record resource:  NULL");
                    } else {
                        LogHelper.info("    * recordName:       " + debugRecord.getSound().getSoundName().getResourcePath());
                        LogHelper.info("    * record resource:  " + debugRecord.getSound().getSoundName());
                    }
                    if (this.world.isRemote) {
                        LogHelper.info("    * recordName Local: " + debugRecord.getRecordNameLocal());
                    }
                }

            }



            // Finds the item ID of the record
            if (nextRecordStack != null) {
                recordInfoId = ModRedstoneJukebox.instance.getRecordInfoManager().getRecordInfoIdFromItemStack(nextRecordStack);
                recordInfo = ModRedstoneJukebox.instance.getRecordInfoManager().getRecordInfoFromId(recordInfoId);
            }


            // Check if it has a valid item id and a valid song time
            if (recordInfo != null && recordInfo.getRecordDurationSeconds() > 0) {
                // --- Debug ---
                if (ModConfig.debugJukeboxRecordPlay) {
                    LogHelper.info("    Record info id: " + recordInfoId);
                    LogHelper.info("    Song time:      " + recordInfo.getRecordDurationSeconds() + " + " + TileEntityRedstoneJukebox.songInterval + " (jukebox interval)");
                }

                // Record found
                this.songTimer = recordInfo.getRecordDurationSeconds() + TileEntityRedstoneJukebox.songInterval;

                // Send update to clients
                NetworkManager.sendJukeboxPlayRecordMessage(this, recordInfoId, this.getCurrentJukeboxPlaySlot(), this.getExtraVolume(true));

                // To update comparators
                //TODO: update BlockRedstoneJukebox.updateJukeboxBlockState(this.isBlockPowered, this.world, this.pos);

            } else {
                // --- Debug ---
                if (ModConfig.debugJukeboxRecordPlay) {
                    LogHelper.info("    Invalid record, skipping to next slot");
                }

                // if it's not a valid record, skip to the next one
                this.schedulePlayNextRecord = true;

            }

        }


        if (this.currentIndex >= this.getSizeInventory()) {
            // Reached the end
            this.isPlaylistFinished = true;

            if (this.paramLoop) {
                // Must loop, start playing again
                this.scheduleStartPlaying = true;
            } else {
                // No loop, stops
                this.scheduleStopPlaying = true;
            }

        }

    }



    /**
     * Set the playlist order. Also, resets the index to the first position.
     * 
     */
    private void setPlaylistOrder()
    {
        /*
         * // DEBUG
         * LogHelper.info("TileEntityRedstoneJukebox.setPlaylistOrder() - Shuffle: " + (this.paramPlayMode == 1));
         */

        int totalRecords = 0;
        boolean validRecord = false;


        // resets the playlist order
        this.currentIndex = -1;
        for (int i = 0; i < this.playOrder.length; i++) {
            this.playOrder[i] = -1;
        }


        // adds the records with the regular order
        for (byte i = 0; i < this.playOrder.length; i++) {
            this.playOrder[i] = i;


            // check every slot to search for records.
            final ItemStack s = this.getStackInSlot(i);
            if (ModRedstoneJukebox.instance.getRecordInfoManager().isRecord(s)) {
                validRecord = true;

                /*
                 * // Only counts valid records, custom records with no song are ignored
                 * if (Item.itemsList[s.itemID] instanceof ItemCustomRecord) {
                 * if (((ItemCustomRecord) Item.itemsList[s.itemID]).getSongID(s).equals("")) {
                 * validRecord = false;
                 * }
                 * }
                 */

                if (validRecord) {
                    ++totalRecords;
                }
            }

        }


        // shuffle if needed
        if (this.paramPlayMode == 1 && totalRecords > 1) {
            // Swaps the play order twice
            for (int i = 0; i < this.playOrder.length; i++) {
                final int randomPosition = this.world.rand.nextInt(this.playOrder.length);
                final byte temp = this.playOrder[i];
                this.playOrder[i] = this.playOrder[randomPosition];
                this.playOrder[randomPosition] = temp;
            }
            for (int i = 0; i < this.playOrder.length; i++) {
                final int randomPosition = this.world.rand.nextInt(this.playOrder.length);
                final byte temp = this.playOrder[i];
                this.playOrder[i] = this.playOrder[randomPosition];
                this.playOrder[randomPosition] = temp;
            }
        }


        // Debug
        /*
         * String debugOrder = "";
         * for (final byte element : this.playOrder) {
         * debugOrder += "[" + element + "]";
         * }
         * LogHelper.info("    Playlist slot order: " + debugOrder + ", amount of actual records: " + totalRecords);
         */


    }



    /**
     * Checks the redstone jukebox block for the note blocks that will increase the volume range.
     * 
     */
    public int getExtraVolume(boolean mustRefresh)
    {
        if (_jukeboxExtraVolumeCached < 0 || mustRefresh) {
            _jukeboxExtraVolumeCached = BlockRedstoneJukebox.getAmplifierPower(this.world, this.pos);
        }

        return _jukeboxExtraVolumeCached;
    }



    // --------------------------------------------------------------------
    // Miscellaneous
    // --------------------------------------------------------------------

    /**
     * Called by the RedstoneJukebox block whenever it's powered status updates.
     * 
     */
    public void updateJukeboxTileState(boolean haveEnergy)
    {
        this.isBlockPowered = haveEnergy;
        if (!haveEnergy) {
            this.scheduleStartPlaying = false;
            this.schedulePlayNextRecord = false;
            this.scheduleStopPlaying = true;

            // Resets the playlist status ONLY when the block is unpowered
            this.isPlaylistStarted = false;
            this.isPlaylistFinished = false;
        } else {
            this.scheduleStartPlaying = true;
            this.schedulePlayNextRecord = false;
            this.scheduleStopPlaying = false;
        }
    }


    /**
     * Indicates if the jukebox inventory is empty.
     * 
     */
    public boolean isEmpty()
    {
        for (final ItemStack jukeboxItem : this.jukeboxItems) {
            if (jukeboxItem != null) {
                return false;
            }
        }
        return true;
    }


    /**
     * Returns if this Jukebox is playing a record now.
     * 
     */
    public boolean isPlaying()
    {
        return this.isPlaylistStarted && !this.isPlaylistFinished();
    }


    /**
     * Returns if this Jukebox reached the end of the playlist.
     * 
     */
    public boolean isPlaylistFinished()
    {
        return this.isPlaylistFinished && !this.paramLoop;
    }


    /**
     * Returns the index currently playing (of the playlist, NOT the jukebox inventory).
     * 
     */
    public int getCurrentPlaySlot()
    {
        return this.currentIndex;
    }



    /**
     * Returns the slot currently playing (of the jukebox inventory, NOT the playlist array).
     * 
     */
    public byte getCurrentJukeboxPlaySlot()
    {
        return this.currentJukeboxPlaySlot;
    }


    /**
     * Sets the slot currently playing (of the jukebox).
     * 
     */
    public void setCurrentJukeboxPlaySlot(byte slot)
    {
        this.currentJukeboxPlaySlot = slot;
    }



    /**
     * Eject all records to the world.
     * 
     */
    public void ejectAll(World world, BlockPos pos)
    {
        for (int i1 = 0; i1 < this.getSizeInventory(); ++i1) {
            final ItemStack item = this.getStackInSlot(i1);

            if (item != null) {
                final float f1 = this.world.rand.nextFloat() * 0.8F + 0.1F;
                final float f2 = this.world.rand.nextFloat() * 0.8F + 0.1F;
                final float f3 = this.world.rand.nextFloat() * 0.8F + 0.1F;

                while (item.getCount() > 0) {
                    int j1 = this.world.rand.nextInt(21) + 10;

                    if (j1 > item.getCount()) {
                        j1 = item.getCount();
                    }

                    // TODO: update item.getCount() -= j1;
                    final EntityItem entityitem = new EntityItem(world, pos.getX() + f1, pos.getY() + f2, pos.getZ() + f3, new ItemStack(item.getItem(), j1, item.getItemDamage()));

                    if (item.hasTagCompound()) {
                        entityitem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                    }

                    final float f4 = 0.05F;
                    entityitem.motionX = (float) this.world.rand.nextGaussian() * f4;
                    entityitem.motionY = (float) this.world.rand.nextGaussian() * f4 + 0.2F;
                    entityitem.motionZ = (float) this.world.rand.nextGaussian() * f4;
                    world.spawnEntity(entityitem);
                }
            }
        }

    }


    
    
    
	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerRedstoneJukebox(playerInventory, this);
	}


	// TODO: put the code below in better position
	
	@Override
	public String getGuiID() {
		return ProxyClient.getResourceName("redstone_jukebox");
	}


	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
	}


	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void clear() {
        for (int i = 0; i < this.jukeboxItems.length; ++i)
        {
            this.jukeboxItems[i] = null;
        }
	}




}
