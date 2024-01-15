package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShader;

public class InstancedMundusPBRShader extends MundusPBRShader {
    public InstancedMundusPBRShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix + "#define instanced\n");
    }

    @Override
    public boolean canRender(Renderable renderable) {
        return super.canRender(renderable) && renderable.meshPart.mesh.isInstanced();
    }
}
