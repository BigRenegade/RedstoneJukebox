package redstonejukebox.command;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import redstonejukebox.ModRedstoneJukebox;
import redstonejukebox.network.NetworkManager;

// TODO: support for ~ on xyz coords, autocomplete of player name
public class CommandPlayRecordAt extends CommandBase
{

    @Override
    public String getName()
    {
        return "playrecordat";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "commands.playrecordat.usage";
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2) {
            throw new CommandException(this.getUsage(sender), new Object[0]);
        } else {
            /*
             * Command syntax:
             * playrecordat <record name> <player> [showname] [x] [y] [z] [range]
             */

            final String recordName = args[0];
            final EntityPlayerMP player = getPlayer(server, sender, args[1]);
            int recordInfoId = -1;
            boolean showName = false;
            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;
            int extraVolumeRange = 0;



            // Find the info id of the given record name (url). Throws exception if the id is invalid.
            recordInfoId = ModRedstoneJukebox.instance.getRecordInfoManager().getRecordInfoIdFromUrl(recordName);
            if (recordInfoId < 0) {
                throw new CommandException("commands.playrecordat.record_not_found", new Object[] { recordName });
            }



            if (args.length > 2) {
                showName = parseBoolean(args[2]);
            }

            if (args.length > 3) {
                x = parseDouble(args[3]);
            }

            if (args.length > 4) {
                y = parseDouble(args[4], 0);
            }

            if (args.length > 5) {
                z = parseDouble(args[5]);
            }

            if (args.length > 6) {
                extraVolumeRange = parseInt(args[6]);
            }


            // Send packet requesting record play
            NetworkManager.sendCommandPlayRecordAtMessage(recordInfoId, showName, x, y, z, extraVolumeRange, player);


            // Writes text on the chat
            notifyCommandListener(sender, this, "commands.playrecordat.success", new Object[] { recordName, player.getName() });
        }

    }


    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, ModRedstoneJukebox.instance.getRecordInfoManager().getRecordNames());
        }

        return null;
    }

}
