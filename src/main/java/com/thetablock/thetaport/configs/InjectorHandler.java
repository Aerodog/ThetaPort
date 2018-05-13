package com.thetablock.thetaport.configs;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.thetablock.thetaport.repositories.*;

public class InjectorHandler extends AbstractModule {

    @Override
    protected void configure() {
        bind(PortDataRepository.class).to(PortDataRepositoryImpl.class).in(Scopes.SINGLETON);
        bind(TempRepository.class).to(TempRepositoryImpl.class).in(Scopes.SINGLETON);
    }
}
