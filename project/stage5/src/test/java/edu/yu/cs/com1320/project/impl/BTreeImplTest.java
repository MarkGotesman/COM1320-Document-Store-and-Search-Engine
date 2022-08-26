package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import jakarta.xml.bind.DatatypeConverter;
import com.google.gson.*;

public class BTreeImplTest {
//---------------------------Debug------------------//
	final static boolean DEBUG = false;
  private static void print (Object str) {if (DEBUG) System.out.println(" * " + new Throwable().getStackTrace()[1] + ": " + str);}

//---------------------Class_Variables--------------//
	BTree bTree;

	URI uriText1;
	URI uriText2;
	URI uriText3;
	
	URI uriByte1;
	URI uriByte2;
	URI uriByte3;

	String text1;
	String text2;
	String text3;

	byte[] byte1;
	byte[] byte2;
	byte[] byte3;
	
	String dir;

	File[] orignialFileArray;

	DocumentPersistenceManager docPersistManager;

	Document textDoc1;	
	Document textDoc2;	
	Document textDoc3;	

	Document byteDoc1;
	Document byteDoc2;
	Document byteDoc3;

	File textDoc1File;
	File textDoc2File;
	File textDoc3File;

	File byteDoc1File;
	File byteDoc2File;
	File byteDoc3File;

	@BeforeEach
	void init () throws URISyntaxException {
		bTree = new BTreeImpl<URI, Document>();

		uriText1 = new URI("http://www.yu.org/documents/DocText1");
		uriText2 = new URI("http://www.yu.org/poems/DocText2");
		uriText3 = new URI("http://www.yu.org/documents/DocText3");

		uriByte1 = new URI("foo://example.org.8042/bar/DocByte1");
		uriByte2 = new URI("http://www.yu.org/poems/DocByte2");
		uriByte3 = new URI("foo://example.org.8042/baz/DocByte3");

		text1 = "Someone tells me that he could not think of a more creative string here...";
		text2 = "*^%$#$%^%R$DFGTRFDSWE#$R%TYHN E#$RTGYFD 3e$R%T^GFDS";
		text3 = "123456765 4565432 23456543 234543";

		Random rd = new Random();
		byte1 = "test".getBytes();
		byte2 = "test2".getBytes();
		byte3 = "test3".getBytes();

		textDoc1 = new DocumentImpl(uriText1, text1);
		textDoc2 = new DocumentImpl(uriText2, text2);
		textDoc3 = new DocumentImpl(uriText3, text3);

		byteDoc1 = new DocumentImpl (uriByte1, byte1);
		byteDoc2 = new DocumentImpl (uriByte2, byte2);
		byteDoc3 = new DocumentImpl (uriByte3, byte3);

		dir = "C:/Users/User/Documents/CS/COM1320_Data_Structures/Document_Store_and_Search_Engine_Semester_Project/Gotesman_Mark_2018243426/DataStructures/project/stage5";

		orignialFileArray = Paths.get(dir).toFile().listFiles();

		textDoc1File = new File(dir + "/" + "www.yu.org/documents/DocText1" + ".json");
		textDoc2File = new File(dir + "/" + "www.yu.org/poems/DocText2" + ".json");
		textDoc3File = new File(dir + "/" + "www.yu.org/documents/DocText3" + ".json");

		byteDoc1File = new File(dir + "/" + "example.org.8042/bar/DocByte1" + ".json");
		byteDoc2File = new File(dir + "/" + "www.yu.org/poems/DocByte2" + ".json");
		byteDoc3File = new File(dir + "/" + "example.org.8042/baz/DocByte3" + ".json");

		docPersistManager = new DocumentPersistenceManager(null); 		

		bTree.put(uriText1, textDoc1);
		bTree.put(uriText2, textDoc2);
		bTree.put(uriText3, textDoc3);
		bTree.put(uriByte1, byteDoc1);
		bTree.put(uriByte2, byteDoc2);
		bTree.put(uriByte3, byteDoc3);

		bTree.setPersistenceManager(docPersistManager);
	}

