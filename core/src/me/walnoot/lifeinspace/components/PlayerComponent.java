package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Entity;
import me.walnoot.lifeinspace.Item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class PlayerComponent extends Component {
	public static final float PICKUP_RADIUS = 10f;
	
	private float nearestItemDst2;
	private Entity nearestItem;
	
	@Override
	public void update() {
		ShipComponent ship = e.get(ShipComponent.class);
		
		if(Gdx.input.isKeyPressed(Keys.W)) ship.moveForward();
//		if(Gdx.input.isKeyPressed(Keys.S)) ship.moveBackward();
		if(Gdx.input.isKeyPressed(Keys.A)) ship.moveLeft();
		if(Gdx.input.isKeyPressed(Keys.D)) ship.moveRight();

		if(Gdx.input.isKeyPressed(Keys.SPACE)) ship.fire();
		
		if(Gdx.input.isKeyJustPressed(Keys.E)) {
			Entity item = getNearestPickup();
			if(item != null) {
				Item newItem = item.get(ItemComponent.class).item;
				Item oldItem = ship.getItem(newItem.itemClass);
				ship.equip(newItem);
				
				if(oldItem == null) world.removeEntity(item);
				else item.get(ItemComponent.class).item = oldItem;
			}
		}
	}
	
	public Entity getNearestPickup() {
		nearestItemDst2 = Float.MAX_VALUE;
		nearestItem = null;
		
		world.queryRadius(body.getPosition(), PICKUP_RADIUS, (e) -> {
			if(e.has(ItemComponent.class)) {
				float dst2 = e.getBody().getPosition().dst2(body.getPosition());
				if(dst2 < nearestItemDst2) {
					nearestItemDst2 = dst2;
					nearestItem = e;
				}
			}
		});
		
		return nearestItem;
	}
}
