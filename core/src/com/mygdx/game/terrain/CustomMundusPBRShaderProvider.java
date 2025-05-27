package com.mygdx.game.terrain;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShaderProvider;
import com.mbrlabs.mundus.commons.terrain.TerrainMaterial;
import com.mbrlabs.mundus.commons.terrain.attributes.TerrainMaterialAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

public class CustomMundusPBRShaderProvider extends MundusPBRShaderProvider {
    public CustomMundusPBRShaderProvider(final PBRShaderConfig config) {
        super(config);
    }

    @Override
    protected PBRShader createPBRTerrainShader(Renderable renderable, PBRShaderConfig config, String prefix) {
        TerrainMaterialAttribute terrainMaterialA = (TerrainMaterialAttribute) renderable.material.get(TerrainMaterialAttribute.TerrainMaterial);
        TerrainMaterial terrainMaterial = terrainMaterialA.terrainMaterial;

        prefix += getTerrainPrefix(terrainMaterial);
        prefix += "#define splatLinesFlag\n";

        return new CustomPBRTerrainShader(renderable, config, prefix);
    }
}
