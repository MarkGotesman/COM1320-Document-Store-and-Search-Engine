package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest  {
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}	
	MinHeap<MockComparable> heap;

	class MockComparable implements Comparable<MockComparable> {
		int i;
		String s;
		public MockComparable (int i, String s) {
			this.i = i;
			this.s = s;
		}
		@Override 
		public int compareTo (MockComparable mc) {
			return ((this.i + this.s.length()) - (mc.i + mc.s.length()));
		}
	}

	@BeforeEach
	void init() {
		heap = new MinHeapImpl<>();
	}

	@Test
	void reHeapify () {
		MockComparable mc1 = new MockComparable (9, "fuzzy");
		MockComparable mc2 = new MockComparable (10, "wuzzy");
		MockComparable mc3 = new MockComparable (1, "r");
		//Expected order (least to greatest): mc3, mc1, mc2

		heap.insert(mc1);
		heap.insert(mc2);
		heap.insert(mc3);

		assertEquals (mc3, heap.remove());
		assertEquals (mc1, heap.remove());
		assertEquals (mc2, heap.remove());
		//Once the ordering is verified, proceed to reinsert the MC's, modify ther satic fields, and then call reHeapify()

		heap.insert(mc1);
		heap.insert(mc2);
		heap.insert(mc3);	

		mc1.i = 5;
		mc1.s = "pie";
		heap.reHeapify(mc1);

		mc2.i = 7;
		mc2.s = "pies";
		heap.reHeapify(mc2);	

		mc3.i = 9;
		mc3.s ="tricky";
		heap.reHeapify(mc3);		
		//Expected order (least to greatest): mc1, mc2, mc3

		assertEquals (mc1, this.heap.remove());
		assertEquals (mc2, this.heap.remove());
		assertEquals (mc3, this.heap.remove());		

	}


}