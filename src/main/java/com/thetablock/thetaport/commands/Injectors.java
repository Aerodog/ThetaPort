package com.thetablock.thetaport.commands;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.thetablock.thetaport.configs.InjectorHandler;

public interface Injectors {
    Injector injector = Guice.createInjector(new InjectorHandler());

}
