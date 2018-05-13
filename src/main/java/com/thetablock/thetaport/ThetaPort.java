package com.thetablock.thetaport;

import com.google.inject.Inject;
import com.thetablock.thetaport.commands.*;
import com.thetablock.thetaport.commands.Point;
import com.thetablock.thetaport.listeners.ToolItemClickEvent;
import com.thetablock.thetaport.services.EventServices;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.RegisteredCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ThetaPort  extends JavaPlugin implements Injectors {
    PortServices portServices;
    EventServices eventServices;

    @Override
    public void onEnable() {
        RegisteredCommands registeredCommands = injector.getInstance(RegisteredCommands.class);
        EventServices eventServices = injector.getInstance(EventServices.class);

        eventServices.startListenerEvents(this);
        Bukkit.getPluginManager().registerEvents(new ToolItemClickEvent(), this);

        registeredCommands.registerCommand(Create::new, this)
                .registerCommand(Delete::new, this)
                .registerCommand(Link::new, this)
                .registerCommand(ArrivalMessage::new, this)
                .registerCommand(DepatureMessage::new, this)
                .registerCommand(Point::new, this)
                .registerCommand(Unlink::new, this)
                .registerCommand(Update::new, this)
                .registerCommand(WarpList::new, this);
    }


}
