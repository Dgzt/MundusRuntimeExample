package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.JoltPhysicsPlugin;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.component.JoltPhysicsComponent;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.manager.ComponentManager;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.InvalidComponentException;
import jolt.gdx.DebugRenderer;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CustomInputController extends InputAdapter {
    private static final float BOX_WIDTH = 1.0f;
    private static final float BOX_HEIGHT = 1.0f;
    private static final float BOX_DEPTH = 1.0f;

    private final Scene scene;
    private final DebugRenderer debugRenderer;
    private final ComponentManager componentManager;

    public CustomInputController(final Scene scene, final DebugRenderer debugRenderer) {
        this.scene = scene;
        this.debugRenderer = debugRenderer;
        componentManager = JoltPhysicsPlugin.getComponentManager();
    }

    @Override
    public boolean keyUp(final int keycode) {
        if (Input.Keys.L == keycode) {
            debugRenderer.setEnable(!debugRenderer.isEnable());

            return true;
        }

        if (Input.Keys.NUM_1 == keycode) {
            final ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            final MeshPartBuilder meshPartBuilder = modelBuilder.part(
                "part",
                GL30.GL_TRIANGLES,
                VertexAttributes.Usage.Position,
                new Material(PBRColorAttribute.createBaseColorFactor(Color.RED)));
            BoxShapeBuilder.build(meshPartBuilder, BOX_WIDTH, BOX_HEIGHT, BOX_DEPTH);
            final Model model = modelBuilder.end();

            final GameObject boxGo = scene.sceneGraph.addGameObject(model, scene.cam.position);

            final JoltPhysicsComponent physicsComponent = componentManager.createBoxPhysicsComponent(boxGo, 10f);

            try {
                boxGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
