package me.walnoot.lifeinspace;

public class CrewMember {
	private Task task = Task.GUNNER;
	private int health = 100, maxHealth = 100;
	private int level = 1;
	
	public CrewMember() {
	}
	
	public CrewMember(Task task) {
		this.task = task;
	}

	public int getLevel() {
		return level;
	}
	
	public Task getTask() {
		return task;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public enum Task {
		GUNNER, PILOT, ENGINEER, MEDIC;
	}
}
