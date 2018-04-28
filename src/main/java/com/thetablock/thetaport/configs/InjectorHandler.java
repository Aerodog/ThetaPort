package com.thetablock.thetaport.configs;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.thetablock.thetaport.repositories.*;

public class InjectorHandler extends AbstractModule {

    @Override
    protected void configure() {
        bind(PortDataRepository.class).to(PortDataRepositoryImpl.class).in(Scopes.SINGLETON);
        bind(TimerRepository.class).to(TimerRepositoryImpl.class).in(Scopes.SINGLETON);
        bind(TempStorageRepository.class).to(TempStorageImpl.class).in(Scopes.SINGLETON);
    }
}
