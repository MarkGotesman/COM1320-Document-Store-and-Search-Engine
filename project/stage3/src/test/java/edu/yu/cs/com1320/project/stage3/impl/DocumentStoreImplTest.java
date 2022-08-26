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


public class DocumentStoreImplTest  {
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}
	
	DocumentStore documentStore;
	URI uri1;
	URI uri2;
	URI uri3;
	URI uri4;
	URI uri5;
	URI uri6;
	URI uri7;
	URI uri8;
	URI uri9;
	URI uri10;
	URI uri11;
	URI uri12;
	URI uri13;

	InputStream content1;
	InputStream content2;
	InputStream content3;
	InputStream content4;
	InputStream content5;
	InputStream content6;
	InputStream content7;
	InputStream content8;
	InputStream content9;
	InputStream content10;
	InputStream content11;
	InputStream content12;
	InputStream content13;

	
	List<URI> uriList = new ArrayList<>();
	List<InputStream> contentList = new ArrayList<>();

	Comparator<Document> keywordDocumentComparator;
	Comparator<Document> prefixDocumentComparator;

	@BeforeEach
	public void init() throws IOException, URISyntaxException {
		documentStore = new DocumentStoreImpl();
		 uri1 = new URI("docs.oracle.com1");
		 uri2 = new URI("docs.oracle.com2");
		 uri3 = new URI("docs.oracle.com3");
		 uri4 = new URI("docs.oracle.com4");
		 uri5 = new URI("docs.oracle.com5");
		 uri6 = new URI("docs.oracle.com6");
		 uri7 = new URI("docs.oracle.com7");
		 uri8 = new URI("docs.oracle.com8");
		 uri9 = new URI("docs.oracle.com9");
		 
		 uri10 = new URI("docs.oracle.com10");
		 uri11 = new URI("docs.oracle.com11");
		 uri12 = new URI("docs.oracle.com12");
		 uri13 = new URI("docs.oracle.com13");


		 content1 = new ByteArrayInputStream("Test1".getBytes());
		 content2 = new ByteArrayInputStream("Test2".getBytes());
		 content3 = new ByteArrayInputStream("Test3".getBytes());
		 content4 = new ByteArrayInputStream("Test4".getBytes());
		 content5 = new ByteArrayInputStream("Test5".getBytes());
		 content6 = new ByteArrayInputStream("Test6".getBytes());
		 content7 = new ByteArrayInputStream("Test7".getBytes());
		 content8 = new ByteArrayInputStream("Test8".getBytes());
		 content9 = new ByteArrayInputStream("Test9".getBytes());

		content10 = new ByteArrayInputStream("It was supposed to be a dream vacation. They had planned it over a year in advance so that it would be perfect in every way. It had been what they had been looking forward to through all the turmoil and negativity around them. It had been the light at the end of both their tunnels. Now that the dream vacation was only a week away, the virus had stopped all air travel.".getBytes());
		content11 = new ByteArrayInputStream("I'm meant to be writing at this moment. What I mean is, I'm meant to be writing something else at this moment. The document I'm meant to be writing is, of course, open in another program on my computer and is patiently awaiting my attention. Yet here I am plonking down senseless sentiments in this paragraph because it's easier to do than to work on anything particularly meaningful. I am grateful for the distraction.".getBytes());
		content12 = new ByteArrayInputStream("hb sb 3w ft e1 lj eb zl by p0 8e 4s c9 tp su 24 xd ct d1 s2 wz hd jg k6 ui 6x ic 9k z2 8y tx ao n6 mc ky 6q mz j0 jv qn oo 5x 53 8o xi ty ic 4r 31 uh 7y xt pq ya b8 co xl bv jx c4 i2 tk 36 0h k8 ec w2 6y tg v0 47 bs hn l7 86 wk mf nu uh jt 1q zr iq zi uz km 0r 6n xo ng 0r xb i4 sa ar 7n 8d o3 u5 9h kr 94 9s h5 5u 3v d5 py 0v pz uf gz em j5 p6 gg yu wu if dw kj w1 d3 41 y5 xv c3 2x 7u 2z ox 9o hc zq d1 lr q1 0i m8 aw iq kp 2t mq um d4 rv s4 20 qd uq xn o6 tq jv jb n3 cb jj ke ma qi 30 qs 1c 01 lg 01 qy fb q7 6b t5 23 m9 x4 iy k5 ro ff aj g9 1h q9 wo g6 dx 3h zy ij db wt sd 86 wh yf zq zx n3 ca hi 1k 5k ts k8 at u0 3f u3 xr x7 y5 3s 1e ut 8e vm u4 lm ld ea c4 bf ok me lw rj v7 81 7n t9 j2 v4 2m ua i6 oc n3 ux 24 94 u2 fl iw f2 g2 5u 4f cz 50 mk c3 op wu 8x to sz jy xk vm jo t2 sh 0q 37 sx mx rp c5 d0 4q og x4 0n ng i4 tt 4t 50 5q 3o ry kr gx e1 aa k3 fs k5 17 t1 ai cs um m5 l4 t3 vq gx 19 y6 qf e1 hz 3b ti t3 1c n8 yg fu jl 4o r5 2b bg 6m i1 kz n5 d7 26 p4 s2 9g vd c4 c5 w4 29 ct k1 en uh q4 cy 0z en rp on nw 08 a9 at nc lw h4 ox pe 2i 3a 9g do h7 bf d4 gc j8 bg x0 ui bz a5 vc ok bu 6y 49 pv jn 4h ek tf pp p5 rh am gh vz s1 qa j6 35 ap 0n kv y9 jf fd 7n cy dw ar 8z rp 5u a9 wa ug nn m7 f2 tg d1 4y 1f le 12 98 k4 g0 zl va dw ks lh dk t2 ry 18 2x ez 5t vg bz 2q 5s uo wl 49 gv 4g y3 31 29 rc h7 sa wt 73 wl 39 08 nb 5r pp vm mf 0p ih z3 32 cu ht as y7 79 rs mc 6d 9a 0o xl gh p6 yk h0 sk k2 t9 2m vr bh r0 yy 0p v4 1p il bh v0 yr u9 16 dd 2e yf 1t u6 6v 11 jm 4f 80 bs 6f kz ec fd z5".getBytes());
		content13 = new ByteArrayInputStream("He picked up 11 the burnt 11 end of 11 the branch 11 and made a 11 mark on 11 the stone. 11 Day 52 if 11 the 43marks on the stone were accurate. 44 He couldn't be sure. Day and nights had begun to blend together creating confusion, but he knew it was a long time. Much too long.".getBytes());


		uriList.add(uri1);
		uriList.add(uri2);
		uriList.add(uri3);
		uriList.add(uri4);
		uriList.add(uri5);
		uriList.add(uri6);
		uriList.add(uri7);
		uriList.add(uri8);
		uriList.add(uri9); 
		uriList.add(uri10);
		uriList.add(uri11);
		uriList.add(uri12);
		uriList.add(uri13);

		contentList.add(content1);
		contentList.add(content2);
		contentList.add(content3);
		contentList.add(content4);
		contentList.add(content5);
		contentList.add(content6);
		contentList.add(content7);
		contentList.add(content8);
		contentList.add(content9);
		contentList.add(content10);
		contentList.add(content11);
		contentList.add(content12);
		contentList.add(content13);

		Iterator<InputStream> it1 = contentList.iterator();
		Iterator<URI> it2 = uriList.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			InputStream nextInputStream = it1.next();
			URI nextURI = it2.next();
			documentStore.putDocument(nextInputStream, nextURI, DocumentFormat.TXT);
		}

	}

	@Test
	void undoLastPutDocument() {
		documentStore.undo();
		assertEquals(null,documentStore.getDocument(uri13)); //I assume that when an undo has been made, the resulting document call will give null, as a delete is putting null in the hashtable. uri9 = last put into table
	}

	@Test
	void undoPutDocumentURI() {		
		documentStore.undo(uri7);
		assertEquals(null,documentStore.getDocument(uri7)); //I assume that when an undo has been made, the resulting document call will give null, as a delete is putting null in the hashtable
	}

	@Test
	void undoLastModifiedDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("foo".getBytes()), uri9, DocumentFormat.TXT);
		assertEquals("foo", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.undo();
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
	}

	@Test
	void undoModifiedDocumentURI () throws IOException, URISyntaxException{
		assertEquals("Test7", documentStore.getDocument(uri7).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("foo".getBytes()), uri7, DocumentFormat.TXT);

		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("bar".getBytes()), uri9, DocumentFormat.TXT);

		documentStore.undo(uri7);
		assertEquals("Test7", documentStore.getDocument(uri7).getDocumentTxt());
		assertEquals("bar", documentStore.getDocument(uri9).getDocumentTxt());
	}


	@Test
	void undoLastDeletedDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(null, uri9, DocumentFormat.TXT);
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo();
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
	}

	@Test
	void undoDeletedDocumentURI () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		
		documentStore.putDocument(null, uri3, DocumentFormat.TXT);
		documentStore.putDocument(null, uri9, DocumentFormat.TXT);
		assertEquals(null, documentStore.getDocument(uri3));		
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo(uri3);
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		assertEquals(null, documentStore.getDocument(uri9));		
	}	

	@Test
	void undoDeletedDocumentURIWithDeleteDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		
		documentStore.deleteDocument(uri3);
		documentStore.deleteDocument(uri9);
		assertEquals(null, documentStore.getDocument(uri3));		
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo(uri3);
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		assertEquals(null, documentStore.getDocument(uri9));		
	}	

	@Test
	void undoEntireStackThrowISE () throws IOException, URISyntaxException {
		for (URI uri : uriList) {
			documentStore.undo();
		}
		assertThrows(IllegalStateException.class, () -> documentStore.undo());
	}
	
	@Test
	void attemptUndoNonExistantURIThrowISE() throws IOException, URISyntaxException {
		assertThrows(IllegalStateException.class, () -> documentStore.undo(new URI ("docs.oracle.com100")));
	}

	@Test 
	void searchForTo() {
		List<Document> toList = Arrays.asList(
			documentStore.getDocument(uri11),
			documentStore.getDocument(uri10),
			documentStore.getDocument(uri13),
			documentStore.getDocument(uri12)			
			);
		assertEquals(toList, documentStore.search("to"));
		assertEquals(toList, documentStore.search("To"));
		assertEquals(toList, documentStore.search("tO"));
	}

	@Test
	void searchByPrefixTh() {
		List<Document> thList = Arrays.asList(
			documentStore.getDocument(uri11),
			documentStore.getDocument(uri13),
			documentStore.getDocument(uri10)
			);		
		print (thList);
		thList.sort(this.prefixDocumentComparatorGenerator("th"));
		print (thList);
		assertEquals (thList, documentStore.searchByPrefix("Th"));		
		assertEquals (thList, documentStore.searchByPrefix("th"));		
		assertEquals (thList, documentStore.searchByPrefix("TH"));		
	}
	@Test
	void deleteAllThe() {
		HashSet<URI> thSet = new HashSet<> (Arrays.asList(
			documentStore.getDocument(uri10).getKey(),
			documentStore.getDocument(uri11).getKey(),
			documentStore.getDocument(uri13).getKey()
			));	
		assertEquals (thSet, new HashSet<URI>(documentStore.deleteAll("the")));
		assertEquals (null, documentStore.getDocument(uri10));
		assertEquals (null, documentStore.getDocument(uri11));
		assertEquals (null, documentStore.getDocument(uri13));
	}

	@Test
	void deleteAllPrefixMa() {
		HashSet<URI> maSet = new HashSet<> (Arrays.asList(
			documentStore.getDocument(uri12).getKey(),
			documentStore.getDocument(uri13).getKey()
			));	
		assertEquals (maSet, new HashSet<URI>(documentStore.deleteAllWithPrefix("ma")));
		assertEquals (null, documentStore.getDocument(uri12));
		assertEquals (null, documentStore.getDocument(uri13));		

		assertEquals (new ArrayList<>(), documentStore.search("ma"));		
	}

	@Test
	void undoAllCommands() {
		while (true) {
			try {
				documentStore.undo();
			} catch (IllegalStateException e) {
				break;
			}
		}
		for (URI uri : uriList) {
			assertEquals (null, documentStore.getDocument(uri));
		}
	}

	@Test
	void deleteAllWithPrefixandUndo() throws IOException, URISyntaxException {
		documentStore = new DocumentStoreImpl();
		String text1 = "them they thOR At Avalanch Village thistle";
		String text2 = "thick random text here to test this document";

		URI uriDoc1 = new URI("yu.edu1");
		URI uriDoc2 = new URI("yu.edu2");

		documentStore.putDocument (new ByteArrayInputStream(text1.getBytes()), uriDoc1, DocumentFormat.TXT);
		documentStore.putDocument (new ByteArrayInputStream(text2.getBytes()), uriDoc2, DocumentFormat.TXT);

		Document doc1 = documentStore.getDocument(uriDoc1);
		Document doc2 = documentStore.getDocument(uriDoc2);

		//Test presence of both documents.
		for (String word : text1.split("\\s+")) {
			assertEquals (Arrays.asList(doc1), documentStore.search(word));
		}
		for (String word : text2.split("\\s+")) {
			assertEquals (Arrays.asList(doc2), documentStore.search(word));
		}		

		//Test deletion of both documents.
		documentStore.deleteAllWithPrefix("th");
		for (String word : text1.split("\\s+")) {
			assertEquals (new ArrayList<>(), documentStore.search(word));
		}
		for (String word : text2.split("\\s+")) {
			assertEquals (new ArrayList<>(), documentStore.search(word));
		}	

		//Test undo of actions on Doc1 - presence of Doc1, deletion of Doc 2.
		documentStore.undo(uriDoc1);
		for (String word : text1.split("\\s+")) {
			assertEquals (Arrays.asList(doc1), documentStore.search(word));
		}			
		for (String word : text2.split("\\s+")) {
			assertEquals (new ArrayList<>(), documentStore.search(word));
		}

		//Test undo of actions on Doc2 - presence of Doc2, deletion of Doc 1.
		documentStore.putDocument (new ByteArrayInputStream(text1.getBytes()), uriDoc1, DocumentFormat.TXT);
		documentStore.putDocument (new ByteArrayInputStream(text2.getBytes()), uriDoc2, DocumentFormat.TXT);		
		documentStore.deleteAllWithPrefix("th");
		documentStore.undo(uriDoc2);
		for (String word : text1.split("\\s+")) {
			assertEquals (new ArrayList<>(), documentStore.search(word));
		}
		for (String word : text2.split("\\s+")) {
			assertEquals (Arrays.asList(doc2), documentStore.search(word));
		}

		//Test undo on entire document store - presence of Doc1, presence of Doc 2.
		documentStore.putDocument (new ByteArrayInputStream(text1.getBytes()), uriDoc1, DocumentFormat.TXT);
		documentStore.putDocument (new ByteArrayInputStream(text2.getBytes()), uriDoc2, DocumentFormat.TXT);
		documentStore.deleteAllWithPrefix("th");
		documentStore.undo();
		for (String word : text1.split("\\s+")) {
			assertEquals (Arrays.asList(doc1), documentStore.search(word));
		}
		for (String word : text2.split("\\s+")) {
			assertEquals (Arrays.asList(doc2), documentStore.search(word));
		}										
	}

//-----------------Helper Methods--------------------//
	private Comparator<Document> keywordDocumentComparatorGenerator (String keyword) {
		Comparator<Document> keywordDocumentComparator = (Document doc1, Document doc2) -> doc2.wordCount(keyword) - doc1.wordCount(keyword);
		return keywordDocumentComparator;
	}

	private Comparator<Document> prefixDocumentComparatorGenerator (String prefix) {
		Comparator<Document> prefixDocumentComparator = (Document doc1, Document doc2) -> {
			Set<String> allWords = new HashSet<>();
			allWords.addAll(doc1.getWords());
			allWords.addAll(doc2.getWords());
			int difference = 0;
			allWords.remove(null);

			for (String word : allWords) {
				if (word.length() >= prefix.length() && word.substring(0, prefix.length()).equals(prefix)) {
					difference += (doc2.wordCount(word) - doc1.wordCount(word));
				} 
			}
			return difference;
		};
		return prefixDocumentComparator;
	}	

 
}

