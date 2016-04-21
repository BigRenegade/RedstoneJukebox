package sidben.redstonejukebox.helper;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import sidben.redstonejukebox.handler.ConfigurationHandler;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.Loader;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;


/**
 * Class to provide info about records, from vanilla and mods.
 * 
 */
public class RecordInfoManager
{

    private final Map<Integer, RecordInfo> recordsInfoCollection;
    private String[] recordNamesList;
    private int[] recordsInfoIdRandomCandidates;
    

    /*
     * TODO: Find the music length by reading the OGG files (will revisit when adding custom records)
     * 
     * http://www.jsresources.org/faq_audio.html#file_length)
     * http://fossies.org/linux/www/webCDwriter-2.8.2.tar.gz/webCDwriter-2.8.2/webCDcreator/Ogg.java?m=t
     * 
     * minecraft:sounds/records/wait.ogg
     * mcsounddomain:minecraft:sounds/records/wait.ogg
     * 
     * OBS: getURLForSoundResource
     */
    
    
    
    public RecordInfoManager() {
        this.recordsInfoCollection = Maps.newHashMap();
        int idCount = 0;
        int countVanillaRecords = 0;
        int countCustomRecords = 0;
        
        
        // Adds the vanilla records
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.13", 178, "item.record.13.desc", Item.getIdFromItem(Items.record_13), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.cat", 184, "item.record.cat.desc", Item.getIdFromItem(Items.record_cat), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.blocks", 345, "item.record.blocks.desc", Item.getIdFromItem(Items.record_blocks), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.chirp", 185, "item.record.chirp.desc", Item.getIdFromItem(Items.record_chirp), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.far", 174, "item.record.far.desc", Item.getIdFromItem(Items.record_far), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.mall", 197, "item.record.mall.desc", Item.getIdFromItem(Items.record_mall), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.mellohi", 96, "item.record.mellohi.desc", Item.getIdFromItem(Items.record_mellohi), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.stal", 150, "item.record.stal.desc", Item.getIdFromItem(Items.record_stal), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.strad", 188, "item.record.strad.desc", Item.getIdFromItem(Items.record_strad), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.ward", 251, "item.record.ward.desc", Item.getIdFromItem(Items.record_ward), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.11", 71, "item.record.11.desc", Item.getIdFromItem(Items.record_11), 0));
        recordsInfoCollection.put(idCount++, new RecordInfo("minecraft:records.wait", 237, "item.record.wait.desc", Item.getIdFromItem(Items.record_wait), 0));
        
        countVanillaRecords = idCount;
        

        // Adds other mods records (TODO: test mods)
        if (Loader.isModLoaded("portalgun")) {
            recordsInfoCollection.put(idCount++, new RecordInfo("portalgun:records.radioloop", 21, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("portalgun:records.stillalive", 176, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("portalgun:records.wantyougone", 141, ""));
        }

        if (Loader.isModLoaded("biomesoplenty")) {
            recordsInfoCollection.put(idCount++, new RecordInfo("biomesoplenty:records.wanderer", 289, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("biomesoplenty:records.corruption", 183, ""));
        }

        if (Loader.isModLoaded("vocaloidmod")) {
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:LoveIsWar", 234, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:Melt", 257, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:OnlineGameAddictsSprechchor", 287, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:RollingGirl", 188, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:RomeoAndCinderella", 275, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:SPiCa", 213, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:TellYourWorld", 252, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:TwoFacedLovers", 182, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:WeekenderGirl", 209, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:WorldIsMine", 251, ""));
            recordsInfoCollection.put(idCount++, new RecordInfo("vocaloidmod:Yellow", 196, ""));
        }

        
        
        // record names
        String recordNames = "";
        for (final Entry<Integer, RecordInfo> entry : this.recordsInfoCollection.entrySet()) {
            recordNames += ";" + entry.getValue().recordUrl;
        }
        recordNames = recordNames.substring(1);
        
        recordNamesList = recordNames.split(";");
        
        
        // records that are valid for record trading
        recordsInfoIdRandomCandidates = new int[countVanillaRecords + countCustomRecords];
        for (int i = 0; i < countVanillaRecords; i++) {
            recordsInfoIdRandomCandidates[i] = i;
        }
    }
    
    



    /*
     * ======================================================================================
     * 
     * Records Info
     * 
     * ======================================================================================
     */

    /**
     * Returns if the given ItemStack is a record.
     * 
     */
    public boolean isRecord(ItemStack s)
    {
        return s != null && s.getItem() instanceof ItemRecord;
    }
    
    

    public String[] getRecordNames() {
        return this.recordNamesList;
    }
    
    
    
    
    
    public String getRecordResourceUrl(ItemRecord record) {
        if (record == null) return "";
        
        String resourceName = record.getRecordResource("records." + record.recordName).toString();
        return resourceName;
    }
    

    
    
    /**
     * Finds the ID of the given record in the internal record info collection.
     * 
     */
    public int getRecordInfoIdFromItemStack(ItemStack s) {
        if (!isRecord(s)) {
            return -1;
        }

        ItemRecord record = (ItemRecord) s.getItem();
        String resourceName = getRecordResourceUrl(record);
                
        return getRecordInfoIdFromUrl(resourceName);
    }
    
    
    /**
     * Finds the ID of the given record in the internal record info collection.
     * 
     */
    public int getRecordInfoIdFromUrl(String resourceName) {
        if (resourceName.isEmpty()) return -1;

        for (final Entry<Integer, RecordInfo> entry : this.recordsInfoCollection.entrySet()) {
            if (entry.getValue().recordUrl.equalsIgnoreCase(resourceName)) {
                return entry.getKey();
            }
        }
        
        return -1;
    }
    
    
    
    
    public RecordInfo getRecordInfoFromId(int recordInfoId) {
        if (this.recordsInfoCollection.containsKey(recordInfoId)) {
            return this.recordsInfoCollection.get(recordInfoId);
        }
        
        return null;
    }
    
    
    

    /**
     * Returns the time in seconds that a record should be playing.
     * 
     */
    public int getSongTime(ItemStack s)
    {
        int infoId;
        RecordInfo recordInfo;
        
        infoId = getRecordInfoIdFromItemStack(s);
        recordInfo = getRecordInfoFromId(infoId);

        if (recordInfo != null && recordInfo.recordDurationSeconds >= 0) { 
            return recordInfo.recordDurationSeconds;
        } else {
            return ConfigurationHandler.defaultSongTime;
        }
    }







    /*
     * ======================================================================================
     * 
     * Record Trading
     * 
     * ======================================================================================
     */

    /**
     * Returns a random record.
     */
    public ItemStack getRandomRecord(Random rand)
    {
        // NOTE: Only consider vanilla or custom records from this mod.
        final int randomPosition = rand.nextInt(this.recordsInfoIdRandomCandidates.length);
        final int randomInfoId = this.recordsInfoIdRandomCandidates[randomPosition];
        final RecordInfo recordInfo = this.recordsInfoCollection.get(randomInfoId);
        final Item recordItem = Item.getItemById(recordInfo.recordItemId);
        
        LogHelper.info("getRandomRecord");
        LogHelper.info("    position: " + randomPosition);
        LogHelper.info("    info id: " + randomInfoId);
        LogHelper.info("    info: " + recordInfo);
        LogHelper.info("    item id: " + recordInfo.recordItemId);
        LogHelper.info("    item: " + recordItem);

        return new ItemStack(recordItem, 1, recordInfo.recordItemDamage);
    }


}