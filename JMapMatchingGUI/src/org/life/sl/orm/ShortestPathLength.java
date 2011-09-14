package org.life.sl.orm;

public class ShortestPathLength {
	private int id;
	private int fromnode;
	private int tonode;
	private double length;
	
	public double getLength() {
		return length;
	}

	public void setLength(double d) {
		this.length = d;
	}

	public ShortestPathLength() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFromnode() {
		return fromnode;
	}

	public void setFromnode(int i) {
		this.fromnode = i;
	}

	public int getTonode() {
		return tonode;
	}

	public void setTonode(int j) {
		this.tonode = j;
	}

	

}
