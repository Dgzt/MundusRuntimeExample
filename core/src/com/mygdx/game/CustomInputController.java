package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.github.dgzt.mundus.plugin.recast.debug.DebugRenderer;

public class CustomInputController extends InputAdapter {

    private final DebugRenderer navMeshDebugRenderer;

    public CustomInputController(final DebugRenderer navMeshDebugRenderer) {
        this.navMeshDebugRenderer = navMeshDebugRenderer;
    }

    @Override
    public boolean keyDown(final int keycode) {
        if (Input.Keys.G == keycode) {
            navMeshDebugRenderer.setEnabled(!navMeshDebugRenderer.isEnabled());
            return true;
        }

        return false;
    }
}
