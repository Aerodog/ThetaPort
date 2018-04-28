package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

@Cmd(name = "tpdeparture", usage = "/tparrival", aliases = {}, enabled = true, label = "", permission = "")
public class DepatureMessage extends CommandHandler implements Injectors {
    private PortServices portServices = injector.getInstance(PortServices.class);

    private final String permission = "thetaport.arrival";
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission(permission)) {
            if (args.length > 1) {
                ArrayUtils.remove(args, 0);
                String message = String.join("", args);
                Response response = portServices.setDepatureMessage(args[0], message);

                switch (response) {
                    case SUCCESS:
                        sender.sendMessage("Departure has been set for " + args[0]);
                        break;
                    case INVALID_PORT:
                        sender.sendMessage(args[0] + " is not a valid warp.");
                        break;
                }
            }
        }
        return false;
    }
}
