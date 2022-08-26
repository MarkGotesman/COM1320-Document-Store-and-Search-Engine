package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;

import edu.yu.cs.com1320.project.stage5.Document;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplIntegrationTest  {
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}
	
	DocumentStore documentStore = new DocumentStoreImpl();
 	Random rd = new Random();
	String baseDir = System.getProperty("user.dir");
	File[] orignialFileArray = Paths.get(baseDir).toFile().listFiles();

	@Test
	void IntegrationTest () throws IOException, URISyntaxException {
		// 1. this.putTextDocument(<>,<>);
		// 2. this.putByteDocument(<>);
		// 3. this.getDocument(<>);
		// 4. this.deleteDocument(<>);
		// 5. this.undo();
		// 6. this.undo(<>);
		// 7. this.search(<>);
		// 8. this.searchByPrefix(<>);
		// 9. this.deleteAll(<>);
		// 10. this.deleteAllWithPrefix(<>);
		// 11. this.setMaxDocumentCount(<>);
		// 12. this.setMaxDocumentBytes(<>);
		
		URI uri1 = new URI("http://www.yu.edu/uri.1");
		String text1 = "Whose woods these are I think I know.";
		this.putTextDocument(text1, uri1);
		Document doc1 = this.getDocument(uri1);

		URI uri2 = new URI("http://www.yu.edu/uri.2");
		String text2 = "His house is in the village though;";
		this.putTextDocument(text2, uri2);
		Document doc2 = this.getDocument(uri2);

		URI uri3 = new URI("http://www.yu.edu/uri.3");
		String text3 = "He will not see me stopping here test";
		this.putTextDocument(text3, uri3);
		Document doc3 = this.getDocument(uri3);		

		URI uri4 = new URI("http://www.yu.edu/uri.4");
		String text4 = "To watch his woods fill up with snow.";
		this.putTextDocument(text4, uri4);
		Document doc4 = this.getDocument(uri4);

		//5 (1,2,3,4)
		this.undo();
		assertEquals(null, this.getDocument(uri4));
		
		//10 (1,2,3)
		assertEquals (new HashSet<URI>(Arrays.asList(uri1)), this.deleteAllWithPrefix("kn"));

		print (this.getDocument(uri1));
		print (this.getDocument(uri2));
		print (this.getDocument(uri3));
		print (this.getDocument(uri4));

		//4 (2,3)
		assertEquals (true, this.deleteDocument(uri3));

		print (this.getDocument(uri1));
		print (this.getDocument(uri2));
		print (this.getDocument(uri3));
		print (this.getDocument(uri4));

		//5 (2)
		this.undo();
		assertEquals (doc3, this.getDocument(uri3));

		print (this.getDocument(uri1));
		print (this.getDocument(uri2));
		print (this.getDocument(uri3));
		print (this.getDocument(uri4));

		//6 (2,3)
		this.undo(uri2);
		assertEquals(new ArrayList<>(), this.searchByPrefix("vi"));

		//3 (3)
		print (this.getDocument(uri1));
		print (this.getDocument(uri2));
		print (this.getDocument(uri3));
		print (this.getDocument(uri4));

		assertEquals(null, this.getDocument(uri1));

		//8 (3)
		assertEquals(new ArrayList<Document>(Arrays.asList(doc3)), this.searchByPrefix("he"));

		URI uri5 = new URI("http://www.yu.edu/uri.5");
		String text5 = "My little horse must think it queer test test test "; //He gives his harness bells a shake. //34
		this.putTextDocument(text5, uri5);
		Document doc5 = this.getDocument(uri5);

		URI uri6 = new URI("http://www.yu.edu/uri.6");
		String text6 = "To stop without a farmhouse near test test"; //42
		this.putTextDocument(text6, uri6);
		Document doc6 = this.getDocument(uri6);

		URI uri7 = new URI("http://www.yu.edu/uri.7");
		String text7 = "Between the woods and frozen lake"; //33
		this.putTextDocument(text7, uri7);
		Document doc7 = this.getDocument(uri7);		

		URI uri8 = new URI("http://www.yu.edu/uri.8");
		String text8 = "The darkest evening of the year. the"; //36
		this.putTextDocument(text8, uri8);
		Document doc8 = this.getDocument(uri8);	

		//9 (3,5,6,7,8)
		assertEquals(new HashSet<URI>(Arrays.asList(uri7, uri8)), this.deleteAll("the"));

		//8 (3,5,6)
		assertEquals(new ArrayList<Document>(Arrays.asList(doc5, doc6, doc3)), this.searchByPrefix("te"));

		//5 (3,5,6)
		this.undo();
		assertEquals (new ArrayList<Document>(Arrays.asList(doc8, doc7)), this.search("the"));

		//6 (3,5,6,7,8)
		this.putTextDocument(text4, uri4);
		this.deleteDocument(uri4);
		this.undo (uri4);
		assertEquals(doc4, this.getDocument(uri4));

		//1 (3,4,5,6,7,8)
		int hashCode = doc5.hashCode();
		assertEquals(hashCode, this.putTextDocument("He gives his harness bells a shake", uri5));
		doc5 = this.getDocument(uri5);		

		//2 (3,4,5,6,7,8)
		byte[] byt = new byte[7];
		rd.nextBytes(byt);
		URI uri9 = new URI("http://www.yu.edu/uri.9");		
		this.putByteDocument(byt, uri9);
		Document doc9 = this.getDocument(uri9);	
		assertEquals (doc9, this.getDocument(uri9));
		assertEquals (new String(byt), new String (doc9.getDocumentBinaryData()));

		//10 (3,4,5,6,7,8,9)
		assertEquals(new HashSet<URI>(Arrays.asList(uri3, uri4, uri6)), this.deleteAllWithPrefix("wi"));

		//1 (8,7,5,9; bytes = 110)

		uri1 = new URI("http://www.yu.edu/uri.1");
		text1 = "Whose woods these are I think I know.";
		this.putTextDocument(text1, uri1);
		doc1 = this.getDocument(uri1);

		uri2 = new URI("http://www.yu.edu/uri.2");
		text2 = "His house is in the village though;";
		this.putTextDocument(text2, uri2);
		doc2 = this.getDocument(uri2);

		uri3 = new URI("http://www.yu.edu/uri.3");
		text3 = "He will not see me stopping here test";
		this.putTextDocument(text3, uri3);
		doc3 = this.getDocument(uri3);		

		uri4 = new URI("http://www.yu.edu/uri.4");
		text4 = "To watch his woods fill up with snow.";
		this.putTextDocument(text4, uri4);
		doc4 = this.getDocument(uri4);

		//12 (5,7,8,9,1,2,3,4; bytes = 256)
		this.setMaxDocumentBytes(176); //Docs that should now be moved to disk: 5, 7, 8
		assertTrue (inDisk(uri5));
		assertTrue (inDisk(uri7));
		assertTrue (inDisk(uri8));

		assertEquals (new ArrayList<>(Arrays.asList(doc4)), this.search("snow")); //Doc 4 should be in the store

		//8 (4,9,1,2,3; bytes = 153 [5,7,8; bytes = 103])
		this.searchByPrefix("th");
		assertTrue (inDisk(uri4));
		assertTrue (inDisk(uri9));
		assertTrue (inDisk(uri3));
		assertTrue (inDisk(uri5));


		//10 (9,3,4,2,1; bytes = 153)
		// this.deleteAllWithPrefix("st");

		// //3 (9,4,2,1; bytes = 116)
		// this.getDocument(uri2);

		// //11 (9,4,1,2; bytes = 116)
		// this.setMaxDocumentCount(3);
		// assertEquals(null, this.getDocument(uri9));

		// //2 (4,1,2; bytes = 107)
		// byte[] byt2 = new byte[51];
		// rd.nextBytes(byt2);
		// URI uri10 = new URI("http://www.yu.edu/uri.10");		
		// this.putByteDocument(byt2, uri10);
		// Document doc10 = this.getDocument(uri10);	
		// assertEquals (doc10, this.getDocument(uri10));
		// assertEquals (new String(byt2), new String (doc10.getDocumentBinaryData()));

		// (1,2,10; bytes = 129; maxDocumentBytes = 176, maxDocumentCount = 3)

	}

