package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import net.mgsx.gltf.scene3d.shaders.PBRCommon;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;

public class InstancedPBRDepthShaderProvider extends PBRDepthShaderProvider {
    public InstancedPBRDepthShaderProvider(DepthShader.Config config) {
        super(config);
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        PBRCommon.checkVertexAttributes(renderable);

        String prefix = DepthShader.createPrefix(renderable, config) + morphTargetsPrefix(renderable);
        if (renderable.meshPart.mesh.isInstanced()) {
            prefix += "#define instanced\n";
        }

        return new InstancedPBRDepthShader(renderable, config, prefix);
    }
}
