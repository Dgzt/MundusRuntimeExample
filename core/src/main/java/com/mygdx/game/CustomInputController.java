package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import jolt.gdx.DebugRenderer;

public class CustomInputController extends InputAdapter {
    private final DebugRenderer debugRenderer;

    public CustomInputController(final DebugRenderer debugRenderer) {
        this.debugRenderer = debugRenderer;
    }

    @Override
    public boolean keyUp(final int keycode) {
        if (Input.Keys.L == keycode) {
            debugRenderer.setEnable(!debugRenderer.isEnable());

            return true;
        }

        return false;
    }
}
