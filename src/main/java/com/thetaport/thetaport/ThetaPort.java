package com.thetablock.thetaport;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.thetablock.thetaport.commands.*;
import com.thetablock.thetaport.commands.Point;
import com.thetablock.thetaport.configs.InjectorHandler;
import com.thetablock.thetaport.listeners.OnMoveHandler;
import com.thetablock.thetaport.services.EventServices;
import com.thetablock.thetaport.services.PortServices;
import com.thetablock.thetaport.utils.cmdManager.RegisteredCommands;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

public class ThetaPort  extends JavaPlugin implements Injectors {
//    Injector inject = Guice.createInjector(new InjectorHandler());
    PortServices portServices;
    EventServices eventServices;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new OnMoveHandler(), this);
        RegisteredCommands registeredCommands = injector.getInstance(RegisteredCommands.class);
        EventServices eventServices = injector.getInstance(EventServices.class);

        eventServices.startListenerEvents();

        registeredCommands.registerCommand(Create::new, this)
                .registerCommand(Delete::new, this)
                .registerCommand(Link::new, this)
                .registerCommand(Messages::new, this)
                .registerCommand(Point::new, this)
                //.registerCommand(Schedule::new, this)
                .registerCommand(Unlink::new, this)
                .registerCommand(Update::new, this)
                .registerCommand(WarpList::new, this);
    }


}
