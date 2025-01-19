package com.mygdx.game;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.components.AbstractComponent;
import com.mbrlabs.mundus.commons.scene3d.components.Component;
import com.mbrlabs.mundus.commons.scene3d.components.RenderableComponent;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class InstancedTerrainComponent extends AbstractComponent implements RenderableComponent {

    private static final int INSTANCE_COUNT_SIDE = 5;
    private static final int INSTANCE_COUNT = INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE;

    private final Matrix4 mat4 = new Matrix4();
    private final Vector3 vec3Temp = new Vector3();
    private final RenderableProvider renderableProvider;

    public InstancedTerrainComponent(final GameObject go, final Model model) {
        super(go);

        final ModelInstance modelInstance = new ModelInstance(model);

        for(int i = 0 ; i < modelInstance.nodes.size; i++) {
            final Node node = modelInstance.nodes.get(i);
            for (int ii = 0; ii < node.parts.size; ++ii) {
                final NodePart nodePart = node.parts.get(ii);

                final VertexAttributes vertexAttributes = nodePart.meshPart.mesh.getVertexAttributes();
                final int[] usages = new int[vertexAttributes.size()];
                for (int iii = 0; iii < vertexAttributes.size(); ++iii) {
                    usages[iii] = vertexAttributes.get(iii).usage;
                }

                nodePart.meshPart.mesh = nodePart.meshPart.mesh.copy(true, false, usages);
                setupInstancedMesh(nodePart.meshPart.mesh);
            }
        }

        renderableProvider = new RenderableProvider() {
            @Override
            public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
                modelInstance.getRenderables(renderables, pool);
            }
        };
    }

    @Override
    public void update(float v) {
        // NOOP
    }

    @Override
    public RenderableProvider getRenderableProvider() {
        return renderableProvider;
    }

    @Override
    public Component clone(GameObject gameObject) {
        return null;
    }

    private void setupInstancedMesh(final Mesh mesh) {
        // how to pass a Matrix4 to the shader (using 4 x Vec4 = 16 floats)
        mesh.enableInstancedRendering(true, INSTANCE_COUNT,
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 0),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 1),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 2),
                new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 3));

        // Create offset FloatBuffer that will hold matrix4 for each instance to pass to shader
        final FloatBuffer offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 16); // 16 floats for mat4

        createBoxField(offsets);

        ((Buffer)offsets).position(0);
        mesh.setInstanceData(offsets);
    }

    private void createBoxField(final FloatBuffer buffer){
        for (int x = 1; x <= INSTANCE_COUNT_SIDE; x++) {
            for (int z = 1; z <= INSTANCE_COUNT_SIDE; z++) {
                // set instance position
                vec3Temp.set(
                        270f - x * 30f,
                        30f,
                        430f - z * 30f);

                // create matrix transform
                mat4.idt();
                mat4.setTranslation(vec3Temp);

                // put the 16 floats for mat4 in the float buffer
                buffer.put(mat4.tra().getValues());
            }
        }
    }
}
