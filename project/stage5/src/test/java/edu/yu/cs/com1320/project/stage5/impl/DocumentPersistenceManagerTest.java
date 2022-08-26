package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

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

//user.dir: C:/Users/User/Documents/CS/COM1320_Data_Structures/Document_Store_and_Search_Engine_Semester_Project/Gotesman_Mark_2018243426/DataStructures/project/stage5
public class DocumentPersistenceManagerTest {
	final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(" * " + new Throwable().getStackTrace()[1] + ": " + str);}

	URI uriText1;
	URI uriText2;
	URI uriText3;
	
	URI uriByte1;
	URI uriByte2;
	URI uriByte3;

	String text1;
	String text2;
	String text3;

	byte[] byte1 = new byte[410];
	byte[] byte2 = new byte[43];
	byte[] byte3 = new byte[5];
	
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
	void init() throws IOException, URISyntaxException {
		uriText1 = new URI("http://www.yu.edu/documents/DocText1");
		uriText2 = new URI("http://www.yu.edu/poems/DocText2");
		uriText3 = new URI("http://www.yu.edu/documents/DocText3");

		uriByte1 = new URI("foo://example.com.8042/bar/DocByte1");
		uriByte2 = new URI("http://www.yu.edu/poems/DocByte2");
		uriByte3 = new URI("foo://example.com.8042/baz/DocByte3");

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

		textDoc1File = new File(dir + "/" + "www.yu.edu/documents/DocText1" + ".json");
		textDoc2File = new File(dir + "/" + "www.yu.edu/poems/DocText2" + ".json");
		textDoc3File = new File(dir + "/" + "www.yu.edu/documents/DocText3" + ".json");

		byteDoc1File = new File(dir + "/" + "example.com.8042/bar/DocByte1" + ".json");
		byteDoc2File = new File(dir + "/" + "www.yu.edu/poems/DocByte2" + ".json");
		byteDoc3File = new File(dir + "/" + "example.com.8042/baz/DocByte3" + ".json");

		docPersistManager = new DocumentPersistenceManager(null); 
	}

	@AfterEach 
	void cleanUp () throws IOException {
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

/*NOTES:
- Make test for deleting a serialized doc - test what happens if it is called on a doc that is not serialized, i.e. that file does not exist. 


*/

//======================================================================Text=================================================================================================//
	@Test
	void textFileCreation() throws IOException {
		this.serializeTextDocuments();
		//Assert that the file was created and written to
		assertTrue(textDoc1File.exists());
		assertTrue(textDoc2File.exists());
		assertTrue(textDoc3File.exists());

		assertNotEquals(0, textDoc1File.length());
		assertNotEquals(0, textDoc2File.length());
		assertNotEquals(0, textDoc3File.length());
	}

	@Test
	void textFileChangedDirectory() throws IOException {
		dir = "C:/Users/User/Documents/CS/COM1320_Data_Structures/Document_Store_and_Search_Engine_Semester_Project/Gotesman_Mark_2018243426/DataStructures/project/stage5/src/test";
		orignialFileArray = Paths.get(dir).toFile().listFiles();	

		docPersistManager = new DocumentPersistenceManager(new File(dir));
		this.serializeTextDocuments();
		textDoc1File = new File(dir + "/" + "www.yu.edu/documents/DocText1" + ".json");
		textDoc2File = new File(dir + "/" + "www.yu.edu/poems/DocText2" + ".json");
		textDoc3File = new File(dir + "/" + "www.yu.edu/documents/DocText3" + ".json");

		//Assert that the file was created and written to
		assertTrue(textDoc1File.exists());
		assertTrue(textDoc2File.exists());
		assertTrue(textDoc3File.exists());

		assertNotEquals(0, textDoc1File.length());
		assertNotEquals(0, textDoc2File.length());
		assertNotEquals(0, textDoc3File.length());
		this.restoreOriginalDirectory(Paths.get(dir).toFile(), orignialFileArray);		
	}

	@Test
	void textJsonCorrectMembers() throws IOException {
		this.serializeTextDocuments();		
		//Order of expected serialization (going off the order on the spec, though it is arbitrary): 
		//1. Content; 2. URI; 3. Wordcount Map.

		//textDoc1
		String[] textDoc1JsonStringArr = Files.readString(textDoc1File.toPath()).split("\"");
		assertEquals (textDoc1.getDocumentTxt(), textDoc1JsonStringArr[3]);
		assertEquals (textDoc1.getKey().toString(), textDoc1JsonStringArr[7]);

		//textDoc2
		String[] textDoc2JsonStringArr = Files.readString(textDoc2File.toPath()).split("\"");
		assertEquals (textDoc2.getDocumentTxt(), textDoc2JsonStringArr[3]);
		assertEquals (textDoc2.getKey().toString(), textDoc2JsonStringArr[7]);

		//textDoc3
		String[] textDoc3JsonStringArr = Files.readString(textDoc3File.toPath()).split("\"");
		assertEquals (textDoc3.getDocumentTxt(), textDoc3JsonStringArr[3]);
		assertEquals (textDoc3.getKey().toString(), textDoc3JsonStringArr[7]);

		//Check that "last use time" is NOT serialized, i.e. that there is no 4th member of the arrary that is split at ","
		assertFalse(Files.readString(textDoc1File.toPath()).contains("lastUseTime"));
		assertFalse(Files.readString(textDoc2File.toPath()).contains("lastUseTime"));
		assertFalse(Files.readString(textDoc3File.toPath()).contains("lastUseTime"));
	}

	@Test
	void textDesirialization_GSON() throws IOException {
		Gson gson = new Gson();
		String jsonStringGSON = gson.toJson(textDoc1);
		print (jsonStringGSON);

		this.serializeTextDocuments();				
		//Deserialize the Documents from the Json file that was stored via the Gson API (assuming that custome deserializaion does not fundamentally chage the deafult implementation)  
		Document textDoc1JsonDeserialized = gson.fromJson(Files.readString(textDoc1File.toPath()), DocumentImpl.class);
		Document textDoc2JsonDeserialized = gson.fromJson(Files.readString(textDoc2File.toPath()), DocumentImpl.class);
		Document textDoc3JsonDeserialized = gson.fromJson(Files.readString(textDoc3File.toPath()), DocumentImpl.class);

		//Check that the fields of the newly deserialized documents match that of the original corresponding doc. 
		//documentLastUseTime should be 0 until it is set by DocumentStoreImpl upon a call to "get" in the BTree.

		print (textDoc1JsonDeserialized.getDocumentTxt());
		print (textDoc1.getDocumentTxt());

		print (textDoc1JsonDeserialized.getKey());
		print (textDoc1.getKey());

		print (textDoc1.getWordMap());
		print (textDoc1JsonDeserialized.getWordMap());

		//textDoc1
		assertEquals(textDoc1JsonDeserialized.getDocumentTxt(), text1);
		assertEquals(textDoc1JsonDeserialized.getKey(), uriText1);
		assertEquals(textDoc1JsonDeserialized.getWordMap(), textDoc1.getWordMap());
		assertEquals(textDoc1JsonDeserialized.getLastUseTime(), 0);

		//textDoc2
		assertEquals(textDoc2JsonDeserialized.getDocumentTxt(), text2);
		assertEquals(textDoc2JsonDeserialized.getKey(), uriText2);
		assertEquals(textDoc2JsonDeserialized.getWordMap(), textDoc2.getWordMap());
		assertEquals(textDoc2JsonDeserialized.getLastUseTime(), 0);

		//textDoc3
		assertEquals(textDoc3JsonDeserialized.getDocumentTxt(), text3);
		assertEquals(textDoc3JsonDeserialized.getKey(), uriText3);
		assertEquals(textDoc3JsonDeserialized.getWordMap(), textDoc3.getWordMap());
		assertEquals(textDoc3JsonDeserialized.getLastUseTime(), 0);
	}	

	@Test
	void textDesirialization_DocumentPersistenceManager() throws IOException {
		this.serializeTextDocuments();				
		//Deserialize the Documents from the Json file that was stored via the method in DocumentPersistenceManager  
		Document textDoc1JsonDeserialized = docPersistManager.deserialize(uriText1);
		Document textDoc2JsonDeserialized = docPersistManager.deserialize(uriText2);
		Document textDoc3JsonDeserialized = docPersistManager.deserialize(uriText3);

		//Check that the fields of the newly deserialized documents match that of the original corresponding doc. 
		//documentLastUseTime should be 0 until it is set by DocumentStoreImpl upon a call to "get" in the BTree.

		//textDoc1
		assertEquals(textDoc1JsonDeserialized.getDocumentTxt(), text1);
		assertEquals(textDoc1JsonDeserialized.getKey(), uriText1);
		assertEquals(textDoc1JsonDeserialized.getWordMap(), textDoc1.getWordMap());
		assertEquals(textDoc1JsonDeserialized.getLastUseTime(), 0);

		//textDoc2
		assertEquals(textDoc2JsonDeserialized.getDocumentTxt(), text2);
		assertEquals(textDoc2JsonDeserialized.getKey(), uriText2);
		assertEquals(textDoc2JsonDeserialized.getWordMap(), textDoc2.getWordMap());
		assertEquals(textDoc2JsonDeserialized.getLastUseTime(), 0);

		//textDoc3
		assertEquals(textDoc3JsonDeserialized.getDocumentTxt(), text3);
		assertEquals(textDoc3JsonDeserialized.getKey(), uriText3);
		assertEquals(textDoc3JsonDeserialized.getWordMap(), textDoc3.getWordMap());
		assertEquals(textDoc3JsonDeserialized.getLastUseTime(), 0);
	}	

	@Test
	void textDeleteDocument() throws IOException {
		this.serializeTextDocuments();

		assertTrue (this.docPersistManager.delete(uriText1));
		assertTrue (this.docPersistManager.delete(uriText2));
		assertTrue (this.docPersistManager.delete(uriText3));

		assertFalse(textDoc1File.exists());
		assertFalse(textDoc2File.exists());
		assertFalse(textDoc3File.exists());

		assertEquals(0, textDoc1File.length());
		assertEquals(0, textDoc2File.length());
		assertEquals(0, textDoc3File.length());		

		assertFalse(textDoc1File.delete());
		assertFalse(textDoc2File.delete());
		assertFalse(textDoc3File.delete());		
	}




//======================================================================Byte=================================================================================================//
@Test
	void byteFileCreation() throws IOException {
		this.serializeByteDocuments();
		//Assert that the file was created and written to
		assertTrue(byteDoc1File.exists());
		assertTrue(byteDoc2File.exists());
		assertTrue(byteDoc3File.exists());

		assertNotEquals(0, byteDoc1File.length());
		assertNotEquals(0, byteDoc2File.length());
		assertNotEquals(0, byteDoc3File.length());
	}

	@Test
	void byteFileChangedDirectory() throws IOException {
		dir = "C:/Users/User/Documents/CS/COM1320_Data_Structures/Document_Store_and_Search_Engine_Semester_Project/Gotesman_Mark_2018243426/DataStructures/project/stage5/src/test";
		orignialFileArray = Paths.get(dir).toFile().listFiles();			

		docPersistManager = new DocumentPersistenceManager(new File(dir));
		this.serializeByteDocuments();
		byteDoc1File = new File(dir + "/" + "example.com.8042/bar/DocByte1" + ".json");
		byteDoc2File = new File(dir + "/" + "www.yu.edu/poems/DocByte2" + ".json");
		byteDoc3File = new File(dir + "/" + "example.com.8042/baz/DocByte3" + ".json");

		//Assert that the file was created and written to
		assertTrue(byteDoc1File.exists());
		assertTrue(byteDoc2File.exists());
		assertTrue(byteDoc3File.exists());

		assertNotEquals(0, byteDoc1File.length());
		assertNotEquals(0, byteDoc2File.length());
		assertNotEquals(0, byteDoc3File.length());
		this.restoreOriginalDirectory(Paths.get(dir).toFile(), orignialFileArray);			
	}

	@Test
	void byteJsonCorrectMembers() throws IOException {
		this.serializeByteDocuments();		
		//Order of expected serialization (going off the order on the spec, though it is arbitrary): 
		//1. Content; 2. URI; 3. Wordcount Map.

		//byteDoc1
		String[] byteDoc1JsonStringArr = Files.readString(byteDoc1File.toPath()).split("\"");
		assertEquals (byteDoc1.getKey().toString(), byteDoc1JsonStringArr[7]);

		//byteDoc2
		String[] byteDoc2JsonStringArr = Files.readString(byteDoc2File.toPath()).split("\"");
		assertEquals (byteDoc2.getKey().toString(), byteDoc2JsonStringArr[7]);

		//byteDoc3
		String[] byteDoc3JsonStringArr = Files.readString(byteDoc3File.toPath()).split("\"");
		assertEquals (byteDoc3.getKey().toString(), byteDoc3JsonStringArr[7]);

		//Check that "last use time" is NOT serialized, i.e. that there is no 4th member of the arrary that is split at ","
		assertFalse(Files.readString(byteDoc1File.toPath()).contains("lastUseTime"));
		assertFalse(Files.readString(byteDoc2File.toPath()).contains("lastUseTime"));
		assertFalse(Files.readString(byteDoc3File.toPath()).contains("lastUseTime"));

	}

	@Test
	//Due to the nature of the custom deserialization (specifically, the binary array being encoded), GSON will not be able to parse the JSON into the Document object, as it is looking for a JSON array to match the byte array.  
	void byteDesirialization_GSON() throws IOException {
	}	

	@Test
	void byteDesirialization_DocumentPersistenceManager() throws IOException {
		this.serializeByteDocuments();				
		//Deserialize the Documents from the Json file that was stored via the method in DocumentPersistenceManager  
		Document byteDoc1JsonDeserialized = docPersistManager.deserialize(uriByte1);
		Document byteDoc2JsonDeserialized = docPersistManager.deserialize(uriByte2);
		Document byteDoc3JsonDeserialized = docPersistManager.deserialize(uriByte3);

		//Check that the fields of the newly deserialized documents match that of the original corresponding doc. 
		//documentLastUseTime should be 0 until it is set by DocumentStoreImpl upon a call to "get" in the BTree.

		//byteDoc1
		assertTrue(Arrays.equals(byteDoc1JsonDeserialized.getDocumentBinaryData(), (byte1)));
		assertEquals(byteDoc1JsonDeserialized.getKey(), uriByte1);
		assertEquals(byteDoc1JsonDeserialized.getLastUseTime(), 0);

		//byteDoc2
		assertTrue(Arrays.equals(byteDoc2JsonDeserialized.getDocumentBinaryData(), (byte2)));
		assertEquals(byteDoc2JsonDeserialized.getKey(), uriByte2);
		assertEquals(byteDoc2JsonDeserialized.getLastUseTime(), 0);

		//byteDoc3
		assertTrue(Arrays.equals(byteDoc3JsonDeserialized.getDocumentBinaryData(), (byte3)));
		assertEquals(byteDoc3JsonDeserialized.getKey(), uriByte3);
		assertEquals(byteDoc3JsonDeserialized.getLastUseTime(), 0);
	}		
		

	@Test
	void byteDeleteDocument() throws IOException {
		this.serializeByteDocuments();

		assertTrue (this.docPersistManager.delete(uriByte1));
		assertTrue (this.docPersistManager.delete(uriByte2));
		assertTrue (this.docPersistManager.delete(uriByte3));

		assertFalse(byteDoc1File.exists());
		assertFalse(byteDoc2File.exists());
		assertFalse(byteDoc3File.exists());

		assertEquals(0, byteDoc1File.length());
		assertEquals(0, byteDoc2File.length());
		assertEquals(0, byteDoc3File.length());		

		assertFalse(byteDoc1File.delete());
		assertFalse(byteDoc2File.delete());
		assertFalse(byteDoc3File.delete());		
	}
//----------------Helper_Metods----------------//
	//Docs should now exist on disk at the locations of their URIs. user.dir should be the root by default.
	void serializeTextDocuments() throws IOException {
		docPersistManager.serialize(uriText1, textDoc1);
		docPersistManager.serialize(uriText2, textDoc2);
		docPersistManager.serialize(uriText3, textDoc3);
	}

	//Docs should now exist on disk at the locations of their URIs. user.dir should be the root by default.
	void serializeByteDocuments() throws IOException {
		docPersistManager.serialize(uriByte1, byteDoc1);
		docPersistManager.serialize(uriByte2, byteDoc2);
		docPersistManager.serialize(uriByte3, byteDoc3);
	}	

	void restoreOriginalDirectory (File currentDirctory, File[] oldFileArray) throws IOException {
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
	void deleteDirectory(File directory) {

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
    directory.delete();
  }
}