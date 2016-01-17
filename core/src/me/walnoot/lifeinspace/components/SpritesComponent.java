package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class SpritesComponent extends Component {
	public final Array<Sprite> sprites = new Array<Sprite>();
	
	public void setColors(Color c) {
		for(Sprite s : sprites) {
			s.setColor(c);
		}
	}
}
