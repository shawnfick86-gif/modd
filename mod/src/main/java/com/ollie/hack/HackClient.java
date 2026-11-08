package com.ollie.hack;

import net.fabricmc.api.ClientModInitializer;

public class HackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("--- OLLIEHACK LOADED SUCCESSFULLY ---");
    }
}
