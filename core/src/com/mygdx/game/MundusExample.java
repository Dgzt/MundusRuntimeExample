package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mbrlabs.mundus.commons.Scene;
import com.mbrlabs.mundus.commons.assets.SkyboxAsset;
import com.mbrlabs.mundus.commons.assets.meta.MetaFileParseException;
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

//		texture.bind();
		scene.batch.begin(scene.cam);
		scene.batch.render(renderable, scene.environment);
		scene.batch.end();

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
			floatTemp = new float[16];

			// size of box, update last float (0.95f) to change size: 0.5f to 2.0f range is good
//			size = 1f / (float)Math.sqrt(INSTANCE_COUNT) * 0.95f;
			size = 10f;

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

	private Mesh mesh;
	private Texture texture;
	private FloatBuffer offsets;
//	private Renderable renderable;
	private ModelInstance renderable;
	private float size;
	private Quaternion q;
	private Matrix4 mat4;
	private Vector3 vec3Temp;
	private float[] floatTemp;

	private void setupInstancedMesh() {
		final SceneAsset treeAsset = mundus.getAssetManager().getGdxAssetManager().get("models/tree1.gltf");

		renderable = new ModelInstance(treeAsset.scene.model);

		// assumes the instance has one node,  and the meshPart covers the whole mesh
		for(int i = 0 ; i < renderable.nodes.first().parts.size; i++) {
			Mesh mesh = renderable.nodes.first().parts.get(i).meshPart.mesh;
			setupInstancedMesh(mesh);
		}

//		// Create a 3D cube mesh
//		mesh = new Mesh(true, 24, 36,
//				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
//				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords0")
//		);
//
//		// 24 vertices - one of the texture coordinates is flipped, but no big deal
//		float[] vertices = new float[] {
//				-size, size, -size, 0.0f, 1.0f,
//				size, size, -size, 1.0f, 1.0f,
//				size, -size, -size, 1.0f, 0.0f,
//				-size, -size, -size, 0.0f, 0.0f,
//				size, size, size, 1.0f, 1.0f,
//				-size, size, size, 0.0f, 1.0f,
//				-size, -size, size, 0.0f, 0.0f,
//				size, -size, size, 1.0f, 0.0f,
//				-size, size, size, 1.0f, 1.0f,
//				-size, size, -size, 0.0f, 1.0f,
//				-size, -size, -size, 0.0f, 0.0f,
//				-size, -size, size, 1.0f, 0.0f,
//				size, size, -size, 1.0f, 1.0f,
//				size, size, size, 0.0f, 1.0f,
//				size, -size, size, 0.0f, 0.0f,
//				size, -size, -size, 1.0f, 0.0f,
//				-size, size, size, 1.0f, 1.0f,
//				size, size, size, 0.0f, 1.0f,
//				size, size, -size, 0.0f, 0.0f,
//				-size, size, -size, 1.0f, 0.0f,
//				-size, -size, -size, 1.0f, 1.0f,
//				size, -size, -size, 0.0f, 1.0f,
//				size, -size, size, 0.0f, 0.0f,
//				-size, -size, size, 1.0f, 0.0f
//		};
//
//		// 36 indices
//		short[] indices = new short[]
//				{0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10, 11, 8, 12, 13,
//						14, 14, 15, 12, 16, 17, 18, 18, 19, 16, 20, 21, 22, 22, 23, 20 };
//
//		mesh.setVertices(vertices);
//		mesh.setIndices(indices);
//
//		// Thanks JamesTKhan for saving me hours: how to pass a Matrix4 to the shader (using 4 x Vec4 = 16 floats)
//		mesh.enableInstancedRendering(true, INSTANCE_COUNT,
//				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 0),
//				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 1),
//				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 2),
//				new VertexAttribute(VertexAttributes.Usage.Generic, 4, "i_worldTrans", 3));
//
//		// Create offset FloatBuffer that will hold matrix4 for each instance to pass to shader
//		offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 16); // 16 floats for mat4
//
//		createBoxField(); // regular box field
//
//		((Buffer)offsets).position(0);
//		mesh.setInstanceData(offsets);
//
//		renderable = new Renderable();
//		renderable.meshPart.set("Cube", mesh, 0, 36, GL20.GL_TRIANGLES); // 36 indices
//		renderable.environment = scene.environment;
//		renderable.worldTransform.idt();
//		renderable.shader = createShader(); // see method for more details
//		renderable.shader.init();
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
//		texture = new Texture(Gdx.files.internal("graphics/zebra.png")); // our mascot!

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

	/** See assets/shaders/instanced.vert + assets/shaders/instanced.frag files to see how:

	 a_position
	 a_texCoords0
	 i_worldTrans

	 vertex attributes are used to update each instance.

	 u_projViewTrans uniform needs to be set with camera.combined
	 so shader can calculate the updated position and rotation
	 */
	private BaseShader createShader() {
		return new BaseShader() {

			@Override
			public void begin(Camera camera, RenderContext context) {
				program.bind();
				program.setUniformMatrix("u_projViewTrans", camera.combined);
				program.setUniformi("u_texture", 0);
				context.setDepthTest(GL30.GL_LEQUAL);
			}

			@Override
			public void init () {
//				ShaderProgram.prependVertexCode = "#version 300 es\n";
//				ShaderProgram.prependFragmentCode = "#version 300 es\n";
				program = new ShaderProgram(Gdx.files.internal("shaders/instanced.vert"),
						Gdx.files.internal("shaders/instanced.frag"));
				if (!program.isCompiled()) {
					throw new GdxRuntimeException("Shader compile error: " + program.getLog());
				}
//				init(program, renderable);
			}

			@Override
			public int compareTo (Shader other) {
				return 0;
			}

			@Override
			public boolean canRender (Renderable instance) {
				return true;
			}
		};
	}
}
