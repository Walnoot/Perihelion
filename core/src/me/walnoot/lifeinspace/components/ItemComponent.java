package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Item;
import me.walnoot.lifeinspace.Item.ItemClass;
import me.walnoot.lifeinspace.Item.ItemTrait;

public class ItemComponent extends Component {
	public Item item = new Item();
	
	public ItemComponent() {
		item.name = "Plasma Gun";
		item.itemClass = ItemClass.GUN;

		item.traits.put(ItemTrait.GUN_DAMAGE, 1f);
		item.traits.put(ItemTrait.GUN_FIRERATE, 1f);
	}
}