//-----------------Document_Store_Methods--------------------//
	//1
	private int putTextDocument (String text, URI uri) throws IOException {
		return this.documentStore.putDocument(new ByteArrayInputStream (text.getBytes()), uri, DocumentFormat.TXT);
	}

	//2
	private int putByteDocument (byte[] arr, URI uri) throws IOException {
		return this.documentStore.putDocument(new ByteArrayInputStream(arr), uri, DocumentFormat.BINARY);
	}

	//3
	private Document getDocument (URI uri) {
		return this.documentStore.getDocument(uri);
	}

	//4
	private boolean deleteDocument (URI uri) {
		return this.documentStore.deleteDocument(uri);
	}

	//5
	private void undo ()  {
		this.documentStore.undo();
	}

	//6
	private void undo (URI uri)  {
		this.documentStore.undo(uri);
	}

	//7
	private List<Document> search(String keyword) {
		return this.documentStore.search(keyword);
	}

	//8
	private List<Document> searchByPrefix(String keywordPrefix)  {
		return this.documentStore.searchByPrefix(keywordPrefix);
	}

	//9
	private Set<URI> deleteAll(String keyword) {
		return this.documentStore.deleteAll (keyword);
	}

	//10
	private Set<URI> deleteAllWithPrefix(String keywordPrefix) {
		return this.documentStore.deleteAllWithPrefix(keywordPrefix);
	}

	//11
	private void setMaxDocumentCount (int limit) {
		this.documentStore.setMaxDocumentCount(limit);
	}

	//12
	private void setMaxDocumentBytes (int limit) {
		this.documentStore.setMaxDocumentBytes(limit);
	}	

