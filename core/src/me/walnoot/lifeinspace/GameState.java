package me.walnoot.lifeinspace;

import me.walnoot.lifeinspace.components.HealthComponent;
import me.walnoot.lifeinspace.components.PlayerComponent;
import me.walnoot.lifeinspace.components.ShipComponent;
import me.walnoot.lifeinspace.components.WarpgateComponent;
import walnoot.libgdxutils.State;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class GameState extends State {
	public static final int WARP_TIME = (int) LifeInSpaceGame.FPS * 2;
	
	private GameWorld world;
	private WorldRenderer renderer;
	
	private int warpTimer = WARP_TIME;

	private Entity player;
	private boolean playerDead;

	private int difficulty;
	
	private Sound warpSound = Assets.sounds.get("sounds/teleport");
	
	public GameState(PrototypeLoader loader) {
		this(loader, loader.createProto("player"), 1);
	}
	
	public GameState(PrototypeLoader loader, Entity player, int difficulty) {
		this.difficulty = difficulty;
		
		world = new GameWorld(loader);
		renderer = new WorldRenderer(world);

		float spawnX = MathUtils.random(-100f, 100f);
		float spawnY = MathUtils.random(-100f, 100f);
		
		float gateAngle = MathUtils.random(MathUtils.PI2);
		float gateDist = 300f + 25f * difficulty;
		
		float gateX = spawnX + MathUtils.sin(gateAngle) * gateDist;
		float gateY = spawnY + MathUtils.cos(gateAngle) * 300f;
		
		world.addEntity(player).getBody().setTransform(spawnX, spawnY, 0f);
		if(difficulty == 1) player.get(ShipComponent.class).equip(Util.getGun(difficulty));
		
		world.addEntity("warpgate").getBody().setTransform(gateX, gateY, 0f);

//		Entity gun = world.addEntity("gun");
//		gun.getBody().setTransform(spawnX, spawnY + 4f, 0f);
//		gun.get(ItemComponent.class).item = Util.getGun(difficulty);
		
		float enemyDensity = Math.max(40f, 120f - difficulty * 5f);
		
		for (int x = -10; x <= 10; x++) {
			for (int y = -10; y <= 10; y++) {
				if (!(x == 0 && y == 0)) {
					float rx = MathUtils.random(-enemyDensity / 8f, enemyDensity / 8f);
					float ry = MathUtils.random(-enemyDensity / 8f, enemyDensity / 8f);
					
					Entity enemy = world.addEntity("enemy");
					enemy.getBody().setTransform(x * enemyDensity + spawnX + rx, y * enemyDensity + spawnY + ry, 0f);
					enemy.get(ShipComponent.class).equip(Util.getGun(difficulty));
					float health = 0.5f + difficulty * 0.5f;
					enemy.get(HealthComponent.class).setMaxHealth(health);
				}
			}
		}
	}
	
	@Override
	public void render() {
		renderer.render(getWarpTime(), difficulty);
	}
	
	@Override
	public void update() {
		world.update();
		
		playerDead = true;
		//fix this when there are multiple warpgates future self
		world.forAllEntities((e) -> {
			if (e.has(WarpgateComponent.class)) {
				if (e.get(WarpgateComponent.class).isWarping()) {
					warpTimer++;
					if (warpTimer > WARP_TIME) warpTimer = WARP_TIME;
				} else {
					warpTimer--;
					if (warpTimer < 0) warpTimer = 0;
				}
			}
			
			if(e.has(PlayerComponent.class)) {
				player = e;
				playerDead = false;
			}
		});
		
		if(playerDead) {
			if(Gdx.input.isKeyJustPressed(Keys.SPACE)) manager.transitionTo(new GameState(world.getLoader()), 0.5f);
		}
		
		if (warpTimer == WARP_TIME) {
			world.forAllEntities((e) -> {
				if(e.has(PlayerComponent.class)) e.get(HealthComponent.class).heal(8f);
			});
			
			warpSound.play();
			
			manager.setState(new GameState(world.getLoader(), player, difficulty + 1));
		}
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		renderer.resize(width, height);
	}
	
	public float getWarpTime() {
		return (float) warpTimer / WARP_TIME;
	}
}
