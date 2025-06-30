package com.mygdx.game.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetFilter = new MundusAssetFilter();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();

        // Register any extra classpath assets here:
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/skybox.frag.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/skybox.vert.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/terrain.uber.frag.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/terrain.uber.vert.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/water.uber.frag.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/water.uber.vert.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/depth.frag.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/depth.vert.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/shadowmap.frag.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/shadowmap.vert.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/light.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/compat.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/custom-gdx-pbr.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/custom-gdx-pbr.vs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/pbr/pbr.vs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/pbr/pbr.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/mbrlabs/mundus/commons/shaders/pbr/material.glsl");

        // Register any classes or packages that require reflection here:
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.SceneDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.GameObjectDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.WaterComponentDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.TerrainComponentDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.BaseLightDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.LightComponentDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.CustomPropertiesComponentDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.DirectionalLightDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.ShadowSettingsDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.FogDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.dto.ModelComponentDTO");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.water.WaterResolution");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.shadows.ShadowResolution");
        TeaReflectionSupplier.addReflectionClass("com.mbrlabs.mundus.commons.env.lights.LightType");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        // You can uncomment the line below to use WASM instead of JavaScript as a target.
        // Some code can see very significant performance benefits from WASM, and some won't.
//        tool.setTargetType(TeaVMTargetType.WEBASSEMBLY_GC);
        tool.setMainClass(TeaVMLauncher.class.getName());
        // For many (or most) applications, using a high optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change ADVANCED to SIMPLE .
        tool.setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED);
        // The line below should use tool.setObfuscated(false) if you want clear debugging info.
        // You can change it to tool.setObfuscated(true) when you are preparing to release, to try to hide your original code.
        tool.setObfuscated(false);
        TeaBuilder.build(tool);
    }
}
