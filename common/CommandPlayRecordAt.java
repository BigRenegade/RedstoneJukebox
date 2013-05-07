package sidben.redstonejukebox.common;

import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.sound.PlayBackgroundMusicEvent;
import net.minecraftforge.client.event.sound.SoundEvent;



public class CommandPlayRecordAt  extends CommandBase
{
	
	private static final String myUsage = "/playrecordat <record name> <x> <y> <z> [showname]"; 
	
	

    public String getCommandName()
    {
        return "playrecordat";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.translateString(myUsage, new Object[0]);
    }

    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
		boolean sucess = false;
		boolean showName = false; 
		
    	
    	if (par2ArrayOfStr.length < 4)
        {
            throw new WrongUsageException(myUsage, new Object[0]);
        }
        else
        {
			// Show playing message, if needed 
			if (par2ArrayOfStr.length >= 2)
			{
				if (par2ArrayOfStr[1].equals("showname")) { showName = true; }
			}

			// Play the song
           	sucess = CustomRecordHelper.playRecordAt(par2ArrayOfStr[0].toLowerCase(), parseInt(par1ICommandSender, par2ArrayOfStr[1]), parseInt(par1ICommandSender, par2ArrayOfStr[2]), parseInt(par1ICommandSender, par2ArrayOfStr[3]), showName, 0);
			if (!sucess)
			{
				// error message
				throw new CommandException("Record not found", new Object[0]);
			}

        }
    }

    
    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return par2ArrayOfStr.length == 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, CustomRecordHelper.getRecordNamesList()): null;
    }
    
    
    

    
	
}
