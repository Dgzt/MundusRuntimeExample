package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.github.dgzt.mundus.plugin.recast.component.NavMeshAsset;
import com.github.dgzt.mundus.plugin.recast.component.RecastNavMeshComponent;
import com.github.dgzt.mundus.plugin.recast.debug.DebugRenderer;
import com.mbrlabs.mundus.commons.scene3d.SceneGraph;
import com.mbrlabs.mundus.commons.scene3d.components.Component;
import com.mbrlabs.mundus.commons.scene3d.components.TerrainComponent;

public class CustomInputController extends InputAdapter {

    private final DebugRenderer navMeshDebugRenderer;
    private final SceneGraph sceneGraph;
    private final Array<float[]> path;
    private final NavMeshAsset navMeshAsset;

    private Vector3 startPoint;

    public CustomInputController(final DebugRenderer navMeshDebugRenderer, final SceneGraph sceneGraph, final Array<float[]> path) {
        this.navMeshDebugRenderer = navMeshDebugRenderer;
        this.sceneGraph = sceneGraph;
        this.path = path;

        final RecastNavMeshComponent navMeshComponent = sceneGraph.findByName("Terrain 2").findComponentByType(Component.Type.NAVMESH);
        navMeshAsset = navMeshComponent.findNavMeshAssetByName("main");
    }

    @Override
    public boolean keyDown(final int keycode) {
        if (Input.Keys.G == keycode) {
            navMeshDebugRenderer.setEnabled(!navMeshDebugRenderer.isEnabled());
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        final Ray ray =sceneGraph.scene.cam.getPickRay(screenX, screenY);

        final TerrainComponent terrainComponent = sceneGraph.findByName("Terrain 2").findComponentByType(Component.Type.TERRAIN);

        final Vector3 intersection = terrainComponent.getRayIntersection(new Vector3(), ray);

        if (startPoint == null) {
            startPoint = intersection;
            path.clear();
        } else {
            navMeshAsset.getPath(startPoint, intersection, path);
            startPoint = null;
        }


        return true;
    }
}
