package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.github.dgzt.mundus.plugin.ode4j.MundusOde4jRuntimePlugin;
import com.github.dgzt.mundus.plugin.ode4j.component.Ode4jPhysicsComponent;
import com.github.dgzt.mundus.plugin.ode4j.debug.DebugRenderer;
import com.github.dgzt.mundus.plugin.ode4j.util.Ode4jPhysicsComponentUtils;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.InvalidComponentException;
import com.mbrlabs.mundus.runtime.Mundus;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class CustomInputController extends InputAdapter {

    private static final double FORCE = 50.0;

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

        Ode4jPhysicsComponent physicsComponent = null;

        if (Input.Keys.NUM_1 == keycode) {
            final SceneAsset boxSceneAsset = mundus.getAssetManager().getGdxAssetManager().get("shapes/box/box.gltf");
            final GameObject boxGo = scene.sceneGraph.addGameObject(boxSceneAsset.scene.model, scene.cam.position);

            physicsComponent = Ode4jPhysicsComponentUtils.createBoxPhysicsComponent(boxGo, 10.0);
            try {
                boxGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }

        }

        if (Input.Keys.NUM_2 == keycode) {
            final SceneAsset sphereSceneAsset = mundus.getAssetManager().getGdxAssetManager().get("shapes/sphere/sphere.gltf");
            final GameObject sphereGo = scene.sceneGraph.addGameObject(sphereSceneAsset.scene.model, scene.cam.position);

            physicsComponent = Ode4jPhysicsComponentUtils.createSpherePhysicsComponent(sphereGo, 1.0, 10.0);
            try {
                sphereGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }
        }

        if (Input.Keys.NUM_3 == keycode) {
            final SceneAsset cylinderSceneAsset = mundus.getAssetManager().getGdxAssetManager().get("shapes/cylinder/cylinder.gltf");
            final GameObject cylinderGo = scene.sceneGraph.addGameObject(cylinderSceneAsset.scene.model, scene.cam.position);

            physicsComponent = Ode4jPhysicsComponentUtils.createCylinderPhysicsComponent(cylinderGo, 1, 2, 10.0);
            try {
                cylinderGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }
        }

        if (Input.Keys.NUM_4 == keycode) {
            final float radius = 0.75f;
            final float height = 3;

            final ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            final MeshPartBuilder meshPartBuilder = modelBuilder.part(
                    "part",
                    GL30.GL_TRIANGLES,
                    VertexAttributes.Usage.Position,
                    new Material(PBRColorAttribute.createBaseColorFactor(Color.CYAN)));
            CapsuleShapeBuilder.build(meshPartBuilder, radius, height, 30);
            final Model model = modelBuilder.end();

            rotateMesh(model);

            final GameObject capsuleGo = scene.sceneGraph.addGameObject(model, scene.cam.position);

            physicsComponent = Ode4jPhysicsComponentUtils.createCapsulePhysicsComponent(capsuleGo, radius, height, 10.0);
            try {
                capsuleGo.addComponent(physicsComponent);
            } catch (InvalidComponentException e) {
                throw new RuntimeException(e);
            }
        }

        if (physicsComponent != null) {
            MundusOde4jRuntimePlugin.getPhysicsWorld().getPhysicsComponents().add(physicsComponent);

            final Vector3 camDirection = scene.cam.direction;
            physicsComponent.getBody().setLinearVel(FORCE * camDirection.x, FORCE * camDirection.y, FORCE * camDirection.z);
        }
        return false;
    }

    // LibGDX cylinder is oriented along Y axis,  ODE4j on Z axis
    // rotate mesh to match ODE definition of a cylinder with the main axis on Z instead of Y
    // this hard-codes the rotation into the mesh so that we can later use transforms as normal.
    private static void rotateMesh(Model model){
        Vector3 v = new Vector3();
        Mesh mesh = model.meshes.first();
        int n = mesh.getNumVertices();
        int stride = mesh.getVertexSize() / 4;  // size of vertex in number of floats
        float [] vertices = new float[stride*n];
        mesh.getVertices(vertices);
        for(int i = 0 ; i < n; i++) {
            v.set(vertices[i*stride], vertices[i*stride+1], vertices[i*stride+2]);
            v.rotate(Vector3.X, 90);
            vertices[i*stride] = v.x;
            vertices[i*stride+1] = v.y;
            vertices[i*stride+2] = v.z;
        }
        mesh.setVertices(vertices);
    }

}
