package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.enums.ToolNames;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import net.minecraft.server.v1_12_R1.Item;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            .addOption("t", "ticket", false, "Sets required item to teleport.")
            .addOption("ar", "arrivaldeparture", false, "Sets the arrival message when a player is warped.")
            .addOption("dr", "departureRedstone", false, "Sets the redstone executor")
            .addOption("r", "reset", false, "Resets current in progress.")
            .addOption("np", "nextport", false, "sets next port");

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
                    String offsetUnparsed = (cmdLine.hasOption("os")) ? cmdLine.getOptionValue("os") : "";

                    ItemStack item = null;

                    Response response = portServices.createPort(
                            player.getUniqueId(), args[0], false , cmdLine.hasOption("d"), cmdLine.hasOption("os"), cmdLine.hasOption("t"),
                            cmdLine.hasOption("ar"), cmdLine.hasOption("np"),
                            Optional.ofNullable(cmdLine.getOptionValue("t")), Optional.ofNullable(player.getInventory().getItemInMainHand()), cmdLine.hasOption("r"));

                    switch (response) {
                        case SUCCESS:
                            System.out.println("TOOL NAME " + ToolNames.CREATE_TOOL.getName());
                            player.sendMessage("§3Select the first point.");
                            ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
                            ItemMeta  meta = itemStack.getItemMeta();
                            meta.setDisplayName(ToolNames.CREATE_TOOL.getName());
                            itemStack.setItemMeta(meta);
                            player.getInventory().setItemInMainHand(itemStack);
                            break;
                        case WARP_EXISTS:
                            player.sendMessage("§4The warp name is already in use, please use another one.");
                            break;
                        case REQUIRE_TWO_POINTS:
                            player.sendMessage("§4Two points are required to be selected, please use /point");
                            break;
                        case INVALID_OFFSET:
                            player.sendMessage("§4 Invalid offset, must be S, M, or H");
                            break;
                        case INVALID_REQUIRED_ITEM:
                            player.sendMessage("§4 The item you have submitted is not valid.");
                            break;
                        case TEMP_PORT_EXISTS:
                            player.sendMessage("§eYou already have a port in progress, if you wish to clear it use -r");
                            break;
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
