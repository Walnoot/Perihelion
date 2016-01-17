package me.walnoot.lifeinspace.components;

import me.walnoot.lifeinspace.Component;
import me.walnoot.lifeinspace.Entity;
import me.walnoot.lifeinspace.components.HealthComponent.HealthListener;

import com.badlogic.gdx.audio.Sound;

public class SoundComponent extends Component implements HealthListener {
	public Sound onCreate, onRemove, onHit;
	
	@Override
	public void newComponent(Component c) {
		if(c instanceof HealthComponent) {
			HealthComponent health = (HealthComponent) c;
			health.addListener(this);
		}
	}
	
	@Override
	public void onHit(float damage) {
		play(onHit);
	}
	
	@Override
	public void addTo(Entity e) {
		super.addTo(e);
		
		play(onCreate);
	}
	
	@Override
	public void onRemove() {
		play(onRemove);
	}
	
	private void play(Sound s) {
		if(s != null) s.play();
	}
}
