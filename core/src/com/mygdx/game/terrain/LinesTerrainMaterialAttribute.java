package com.mygdx.game.terrain;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;

public class LinesTerrainMaterialAttribute extends Attribute {
    public final static String TerrainLinesAlias = "terrainLinesData";
    public final static long TerrainLines = register(TerrainLinesAlias);

    private final Texture texture;

    protected LinesTerrainMaterialAttribute(long type, final Texture texture) {
        super(type);
        this.texture = texture;
    }

    public static LinesTerrainMaterialAttribute createLinesTerrainMaterialAttribute(final Texture texture) {
        return new LinesTerrainMaterialAttribute(TerrainLines, texture);
    }

    @Override
    public Attribute copy() {
        return new LinesTerrainMaterialAttribute(TerrainLines, texture);
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type) return type < o.type ? -1 : 1;
        LinesTerrainMaterialAttribute otherValue = ((LinesTerrainMaterialAttribute) o);
        return texture.equals(otherValue.getTexture()) ? 0 : -1;
    }

    public Texture getTexture() {
        return texture;
    }
}
