package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.github.dgzt.mundus.plugin.ode4j.MundusOde4jRuntimePlugin;
import com.github.dgzt.mundus.plugin.ode4j.component.Ode4jPhysicsComponent;
import com.github.dgzt.mundus.plugin.ode4j.debug.DebugRenderer;
import com.github.dgzt.mundus.plugin.ode4j.util.Ode4jPhysicsComponentUtils;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.InvalidComponentException;
import com.mbrlabs.mundus.runtime.Mundus;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class CustomInputController extends InputAdapter {

    private final Mundus mundus;
    private final Scene scene;
    private final DebugRenderer debugRenderer;

    public CustomInputController(
            final Mundus mundus,
            final Scene scene,
            final DebugRenderer debugRenderer
    ) {
        this.mundus = mundus;
        this.scene = scene;
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

        if (Input.Keys.B == keycode) {
            final SceneAsset boxSceneAsset = mundus.getAssetManager().getGdxAssetManager().get("shapes/box/box.gltf");
            final GameObject boxGo = scene.sceneGraph.addGameObject(boxSceneAsset.scene.model, scene.cam.position);

            final Ode4jPhysicsComponent physicsComponent = Ode4jPhysicsComponentUtils.createBoxPhysicsComponent(boxGo, false);
            try {
                boxGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }
            MundusOde4jRuntimePlugin.getPhysicsWorld().getPhysicsComponents().add(physicsComponent);
        }

        return false;
    }

}
