package com.thetablock.thetaport.listeners;

import com.google.inject.Inject;
import com.thetablock.thetaport.commands.Injectors;
import com.thetablock.thetaport.entities.Core;
import com.thetablock.thetaport.enums.Response;
import com.thetablock.thetaport.enums.ToolNames;
import com.thetablock.thetaport.services.PortServices;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ToolItemClickEvent implements Listener, Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().equals(EquipmentSlot.HAND)) {
            if (item.getType().equals(Material.BLAZE_ROD) && item.getItemMeta().getDisplayName().equalsIgnoreCase(ToolNames.CREATE_TOOL.getName())) {
                int size = item.getItemMeta().getLore() != null ? item.getItemMeta().getLore().size() : 0;
                System.out.println("Test " + size);
                Response response = portServices.setPoint(event.getPlayer().getUniqueId(), item, event.getClickedBlock().getLocation(), size);
                Player player = event.getPlayer();
                DecimalFormat f = new DecimalFormat("####.##");
                Location location = event.getClickedBlock().getLocation();

                ItemMeta meta = item.getItemMeta();

                String x = f.format(location.getX());
                String y = f.format(location.getY());
                String z = f.format(location.getZ());

                switch (response) {
                    case FIRST_POINT_SET:
                        meta.setLore(Collections.singletonList("First Point: §5" + location.getWorld() + ", " + x + ", §5" + y + ", §C" + z));
                        player.sendMessage("§3First point set at §4" + f.format(location.getX()) + ", §5" + f.format(location.getY()) + ", §C" + f.format(location.getZ()));
                        player.sendMessage("§3Set the second point.");
                        break;
                    case SECOND_POINT_SET:
                        List<String> lore  = meta.getLore();
                        lore.add("Second Point: §5" + location.getWorld() + ", " + x + ", §5" + y + ", §C" + z);
                        meta.setLore(lore);
                        player.sendMessage("§3Second point set at §4" + f.format(location.getX()) + ", §5" + f.format(location.getY()) + ", §C" + f.format(location.getZ()));
                        player.sendMessage("§3Set the arrival location for this port (Note the direction that you face will define what direction they port into");
                        break;
                    case WARP_TO_POINT_SET:
                        List<String> lore2  = meta.getLore();
                        lore2.add("Arrival Point: §5" + location.getWorld() + ", " + x + ", §5" + y + ", §C" + z);
                        meta.setLore(lore2);
                        player.sendMessage("§3 Port Arrival has been set.");
                        player.sendMessage("§3 Port has been saved.");
                        player.getInventory().setItemInMainHand(null);
                        return;
                }
                item.setItemMeta(meta);
                player.getInventory().setItemInMainHand(null);
                player.getInventory().setItemInMainHand(item);
            }
        }

    }
}
