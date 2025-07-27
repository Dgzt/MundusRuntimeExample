package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.JoltPhysicsPlugin;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.component.JoltPhysicsComponent;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.manager.BodyManager;
import com.github.dgzt.mundus.plugin.joltphysics.runtime.manager.ComponentManager;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.assets.SkyboxAsset;
import com.mbrlabs.mundus.commons.assets.meta.MetaFileParseException;
import com.mbrlabs.mundus.commons.scene3d.InvalidComponentException;
import com.mbrlabs.mundus.commons.scene3d.components.Component;
import com.mbrlabs.mundus.commons.scene3d.components.TerrainComponent;
import com.mbrlabs.mundus.commons.utils.LightUtils;
import com.mbrlabs.mundus.runtime.Mundus;
import jolt.gdx.DebugRenderer;
import jolt.physics.body.BodyManagerDrawSettings;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;

import static com.badlogic.gdx.Application.LOG_INFO;

public class MundusExample extends ApplicationAdapter {
	private FPSLogger fpsLogger;

	private Mundus mundus;
	private Scene scene;

	private GameState gameState = GameState.LOADING;

	private FirstPersonCameraController controller;
	private ShapeRenderer shapeRenderer;
	private final Color mundusTeal = new Color(0x00b695ff);

	private final float camFlySpeed = 20f;
	private Array<Vector3> cameraDestinations;
	private int currentCameraDestIndex = 0;
	private final Vector3 lookAtPos = new Vector3(800,0,800);

	enum GameState {
		LOADING,
        INIT_DEBUG_RENDERER,
		PLAYING
	}

    private DebugRenderer debugRenderer;
    private BodyManagerDrawSettings debugSettings;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();

		Gdx.app.setLogLevel(LOG_INFO);

		OrthographicCamera guiCamera = new OrthographicCamera();
		guiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(guiCamera.combined);

		cameraDestinations = new Array<>();
		cameraDestinations.add(new Vector3(100, 100, 100));
		cameraDestinations.add(new Vector3(1500, 100, 100));
		cameraDestinations.add(new Vector3(1500, 300, 1500));
		cameraDestinations.add(new Vector3(100, 300, 1500));

		Mundus.Config config = new Mundus.Config();
		config.autoLoad = false; // Do not autoload, we want to queue custom assets
		config.asyncLoad = true; // Do asynchronous loading

		// Start asynchronous loading
		mundus = new Mundus(Gdx.files.internal("MundusExampleProject"), config);
		try {
			mundus.getAssetManager().queueAssetsForLoading(true);
		} catch (MetaFileParseException e) {
			e.printStackTrace();
		}

		// Queuing up your own assets to include in asynchronous loading
		mundus.getAssetManager().getGdxAssetManager().load("beach.mp3", Music.class);

