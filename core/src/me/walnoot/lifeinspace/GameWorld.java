package me.walnoot.lifeinspace;

import java.util.function.Consumer;

import me.walnoot.lifeinspace.components.BodyDefComponent;
import me.walnoot.lifeinspace.components.FixturesComponent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class GameWorld implements ContactListener {
	private final BodyDef defaultDef = BodyDefComponent.getDefaultDef();
	
	private World world = new World(new Vector2(), true);
	
	private Array<Body> tmpBodies = new Array<>();
	private Array<Entity> removedEntities = new Array<>(); //entities that will be removed after updating

	private PrototypeLoader loader;
	
	private RadiusSearcher searcher = new RadiusSearcher();
	
	public GameWorld(PrototypeLoader loader) {
		this.loader = loader;
		
		world.setContactListener(this);
	}

	public void update() {
		world.step(LifeInSpaceGame.DELTA, 8, 3);
		
		forAllEntities((e) -> e.update());
		
		for(Entity e : removedEntities) {
			e.onRemove();
			
			if(e.getBody() != null) world.destroyBody(e.getBody());
			e.setWorld(null, null);
		}
		
		removedEntities.size = 0;
	}
	
	public void forAllEntities(Consumer<Entity> c) {
		world.getBodies(tmpBodies);
		for (int i = 0; i < tmpBodies.size; i++) {
			c.accept((Entity) tmpBodies.get(i).getUserData());
		}
	}
	
	public void queryRadius(Vector2 pos, float radius, Consumer<Entity> c) {
		searcher.startSearch(pos, radius, c);
		world.QueryAABB(searcher, pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);
		searcher.endSearch();
	}
	
	public Entity addEntity(Entity e) {
		BodyDefComponent defComponent = e.get(BodyDefComponent.class);
		Body body = world.createBody(defComponent == null ? defaultDef : defComponent.def);
		body.setUserData(e);
		
		if (e.has(FixturesComponent.class)) {
			for (FixtureDef def : e.get(FixturesComponent.class).fixtures) {
				body.createFixture(def);
			}
		}
		
		e.setWorld(this, body);
		
		return e;
	}
	
	public Entity addEntity(String proto) {
		return addEntity(loader.createProto(proto));
	}
	
	public void removeEntity(Entity e) {
		removedEntities.add(e);
	}
	
	@Override
	public void beginContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		a.beginContact(contact, b);
		b.beginContact(contact, a);
	}
	
	@Override
	public void endContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		a.endContact(contact, b);
		b.endContact(contact, a);
	}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
	
	public World getBox2d() {
		return world;
	}
	
	public PrototypeLoader getLoader() {
		return loader;
	}
	
	private class RadiusSearcher implements QueryCallback {
		private Vector2 pos = null;
		private float radius;
		private Consumer<Entity> c;
		
		private void startSearch(Vector2 pos, float radius, Consumer<Entity> c) {
			if(this.pos != null) {
				throw new IllegalStateException("Already searching radius!");
			} else {
				this.pos = pos;
				this.radius = radius;
				this.c = c;
			}
		}
		
		private void endSearch() {
			pos = null;
			radius = 0f;
			c = null;
		}
		
		@Override
		public boolean reportFixture(Fixture fixture) {
			if(fixture.getBody().getPosition().dst2(pos) < radius * radius) {
				c.accept((Entity) fixture.getBody().getUserData());
			}
			
			return true;
		}
	}
}
