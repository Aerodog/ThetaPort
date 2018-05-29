package com.thetablock.thetaport.commands.modify;

import com.thetablock.thetaport.commands.CommandHandler;
import com.thetablock.thetaport.commands.Injectors;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.bukkit.command.CommandSender;

@Cmd(name = "tptilt", usage = "/tptilt", aliases = {}, enabled = true, label = "", permission = "")

public class Tilt  extends CommandHandler implements Injectors {
    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        return false;
    }
}