	@AfterEach 
	void cleanUp  () throws IOException {
		// Path textDoc1FilePath = Paths.get(textDoc1File.getPath());
		// Path textDoc2FilePath = Paths.get(textDoc2File.getPath());
		// Path textDoc3FilePath = Paths.get(textDoc3File.getPath());
		// Path byteDoc1FilePath = Paths.get(byteDoc1File.getPath());
		// Path byteDoc2FilePath = Paths.get(byteDoc2File.getPath());
		// Path byteDoc3FilePath = Paths.get(byteDoc3File.getPath());


		// System.out.println(" * " + new Throwable().getStackTrace()[1] + ": " +
		// 	"Cleaning up. Status of Deletion: \n" + 
		// 	"textDoc1File: " + Files.deleteIfExists(textDoc1FilePath) + 
		// 	". textDoc2File: " + Files.deleteIfExists(textDoc2FilePath) + 
		// 	". textDoc3File: " + Files.deleteIfExists(textDoc3FilePath) + 
		// 	". byteDoc1File: " + Files.deleteIfExists(byteDoc1FilePath) + 
		// 	". byteDoc2File: " + Files.deleteIfExists(byteDoc2FilePath) + 
		// 	". byteDoc3File: " + Files.deleteIfExists(byteDoc3FilePath) 
		// 	);
		this.restoreOriginalDirectory(Paths.get(dir).toFile(), orignialFileArray);
	}

//----------------Tests_With_Persistance--------//

	@Test
	void moveTextToDisk () throws Exception {
		bTree.moveToDisk(uriText1);
		bTree.moveToDisk(uriText2);
		bTree.moveToDisk(uriText3);
		bTree.moveToDisk(uriByte1);
		bTree.moveToDisk(uriByte2);
		bTree.moveToDisk(uriByte3);		
	}

	@Test
	void moveTextToDiskAndGet () throws Exception {
		bTree.moveToDisk(uriText2);
		assertEquals (textDoc2, bTree.get(uriText2));
		
	}

	@Test
	void moveByteToDiskAndGet () throws Exception {
		bTree.moveToDisk(uriByte3);		
		assertEquals (byteDoc3, bTree.get(uriByte3));
	}

	@Test
	void moveAllToDiskAndGet () throws Exception {
		bTree.moveToDisk(uriText1);
		bTree.moveToDisk(uriText2);
		bTree.moveToDisk(uriText3);
		bTree.moveToDisk(uriByte1);
		bTree.moveToDisk(uriByte2);
		bTree.moveToDisk(uriByte3);

		assertEquals (textDoc1, bTree.get(uriText1));
		assertEquals (textDoc2, bTree.get(uriText2));
		assertEquals (textDoc3, bTree.get(uriText3));
		assertEquals (byteDoc1, bTree.get(uriByte1));
		assertEquals (byteDoc2, bTree.get(uriByte2));
		assertEquals (byteDoc3, bTree.get(uriByte3));
	}

	@Test
	void putNullDelete_Memory () {//check that a put returns the prev value, and that put with a null value is a delete
		assertEquals(textDoc1, bTree.put(uriText1, null));
		assertEquals(null, bTree.get(uriText1));
	}


	@Test
	void putNullDelete_Disk () throws Exception {//check that a put returns the prev value, and that put with a null value is a delete
		bTree.moveToDisk(uriText1);		
		assertEquals(textDoc1, bTree.put(uriText1, null));
		assertEquals(null, bTree.get(uriText1));
	}	

	@Test
	void setNewPersistenceManager () throws Exception {
		
		dir = "C:/Users/User/Documents/CS/COM1320_Data_Structures/Document_Store_and_Search_Engine_Semester_Project/Gotesman_Mark_2018243426/DataStructures/project/stage5/src";
		orignialFileArray = Paths.get(dir).toFile().listFiles();	
		docPersistManager = new DocumentPersistenceManager(new File(dir));
		textDoc1File = new File(dir + "/" + "www.yu.org/documents/DocText1" + ".json");
		textDoc2File = new File(dir + "/" + "www.yu.org/poems/DocText2" + ".json");
		textDoc3File = new File(dir + "/" + "www.yu.org/documents/DocText3" + ".json");
		byteDoc1File = new File(dir + "/" + "example.org.8042/bar/DocByte1" + ".json");
		byteDoc2File = new File(dir + "/" + "www.yu.org/poems/DocByte2" + ".json");
		byteDoc3File = new File(dir + "/" + "example.org.8042/baz/DocByte3" + ".json");

		bTree.setPersistenceManager(docPersistManager);

		bTree.put(uriText1, textDoc1);
		bTree.put(uriText2, textDoc2);
		bTree.put(uriText3, textDoc3);
		bTree.put(uriByte1, byteDoc1);
		bTree.put(uriByte2, byteDoc2);
		bTree.put(uriByte3, byteDoc3);

		bTree.moveToDisk(uriText1);
		bTree.moveToDisk(uriText2);
		bTree.moveToDisk(uriText3);
		bTree.moveToDisk(uriByte1);
		bTree.moveToDisk(uriByte2);
		bTree.moveToDisk(uriByte3);

		assertEquals (textDoc1, bTree.get(uriText1));
		assertEquals (textDoc2, bTree.get(uriText2));
		assertEquals (textDoc3, bTree.get(uriText3));
		assertEquals (byteDoc1, bTree.get(uriByte1));
		assertEquals (byteDoc2, bTree.get(uriByte2));
		assertEquals (byteDoc3, bTree.get(uriByte3));		

		this.restoreOriginalDirectory(Paths.get(dir).toFile(), orignialFileArray);
	}
//--------------General_Tests-----------------------//
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

