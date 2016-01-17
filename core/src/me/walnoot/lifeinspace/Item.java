package me.walnoot.lifeinspace;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class Item {
	public String name;
	public ItemClass itemClass;
	
	public ObjectMap<ItemTrait, Float> traits = new ObjectMap<>();
	
	public static enum ItemClass {
		GUN("Gun"), ENGINE("Engine");
		
		public String name;

		private ItemClass(String name) {
			this.name = name;
		}
	}
	
	public static enum ItemTrait {
		GUN_DAMAGE, GUN_FIRERATE, ENGINE_FORCE;
		
		public void appendValue(StringBuilder b, float value) {
			if(value < 0f) {
				value = -value;
				b.append('-');
			}
			
			b.append((int) value);
			b.append(".");
			b.append((int) ((value % 1f) * 100), 2);
		}
	}
}
