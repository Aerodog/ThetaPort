package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

@Cmd(name = "tppoint", label = "", usage = "", aliases = {}, enabled = true, permission = "")
public class Point extends CommandHandler implements Injectors {
    private final String permisison = "thetaport.point";
    PortServices portServices = injector.getInstance(PortServices.class);


    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission(permisison)) {
            Player player = (Player) sender;

            PortLoc portLoc = new PortLoc(player.getLocation());
            Response response = portServices.createPoint(player.getUniqueId(), portLoc);
            DecimalFormat f = new DecimalFormat("####.##");
            switch (response) {
                case FIRST_SLOT:
                    sender.sendMessage("§3First point set at §4" + f.format(portLoc.getX()) + ", §5" + f.format(portLoc.getY())+ ", §C" + f.format(portLoc.getZ()));
                    break;
                case SECOND_SLOT:
                    sender.sendMessage("§3First point set at §4" + f.format(portLoc.getX()) + ", §5" + f.format(portLoc.getY())+ ", §C" + f.format(portLoc.getZ()));
                    break;
                case WARP_COORDS_IN_USE:
                    sender.sendMessage("§4The coordinate you are selecting is already within another existing point.");
                    break;
                case POINTS_RESET:
                    sender.sendMessage("§4Points reset.");
            }
        } else {
            sender.sendMessage("§4You do not have permission to execute this command.");
        }
        return true;
    }
}
