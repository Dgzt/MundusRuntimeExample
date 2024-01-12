package com.mygdx.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.shaders.MundusPBRShaderProvider;
import com.mbrlabs.mundus.runtime.Mundus;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

public class InstancedMundus extends Mundus {
    public InstancedMundus(FileHandle mundusRoot) {
        super(mundusRoot);
    }

    public InstancedMundus(FileHandle mundusRoot, Config config) {
        super(mundusRoot, config);
    }

    @Override
    public Scene loadScene(String name, PBRShaderConfig config, DepthShader.Config depthConfig, RenderableSorter renderableSorter) {
        final Scene scene = super.loadScene(name, config, depthConfig, renderableSorter);
        scene.batch = new ModelBatch(new InstancedMundusPBRShaderProvider(config), renderableSorter);
        scene.depthBatch = new ModelBatch(new PBRDepthShaderProvider(depthConfig));
        return scene;
    }
}
