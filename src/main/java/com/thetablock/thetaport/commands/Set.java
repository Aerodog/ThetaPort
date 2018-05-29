package com.thetablock.thetaport.commands;

import com.google.inject.Inject;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Cmd(name = "tpset", usage = "/tpcreate", aliases = {}, enabled = true, label = "", permission = "")
public class Set extends CommandHandler implements Injectors  {
    private PortServices portServices = injector.getInstance(PortServices.class);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        //tpset <type> <number>
        if (args.length == 2) {
            Player player = (Player) sender;
            portServices.set(player.getLocation(), args[0], args[1]);
            //tpset <name> <type> <number>
        } else if (args.length == 3) {

        }
        return false;
    }
}
