package com.enavamaratha.enavamaratha.activity;

import java.util.ArrayList;

public class Group {
 
	private String Name;
	private long id;
	private ArrayList<Child> Items;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		this.Name = name;
	}
	public ArrayList<Child> getItems() {
		return Items;
	}
	public void setItems(ArrayList<Child> Items) {
		this.Items = Items;
	}
	
	
}
