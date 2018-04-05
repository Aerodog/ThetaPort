package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.entities.MessageType;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.command.CommandSender;

import java.util.stream.Stream;

@Cmd(name = "tpmessages", usage = "/messages", aliases = {}, enabled = true, label = "", permission = "")
public class Messages extends CommandHandler  implements Injectors {
    private final String permission = "thetaport.messages";
    PortServices portServices = injector.getInstance(PortServices.class);
    Options options = new Options()
            .addOption("h", "help", false, "Shows help on how to use the command.");


    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission(permission)) {
            CommandLine cmdLine = null;

            try {
                cmdLine = parser.parse(options, args);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (cmdLine.hasOption("h")) {
                sender.sendMessage("§7/tpmessage  §4<§5port name§4>  §4<§2departure &0| §3arrival§4>  §4<§5Message§4>");
                return true;
            }

            if (args.length >= 3) {
                MessageType type = MessageType.valueOf(args[1]);
                if (MessageType.ARRIVAL.equals(type) || MessageType.DEPARTURE.equals(type)) {
                    String message = Stream.of(args)
                            .filter(m -> m.equals(args[0]))
                            .reduce("", String::concat);
                    Response response = portServices.setMessage(args[0], type, message);

                    if (response.equals(Response.SUCCESS)) {
                        sender.sendMessage("$3The " + type.name() + " has been updated.");
                    } else if (response.equals(Response.INVALID_MESSAGE_TYPE)) {
                        sender.sendMessage("&$4" + args[1] + " is not a valid type,  it must be either  §C arrival §4 | §Cdeparture");

                    } else if (response.equals(Response.WARP_DOES_NOT_EXIST)) {
                        sender.sendMessage("§C" + args[0] + " §4is not a valid port.");
                    }
                }
            }
            sender.sendMessage("§4Invalid argument size.");
            return true;
        } else {
            sender.sendMessage("§4You do not have permission to execute this command.");
        }
        return false;
    }
}
