package me.walnoot.lifeinspace.desktop;

import java.io.IOException;

import me.walnoot.lifeinspace.LifeInSpaceGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class LISLauncher {
	public static void main(String[] arg) throws IOException {
//		Array<String> assets = new Array<String>();
//		String assetDir = "../core/assets/";
//		Files.walk(Paths.get(assetDir)).filter((p) -> p.toFile().isFile())
//				.map((p) -> p.toString().replace(assetDir, "")).forEach((s) -> assets.add(s));
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 1600;
		config.height = 900;
		
		config.title = "Perihelion";
		
		new LwjglApplication(new LifeInSpaceGame(), config);
	}
}
