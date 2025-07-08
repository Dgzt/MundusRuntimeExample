package com.mygdx.game.jolt;

import jolt.Jolt;

public class JoltInstance {

    public JoltInstance() {
        Jolt.Init();
    }

    public void dispose() {
        Jolt.UnregisterTypes();
    }
}
