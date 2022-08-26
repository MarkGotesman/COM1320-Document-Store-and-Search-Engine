package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;


import java.util.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class StackImplTest {

	Stack<Integer> stack;

	@BeforeEach
	void init() {
		stack = new StackImpl<>();	
	}


	@Test
	void pushTestSingle() {
		stack.push(3);
	}

	@Test
	void pushTestMany() {
		for (int i = 0; i < 10; i++) {
			stack.push(i);
		}
	}	

	@Test
	void popTestSingle() {
		stack.push(7);
		assertEquals(7, stack.pop());
	}

	@Test
	void popTestMany() {
		for (int i = 0; i < 10; i++) {
			stack.push(i);
		}
		for (int i = 9; i >= 0; i--) {
			assertEquals(i, stack.pop());
		}
		assertEquals(null, stack.pop());
	}	

	@Test
	void peekTest() {
		stack.push(5);
		stack.push(37);
		stack.push(3);
		assertEquals(3, stack.peek());
	}		

	@Test
	void emptyStack() {
		assertEquals(null, stack.pop());
		assertEquals (null, stack.peek());
		assertEquals(0, stack.size());
	}

	@Test
	void sizeTest() {
		for (int i = 0; i < 10; i++) {
			stack.push(i);
		}
		assertEquals(10, stack.size());		
	}	
}