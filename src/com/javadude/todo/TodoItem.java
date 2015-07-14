package com.javadude.todo;

public class TodoItem {
	private long id = -1;
	private String name;
	private String displayName;
	private int priority;
	
	TodoItem(String name, String displayName, int priority) {
		this.name = name;
		this.displayName = displayName;
		this.priority = priority;
	}
	TodoItem(long id, String name, String displayName, int priority) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.priority = priority;
	}
	
	public long getId() {
		return id;
	}
	void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	@Override
	public String toString() {
		return "TodoItem [name=" + name + ", displayName=" + displayName
				+ ", priority=" + priority + "]";
	}
}
