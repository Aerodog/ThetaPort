package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.Tuple2;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

//TODO
@Cmd(name = "tplist",  usage = "/tplist", aliases = {}, enabled = true, label = "", permission = "")
@Description(desc="creates a warp where two points are selected")
public class WarpList extends CommandHandler implements Injectors {

    PortServices portServices = injector.getInstance(PortServices.class);

    private Options options = new Options()
            .addOption("i", "info", false, "shows full information about the  warps.")
            .addOption(Option.builder().argName("l").longOpt("l").desc("Lists all linked ").optionalArg(true)
                    .hasArg(true).numberOfArgs(1).build())
            .addOption(Option.builder().argName("ul").longOpt("ul").desc("Lists all unlinked ").optionalArg(true)
                    .hasArg(true).numberOfArgs(1).build());

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        CommandLine cmdLine = null;

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            sender.sendMessage("Invalid command line arguments, please use help!");
            e.printStackTrace();
            return true;
        }
        Response response = null;
        int page = 1; //always will default to the first page
        Tuple2<Response, List<String>> listTuple2;
        if (cmdLine.hasOption("l")) {
            String optionValue = cmdLine.getOptionValue("l");
            if (StringUtils.isNumeric(optionValue)) {
                page = Integer.valueOf(optionValue);
            }
            listTuple2 = portServices.getLinkedList(page);
        } else if (cmdLine.hasOption("ul")) {
            String optionValue = cmdLine.getOptionValue("ul");
            if (StringUtils.isNumeric(optionValue)) {
                page = Integer.valueOf(optionValue);
            }
            listTuple2 = portServices.getUnlinked(page);
        } else {
            listTuple2 = portServices.getPortsByPage(page);
        }

        sender.sendMessage("Current Linked Warps");
        listTuple2.getValue().forEach(sender::sendMessage);

        if (listTuple2.getValue().size() > 0) {
        }
        return true;
    }
}
