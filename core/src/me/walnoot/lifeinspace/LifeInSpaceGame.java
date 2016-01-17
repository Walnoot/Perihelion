package me.walnoot.lifeinspace;

import java.util.Scanner;

import walnoot.libgdxutils.State;
import walnoot.libgdxutils.StateApplication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.utils.Array;

public class LifeInSpaceGame extends StateApplication {
	public static final boolean DEBUG = true;
	public static final float FPS = 60f;
	public static final float DELTA = 1f / FPS;
	
	private Array<String> assets;
	
	public LifeInSpaceGame() {
		super(FPS, DEBUG);
		
		this.assets = new Array<>();
	}

	@Override
	protected void init() {
		Scanner scanner = new Scanner(Gdx.files.internal("filelist").read());
		while(scanner.hasNextLine()) {
			assets.add(scanner.nextLine());
		}
		scanner.close();
		
		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 2, true);
		
		for (String file : assets) {
			if (file.endsWith(".png")) {
				packer.pack(file.substring(0, file.length() - 4), new Pixmap(Gdx.files.internal(file)));
			}
			
			if (file.endsWith(".wav")) {
				Assets.sounds.put(file.substring(0, file.length() - 4), Gdx.audio.newSound(Gdx.files.internal(file)));
			}
		}
		
		Assets.atlas = packer.generateTextureAtlas(TextureFilter.MipMapLinearLinear, TextureFilter.Linear, true);
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("VeraMono.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.genMipMaps = true;
		parameter.minFilter = TextureFilter.MipMapLinearLinear;
		parameter.magFilter = TextureFilter.Linear;
		parameter.size = 32;
		parameter.packer = packer;
		parameter.borderWidth = 2f;
		Assets.font = generator.generateFont(parameter);
		Assets.font.setUseIntegerPositions(false);
		Assets.font.getData().markupEnabled = true;
		Assets.font.getData().setScale(.75f / parameter.size);
		
		Box2D.init();
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		super.render();
	}
	
	@Override
	protected State getFirstState() {
		return new GameState(new PrototypeLoader(Assets.atlas));
	}
}
