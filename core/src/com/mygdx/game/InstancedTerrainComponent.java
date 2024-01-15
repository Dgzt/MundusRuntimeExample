package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.components.Component;
import com.mbrlabs.mundus.commons.scene3d.components.CullableComponent;
import com.mbrlabs.mundus.commons.scene3d.components.RenderableComponent;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShaderProvider;
import com.mbrlabs.mundus.commons.utils.ShaderUtils;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class InstancedTerrainComponent extends CullableComponent implements RenderableComponent {

    private static final int INSTANCE_COUNT_SIDE = 5;
    private static final int INSTANCE_COUNT = INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE;

    private Matrix4 mat4 = new Matrix4();
    private Vector3 vec3Temp = new Vector3();
    private final Array<Renderable> renderables = new Array<>();
    private final RenderableProvider renderableProvider;

    public InstancedTerrainComponent(final GameObject go, final int maxNumBones, final Model model) {
        super(go);

        for(int i = 0 ; i < model.nodes.first().parts.size; i++) {
            final Node node = model.nodes.first();
            final NodePart nodePart = node.parts.get(i);

            final VertexAttributes vertexAttributes = nodePart.meshPart.mesh.getVertexAttributes();
            final int[] usages = new int[vertexAttributes.size()];
            for (int ii = 0; ii < vertexAttributes.size(); ++ii) {
                usages[ii] = vertexAttributes.get(ii).usage;
            }

            final Scene scene = go.sceneGraph.scene;

            final Renderable renderable = new Renderable();
            renderable.meshPart.set(new MeshPart(nodePart.meshPart));
            renderable.meshPart.id += "0";
            renderable.meshPart.mesh = renderable.meshPart.mesh.copy(true, false, usages);
            setupInstancedMesh(node.translation, node.rotation, node.scale, renderable.meshPart.mesh);
            renderable.material = nodePart.material;
            renderable.environment = scene.environment;
            renderable.worldTransform.idt();

            PBRShaderConfig config = ShaderUtils.buildPBRShaderConfig(maxNumBones);
            config.vertexShader = Gdx.files.internal("shaders/custom-gdx-pbr.vs.glsl").readString();
            config.fragmentShader = Gdx.files.internal("shaders/custom-gdx-pbr.fs.glsl").readString();
            config.prefix = "#define instanced\n";

            renderable.shader = new MundusPBRShaderProvider(config).getShader(renderable);

            renderables.add(renderable);
        }

        renderableProvider = new RenderableProvider() {
            @Override
            public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
                renderables.addAll(InstancedTerrainComponent.this.renderables);
            }
        };
    }

    @Override
    public RenderableProvider getRenderableProvider() {
        return renderableProvider;
    }

    @Override
    public Component clone(GameObject gameObject) {
        return null;
    }

    private void setupInstancedMesh(final Vector3 nodeTranslation, final Quaternion nodeRotation, final Vector3 nodeScale, final Mesh mesh) {
        // how to pass a Matrix4 to the shader (using 4 x Vec4 = 16 floats)
        mesh.enableInstancedRendering(true, INSTANCE_COUNT,
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 0),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 1),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 2),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 3));

        // Create offset FloatBuffer that will hold matrix4 for each instance to pass to shader
        final FloatBuffer offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 16); // 16 floats for mat4

        createBoxField(nodeTranslation, nodeRotation, nodeScale, offsets);

        ((Buffer)offsets).position(0);
        mesh.setInstanceData(offsets);
    }

    private void createBoxField(final Vector3 nodeTranslation, final Quaternion nodeRotation, final Vector3 nodeScale, final FloatBuffer buffer){
        for (int x = 1; x <= INSTANCE_COUNT_SIDE; x++) {
            for (int z = 1; z <= INSTANCE_COUNT_SIDE; z++) {
                // set instance position
                vec3Temp.set(
                        270f - x * 30f,
                        30f,
                        430f - z * 30f);

                // set random rotation
//					q.setEulerAngles(MathUtils.random(-90, 90), MathUtils.random(-90, 90), MathUtils.random(-90, 90));

                // create matrix transform
                mat4.set(nodeTranslation, nodeRotation, nodeScale);
                mat4.trn(vec3Temp);

                // put the 16 floats for mat4 in the float buffer
                buffer.put(mat4.getValues());
            }
        }
    }
}
