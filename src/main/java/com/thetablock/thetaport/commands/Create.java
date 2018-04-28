package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

//tpcreate warpName -os 30m --disabled
@Cmd(name = "tpcreate", usage = "/tpcreate", aliases = {}, enabled = true, label = "", permission = "")
@Description(desc="creates a warp where two points are selected")
public class Create extends CommandHandler implements Injectors {
    private PortServices portServices = injector.getInstance(PortServices.class);
    private final String permission = "thetaport.create";

    private Option option = new Option("ar", "sets arrival message");

    private Options options = new Options()
            .addOption("os", "offset", true, "Sets the amount of time till next warp.")
            .addOption("d", "disable", false, "Allows user to disable the function.")
            .addOption("l", "linked", true, "Links a vendor to ")
            .addOption(Option.builder().argName("ar").longOpt("arrival").valueSeparator(' ').hasArg(true).numberOfArgs(10).build());

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission(permission)) {
            if (args.length >= 1) {
                if (args[0].matches("^(?=.{5,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$")) {
                    Player player = (Player) sender;
                    CommandLine cmdLine = null;
                    try {
                        cmdLine = parser.parse(options, args);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        player.sendMessage("§4An error exists in your arguments, please use -h or --help");
                        return true;
                    }

                    if (cmdLine.hasOption("h")) {
                        sender.sendMessage("§7/tpcreate  §4<&port Name§4>");
                        getHelp(options).forEach(sender::sendMessage);
                    }

                    System.out.println(cmdLine.hasOption("ar"));

                    String offsetUnparsed = (cmdLine.hasOption("os")) ? cmdLine.getOptionValue("os") : "";
                    String arrivalMessage = cmdLine.hasOption("ar") ? cmdLine.getOptionValues("ar").toString() : "";
                    String departureMessage = cmdLine.hasOption("dm") ? cmdLine.getOptionValue("dm") : "";
                    Response response = portServices.createPort(player.getUniqueId(), args[0], offsetUnparsed, cmdLine.hasOption("d"), arrivalMessage, departureMessage,
                            Optional.ofNullable(cmdLine.getOptionValue("l")));


                    switch (response) {
                        case SUCCESS:
                            player.sendMessage("§3the warp point has been created, to create an active link use /tplink");
                            break;
                        case WARP_EXISTS:
                            player.sendMessage("§4The warp name is already in use, please use another one.");
                            break;
                        case REQUIRE_TWO_POINTS:
                            player.sendMessage("§4Two points are required to be selected, please use /point");
                            break;
                        case INVALID_OFFSET:
                            player.sendMessage("§4 Invalid offset, must be S, M, or H");
                        default:
                            player.sendMessage("§4An error has occurred");
                    }
                } else {
                    sender.sendMessage("§4The name you have submitted is invalid.");
                }
            } else {
                sender.sendMessage("§4Invalid Arguments");
            }
        } else {
            sender.sendMessage("§4You do not have permission to execute this command.");
        }
        return true;
    }
}
