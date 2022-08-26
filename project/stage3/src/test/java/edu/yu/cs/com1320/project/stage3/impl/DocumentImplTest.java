package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;

import java.util.*;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentImplTest {

	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

	URI uri1;
	URI uri2;
	URI uri3;
	URI uri4;
	URI uri5;
	URI uri6;
	URI uri7;
	URI uri8;
	URI uri9;	

	String text1;
	String text2;
	String text3;
	String text4;
	String text5;
	String text6;
	String text7;
	String text8;
	String text9;

	byte[] byte1 = new byte[7];
	byte[] byte2 = new byte[7];
	byte[] byte3 = new byte[7];
	byte[] byte4 = new byte[7];
	byte[] byte5 = new byte[7];
	byte[] byte6 = new byte[7];
	byte[] byte7 = new byte[7];
	byte[] byte8 = new byte[7];
	byte[] byte9 = new byte[7];

	Set<URI> uriSet;
	Set<String> textSet;
	Set<byte[]> byteSet;

	@BeforeEach
	void init() throws IOException, URISyntaxException {
		 uri1 = new URI("docs.oracle.com1");
		 uri2 = new URI("docs.oracle.com2");
		 uri3 = new URI("docs.oracle.com3");
		 uri4 = new URI("docs.oracle.com4");
		 uri5 = new URI("docs.oracle.com5");
		 uri6 = new URI("docs.oracle.com6");
		 uri7 = new URI("docs.oracle.com7");
		 uri8 = new URI("docs.oracle.com8");
		 uri9 = new URI("docs.oracle.com9");	

		 text1 = "Lorem ipsum dolor sit amet";
		 text2 = "consectetur adipiscing elit";	
		 text3 = "Fusce condimentum eu nisi non congue";
		 text4 = "Curabitur in erat ac tellus vulputate";
		 text5 = "consequat ultrices vitae enim";
		 text6 = "Etiam finibus orci nec lobortis posuere";
		 text7 = "Sed et malesuada turpis";
		 text8 = "id fringilla nisi";
		 text9 = "Pellentesque lorem metus";

		 Random rd = new Random();

		 rd.nextBytes(byte1);
		 rd.nextBytes(byte2);
		 rd.nextBytes(byte3);
		 rd.nextBytes(byte4);
		 rd.nextBytes(byte5);
		 rd.nextBytes(byte6);
		 rd.nextBytes(byte7);
		 rd.nextBytes(byte8);
		 rd.nextBytes(byte9);

		 uriSet = new HashSet<URI>(Arrays.asList(uri1, uri2, uri3, uri4, uri5, uri6, uri7, uri8, uri9));
		 textSet = new HashSet<String>(Arrays.asList(text1, text2, text3, text4, text5, text6, text7, text8, text9));
		 byteSet = new HashSet<byte[]>(Arrays.asList(byte1, byte2, byte3, byte4, byte5, byte6, byte7, byte8, byte9));

	}	

	@Test
	void simpleDocumentText() {
		Document doc = new DocumentImpl(uri1, text1);
		assertEquals (text1, doc.getDocumentTxt());
	}

	@Test
	void simpleDocumentByte() {
		Document doc = new DocumentImpl (uri5, byte5);
		assertEquals (byte5, doc.getDocumentBinaryData());
	}

	@Test
	void getKeyManyDocuments() {
		List<URI> uriList = new ArrayList<>(uriSet);
		Iterator<URI> uriIterator = uriList.iterator();
		Iterator<String> textIterator = textSet.iterator();
		Iterator<byte[]> byteIterator = byteSet.iterator();

		List<Document> docList = new ArrayList<>();
		while (uriIterator.hasNext()) {
			if (Math.random() < .5) {
				docList.add(new DocumentImpl (uriIterator.next(), textIterator.next()));
			} else {
				docList.add (new DocumentImpl (uriIterator.next(), byteIterator.next()));
			}
 		}

 		uriIterator = uriList.iterator();

 		for (Document doc : docList) {
 			assertEquals (uriIterator.next(), doc.getKey());
 			print (doc + ". Text: " + doc.getDocumentTxt() + ". Byte: " + doc.getDocumentBinaryData());
 		}
	}

	@Test
	void getWordsBinary() {
		Document doc = new DocumentImpl(uri3, byte8);
		assertEquals(new HashSet<>(), doc.getWords());
	}

	@Test
	void getWordsTextSmall() {
		Document doc = new DocumentImpl(uri2, text1);
		assertEquals (this.lowerCaseSet(text1), doc.getWords());
	}

	@Test
	void getWordsTextLarge() {
		StringBuilder strBuilder = new StringBuilder();
		Set<String> randomStringSet = this.randomStringSet(1000);

		for (String str : randomStringSet) {
			strBuilder.append(str + " ");
		}
		Document doc = new DocumentImpl(uri5, strBuilder.toString());
		assertEquals(strBuilder.toString(), doc.getDocumentTxt());

		assertEquals (this.lowerCaseSet(strBuilder.toString()), new HashSet<String> (doc.getWords()));
	}

	@Test
	void wordCountBinary() {
		Document doc = new DocumentImpl(uri3, byte8);
		assertEquals(0, doc.wordCount("foo"));
	}

	@Test
	void wordCount() {
		String lorem_27 = "Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  ";
		Document doc = new DocumentImpl(uri4, lorem_27);
		assertEquals (27, doc.wordCount("Lorem"));
	}

	@Test
	void wordCountCASEINSENSITIVE() {
		String lorem_27 = "Lorem ipsum dolor sit amet LOrEm ipsum dolor sit amet LoreM ipsum dolor sit amet lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem ipsum dolor sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem sit amet Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  Lorem dolor  ";
		Document doc = new DocumentImpl(uri4, lorem_27);
		assertEquals (27, doc.wordCount("LoReM"));
	}	

	@Test
	void wordCountNonAlphaNumericIgnored () {
		String lorem_27 = "Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet %* #Lorem ipsum dolor sit amet Lorem sit amet !Lorem sit amet !Lorem sit amet !Lorem sit amet !Lorem sit amet Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ^^^Lorem dolor  ";
		Document doc = new DocumentImpl(uri4, lorem_27);
		assertEquals (27, doc.wordCount("LoReM"));
		assertEquals(0, doc.wordCount("%*"));	
	}

	@Test 
	void wordCountWordsWithNumbers () {
		String dolor1_22 = "Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem ipsum dolor1 sit amet Lorem sit3 amet !Lorem sit3 amet !Lorem sit3 amet !Lorem sit3 amet !Lorem sit3 amet Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  Lorem dolor1  ";
		Document doc = new DocumentImpl(uri7, dolor1_22);
		assertEquals (22, doc.wordCount("dolor1"));		
	}

//-----------------Helper Methods--------------------//
	public Set<String> randomStringSet (int size) {
		Set<String> stringSet = new HashSet<>();
		while (stringSet.size() < size) {
			stringSet.addAll(this.randomStringSet(size, stringSet, new String ("")));
		}
		return stringSet;
	}

	private Set<String> randomStringSet (int size, Set<String> stringSet, String key) {
		int incrementor = (int)(Math.random() * 58);
		for (int i = incrementor; Character.isAlphabetic(65 + i) && stringSet.size() < size && key.length() < 5; i = i + incrementor) {
			key = key + (char)(65 + i);
			if (Math.random() < .5 && !stringSet.stream().anyMatch(key::equalsIgnoreCase)) {
				stringSet.add(key);
			}
			this.randomStringSet(size, stringSet, key);
		} 
		return stringSet;
	}	

	private Set<String> lowerCaseSet (String txt){
		HashSet<String> tempWordSet = new HashSet<>();
		for (String word : txt.split(" ")) {
			tempWordSet.add(word.toLowerCase());
		}
		return tempWordSet;		
	}
}