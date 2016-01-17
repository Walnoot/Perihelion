package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.*;
import me.walnoot.lifeinspace.CrewMember.Task;
import me.walnoot.lifeinspace.Item.ItemClass;
import me.walnoot.lifeinspace.Item.ItemTrait;
import me.walnoot.lifeinspace.components.HealthComponent.HealthListener;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class ShipComponent extends Component implements HealthListener {
	public static final float BASE_TORQUE = 25f;
	public static final float BASE_FORCE = 25f;
	private static final float THRESHOLD_SPEED = 10f;
	
	private static final float CREW_HURT_CHANCE = 0.5f;
	private static final int CREW_DAMAGE_SPREAD = 2;
	private static final int CREW_BASE_DAMAGE = 10;
	
	private float forwardForce = BASE_FORCE;
	private float rotTorque = BASE_TORQUE;
	private float gunDamage = 1f;
	private float gunFireRate = 0.5f; //shots per second

	private Vector2 tmp1 = new Vector2();
	private Vector2 tmp2 = new Vector2();
	
	private float fireTimer;
	
	private boolean movForward, movLeft, movRight;
	
	private Array<Item> items = new Array<Item>();
	private ObjectMap<CrewMember.Task, CrewMember> crew = new ObjectMap<CrewMember.Task, CrewMember>();
	
	private Sound engine = Assets.sounds.get("sounds/engine");
	private long soundId = -1;
	
	public ShipComponent() {
		addMember(new CrewMember(Task.PILOT));
		addMember(new CrewMember(Task.GUNNER));
	}
	
	@Override
	public void update() {
		fireTimer += LifeInSpaceGame.DELTA;
		
		if(e.has(PlayerComponent.class)) {
			if(soundId == -1) {
				soundId = engine.loop();
			}
			
			if(movForward) engine.resume(soundId);
			else engine.pause(soundId);
		}
		
		if(e.has(SpritesComponent.class)) {
			SpritesComponent sprites = e.get(SpritesComponent.class);
			
			if(sprites.sprites.size >= 4) {
				sprites.sprites.get(1).setAlpha(movForward ? 1f : 0f);
				sprites.sprites.get(2).setAlpha(movRight ? 1f : 0f);
				sprites.sprites.get(3).setAlpha(movLeft ? 1f : 0f);

				movForward = false;
				movLeft = false;
				movRight = false;
			}
		}
		
		gunDamage = 1f;
		gunFireRate = 1f;
		forwardForce = BASE_FORCE;
		
		for(Item i : items) {
			gunDamage = i.traits.get(ItemTrait.GUN_DAMAGE, gunDamage);
			gunFireRate = i.traits.get(ItemTrait.GUN_FIRERATE, gunFireRate);
			forwardForce = i.traits.get(ItemTrait.ENGINE_FORCE, forwardForce);
		}
		
//		for(Task t : Task.values()) {
//			int level = crew.containsKey(t) ? crew.get(t).getLevel() : 0;
//			
//			switch (t) {
//			case GUNNER:
//				gunFireRate *= 1f + (0.125f * level);
//				break;
//			case PILOT:
//				forwardForce *= 1f + (0.125f * level);
//				break;
//			default:
//				break;
//			}
//		}
		
		Vector2 vel = body.getLinearVelocity();
		float damping = Math.max(0f, (vel.len() - THRESHOLD_SPEED) * .5f);
		
		body.applyForceToCenter(-vel.x * damping, -vel.y * damping, false);
	}
	
	@Override
	public void newComponent(Component c) {
		if(c instanceof HealthComponent) {
			((HealthComponent) c).addListener(this);
		}
	}
	
	@Override
	public void onHit(float damage) {
		for(CrewMember c : crew.values()) {
			if(MathUtils.randomBoolean(CREW_HURT_CHANCE)) {
				int min = CREW_BASE_DAMAGE - CREW_DAMAGE_SPREAD;
				int max = CREW_BASE_DAMAGE + CREW_DAMAGE_SPREAD;
				c.setHealth(c.getHealth() - MathUtils.random(min, max));
				
				//RIP
				if(c.getHealth() < 0) {
					crew.remove(c.getTask());
				}
			}
		}
	}
	
	public void addMember(CrewMember c) {
		crew.put(c.getTask(), c);
	}
	
	public CrewMember getMember(Task t) {
		return crew.get(t);
	}
	
	public void moveForward() {
		tmp1.set(0f, forwardForce).rotateRad(body.getAngle());
		body.applyForceToCenter(tmp1.x, tmp1.y, true);
		
		movForward = true;
	}
	
	public void moveBackward() {
		tmp1.set(0f, -forwardForce).rotateRad(body.getAngle());
		body.applyForceToCenter(tmp1.x, tmp1.y, true);
	}
	
	public void moveLeft() {
		body.applyTorque(rotTorque, true);
		
		movLeft = true;
	}
	
	public void moveRight() {
		body.applyTorque(-rotTorque, true);
		
		movRight = true;
	}
	
	public void fire() {
		if(fireTimer > 1f / gunFireRate) {
			fireTimer = 0f;
			
			Entity entity = world.addEntity("bullet");
			entity.get(BulletComponent.class).damage = gunDamage;
			
			tmp1.set(0f, 1f).rotateRad(body.getAngle());
			tmp2.set(0f, 0f).add(tmp1).scl(2.5f).add(body.getPosition());
			tmp1.scl(30f);
			
			entity.getBody().setTransform(tmp2, body.getAngle());
			entity.getBody().setLinearVelocity(tmp1);
		}
	}

	/**
	 * @param item - New item to be installed
	 * @return The item that was replaced, if any
	 */
	public Item equip(Item item) {
		Item current = null;
		for(Item i : items) {
			if(i.itemClass == item.itemClass) current = i;
		}
		
		items.removeValue(current, true);
		items.add(item);
		
		return current;
	}
	
	public Array<Item> getItems() {
		return items;
	}
	
	public Item getItem(ItemClass itemClass) {
		for(Item i : items) {
			if(i.itemClass == itemClass) return i;
		}
		
		return null;
	}
}
