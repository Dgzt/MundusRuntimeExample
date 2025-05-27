package com.mygdx.game.terrain;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.mbrlabs.mundus.commons.shaders.PBRTerrainShader;

public class CustomPBRTerrainShader extends PBRTerrainShader {

    public static class TerrainInputs {
        public final static Uniform linesTexture = new Uniform("u_texture_lines");
    }

    public static class TerrainSetters {
        public final static Setter linesTexture = new LocalSetter() {
            @Override
            public void set(final BaseShader shader, final int inputID, Renderable renderable, final Attributes combinedAttributes) {
                final LinesTerrainMaterialAttribute linesTerrainMaterialAttribute = (LinesTerrainMaterialAttribute) combinedAttributes.get(LinesTerrainMaterialAttribute.TerrainLines);
                textureDescription.texture = linesTerrainMaterialAttribute.getTexture();
                final int unit = shader.context.textureBinder
                        .bind(textureDescription);
                shader.set(inputID, unit);
            }
        };
    }

    public final int u_linesTexture;

    public CustomPBRTerrainShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);

        u_linesTexture = register(TerrainInputs.linesTexture, TerrainSetters.linesTexture);
    }
}
