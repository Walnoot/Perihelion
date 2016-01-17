package me.walnoot.lifeinspace;

import me.walnoot.lifeinspace.Item.ItemClass;
import me.walnoot.lifeinspace.Item.ItemTrait;

import com.badlogic.gdx.math.MathUtils;

public class Util {
	public static int wrap(int n, int modulus) {
		return ((n % modulus) + modulus) % modulus;
	}
	
	public static Item getGun(float difficulty) {
		Item item = new Item();
		item.itemClass = ItemClass.GUN;
		
		float dps = 0.5f * difficulty;
		float firerate = 1f;
		
		switch (MathUtils.random(difficulty > 2 ? 2 : 1)) {
		case 0:
			item.name = "Auto-Cannon";
			firerate = 2f;
			break;
		case 1:
			item.name = "Plasma Gun";
			firerate = 1f;
			break;
		case 2:
			item.name = "Ion Torpedo";
			firerate = 0.5f;
			break;
		default:
			break;
		}
		
		float damage = (float) ((dps / firerate) * Math.pow(1.2, MathUtils.random(-1f, 1f)));
		
		item.traits.put(ItemTrait.GUN_DAMAGE, damage);
		item.traits.put(ItemTrait.GUN_FIRERATE, firerate);
		
		return item;
	}
}
