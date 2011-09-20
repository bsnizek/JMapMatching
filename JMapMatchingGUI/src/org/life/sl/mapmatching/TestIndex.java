package org.life.sl.mapmatching;

import gnu.trove.TIntProcedure;

import java.util.Properties;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.test.SpatialIndexFactory;

public class TestIndex {
	
	private SpatialIndex si;

	public TestIndex() {
		
		Properties p = new Properties();
	    p.setProperty("MinNodeEntries", "1");
	    p.setProperty("MaxNodeEntries", "10");
		si = SpatialIndexFactory.newInstance("rtree.RTree", p);
	}
	
	public void testAddData() {
		si.add(new Rectangle(0,0,10,10), 0);
		si.add(new Rectangle(20,20,30,30), 1);
	}
	
	public void testPoint1() {
		Point p = new Point(1,2);
		Return r = new Return();
		this.si.nearestN(p, r, 5, Float.POSITIVE_INFINITY);
	}

	public void testPoint2() {
		Point p = new Point(15,14);
		Return r = new Return();
		this.si.nearestN(p, r, 5, Float.POSITIVE_INFINITY);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestIndex ti = new TestIndex();
		ti.testAddData();
		ti.testPoint1();
		ti.testPoint2();
	}

}

class Return implements TIntProcedure {

	public boolean execute(int value) {
		System.out.println(value);
		return true;
	} 
}
