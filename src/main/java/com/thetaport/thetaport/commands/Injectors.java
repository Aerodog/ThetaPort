package com.thetablock.thetaport.commands;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.thetablock.thetaport.configs.InjectorHandler;
import com.thetablock.thetaport.services.EventServices;
import com.thetablock.thetaport.services.PortServices;

public interface Injectors {
    Injector injector = Guice.createInjector(new InjectorHandler());

}
