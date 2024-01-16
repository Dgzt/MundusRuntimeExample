package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShaderProvider;
import com.mbrlabs.mundus.commons.terrain.attributes.TerrainMaterialAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

public class InstancedMundusPBRShaderProvider extends MundusPBRShaderProvider {
    public InstancedMundusPBRShaderProvider(final PBRShaderConfig config) {
        super(config);
    }

    @Override
    protected PBRShader createShader(Renderable renderable, PBRShaderConfig config, String prefix) {
        if (renderable.material.has(TerrainMaterialAttribute.TerrainMaterial)) {
            return this.createPBRTerrainShader(renderable, config, prefix);
        } else {
            if (renderable.meshPart.mesh.isInstanced()) {
                prefix += "#define instanced\n";
            }

            return new InstancedMundusPBRShader(renderable, config, prefix);
        }
    }
}
