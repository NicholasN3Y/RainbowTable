package rainbowtable;

import java.util.*;

import com.sun.glass.ui.TouchInputSupport;

import sun.security.util.Length;

import java.awt.print.Printable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


/*
 * A data structure to hold the rainbow table
 * each entry is a 24-bit string, hence a total of 2^24 possible entries
 * every entry is hashed using SHA1
 */

public class RAINBOW {
	
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
	
	static byte[] sha1 (byte[] input){
		return md.digest(input);	
	}
	
	static byte[] reduce(byte[] digest) throws UnsupportedEncodingException{
		byte[] char1 = Arrays.copyOfRange(digest, 0, 1);
		byte[] char2 = Arrays.copyOfRange(digest, 1, 2);
		byte[] char3 = Arrays.copyOfRange(digest, 2, 3);
		byte[] word = new byte[char1.length + char2.length + char3.length];
		System.arraycopy(char1, 0, word, 0, 1);
		System.arraycopy(char2, 0, word, 1, 1);
		System.arraycopy(char3, 0, word, 2, 1);
		//System.out.println(byteArrayToHex(word));
		return word;
	}
	
	//Usage: RAINBOW.java <chain-num> <table-size> <starting word>
	public static void main (String[] args) throws UnsupportedEncodingException {
		final int NUM_CHAIN = (int) Math.pow(2, Integer.parseInt(args[0]));
		final int TABLE_SIZE = (int) Math.pow(2, Integer.parseInt(args[1]));
		Random r = new Random();
		HashMap <String,Integer> a = new HashMap<String, Integer>();
		int k = 0;
		
		String word = args[2];
		int useless = 0;
		byte[] byteword = null;
		byte[] digest = null;
		int sums = 0;
		byte[] initial_word = args[2].getBytes();
		for (int j=0; j < TABLE_SIZE; j++){
			for (int i=0; i< NUM_CHAIN; i++) {
				k++;
				if (byteword == null){
					digest = sha1(word);
					
					//System.out.println(byteArrayToHex(digest));
					word = byteArrayToHex(reduce(digest));
					if (a.containsKey(word)){
						//System.out.println(k+" ");
						if (k == 1){
							useless++;
						}
						sums += k;
						break;
					}else{
						a.put(word, 1);
						//System.out.println("ok");
					}
				}else{
					digest = sha1(byteword);
					//System.out.println("digest after hash " + byteArrayToHex(digest));
					byteword = reduce(digest);
					//System.out.println("word after reduce " + byteArrayToHex(byteword));
					if (a.containsKey(byteArrayToHex(byteword))){
						//System.out.println(k+" ");
						if (k == 1){
							useless++;
						}
						sums += k;
						break;
					}else{
						a.put(byteArrayToHex(byteword), 1);
						//System.out.println("ok");
					}
				}
			}
			
			if (initial_word[2] == 255){
				initial_word[0] += 1;
				initial_word[2] = 0;
			}else{
				initial_word[2] +=1;
				if (r.nextBoolean()){
					initial_word[1] += 1;
				}else{
					initial_word[1] = (byte)(initial_word[1] + initial_word[0] % 256);
				}
			}
			//byteword = initial_word;
			byteword = Arrays.copyOfRange(digest, 4, 7);
			//System.out.println("new starting "+j+" " + byteArrayToHex(byteword));
			k = 0;
		}
		System.out.println("average chain Length "+ (sums/TABLE_SIZE));
		System.out.println(useless);
	}
	
}


