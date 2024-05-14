package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.github.dgzt.mundus.plugin.ode4j.debug.DebugRenderer;

public class CustomInputController extends InputAdapter {

    private final DebugRenderer debugRenderer;

    public CustomInputController(final DebugRenderer debugRenderer) {
        this.debugRenderer = debugRenderer;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (Input.Keys.L == keycode) {
            debugRenderer.setEnabled(!debugRenderer.isEnabled());
            if (debugRenderer.isEnabled()) {
                Gdx.app.log("Ode4j Physics Plugin", "Debug renderer enabled.");
            } else {
                Gdx.app.log("Ode4j Physics Plugin", "Debug renderer disabled.");
            }

            return true;
        }

        return false;
    }
}
