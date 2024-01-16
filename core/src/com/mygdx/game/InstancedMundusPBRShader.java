package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShader;

public class InstancedMundusPBRShader extends MundusPBRShader {

    private final boolean instanced;

    public InstancedMundusPBRShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);
        instanced = renderable.meshPart.mesh.isInstanced();
    }

    @Override
    public boolean canRender(Renderable renderable) {
        return renderable.meshPart.mesh.isInstanced() == instanced && super.canRender(renderable);
    }
}
