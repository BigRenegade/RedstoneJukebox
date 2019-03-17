package redstonejukebox.init;


import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import redstonejukebox.main.Reference;

@ObjectHolder(Reference.ModID)
public class MusicRecords {
	
	public static SoundEvent perspectives = registerSound("perspectives");
	public static SoundEvent kissthis = registerSound("kissthis");
	public static SoundEvent kissass = registerSound("kissass");
	
	public static void registerSounds() {
	}
	
	private static SoundEvent registerSound(String soundName) {
		final SoundEvent sound = new SoundEvent(new ResourceLocation(Reference.ModID, soundName)).setRegistryName(new ResourceLocation(Reference.ModID, soundName));
		ForgeRegistries.SOUND_EVENTS.register(sound);
		return (sound);
	}
	
}
