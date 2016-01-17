package me.walnoot.lifeinspace;

import java.util.function.Consumer;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public final class Entity {
	private Array<Component> components = new Array<>();
	private IntMap<Component> map = new IntMap<>();
	private GameWorld world;
	private Body body;
	
	public void update() {
		forAllComponents((c) -> c.update());
	}
	
	public void addComponent(Component c) {
		if(get(c.getClass()) != null) {
			throw new IllegalStateException("This entity already contains a component of type " + c.getClass());
		} else {
			//notify other components that a new component was added
			//and let the new component know which components there are
			for (Component component : components) {
				component.newComponent(c);
				c.newComponent(component);
			}
			
			components.add(c);
			c.addTo(this);
			c.setWorld(world, body);
			map.put(c.getClass().hashCode(), c);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T get(Class<T> clazz) {
		return (T) map.get(clazz.hashCode(), null);
	}
	
	public boolean has(Class<? extends Component> clazz) {
		return get(clazz) != null;
	}
	
	public void forAllComponents(Consumer<Component> c) {
		for(int i = 0; i < components.size; i++) {
			c.accept(components.get(i));
		}
	}
	
	public void setWorld(GameWorld world, Body body) {
		this.world = world;
		this.body = body;
		
		forAllComponents((c) -> c.setWorld(world, body));
	}
	
	public void beginContact(Contact contact, Entity other) {
		forAllComponents((c) -> c.beginContact(contact, other));
	}
	
	public void endContact(Contact contact, Entity other) {
		forAllComponents((c) -> c.endContact(contact, other));
	}
	
	public void onRemove() {
		forAllComponents((c) -> c.onRemove());
	}
	
	public float getX() {
		return body.getPosition().x;
	}
	
	public float getY() {
		return body.getPosition().y;
	}
	
	public Body getBody() {
		return body;
	}
}
