package me.walnoot.lifeinspace;

import me.walnoot.lifeinspace.components.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class PrototypeLoader {
	private ObjectMap<String, JsonValue> prototypes = new ObjectMap<>();
	private TextureAtlas atlas;
	private Json json = new Json();
	
	public PrototypeLoader(TextureAtlas atlas) {
		this.atlas = atlas;
		JsonValue protos = new JsonReader().parse(Gdx.files.internal("proto.json"));
		
		JsonValue proto = protos.child;
		while (proto != null) {
			prototypes.put(proto.name, proto);
			
			proto = proto.next;
		}
		
		json.setSerializer(Shape.class, new ReadOnlySerializer<Shape>() {
			@Override
			@SuppressWarnings("rawtypes")
			public Shape read(Json json, JsonValue jsonData, Class type) {
				if (jsonData.getString(0).equals("box")) {
					PolygonShape box = new PolygonShape();
					Vector2 pos = new Vector2();
					
					if(jsonData.get(4) != null) {
						pos.set(jsonData.getFloat(3), jsonData.getFloat(4));
					}
					
					box.setAsBox(jsonData.getFloat(1), jsonData.getFloat(2), pos, 0f);
					
					return box;
				} else if (jsonData.getString(0).equals("circle")) {
					CircleShape circle = new CircleShape();
					circle.setRadius(jsonData.getFloat(1));
					
					return circle;
				}
				
				return null;
			}
		});
		
		json.setSerializer(Sound.class, new ReadOnlySerializer<Sound>() {
			@Override
			@SuppressWarnings("rawtypes")
			public Sound read(Json json, JsonValue jsonData, Class type) {
				return Assets.sounds.get(jsonData.asString(), null);
			}
		});
	}
	
	public Entity createProto(String name) {
		Entity entity = new Entity();
		
		JsonValue component = prototypes.get(name).child;
		
		while (component != null) {
			switch (component.name) {
			case "player":
				entity.addComponent(new PlayerComponent());
				break;
			case "bullet":
				entity.addComponent(new BulletComponent());
				break;
			case "health":
				entity.addComponent(new HealthComponent(component.asInt()));
				break;
			case "enemy":
				entity.addComponent(new EnemyComponent());
				break;
			case "ship":
				entity.addComponent(json.readValue(ShipComponent.class, component));
				break;
			case "warpgate":
				entity.addComponent(new WarpgateComponent());
				break;
			case "item":
				entity.addComponent(new ItemComponent());
				break;
			case "sound":
				entity.addComponent(json.readValue(SoundComponent.class, component));
				break;
			case "bodyDef":
				BodyDef def = BodyDefComponent.getDefaultDef();
				json.readFields(def, component);
				
				entity.addComponent(new BodyDefComponent(def));
				
				break;
			case "fixtures":
				FixturesComponent fixtures = new FixturesComponent();
				
				JsonValue fixture = component.child;
				while (fixture != null) {
					FixtureDef fdef = FixturesComponent.getDefaultFixture();
					json.readFields(fdef, fixture);
					
					fixtures.fixtures.add(fdef);
					
					fixture = fixture.next;
				}
				
				entity.addComponent(fixtures);
				break;
			case "sprites":
				SpritesComponent spritesComponent = new SpritesComponent();
				
				JsonValue sprite = component.child;
				while (sprite != null) {
					Sprite s = atlas.createSprite(sprite.getString(0));
					s.setSize(sprite.getFloat(1), sprite.getFloat(2));
					
					spritesComponent.sprites.add(s);
					
					sprite = sprite.next;
				}
				
				entity.addComponent(spritesComponent);
				break;
			default:
				System.out.println("Unknown component type: " + component.name);
				break;
			}
			
			component = component.next;
		}
		
		return entity;
	}
	
	public TextureAtlas getAtlas() {
		return atlas;
	}
}
