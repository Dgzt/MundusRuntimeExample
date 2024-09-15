package com.mygdx.game;


import com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3VulkanApplication;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("MundusRuntimeExample");
		config.setWindowedMode(1280, 720);
		config.setBackBufferConfig(8,8,8,8,24,0,8);
		config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 0, 0);
		new Lwjgl3VulkanApplication(new MundusExample(), config);
	}
}
