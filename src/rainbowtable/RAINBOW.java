package rainbowtable;

import java.util.*;
import java.util.zip.GZIPOutputStream;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;


/*
 * A data structure to hold the rainbow table
 * each entry is a 24-bit string, hence a total of 2^24 possible entries
 * every entry is hashed using SHA1
 */

public class RAINBOW {
	static HashMap <String, Integer> unique_words = new HashMap<String, Integer>();
	static HashMap <String, String> rainbow_table = new HashMap<String, String>();
	static HashMap <String, String> rainbow_table2 = new HashMap<String, String>();
	static HashMap <String, String> rainbow_table3 = new HashMap<String, String>();
	static HashMap <String, String> rainbow_table4 = new HashMap<String, String>();
	
	static MessageDigest md = SHA1constructor();
	
	static MessageDigest SHA1constructor() {
		try {
			return MessageDigest.getInstance("SHA1");
		} catch (Exception e) {
			return null;
		}
	}
	
	//-- SHA1 Hash function :-returns byte array representing the hashed value
	static byte[] sha1(String input) {
		byte[] msg = input.getBytes();
		return md.digest(msg);
	}
	
	static String byteArrayToHex(byte[] input){
		return javax.xml.bind.DatatypeConverter.printHexBinary(input);
	} 
	
	static byte[] hexToByteArray(String s){
		return javax.xml.bind.DatatypeConverter.parseHexBinary(s);
	}
	
	static byte[] sha1(byte[] input){
		return md.digest(input);	
	}
	
	static byte[] reduce(byte[] digest, int i) throws UnsupportedEncodingException{
		if (i%4==0){
			return new byte[]{(byte)(digest[0]), (byte)(digest[1]), (byte)(digest[2])};
		}else if(i%4==1){
			return new byte[]{(byte)(digest[2]), (byte)(digest[3]), (byte)(digest[4])};
		}else if (i%4==2){
			return new byte[]{(byte)(digest[3]), (byte)(digest[4]), (byte)(digest[5])};
		}else{
			String hex_digest = byteArrayToHex(digest);
			int reduction = hex_digest.hashCode();
			byte[] arr = bytefy(reduction);
			return arr;
		}
	}
		
	
	
	static byte[] bytefy(int number){
		byte b1,b2,b3;
		b3 = (byte)(number & 0xFF);
		b2 = (byte)((number >> 8) & 0xFF);
		b1 = (byte)((number >> 16) & 0xFF);
		return new byte[]{b1, b2, b3};
	}
	
	//for now store entire word and entire digest, to optimize later
	static void insertToHashMap(String word, String lastdigest,int i){
		switch (i) {
		case 1:
			rainbow_table.put(word, lastdigest);
			break;
			
		case 2:
			rainbow_table2.put(word, lastdigest);
			break;
			
		case 3:
			rainbow_table3.put(word, lastdigest);
			break;
			
		case 4:
			rainbow_table4.put(word, lastdigest);
			break;
		default:
			break;
		}
		
	}

	static void encodeRT2File() throws IOException{

//			PrintWriter f = new PrintWriter("rainbow2.dat");
//			rainbow_table.forEach((k,v) -> f.print(k+" "+v+" "));
			FileOutputStream output = new FileOutputStream("compressed.gzip");
			Writer f = new OutputStreamWriter(new GZIPOutputStream(output),"UTF-8");
			rainbow_table.forEach((k,v) -> {try{
												f.write(k+" "+v+" ");
											}catch(Exception e){
												System.err.println("Error occured");
											}});
			f.close();
			output.close();
		
	}
	 
	
	//Usage: RAINBOW.java <chain-num> <table-size>
	public static void main (String[] args) throws Exception {
		final int NUM_CHAIN = Integer.parseInt(args[0]);
		final int TABLE_SIZE = Integer.parseInt(args[1]);
		
		int initial = 0;
		byte[] digest = null;
		byte[] digest2 = null;
		byte[] digest3 = null;
		byte[] digest4 = null;
		int j = 0;
		while (j < TABLE_SIZE){
			ArrayList<String> chainOfWords = new ArrayList<String>();
			byte[] byteword = bytefy(initial);
			ArrayList<String> chainOfWords2 = new ArrayList<String>();
			byte[] byteword2 = bytefy(initial);
			ArrayList<String> chainOfWords3 = new ArrayList<String>();
			byte[] byteword3 = bytefy(initial);
			ArrayList<String> chainOfWords4 = new ArrayList<String>();
			byte[] byteword4 = bytefy(initial);
			
			String firstword = byteArrayToHex(byteword);
			//System.out.println(byteArrayToHex(byteword));
			
			chainOfWords.add(firstword);
			digest = sha1(byteword);
			chainOfWords2.add(firstword);
			digest2 = sha1(byteword2);
			chainOfWords3.add(firstword);
			digest3 = sha1(byteword3);
			chainOfWords4.add(firstword);
			digest4 = sha1(byteword4);
			int k = 0;
			boolean discard = false;
			for (int i=0; i < NUM_CHAIN-1 ; i++) {
					k++;
					byteword =  reduce(digest,i);
					digest = sha1(byteword);
					chainOfWords.add(byteArrayToHex(byteword));
					byteword2 = reduce(digest,i+1);
					digest2 = sha1(byteword2);
					chainOfWords2.add(byteArrayToHex(byteword2));
					byteword3 = reduce(digest,i+2);
					digest3 = sha1(byteword3);
					chainOfWords3.add(byteArrayToHex(byteword3));
					byteword4 = reduce(digest,i+3);
					digest4 = sha1(byteword4);
					chainOfWords4.add(byteArrayToHex(byteword4));
			}
			if (discard == false){
				//store the last digest first word
				String digeststr = byteArrayToHex(digest);
				insertToHashMap(firstword, digeststr, 1);
				chainOfWords.forEach(a -> unique_words.put(a, 1));
				
				String digeststr2 = byteArrayToHex(digest2);
				insertToHashMap(firstword, digeststr2, 2);
				chainOfWords2.forEach(a -> unique_words.put(a, 1));
				
				String digeststr3 = byteArrayToHex(digest3);
				insertToHashMap(firstword, digeststr3, 3);
				chainOfWords3.forEach(a -> unique_words.put(a, 1));
				
				String digeststr4 = byteArrayToHex(digest4);
				insertToHashMap(firstword, digeststr4, 4);
				chainOfWords4.forEach(a -> unique_words.put(a, 1));
				//System.out.println(j);
				j++;
			}
			
			digest = null;
			byteword = null;
			digest2 = null;
			byteword2 = null;
			digest3 = null;
			byteword3 = null;
			digest4 = null;
			byteword4 = null;
			initial += 1;		
		}
		encodeRT2File();
		System.out.println("total unique words " + unique_words.size());
	}
	
}