	@Test
	void testComparables () {
		bTree = new BTreeImpl<MockComparable, Integer>();		
		MockComparable mc1 = new MockComparable (9, "fuzzy");
		MockComparable mc2 = new MockComparable (10, "wuzzy");
		MockComparable mc3 = new MockComparable (1, "r");

		bTree.put (mc1, 21345);
		bTree.put (mc2, 34543);
		bTree.put (mc3, 34765);

		assertEquals (34543, bTree.get(mc2));
		assertEquals (21345, bTree.get(mc1));
		assertEquals (34765, bTree.get(mc3));
	}

//----------------Legacy_Tests_From_Hash_Table-----------------------//
	@Test
	void manyEntriesExpansion () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(1,100);
		bTreeHashTests.put(2,200);
		bTreeHashTests.put(3,300);
		bTreeHashTests.put(4,400);
		bTreeHashTests.put(5,500);
		bTreeHashTests.put(6,600);
		bTreeHashTests.put(7,700);
		bTreeHashTests.put(8,800);
		bTreeHashTests.put(9,900);
		bTreeHashTests.put(10,1000);
		bTreeHashTests.put(11,1100);

		assertEquals(100, bTreeHashTests.get(1));
		assertEquals(400, bTreeHashTests.get(4));
		assertEquals(1100, bTreeHashTests.get(11));
	}


	@Test
	void putManyValues  () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		for (int i = 1, j = 1; i < 20000; i++, j++) {
			bTreeHashTests.put(i,j);		
		}
		for (int i = 1, j = 1; i < 20000; i++, j++) {
			assertEquals(j, bTreeHashTests.get(i));		
		}

	}

	@Test
	void putKeyNullValueNull () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(null, null);		
		assertEquals(bTreeHashTests.get(null), null);
	}

	@Test
	void returnNullNonExistentValue () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		assertEquals(null, bTreeHashTests.get(1));	
	}
	@Test
	void putKeyIntValueNull () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(5, null);		
		assertEquals(null, bTreeHashTests.get(5));
	}

	@Test 
	void putKeyIntInt () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		assertEquals(null, bTreeHashTests.put(1, 2));
		assertEquals(2, bTreeHashTests.get(1));
	}

	@Test 
	void putKeyDeleteKey () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(1, 2);
		bTreeHashTests.put(2, 4);
		assertEquals(2, bTreeHashTests.get(1));
		bTreeHashTests.put(1, null);
		assertEquals(null, bTreeHashTests.get(1));
	}	

	@Test 
	void putNewKeyReturnOldValue () {
		BTree<Integer, Integer> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(1, 2);
		assertEquals(2, bTreeHashTests.get(1));
		assertEquals(2, bTreeHashTests.put(1, 7));
	}	

	@Test 
	void putKeyCharChar () {
		BTree<Character, Character> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put('A', 'B');
		assertEquals('B', bTreeHashTests.get('A'));
	}

	@Test 
	void putKeyStringString () {
		BTree<String, String> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put("Hi", "how are you today?");
		assertEquals("how are you today?", bTreeHashTests.get("Hi"));		
	}

	@Test 
	void putKeyIntValueString () {
		BTree<Integer, String> bTreeHashTests = new BTreeImpl<>();
		bTreeHashTests.put(7, "Cheese");
		assertEquals("Cheese", bTreeHashTests.get(7));
	}


//----------------Helper_Metods----------------//
	void restoreOriginalDirectory  (File currentDirctory, File[] oldFileArray) throws IOException {
		File[] newFileArray = currentDirctory.listFiles();
		List<File> oldFileList = Arrays.asList(oldFileArray);
		print ("Deleting directories");
		for (File file : newFileArray) {
			print ("Checking file: " + file + ". Contained in original array: " + oldFileList.contains(file));
			if (!(oldFileList.contains(file))) {
				print ("Deleted Directory on Cleanup: " + file);
				this.deleteDirectory(file);
			}
		}

	}

	//Code credit to: Programiz, https://www.programiz.com/java-programming/examples/delete-directory
	void deleteDirectory (File directory) {

    // if the file is directory or not
    if(directory.isDirectory()) {
      File[] files = directory.listFiles();

      // if the directory contains any file
      if(files != null) {
        for(File file : files) {

          // recursive call if the subdirectory is non-empty
          deleteDirectory(file);
        }
      }
    }

    if(directory.delete()) {
      print(directory + " is deleted");
    }
    else {
      print("Directory not deleted");
    }
  }


}