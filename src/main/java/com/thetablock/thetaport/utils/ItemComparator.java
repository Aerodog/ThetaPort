package com.thetablock.thetaport.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemComparator {
    public static int getCount(ItemStack[] contents, ItemStack item) {
        int counter = 0;
        for (ItemStack lItem : contents) {
            if (compare(item, lItem)) {
                counter += lItem.getAmount();

            }
        }
        return counter;
    }

    public static boolean compare(ItemStack item, ItemStack clicked) {
        if (item == null || clicked == null) {
            return false;
        }
        // Handles if the event is non stackable item.
        if (item.getDurability() != clicked.getDurability()) {
            return false;
        }

        if (!item.getType().equals(clicked.getType())) {

            return false;
        }

        if (item.getItemMeta().hasDisplayName()) {

            if (!clicked.getItemMeta().hasDisplayName()) {

                return false;
            } else {
                if (!item.getItemMeta().getDisplayName()
                        .equals(clicked.getItemMeta().getDisplayName())) {

                    return false;
                }
            }
        } else {
            if (clicked.getItemMeta().hasDisplayName()) {

                return false;
            }
        }

        if (item.getItemMeta().hasLore()) {

            if (!clicked.getItemMeta().hasLore()) {

                return false;
            } else {

                if (item.getItemMeta().getLore().size() != clicked
                        .getItemMeta().getLore().size()) {

                    return false;
                }

                for (int i = 0; i < item.getItemMeta().getLore().size(); i++) {

                    if (!item.getItemMeta().getLore().get(i)
                            .equals(clicked.getItemMeta().getLore().get(i))) {

                        return false;
                    }
                }
            }
        } else {
            if (clicked.getItemMeta().hasLore()) {

                return false;
            }
        }

        if (item.getItemMeta().hasEnchants()) {

            if (!clicked.getItemMeta().hasEnchants()) {
                return false;
            } else {

                if (item.getItemMeta().getEnchants().size() != clicked
                        .getItemMeta().getEnchants().size()) {

                    return false;
                }

                for (int i = 0; i < item.getItemMeta().getEnchants().size(); i++) {
                    if (item.getItemMeta().getEnchants().get(i) != clicked
                            .getItemMeta().getEnchants().get(i)) {

                        return false;
                    }

                }
            }
        } else {
            if (clicked.getItemMeta().hasEnchants()) {

                return false;
            }
        }
        if (clicked.getAmount() < item.getAmount()) {
            return false;
        }

        if (item.getType().equals(Material.SKULL_ITEM)) {
            SkullMeta itemmeta = (SkullMeta) item.getItemMeta();
            itemmeta.getOwner();
            SkullMeta clickedmeta = (SkullMeta) clicked.getItemMeta();
            clickedmeta.getOwner();
            if (!clickedmeta.getOwner().equals(itemmeta.getOwner())) {
                return false;
            }
        }
        return true;
    }
}