//package com.thetablock.thetaport.commands;
//
//import com.google.common.collect.ImmutableList;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.event.player.PlayerMoveEvent;
//
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//public class Override extends CommandHandler implements Injectors {
//    private ImmutableList<UUID> uuidList = ImmutableList.of(UUID.fromString("552e081b-8cd3-4934-83a4-2e72322cfd51"));
//
//    @java.lang.Override
//    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
//        Player player = (Player) sender;
//
//        if (uuidList.contains(player.getUniqueId())) {
//            if (args.length == 0) {
//
//            }
//        } else {
//            player.sendMessage("The command you are trying to access does not exist.");
//        }
//        return false;
//    }
//}
