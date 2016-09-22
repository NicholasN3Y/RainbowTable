package rainbowtable;

import java.util.*;
import java.util.zip.GZIPOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import rainbowtable.Conversion.*;


/*
 * A data structure to hold the rainbow table
 * each entry is a 24-bit string, hence a total of 2^24 possible entries
 * every entry is hashed using SHA1
 */

public class RAINBOW {
	static HashMap <String, Integer> unique_words = new HashMap<String, Integer>();
	//static HashMap <String, Integer> localunique_words2 = new HashMap<String, Integer>();
	static HashMap <String, String> rainbow_table = new HashMap<String, String>();
	static HashMap <String, String> rainbow_table2 = new HashMap<String, String>();
	static HashMap <String, String> rainbow_table3 = new HashMap<String, String>();
	//	static HashMap <String, String> rainbow_table4 = new HashMap<String, String>();
	//	static HashMap <String, String> rainbow_table5 = new HashMap<String, String>();

	static MessageDigest md = SHA1constructor();
	static MessageDigest hash = MD5constructor();


	static MessageDigest MD5constructor() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			return null;
		}
	}

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

	static byte[] sha1(byte[] input){
		return md.digest(input);	
	}

	static byte[] reduce(byte[] digest, int seed, int chainlength) throws UnsupportedEncodingException{
		byte[] a = hash.digest(digest);
		int factor = digest.hashCode();
		int flag = (chainlength)%3;
		if (flag == 1 ){
			return new byte[]{(byte)((a[0])*factor %256 ), (byte)((a[1]+a[2]-a[3]-seed)*factor %256), (byte)((a[3]+a[2]-a[1]-seed)*factor %256)};
		}else if (flag == 2){
			return new byte[]{(byte)(a[7]-seed * factor), (byte)(a[8]+seed * factor), (byte)(a[9]- seed * factor)};
		}else{
			return new byte[]{(byte)(a[9]+seed), (byte)(a[10]+seed), (byte)(a[11]+seed)};
		}
	}
	
	static byte[] reduce2(byte[] digest, int seed, int chainlength) throws UnsupportedEncodingException{
		byte[] a = hash.digest(digest);
		int factor = digest.hashCode();
		int flag = (chainlength)%3;
		if (flag == 1 ){
			return new byte[]{(byte)((a[1])*factor %256 ), (byte)((a[1]+a[0]-a[4]-seed)*factor %256), (byte)((a[1]+a[6]-a[8]-seed)*factor %256)};
		}else if (flag == 2){
			return new byte[]{(byte)(a[3]-seed), (byte)(a[9]+seed), (byte)(a[7]-seed)};
		}else{
			return new byte[]{(byte)(a[5]+seed), (byte)(a[11]+seed), (byte)(a[4]+seed)};
		}
	}

	//	static byte[] reduce2(byte[] digest, int seed) throws UnsupportedEncodingException{
	//		byte[] a = hash.digest(digest);
	//		return new byte[]{(byte)(a[9]-seed), (byte)(a[5]-seed), (byte)(a[3]-seed)};
	//	}

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
//		case 3:
//			rainbow_table3.put(word, lastdigest);
//			break;
//		case 4:
//			rainbow_table4.put(word, lastdigest);
//			break;

		default:
			break;
		}

	}

	static void encodeRT2File() throws IOException{

		FileOutputStream output = new FileOutputStream("compressed.gzip");
		Writer f = new OutputStreamWriter(new GZIPOutputStream(output),"UTF-8");
		rainbow_table.forEach((k,v) -> {try{
			f.write(k+" "+v+" ");
			//System.out.println(k);
		}catch(Exception e){
			System.err.println("Error occured");
		}});
		rainbow_table2.forEach((k,v) -> {try{
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
		int rounds = 1;
		int n = 0;
		while (n<2){
			n++;
			int initial = 0;
			System.out.println("round" + rounds);
			byte[] digest = null;
			byte[] digest2 = null;
			byte[] digest3 = null;
			
			int j = 0;
			boolean once = false;
			//localunique_words2.clear();
			System.out.println("1st table, seed = "+ (rounds));
			while (j < TABLE_SIZE){
				if (j%1000 == 0 && once == false){
					once = true;
					//System.out.println("unique words " + unique_words.size());

				}
				ArrayList<String> chainOfWords = new ArrayList<String>();
				byte[] byteword = bytefy(initial);
				String firstword = Conversion.byteArrayToHex(byteword);
				chainOfWords.add(firstword);
				digest = sha1(byteword);
				int k = 0;
				for (int i=0; i < NUM_CHAIN-1 ; i++) {
					k++;
					byteword =  reduce(digest, rounds, k);
					digest = sha1(byteword);
					chainOfWords.add(Conversion.byteArrayToHex(byteword));
				}
				//store the last digest first word
				String digeststr = Conversion.byteArrayToHex(digest);
				String lastDig = "";
				//System.out.println("hello " + digeststr);
				for (int pos=0; pos < 40; pos +=4){
					lastDig = lastDig+digeststr.charAt(pos);
				}
				//System.out.println("hello " + lastDig);
				//digest stored as base64.
				
				insertToHashMap(firstword, digeststr, 1);
				chainOfWords.forEach(a -> unique_words.put(a, 1));
				//System.out.println(j + " " + k);
				once = false;
				j++;
				initial += 5;
			}
			System.out.println("unique words " + unique_words.size());
			
			initial = 0;
			j = 0;
			once = false;
			//localunique_words2.clear();
			System.out.println("2nd table, seed = "+ (rounds+1));
			while (j < TABLE_SIZE){
				if (j%1000 == 0 && once == false){
					once = true;
					//System.out.println("unique words " + unique_words.size());
				}
				int k = 0;
				ArrayList<String> chainOfWords2 = new ArrayList<String>();
				byte[] byteword2 = bytefy(initial);
				String firstword = Conversion.byteArrayToHex(byteword2);
				chainOfWords2.add(firstword);
				digest2 = sha1(byteword2);
				k = 0;
				for (int i=0; i < NUM_CHAIN-1 ; i++) {
					k++;
					byteword2 = reduce(digest2, rounds+1, k);
					digest2 = sha1(byteword2);
					chainOfWords2.add(Conversion.byteArrayToHex(byteword2));
					chainOfWords2.add(Conversion.byteArrayToHex(byteword2));
				}
				String digeststr2 = Conversion.byteArrayToHex(digest2);
				insertToHashMap(firstword, digeststr2, 2);
				chainOfWords2.forEach(a -> unique_words.put(a, 1));
				once = false;
				j++;
				initial -= 2;
			}
			System.out.println("unique words " + unique_words.size());
			
			j = 0;
			once = false;
			initial = 4545;
			System.out.println("3st table, seed = "+ (rounds+2));
			while (j < TABLE_SIZE){
				if (j%1000 == 0 && once == false){
					once = true;
					//System.out.println("unique words " + unique_words.size());
				}
				ArrayList<String> chainOfWords3 = new ArrayList<String>();
				byte[] byteword3 = bytefy(initial);
				String firstword = Conversion.byteArrayToHex(byteword3);
				chainOfWords3.add(firstword);
				digest3 = sha1(byteword3);
				int k = 0;
				for (int i=0; i < NUM_CHAIN-1 ; i++) {
					k++;
					if (rounds%2==0){
						byteword3 =  reduce2(digest3, rounds-2, k);
					}else{
						byteword3 =  reduce(digest3, rounds+2, k);
					}
					digest3 = sha1(byteword3);
					chainOfWords3.add(Conversion.byteArrayToHex(byteword3));
				}
				//store the last digest first word
				String digeststr3 = Conversion.byteArrayToHex(digest3);
				insertToHashMap(firstword, digeststr3, 3);
				chainOfWords3.forEach(a -> unique_words.put(a, 1));
				//System.out.println(j + " " + k);
				once = false;
				j++;
				initial +=7 ;
			}
			
			System.out.println("unique words "+ unique_words.size());
			rounds += 2;
		}


		encodeRT2File();
		System.out.println("total unique words " + unique_words.size());
	}
}


