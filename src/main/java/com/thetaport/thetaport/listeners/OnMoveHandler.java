package com.thetablock.thetaport.listeners;

        import com.google.inject.Guice;
        import com.google.inject.Injector;
        import com.thetablock.thetaport.commands.Injectors;
        import com.thetablock.thetaport.configs.InjectorHandler;
        import com.thetablock.thetaport.services.EventServices;
        import com.thetablock.thetaport.services.PortServices;
        import org.bukkit.entity.Player;
        import org.bukkit.event.EventHandler;
        import org.bukkit.event.Listener;
        import org.bukkit.event.player.PlayerMoveEvent;


public class OnMoveHandler implements Listener, Injectors {
    PortServices portServices = injector.getInstance(PortServices.class);
    EventServices eventServices = injector.getInstance(EventServices.class);
    @EventHandler
    public void OnPlayerMoveEvent(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        eventServices.checkIfPlayerLeavesPortZone(player, event.getTo());
        eventServices.checkIfPlayerEntersPortZone(player, event.getTo());
    }


}
