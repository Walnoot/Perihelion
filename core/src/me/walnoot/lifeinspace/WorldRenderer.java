package me.walnoot.lifeinspace;

import me.walnoot.lifeinspace.components.EnemyComponent;
import me.walnoot.lifeinspace.components.HealthComponent;
import me.walnoot.lifeinspace.components.ItemComponent;
import me.walnoot.lifeinspace.components.PlayerComponent;
import me.walnoot.lifeinspace.components.ShipComponent;
import me.walnoot.lifeinspace.components.SpritesComponent;
import me.walnoot.lifeinspace.components.WarpgateComponent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StringBuilder;

public class WorldRenderer {
	private static final float SECTOR_SIZE = 40f;
	
	private final GameWorld world;
	
	private float[] randoms = new float[73];
	
	private Box2DDebugRenderer debug = new Box2DDebugRenderer();
	
	private SpriteBatch batch = new SpriteBatch();
	private ShapeRenderer shape = new ShapeRenderer();
	private OrthographicCamera camera = new OrthographicCamera();
	private OrthographicCamera uiCamera = new OrthographicCamera();
	
	private Array<Sprite> sprites = new Array<>();
	
	private Sprite starSprite, healthBar, healthBarFrame;
	private float healthBarU2;
	
	private Entity player;
	private float lastHealth = 0;
	
	private float shakeTimer;
	private float timer;
	
	private StringBuilder builder = new StringBuilder();
	private GlyphLayout layout = new GlyphLayout();
	
	private Vector2 tmp = new Vector2();
	
	public WorldRenderer(GameWorld world) {
		this.world = world;
		
		starSprite = world.getLoader().getAtlas().createSprite("star");
		healthBarFrame = world.getLoader().getAtlas().createSprite("healthbarframe");
		healthBar = world.getLoader().getAtlas().createSprite("healthbar");
		healthBarU2 = healthBar.getU2();
		
		for (int i = 0; i < randoms.length; i++) {
			randoms[i] = MathUtils.random();
		}
	}
	
