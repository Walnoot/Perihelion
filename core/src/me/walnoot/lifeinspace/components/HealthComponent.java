package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;

import com.badlogic.gdx.utils.Array;

public class HealthComponent extends Component {
	private float maxHealth, health;
	private Array<HealthListener> listeners = new Array<>();
	
	public HealthComponent(float maxHealth) {
		this.maxHealth = maxHealth;
		health = maxHealth;
	}
	
	private float add(float amount) {
		float oldHealth = health;
		health += amount;
		
		if(health <= 0) {
			health = 0;
			world.removeEntity(e);
		}
		
		if(health > maxHealth) {
			health = maxHealth;
		}
		
		return health - oldHealth;
	}
	
	public float hit(float damage) {
		System.out.printf("damage: %.2f, player=%b\n", damage, e.has(PlayerComponent.class));
		
		float actualDamage = -add(-damage);
		
		for(HealthListener l : listeners) {
			l.onHit(actualDamage);
		}
		
		return actualDamage;
	}
	
	public float heal(float amount) {
		return add(amount);
	}
	
	public void addListener(HealthListener l) {
		listeners.add(l);
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getMaxHealth() {
		return maxHealth;
	}
	
	public void setMaxHealth(float maxHealth) {
		this.maxHealth = maxHealth;
		health = maxHealth;
	}
	
	public static interface HealthListener {
		public void onHit(float damage);
	}
}
