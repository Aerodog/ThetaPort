package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.apache.commons.cli.Options;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@Cmd(name = "tpunlink", usage = "/unlink", aliases = {}, enabled = true, label = "", permission = "")


public class Unlink extends CommandHandler implements Injectors {
    private PortServices portServices = injector.getInstance(PortServices.class);

    private Options options = new Options()
            .addOption("h", "help", false, "help info for unlink.");
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 1) {
            Response response = portServices.unlink(args[0]);

            switch (response) {
                case SUCCESS:
                    sender.sendMessage("§2" + args[0] + " §3has been unlinked.");
                case INVALID_WARP:
                    sender.sendMessage("§4 invalid port.");
                case WARP_NOT_LINKED:
                    sender.sendMessage("§4" + args[0] + " is not linked.");
            }
        }
        return false;
    }
}
