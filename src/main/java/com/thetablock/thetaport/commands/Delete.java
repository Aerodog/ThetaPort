package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.command.CommandSender;

@Cmd(name = "tpdelete", usage = "/delete", aliases = {}, enabled = true, label = "", permission = "")
public class Delete extends CommandHandler  implements Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    private final String permission = "thetaport.delete";

    Options options = new Options()
            .addOption("cs", "clean", false, "Cleans all links and linked objects (Note this will delete the other node as well.")
            .addOption("ul", "Unlink", false, "This will only delete the link between and nothing else.");

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission(permission)) {
            if (args.length >= 1) {
                CommandLine commandLine;
                try {
                    commandLine = parser.parse(options, args);
                } catch (ParseException e) {
                    sender.sendMessage("§4Invalid Arguments");
                    return true;
                }
                Response response = portServices.deleteWarp(args[0], commandLine.hasOption("cs"), commandLine.hasOption("ul"));

                if (response.equals(Response.SUCCESS)) {
                    sender.sendMessage("§3The warp §2args[0]§3 has been deleted and unlinked.");
                    return true;
                }
            }
        }
        sender.sendMessage("§4You do not have permission to execute this command.");
        return true;
    }
}
