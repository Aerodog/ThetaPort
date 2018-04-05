package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.command.CommandSender;

@Cmd(name = "tplink", usage = "/tplink", aliases = {}, enabled = true, label = "", permission = "")
@Description(desc= "links two different points together.")
public class Link extends CommandHandler  implements Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    private String permission = "thetaport.link";
    private Options options = new Options()
            .addOption("st", "singletrip", false, "Makes the port a one way trip.")
            .addOption("s", "sync", true, "Syncs the time with whatever is defined")
            .addOption("ul", "unlink", true, "Unlinks a link.");

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Response response = null;
        if (sender.hasPermission(permission)) {
            if (args.length >= 2) {
                CommandLine cmdLine = null;
                try {
                    cmdLine = parser.parse(options, args);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (cmdLine.hasOption("unlink")) {
                    response = portServices.unlink(args[0]);

                    switch (response) {
                        case SUCCESS:
                            sender.sendMessage("§2" +args[0] + "§3 has been successfully unlinked");
                        case INVALID_WARP:
                            sender.sendMessage("§4The warp you are trying to unlink does not exist.");
                        case WARP_NOT_LINKED:
                            sender.sendMessage("§4The warp you are trying to");
                    }
                } else {
                    response = portServices.link(args[0], args[1], cmdLine.getOptionValue("sync"), cmdLine.hasOption("st"));

                    switch (response) {
                        case SUCCESS:
                            sender.sendMessage("§4Link has been successfully created");
                            break;
                        case POINT_ALREADY_LINKED:
                            sender.sendMessage("§4One or more of the points are already linked");
                            break;
                        case LINK_SYNC_FAILED:
                            sender.sendMessage("§4Port time sync has failed.");
                            break;
                        case INVALID_WARP:
                            sender.sendMessage("§4The warp you are trying to link does not exist.");
                            break;
                    }
                }
                return true;
            } else {
                sender.sendMessage("§4Invalid Arguments");
                return true;
            }
        } else {
            sender.sendMessage("§4You do not have permission to execute this command.");
        }
        return true;
    }
}
