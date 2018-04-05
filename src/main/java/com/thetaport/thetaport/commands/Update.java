package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@Cmd(name = "tpupdate", usage = "/update", aliases = {}, enabled = true, label = "", permission = "")
@Description(desc = "creates a warp where two points are selected")
public class Update extends CommandHandler {
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return false;
    }
}
