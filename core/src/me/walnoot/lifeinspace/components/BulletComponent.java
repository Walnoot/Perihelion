package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Assets;
import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Entity;
import me.walnoot.lifeinspace.LifeInSpaceGame;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;

public class BulletComponent extends Component {
	private static final float DESPAWN_TIME = 1.25f;
	private int timer;
	
	public float damage = 1f;
	
	private Sound sound = Assets.sounds.get("sounds/hit");
	
	@Override
	public void update() {
		if(timer++ > DESPAWN_TIME * LifeInSpaceGame.FPS) world.removeEntity(e);
	}
	
	@Override
	public void beginContact(Contact contact, Entity other) {
		//dont collide with sensors
		if(!(contact.getFixtureA().isSensor() || contact.getFixtureB().isSensor())) {
			if(other.has(HealthComponent.class)) {
				other.get(HealthComponent.class).hit(damage);
			}
			
			sound.play();
			
			world.removeEntity(e);
		}
	}
}
