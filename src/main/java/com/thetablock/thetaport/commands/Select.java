package com.thetablock.thetaport.commands;

import com.thetablock.thetaport.entities.PortData;
import com.thetablock.thetaport.entities.PortLoc;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.Tuple2;
import com.thetablock.thetaport.utils.cmdManager.Cmd;
import com.thetablock.thetaport.utils.cmdManager.Description;
import javafx.geometry.Point3D;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Cmd(name = "tpselect", label = "select" , usage = "/tpselect", aliases = {}, enabled = false, permission = "com.thetablock.tpselect")
@Description(desc = "Derp")
public class Select extends CommandHandler implements Injectors {
    private PortServices portServices = injector.getInstance(PortServices.class);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender.hasPermission("com.thetablock.select")) {
            String name = "";
            boolean hasArg = false;
            if (args.length == 1) {
                name = args[0];
                hasArg = true;
            }
            Player player = (Player) sender;
            Tuple2<Response, PortData> response = portServices.selectNearest(player.getUniqueId(), player.getLocation(), true);

            switch (response.getKey()) {
                case SUCCESS: {
                    PortData portData = response.getValue();
                    player.sendMessage("Selected Port: " + portData.getName());
                    player.sendMessage("Location: " + portData.getArrivalPoint().getX() + " " + portData.getArrivalPoint().getY() + " " + portData.getArrivalPoint().getZ());
                    player.sendMessage("Linked: " + portData.getLinked());

                    double floor = portData.getCeilPoint().getY() - portData.getFloorPoint().getY();
                    PortLoc temp = new PortLoc(portData.getFloorPoint().add(0, floor, 0)).setWorld(portData.getCeilPoint().getWorld());
                    PortLoc point1 = portServices.getCorner(portData.getFloorPoint().getWorld(), portData.getFloorPoint(), portData.getCeilPoint(), true);
                    PortLoc point2 = portServices.getCorner(portData.getFloorPoint().getWorld(), portData.getCeilPoint(), portData.getFloorPoint(), true);

                    spawnFences(portData.getFloorPoint().getY(), portData.getCeilPoint().getLocation());
                    spawnFences(portData.getFloorPoint().getY(), temp.getLocation());
                    spawnFences(portData.getFloorPoint().getY(), point1.getLocation());
                    spawnFences(portData.getFloorPoint().getY(), point2.getLocation());
                    break;
                }
                case NO_PORT_IN_RANGE:
            }
        }
        return false;
    }

    private void spawnFences(double bottom, final Location location) {
        for (double i = location.getY(); i >= bottom; i--) {
            location.setY(i);
            if (location.getBlock().getType().equals(Material.AIR)) {
                location.getBlock().setType(Material.STAINED_GLASS_PANE);
            }
        }
    }
}
