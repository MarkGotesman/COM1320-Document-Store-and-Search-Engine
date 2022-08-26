package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;

import edu.yu.cs.com1320.project.stage3.Document;

import java.util.*;

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
 	// byte[] arr = new byte[7];
 	// rd.nextBytes(arr);	

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
		
		URI uri1 = new URI("uri.1");
		String text1 = "Whose woods these are I think I know.";
		this.putTextDocument(text1, uri1);
		Document doc1 = this.getDocument(uri1);

		URI uri2 = new URI("uri.2");
		String text2 = "His house is in the village though;";
		this.putTextDocument(text2, uri2);
		Document doc2 = this.getDocument(uri2);

		URI uri3 = new URI("uri.3");
		String text3 = "He will not see me stopping here test";
		this.putTextDocument(text3, uri3);
		Document doc3 = this.getDocument(uri3);		

		URI uri4 = new URI("uri.4");
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

		URI uri5 = new URI("uri.5");
		String text5 = "My little horse must think it queer test test test "; //He gives his harness bells a shake
		this.putTextDocument(text5, uri5);
		Document doc5 = this.getDocument(uri5);

		URI uri6 = new URI("uri.6");
		String text6 = "To stop without a farmhouse near test test";
		this.putTextDocument(text6, uri6);
		Document doc6 = this.getDocument(uri6);

		URI uri7 = new URI("uri.7");
		String text7 = "Between the woods and frozen lake";
		this.putTextDocument(text7, uri7);
		Document doc7 = this.getDocument(uri7);		

		URI uri8 = new URI("uri.8");
		String text8 = "The darkest evening of the year. the";
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
		URI uri9 = new URI("uri.9");		
		this.putByteDocument(byt, uri9);
		Document doc9 = this.getDocument(uri9);	
		assertEquals (doc9, this.getDocument(uri9));
		assertEquals (new String(byt), new String (doc9.getDocumentBinaryData()));

		//10 (3,4,5,6,7,8,9)
		assertEquals(new HashSet<URI>(Arrays.asList(uri3, uri4, uri6)), this.deleteAllWithPrefix("wi"));

		//(5,7,8,9)
		assertEquals (doc5, this.getDocument(uri5));
		assertEquals (doc7, this.getDocument(uri7));
		assertEquals (doc8, this.getDocument(uri8));
		assertEquals (doc9, this.getDocument(uri9));
	}


//-----------------Helper Methods--------------------//
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
}
