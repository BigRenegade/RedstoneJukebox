package redstonejukebox.init;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;
import redstonejukebox.item.PTJukeboxRecord;
import redstonejukebox.main.Reference;

@ObjectHolder(Reference.ModID)
public class ItemBase {
	
	
	public static final ItemRecord KISSTHIS_RECORD = new PTJukeboxRecord("kissthis", MusicRecords.kissthis);
	public static final ItemRecord PERSPECTIVES_RECORD = new PTJukeboxRecord("perspectives", MusicRecords.perspectives);
	public static final ItemRecord KISSASS_RECORD = new PTJukeboxRecord("kissass", MusicRecords.kissass);
		
	
	@Mod.EventBusSubscriber(modid = Reference.ModID)
	public static class ItemRegistration {
		public static final NonNullList<Item> ITEMS = NonNullList.<Item>create();
		
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			final Item[] items = {
					KISSTHIS_RECORD,
					PERSPECTIVES_RECORD,
					KISSASS_RECORD
			};
			
			for(final Item item : items) {
				event.getRegistry().register(item);
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
				ITEMS.add(item);
			}
		}
	       	
	}

}