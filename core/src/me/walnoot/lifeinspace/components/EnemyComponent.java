package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Entity;
import me.walnoot.lifeinspace.Item.ItemClass;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class EnemyComponent extends Component {
	private static final float NORMAL_SEARCH_RADIUS = 40f;
	private static final float ALERT_SEARCH_RADIUS = 80f;
	
	private Vector2 tmp1 = new Vector2();
	private Vector2 tmp2 = new Vector2();
	
	float searchRadius = NORMAL_SEARCH_RADIUS;
	
	@Override
	public void update() {
		e.get(SpritesComponent.class).setColors(Color.GRAY);
		
		world.queryRadius(body.getPosition(), searchRadius, (e) -> {
			if(e.has(PlayerComponent.class)) {
				searchRadius = ALERT_SEARCH_RADIUS;
				
				tmp1.set(0, 1f).rotateRad(body.getAngle());
				tmp2.set(body.getPosition()).sub(e.getBody().getPosition()).nor();
				
				float angleDiff = tmp1.crs(tmp2);
				
				turnTowards(e, angleDiff);
				
				if(e.getBody().getPosition().dst2(body.getPosition()) > 15f * 15f) {
					moveTowards(e, angleDiff);
				}
				
				checkFire(e, angleDiff);
			}
		});
	}

	private void checkFire(Entity player, float angleDiff) {
		if(Math.abs(angleDiff * MathUtils.radiansToDegrees) < 20f) {
			e.get(ShipComponent.class).fire();
		}
	}

	private void moveTowards(Entity player, float angleDiff) {
		if(Math.abs(angleDiff * MathUtils.radiansToDegrees) < 20f) {
			e.get(ShipComponent.class).moveForward();
		}
	}

	private void turnTowards(Entity player, float angleDiff) {
		ShipComponent ship = e.get(ShipComponent.class);
		if(angleDiff > 0f && body.getAngularVelocity() > -1.5f * angleDiff) ship.moveRight();
		if(angleDiff < 0f && body.getAngularVelocity() < -1.5f * angleDiff) ship.moveLeft();
	}
	
	@Override
	public void onRemove() {
		if(MathUtils.randomBoolean(0.5f)) {
			Entity gun = world.addEntity("gun");
			gun.get(ItemComponent.class).item = e.get(ShipComponent.class).getItem(ItemClass.GUN);
			gun.getBody().setTransform(body.getPosition(), body.getAngle());
			gun.getBody().setLinearVelocity(body.getLinearVelocity());
			gun.getBody().setAngularVelocity(body.getAngularVelocity());
		}
	}
}