	public void render(float warpTime, int difficulty) {
		timer += Gdx.graphics.getDeltaTime();
		boolean blink = timer % 1f > 0.5f;
		
		shakeTimer -= Gdx.graphics.getDeltaTime();
		if (shakeTimer < 0f) shakeTimer = 0f;
		
		player = null;
		world.forAllEntities((e) -> {
			if (e.has(PlayerComponent.class)) {
				if (e.has(HealthComponent.class)) {
					float health = e.get(HealthComponent.class).getHealth();
					if (health < lastHealth) shakeTimer = 1f;
					lastHealth = health;
				}
				
				camera.position.x = e.getX();
				camera.position.y = e.getY();
				
				camera.position.x += MathUtils.random() * shakeTimer;
				camera.position.y += MathUtils.random() * shakeTimer;
				
				player = e;
			}
		});
		
		camera.zoom = (float) (16.0 + Math.pow(1000, warpTime));
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		
		if (camera.zoom < 50f) renderBackground();
		
		sprites.clear();
		
		world.forAllEntities((e) -> {
			if (e.has(SpritesComponent.class)) {
				for (Sprite s : e.get(SpritesComponent.class).sprites) {
					s.setOrigin(s.getWidth() / 2f, s.getHeight() / 2f);
					s.setCenter(e.getX(), e.getY());
					s.setRotation(e.getBody().getAngle() * MathUtils.radiansToDegrees);
					
					sprites.add(s);
				}
			}
		});
		
		//sort sprites
		
		for (Sprite s : sprites) {
			s.draw(batch);
		}
		
		if (player != null) {
			PlayerComponent playerComponent = player.get(PlayerComponent.class);
			Entity nearestPickup = playerComponent.getNearestPickup();
			
			float sWidth = camera.viewportWidth * camera.zoom * 0.5f - 1f;
			float sHeight = camera.viewportHeight * camera.zoom * 0.5f - 1f;
			
			world.queryRadius(player.getBody().getPosition(), 30f, (e) -> {
				builder.length = 0;
				
				if (e.has(EnemyComponent.class)) {
					builder.append("[GRAY]Class ");
					builder.append((char) ('A' + difficulty - 1));
					builder.append(" enemy[]\n");
					builder.append("Hull: [RED][[");
					
					HealthComponent hComponent = e.get(HealthComponent.class);
					float health = hComponent.getHealth() / hComponent.getMaxHealth();
					
					builder.append((int) MathUtils.floor(health * 100f), 3);
					
					builder.append("%][]");
					
					layout.setText(Assets.font, builder);
				}
				
				if(e.has(ItemComponent.class) && e.getBody().getPosition().dst(camera.position.x, camera.position.y) < PlayerComponent.PICKUP_RADIUS) {
					Item item = e.get(ItemComponent.class).item;
					builder.append(item.name);
					builder.append("\n");
					
					for(Entry<Item.ItemTrait, Float> trait : item.traits) {
						builder.append(trait.key);
						builder.append(": ");
						trait.key.appendValue(builder, trait.value);
						builder.append("\n");
					}
					
					if(e == nearestPickup) builder.append("Press [GREEN]E[] to swap");
				}
				
				//relative pos to player
				tmp.set(e.getBody().getPosition()).sub(camera.position.x, camera.position.y);
				
				//make sure text pos is in screen bounds
				if (tmp.x > sWidth - layout.width) tmp.x = sWidth - layout.width;
				if (tmp.x < -sWidth) tmp.x = -sWidth;
				if (tmp.y > sHeight - layout.height - 1f) tmp.y = sHeight - layout.height - 1f;
				if (tmp.y < -sHeight) tmp.y = -sHeight;
				
				tmp.add(player.getBody().getPosition());
				
				Assets.font.draw(batch, builder, tmp.x, tmp.y + 3f);
			});
			
			builder.length = 0;
//			builder.append("[GRAY]Crew:[]\n");
//			
//			for (Task t : Task.values()) {
//				CrewMember member = player.get(ShipComponent.class).getMember(t);
//				
//				if (member != null) {
//					String name = t.toString();
//					builder.append(name);
//					for (int i = name.length(); i < 10; i++) {
//						builder.append(' ');
//					}
//					
//					builder.append("Lv. ");
//					builder.append(member.getLevel());
//					
//					builder.append(" [[");
//					builder.append(member.getHealth(), 3);
//					builder.append("/");
//					builder.append(member.getMaxHealth(), 3);
//					builder.append("]\n");
//				}
//			}
			
			builder.append("\n[GRAY]Ship Status:[]\n");
			for(Item item : player.get(ShipComponent.class).getItems()) {
				builder.append(item.itemClass.name);
				builder.append(": ");
				builder.append(item.name);
				builder.append("\n");
			}
			
			builder.append("\nCurrent Sector: [GREEN][");
			appendSector(player);
			builder.append("[]\n");
			
			world.forAllEntities((e) -> {
				if(e.has(WarpgateComponent.class)) {
					builder.append("Gate Sector: [BLUE][");
					appendSector(e);
					builder.append("[]\n");
					
					if(e.get(WarpgateComponent.class).isEnemyNearby()) {
						builder.append(blink ? "[RED]" : "[WHITE]");
						builder.append("Can't warp; enemies nearby[]");
					}
				}
			});
			
			Assets.font.draw(batch, builder, -sWidth + camera.position.x, sHeight + camera.position.y);
		}
		
		batch.setProjectionMatrix(uiCamera.combined);
		renderUI();
		
		batch.end();
		
//		debug.render(world.getBox2d(), camera.combined);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		shape.setProjectionMatrix(uiCamera.combined);
		shape.begin(ShapeType.Filled);
		shape.setColor(0f, 0f, 0f, warpTime);
		float uiWidth = uiCamera.viewportWidth;
		float uiHeight = uiCamera.viewportHeight;
		shape.rect(-uiWidth * 0.5f, -uiHeight * 0.5f, uiWidth, uiHeight);
		shape.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	private void appendSector(Entity e) {
		int sx = (int) MathUtils.floor(e.getX() / SECTOR_SIZE);
		int sy = (int) MathUtils.floor(e.getY() / SECTOR_SIZE);
		builder.append("[");
		if (sx >= 0) builder.append("+");
		builder.append(sx, 2);
		builder.append(",");
		if (sy >= 0) builder.append("+");
		builder.append(sy, 2);
		builder.append("]");
	}
	
	private void renderBackground() {
		float x1 = camera.position.x - camera.viewportWidth * camera.zoom;
		float x2 = camera.position.x + camera.viewportWidth * camera.zoom;
		float y1 = camera.position.y - camera.viewportHeight * camera.zoom;
		float y2 = camera.position.y + camera.viewportHeight * camera.zoom;
		
		for (int x = (int) MathUtils.floor(x1); x <= (int) MathUtils.ceil(x2); x++) {
			for (int y = (int) MathUtils.floor(y1); y <= (int) MathUtils.ceil(y2); y++) {
				if (x % 4 == 0 && y % 4 == 0) {
					float randx = randoms[Util.wrap(x + y * 20, randoms.length)];
					float randy = randoms[Util.wrap(x + y * 20 + 20, randoms.length)];
					float size = randoms[Util.wrap(x + y * 20 + 40, randoms.length)] * 0.5f + 0.2f;
					
					starSprite.setSize(size, size);
					starSprite.setOrigin(size / 2f, size / 2f);
					starSprite.setCenter(x + randx * 4f, y + randy * 4f);
					starSprite.setRotation(randx * 720f);
					starSprite.draw(batch);
				}
			}
		}
	}
	
	private void renderUI() {
		if (player != null && player.has(HealthComponent.class)) {
			float x = 26f / 256f;//distance of start of health bar to left border of the image
			HealthComponent health = player.get(HealthComponent.class);
			float ratio = MathUtils.lerp(x, 1f - x, (float) health.getHealth() / health.getMaxHealth());
			
			healthBar.setSize(.6f * ratio, .3f);
			healthBar.setPosition(-.3f, -1f);
			healthBar.setU2(healthBarU2 - (healthBarU2 - healthBar.getU()) * (1f - ratio));
			healthBar.draw(batch);
			
			healthBarFrame.setSize(.6f, .3f);
			healthBarFrame.setCenter(0f, -1f + healthBarFrame.getHeight() / 2f);
			healthBarFrame.draw(batch);
		}
	}
	
	public void resize(int width, int height) {
		camera.viewportHeight = 2f;
		camera.viewportWidth = 2f * width / height;
		camera.zoom = 16f;
		
		uiCamera.viewportHeight = 2f;
		uiCamera.viewportWidth = 2f * width / height;
		uiCamera.zoom = 1f;
	}
}
