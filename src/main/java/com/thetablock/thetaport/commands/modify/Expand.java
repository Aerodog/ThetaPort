package com.thetablock.thetaport.commands.modify;

import com.thetablock.thetaport.commands.CommandHandler;
import com.thetablock.thetaport.commands.Injectors;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Cmd(name = "tpexpand", usage = "/tpexpand", aliases = {}, enabled = false, label = "", permission = "thetaport.expand")
@Description(desc = "Expands the region zone based on the direction your looking")
public class Expand extends CommandHandler implements Injectors {
    private PortServices portServices = injector.getInstance(PortServices.class);

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (args.length >= 1) {
            if (StringUtils.isNumeric(args[0])) {
                if (args[0].matches("\\d*\\.?\\d+")) {
                    commandSender.sendMessage("Your expand range cannot be a decimal.");
                    return true;
                }
                int amount = Integer.valueOf(args[0]);
                Player player = (Player) commandSender;
//                portServices.expand(player.getLocation(), amount);

            }
        }
        return true;

    }
}
