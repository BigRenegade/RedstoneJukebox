package redstonejukebox.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;
import redstonejukebox.main.Reference;

public class PTJukeboxRecord extends ItemRecord {

	public PTJukeboxRecord(String name, SoundEvent soundIn) {
		super(name, soundIn);
		this.setUnlocalizedName(name);
		this.setRegistryName(Reference.ModID + ":" + name);
	}

}
