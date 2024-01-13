package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.assets.SkyboxAsset;
import com.mbrlabs.mundus.commons.assets.meta.MetaFileParseException;
import com.mbrlabs.mundus.commons.rendering.DefaultSceneRenderer;
import com.mbrlabs.mundus.commons.utils.LightUtils;
import com.mbrlabs.mundus.commons.utils.ShaderUtils;
import com.mbrlabs.mundus.runtime.Mundus;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

import java.nio.Buffer;
import java.nio.FloatBuffer;

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
		PLAYING
	}

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
		mundus = new InstancedMundus(Gdx.files.internal("MundusExampleProject"), config);
		try {
			mundus.getAssetManager().queueAssetsForLoading(true);
		} catch (MetaFileParseException e) {
			e.printStackTrace();
		}

		// Queuing up your own assets to include in asynchronous loading
		mundus.getAssetManager().getGdxAssetManager().load("beach.mp3", Music.class);

		mundus.getAssetManager().getGdxAssetManager().load("models/tree1.gltf", SceneAsset.class);
	}

	@Override
	public void render () {
		switch (gameState) {
			case LOADING:
				continueLoading();
				break;
			case PLAYING:
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
			PBRShaderConfig config = ShaderUtils.buildPBRShaderConfig(mundus.getAssetManager().maxNumBones);
			config.vertexShader = Gdx.files.internal("shaders/custom-gdx-pbr.vs.glsl").readString();
			config.fragmentShader = Gdx.files.internal("shaders/custom-gdx-pbr.fs.glsl").readString();

			scene = mundus.loadScene("Main Scene.mundus", config);
			scene.setSceneRenderer(new InstancedSceneRenderer());
			scene.cam.position.set(0, 40, 0);

			// setup input
			controller = new FirstPersonCameraController(scene.cam);
			controller.setVelocity(200f);
			Gdx.input.setInputProcessor(controller);

			//
			// reusable variables
			mat4 = new Matrix4();
			q = new Quaternion();
			vec3Temp = new Vector3();

			setupInstancedMesh();
			//

			// Update our game state
			gameState = GameState.PLAYING;

			// Retrieve custom asset we queued
			Music music = mundus.getAssetManager().getGdxAssetManager().get("beach.mp3");
			music.setVolume(0.05f);
			music.play();
		}
	}

	@Override
	public void dispose () {
		mundus.dispose();
	}

	private static final int INSTANCE_COUNT_SIDE = 5;
	private static final int INSTANCE_COUNT = INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE * INSTANCE_COUNT_SIDE;

	private FloatBuffer offsets;
	private ModelInstance renderable;
	private Quaternion q;
	private Matrix4 mat4;
	private Vector3 vec3Temp;

	private void setupInstancedMesh() {
		final SceneAsset treeAsset = mundus.getAssetManager().getGdxAssetManager().get("models/tree1.gltf");

		renderable = new ModelInstance(treeAsset.scene.model);

		// assumes the instance has one node,  and the meshPart covers the whole mesh
		for(int i = 0 ; i < renderable.nodes.first().parts.size; i++) {
			Mesh mesh = renderable.nodes.first().parts.get(i).meshPart.mesh;
			setupInstancedMesh(mesh);
		}
	}

	private void setupInstancedMesh(final Mesh mesh) {
		// how to pass a Matrix4 to the shader (using 4 x Vec4 = 16 floats)
		mesh.enableInstancedRendering(true, INSTANCE_COUNT,
				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 0),
				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 1),
				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 2),
				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 3));

		// Create offset FloatBuffer that will hold matrix4 for each instance to pass to shader
		offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 16); // 16 floats for mat4

		createBoxField();

		((Buffer)offsets).position(0);
		mesh.setInstanceData(offsets);
	}

	private void createBoxField(){
		for (int x = 1; x <= INSTANCE_COUNT_SIDE; x++) {
			for (int z = 1; z <= INSTANCE_COUNT_SIDE; z++) {
				// set instance position
				vec3Temp.set(
						270f - x * 30f,
						30f,
						430f - z * 30f);

				// set random rotation
//					q.setEulerAngles(MathUtils.random(-90, 90), MathUtils.random(-90, 90), MathUtils.random(-90, 90));

				// create matrix transform
				mat4.set(vec3Temp, q);

				// put the 16 floats for mat4 in the float buffer
				offsets.put(mat4.getValues());
			}
		}
	}

	private class InstancedSceneRenderer extends DefaultSceneRenderer {

		@Override
		protected void renderObjects(Scene scene) {
			super.renderObjects(scene);
			scene.batch.render(renderable, scene.environment);
		}
	}

}
