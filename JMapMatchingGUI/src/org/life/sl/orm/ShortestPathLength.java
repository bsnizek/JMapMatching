package org.life.sl.orm;

public class ShortestPathLength {
	private int id;
	private int fromnode;
	private int tonode;
	private double length;
	
	public ShortestPathLength() {
		this(0, 0, 0.);
	}

	public ShortestPathLength(int from, int to, double len) {
		// TODO Auto-generated constructor stub
		fromnode = from;
		tonode = to;
		length = len;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double d) {
		this.length = d;
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
