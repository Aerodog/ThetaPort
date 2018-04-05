package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import org.bukkit.command.CommandSender;

@Cmd(name = "tpsched", usage = "/create", aliases = {}, enabled = false, label = "", permission = "")
@Description(desc="creates a warp where two points are selected")
public class Schedule extends CommandHandler implements Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    public Schedule() {
        super("", "", "", null);
    }


    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return false;
    }
}
