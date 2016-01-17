package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Entity;

import com.badlogic.gdx.physics.box2d.Contact;

public class WarpgateComponent extends Component {
	private boolean inZone, enemyNearby;
	
	@Override
	public void update() {
		enemyNearby = false;
		
		if(inZone) {
			world.queryRadius(body.getPosition(), 50f, (e) -> {
				if(e.has(EnemyComponent.class)) enemyNearby = true;
			});
		}
	}
	
	@Override
	public void beginContact(Contact contact, Entity other) {
		if(hitsWarpzone(contact, other)) {
			inZone = true;
		}
	}
	
	@Override
	public void endContact(Contact contact, Entity other) {
		if(hitsWarpzone(contact, other)) {
			inZone = false;
		}
	}
	
	private boolean hitsWarpzone(Contact contact, Entity other) {
		boolean a = contact.getFixtureA().isSensor() && contact.getFixtureA().getBody() == body;
		boolean b = contact.getFixtureB().isSensor() && contact.getFixtureB().getBody() == body;
		
		return other.has(PlayerComponent.class) && (a || b);
	}
	
	public boolean isWarping() {
		return inZone && !enemyNearby;
	}
	
	public boolean isEnemyNearby() {
		return enemyNearby;
	}
}
