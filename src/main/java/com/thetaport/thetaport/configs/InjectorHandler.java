package com.thetablock.thetaport.configs;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.thetablock.thetaport.repositories.*;

import javax.sound.sampled.Port;

public class InjectorHandler extends AbstractModule {

    @Override
    protected void configure() {
        bind(PortDataRepository.class).to(PortDataRepositoryImpl.class).in(Scopes.SINGLETON);
        bind(TimerRepositories.class).to(TimerRepositoryImpl.class).in(Scopes.SINGLETON);
        bind(TempStorageRepository.class).to(TempStorageImpl.class).in(Scopes.SINGLETON);
    }
}