        JoltPhysicsPlugin.init((joltSuccess, exception) -> {
            Gdx.app.log("", "Jolt Physics loaded: " + joltSuccess);
            if (!joltSuccess) {
                Gdx.app.error("", "Jolt Physics can not load", exception);
            }
        });
	}

	@Override
	public void render () {
		switch (gameState) {
			case LOADING:
				continueLoading();
				break;
            case INIT_DEBUG_RENDERER:
                debugRenderer = new DebugRenderer(false);
                debugSettings = new BodyManagerDrawSettings();

                final CustomInputController customInputController = new CustomInputController(scene, debugRenderer);
                Gdx.input.setInputProcessor(new InputMultiplexer(customInputController, controller));

                gameState = GameState.PLAYING;
                break;
			case PLAYING:
                JoltPhysicsPlugin.update();

				play();
				break;
		}
	}

	private void play() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// How to change scenes
		if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
			scene.dispose();
			scene = mundus.loadScene("Second Scene.mundus");
			scene.cam.position.set(0, 40, 0);
			controller = new FirstPersonCameraController(scene.cam);
			controller.setVelocity(200f);
		}

		// How to change skybox and fog at runtime
		if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
			SkyboxAsset asset = (SkyboxAsset) mundus.getAssetManager().findAssetByFileName("night.sky");
			scene.setSkybox(asset, mundus.getShaders().getSkyboxShader());

			ColorAttribute colorAttribute = (ColorAttribute) scene.environment.get(ColorAttribute.Fog);
			colorAttribute.color.set(Color.BLACK);

			scene.environment.getAmbientLight().color.set(Color.DARK_GRAY);
			LightUtils.getDirectionalLight(scene.environment).intensity = 0.5f;

			FogAttribute fogAttribute = (FogAttribute) scene.environment.get(FogAttribute.FogEquation);
			fogAttribute.value.x = 100f; // Near plane
			fogAttribute.value.y = 500f; // Far plane
		}

		// Move camera towards current destination
		Vector3 dir = scene.cam.position.cpy().sub(cameraDestinations.get(currentCameraDestIndex)).nor();
		scene.cam.position.mulAdd(dir, -camFlySpeed * Gdx.graphics.getDeltaTime());

		scene.cam.lookAt(lookAtPos);
		scene.cam.up.set(Vector3.Y);

		// Update camera destination
		if (scene.cam.position.dst(cameraDestinations.get(currentCameraDestIndex) ) <= 2f) {
			currentCameraDestIndex++;
			if (currentCameraDestIndex >= cameraDestinations.size)
				currentCameraDestIndex = 0;
		}

		// Send camera back to beginning it if wanders off too far
		if (scene.cam.position.dst(cameraDestinations.get(0)) > 2500)
			scene.cam.position.set(cameraDestinations.get(0));

		controller.update();
		scene.sceneGraph.update();
		scene.render();
		fpsLogger.log();

        debugRenderer.begin(scene.cam);
        debugRenderer.DrawBodies(JoltPhysicsPlugin.getPhysicsSystem(), debugSettings);
        debugRenderer.end();
	}

	/**
	 * Continue loading mundus asynchronously
	 */
	private void continueLoading() {
		// Render progress bar
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(0f, Gdx.graphics.getHeight() * .5f, Gdx.graphics.getWidth(), 20f);
		shapeRenderer.setColor(mundusTeal);
		shapeRenderer.rect(0f, Gdx.graphics.getHeight() * .5f, mundus.getProgress() * Gdx.graphics.getWidth(), 20f);
		shapeRenderer.end();

		if (mundus.continueLoading()) {
			// Loading complete, load a scene.
			scene = mundus.loadScene("Main Scene.mundus");

			scene.cam.position.set(0, 40, 0);

			// setup input
			controller = new FirstPersonCameraController(scene.cam);
			controller.setVelocity(200f);
			Gdx.input.setInputProcessor(controller);

			// Update our game state
			gameState = GameState.INIT_DEBUG_RENDERER;

			// Retrieve custom asset we queued
			Music music = mundus.getAssetManager().getGdxAssetManager().get("beach.mp3");
			music.setVolume(0.05f);
			music.play();

            initializePhysics();
		}
	}

	@Override
	public void dispose () {
		mundus.dispose();
        debugRenderer.dispose();
        debugSettings.dispose();
        JoltPhysicsPlugin.dispose();
	}

    private void initializePhysics() {
        final BodyManager bodyManager = JoltPhysicsPlugin.getBodyManager();
        final ComponentManager componentManager = JoltPhysicsPlugin.getComponentManager();

        final TerrainComponent terrainComponent = scene.sceneGraph.getRoot().findComponentsByType(new Array<TerrainComponent>(), Component.Type.TERRAIN, true).first();

        final JoltPhysicsComponent joltPhysicsComponent = componentManager.createTerrainPhysicsComponent(terrainComponent.gameObject);

        try {
            terrainComponent.gameObject.addComponent(joltPhysicsComponent);
        } catch (InvalidComponentException e) {
            throw new RuntimeException(e);
        }
    }
}
