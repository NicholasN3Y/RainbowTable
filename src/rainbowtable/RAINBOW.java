package rainbowtable;

import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


/*
 * A data structure to hold the rainbow table
 * each entry is a 24-bit string, hence a total of 2^24 possible entries
 * every entry is hashed using SHA1
 */

public class RAINBOW {
	static HashMap <String, Integer> unique_words = new HashMap<String, Integer>();
	static HashMap<String, Integer> longdigest = new HashMap<String,Integer>();
	static HashMap<String, Integer> trimmed = new HashMap<String,Integer>();
	static ArrayList <String> rainbow_table = new ArrayList <String>();
	static int reduce1 = 0;
	static int reduce2 = 0;
	static int reduce3 = 0;
	
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
		int factor = a[0]*10+a[1]*100+a[2]*50+a[3]*2+a[4]*7+a[5]*1+a[6]*13+a[7]*2+a[8]*12+a[9]*3+a[10]*1;
		int flag = (chainlength)%3;
		if (flag == 2){
			reduce1 ++;
			return new byte[]{(byte)(a[7]-seed * factor+1), (byte)(a[8]+seed * factor+1), (byte)(a[9]- seed * factor+1)};
		}else if (flag == 1 ){
			reduce2 ++;
			return new byte[]{(byte)((a[0])*factor %256 ), (byte)((a[1]+a[2]-a[3]-seed)*factor %256), (byte)((a[3]+a[2]-a[1]-seed)*factor %256)};
		}else{
			reduce3 ++;
			return new byte[]{(byte)(a[9]+seed-25), (byte)(a[10]+seed-25), (byte)(a[11]+seed-25)};
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
	static void insertToArray(String word, String lastdigest,int i){
		switch (i) {
		case 1:
			rainbow_table.add(lastdigest);
			break;
		default:
			break;
		}
	}

	static void encodeRT2File() throws IOException{

		FileOutputStream output = new FileOutputStream("compressed.zip");
		GZIPOutputStream out = new GZIPOutputStream(output);
		writeRT(rainbow_table, out);
		//writeRT(rainbow_table2, f);
		out.close();
		output.close();

	}
	
	private static void writeRT(ArrayList<String> rt, GZIPOutputStream f){
		rt.forEach((v) -> {try{
			System.out.println(v);
			f.write(v.getBytes("UTF-8"));
		}catch(Exception e){
			System.err.println("Error occured");
		}});
	}


	//Usage: RAINBOW.java <chain-num> <table-size>
	public static void main (String[] args) throws Exception {
		final int NUM_CHAIN = Integer.parseInt(args[0]);
		final int TABLE_SIZE = Integer.parseInt(args[1]);
		//final int TABLE_SIZE = 1;
		int rounds = 1;
		int n = 0;
		while (n<1){
			int initial = 0;
			System.out.println("round" + rounds);
			System.out.println("1st table, seed = "+ (rounds));
			byte[] digest = null;
			int j = 0;
			//localunique_words2.clear();
			
			while (j < TABLE_SIZE){
//				if (j%1000 == 0 && once == false){
//					once = true;
//					//System.out.println("unique words " + unique_words.size());
//
//				}
				ArrayList<String> chainOfWords = new ArrayList<String>();
				byte[] byteword = bytefy(initial);
				String firstword = Conversion.byteArrayToHex(byteword);
				chainOfWords.add(firstword);
				digest = sha1(byteword);
				//System.out.println("digest " + Conversion.byteArrayToHex(digest));
				int k = 0;
				for (int i=0; i < NUM_CHAIN-1 ; i++) {
					k++;
					byteword =  reduce(digest, rounds, k);
					digest = sha1(byteword);
					//System.out.println(Conversion.byteArrayToHex(byteword));
					chainOfWords.add(Conversion.byteArrayToHex(byteword));
				}
				
				//store the last digest first word
				String digeststr = Conversion.byteArrayToHex(digest);
				//print last byteword
				//print last digest
				//System.out.println(firstword+ " "+ digeststr);
				
				
				String lastDig = "";
				for (int pos=0; pos < 40; pos += 4){
					lastDig = lastDig+digeststr.charAt(pos);
				}
				//System.out.println(lastDig);
				longdigest.put(digeststr, 1);
				trimmed.put(lastDig, 1);
				//digest stored as base64.
				//System.out.println(lastDig);
				insertToArray(firstword, lastDig, 1+3*n);
				chainOfWords.forEach(a -> unique_words.put(a, 1));
				j++;
				initial += 2;
			}
			System.out.println("unique words " + unique_words.size());
			
//			initial = 0;
//			j = 0;
//			once = false;
//			//localunique_words2.clear();
//			System.out.println("2nd table, seed = "+ (rounds+1));
//			while (j < TABLE_SIZE){
//				if (j%1000 == 0 && once == false){
//					once = true;
//					//System.out.println("unique words " + unique_words.size());
//				}
//				int k = 0;
//				ArrayList<String> chainOfWords2 = new ArrayList<String>();
//				byte[] byteword2 = bytefy(initial);
//				String firstword = Conversion.byteArrayToHex(byteword2);
//				chainOfWords2.add(firstword);
//				digest2 = sha1(byteword2);
//				k = 0;
//				for (int i=0; i < NUM_CHAIN-1 ; i++) {
//					k++;
//					byteword2 = reduce(digest2, rounds+1, k);
//					digest2 = sha1(byteword2);
//					chainOfWords2.add(Conversion.byteArrayToHex(byteword2));
//					chainOfWords2.add(Conversion.byteArrayToHex(byteword2));
//				}
//				String digeststr2 = Conversion.byteArrayToHex(digest2);
//				String lastDig2 = "";
//				for (int pos=0; pos < 40; pos +=4){
//					lastDig2 = lastDig2+digeststr2.charAt(pos);
//				}
//				longdigest.put(digeststr2, 1);
//				trimmed.put(lastDig2, 1);
//				insertToHashMap(firstword, lastDig2, 2+3*n);
//				chainOfWords2.forEach(a -> unique_words.put(a, 1));
//				once = false;
//				j++;
//				initial -= 2;
//			}
//			System.out.println("unique words " + unique_words.size());
//			
//			
			
	//		System.out.println("unique words "+ unique_words.size());
		//	rounds += 2;
			n++;
		}

		//System.out.println(longdigest.size()+" "+trimmed.size()+" ");
		//System.out.println(rainbow_table.size());
		encodeRT2File();
		System.out.println(reduce1+" "+ reduce2+" "+reduce3);
		System.out.println("total unique words " + unique_words.size());
	}
}