//-----------------Helper_Methods--------------------//
	//Get the number of bytes in a document's in-memory representation. 
	//Document param.
	private int documentBtyeValue (Document doc) {
		if (doc.getDocumentBinaryData() != null) {
			return doc.getDocumentBinaryData().length;
		} else {
			return doc.getDocumentTxt().getBytes().length;
		}
	}

	//Get the number of bytes in a document's in-memory representation. 
	//URI param.
	private int documentBtyeValue (URI uri) {
		return this.documentBtyeValue(documentStore.getDocument(uri));
	}

	private int documentBtyeValue(List<URI> uriList) {
		int byteValue = 0;
		for (URI uri : uriList) {
			byteValue += this.documentBtyeValue(uri);
		}
		return byteValue;
	}

    private boolean inDisk (URI uri) {
        String docFileString = baseDir + this.parseDirectoryFromURI(uri) + ".json"; 
        File docFile = new File (docFileString);
     	print ("Checking for file: " + docFile.toString());  	
		return (docFile.exists());
    }
    
    private boolean notinDisk (URI uri) {
        String docFileString = baseDir + this.parseDirectoryFromURI(uri) + ".json"; 
        File docFile = new File (docFileString);  	
		return (!docFile.exists());
    }
     
     private String parseDirectoryFromURI(URI uri) {
        String uriSchemeSpecific = uri.getSchemeSpecificPart();
        while (uriSchemeSpecific.charAt(0) == '/' && uriSchemeSpecific.charAt(1) == '/') {
            uriSchemeSpecific = uriSchemeSpecific.substring(1);
        }
        return uriSchemeSpecific;
    } 

}

//----------------------Temporary_Public_Methods--------------//
/* 
DocumentStore
	List<URI> getHeapOrder();    

DocumentStoreImpl
	@Override
	public List<URI> getHeapOrder() {
		List<URI> uriList = new ArrayList<>();
		List<Document> heapList = new ArrayList<>();
		try {
			while (true) {
				Document doc = minHeap.remove();
				uriList.add(doc.getKey());
				heapList.add(doc);
			}
		} catch (Exception e) {

		}
		for (Document tempDoc : heapList) {
			this.minHeap.insert(tempDoc);
		}		
		return uriList;
	}

	System.out.println(documentStore.getHeapOrder());
	System.out.println(this.documentBtyeValue(documentStore.getHeapOrder()));
*/


