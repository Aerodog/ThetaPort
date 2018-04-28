package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.Tuple2;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Cmd(name = "tpinfo", usage = "/tpinfo", aliases = {}, enabled = true, label = "", permission = "")
public class WarpInfo extends CommandHandler implements Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                Tuple2<Response, List<String>> warps = portServices.getNearbyWarps(player.getLocation());
                warps.getValue().forEach(player::sendMessage);
            } else {
                Tuple2<Response, PortData> warpData = portServices.getWarpData(args[0]);
                switch (warpData.getKey()) {
                    case SUCCESS: {
                        PortData wd = warpData.getValue();
                        sender.sendMessage("Warp Name: " + wd.getName());
                        sender.sendMessage("Warp To Point:" + wd.getWarpToPoint());
                        sender.sendMessage("Corner: " + wd.getFloorPoint());
                        sender.sendMessage("Corner: " + wd.getCeilPoint());
                        sender.sendMessage("Enabled: true");
                        sender.sendMessage("Linked Point: " + wd.getLinked());
                        sender.sendMessage("Offset: " + wd.getWarpOffset());
                        sender.sendMessage("Arrival Message: " + wd.getArrivalMessage());
                        sender.sendMessage("Departure Message: " + wd.getDepartureMessage());
                    }
                    case INVALID_PORT: {
                        sender.sendMessage("The warp you are trying to load ");
                    }

                }
            }
        }
        return false;
    }
}
