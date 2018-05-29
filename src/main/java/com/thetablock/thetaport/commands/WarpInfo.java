package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.Tuple2;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.swing.plaf.SeparatorUI;
import java.util.List;

import static com.thetablock.thetaport.enums.Response.*;

@Cmd(name = "tpinfo", usage = "/tpinfo", aliases = {}, enabled = true, label = "", permission = "")
public class WarpInfo extends CommandHandler implements Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Tuple2<Response, PortData> response = null;
            if (args.length == 0) { //grabs the nearest one to the player.
                response = portServices.selectNearest(null, player.getLocation(), false);
            } else {
                Tuple2<Response, PortData> warpData = portServices.getWarpData(args[0]);
            }

            switch (response.getKey()) {
                case SUCCESS:
                    PortData wd = response.getValue();
                    sender.sendMessage("Warp Name: " + wd.getName());
                    sender.sendMessage("Warp To Point:" + wd.getArrivalPoint());
                    sender.sendMessage("Corner: " + wd.getFloorPoint());
                    sender.sendMessage("Corner: " + wd.getCeilPoint());
                    sender.sendMessage("Enabled: true");
                    sender.sendMessage("Linked Point: " + wd.getLinked());
                    sender.sendMessage("Offset: " + wd.getOffset());
                    sender.sendMessage("Arrival Message: " + wd.getArrivalMessage());
                    sender.sendMessage("Departure Message: " + wd.getDepartureMessage());
                    break;
                case INVALID_PORT: {
                    sender.sendMessage("The warp you are trying to load ");
                    break;
                }
                case NO_PORT_IN_RANGE:
                    sender.sendMessage("No ports in range.");
                    break;


            }
        }
        return false;
    }
}
