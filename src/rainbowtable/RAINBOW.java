package rainbowtable;

import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import javafx.scene.chart.PieChart.Data;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
	static HashMap<String, Integer> longdigest = new HashMap<String,Integer>();
	static HashMap<String, Integer> trimmed = new HashMap<String,Integer>();
	static ArrayList <String> rainbow_table = new ArrayList <String>();

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
		byte[] word = new byte[]{(byte)(digest[((chainlength+seed)%20)] + chainlength),(byte)(digest[(chainlength+1+seed)%20] + chainlength),(byte)(digest[((chainlength+2)+seed)%20] + chainlength)};
		return word;
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
			rainbow_table.add(lastdigest);
	}

	static void encodeRT2File() throws IOException{

		FileOutputStream output = new FileOutputStream("raw.data");
		Writer out = new OutputStreamWriter(output);
		writeRT(rainbow_table, out);
		//writeRT(rainbow_table2, f);
		out.close();
		output.close();
		Conversion.compressGZIPfile("raw.data","compressedData.gz");
	}
	
	

	private static void writeRT(ArrayList<String> rt, Writer f){
		rt.forEach((v) -> {try{
			//System.out.println(v);
			f.write(v);
		}catch(Exception e){
			System.err.println("Error occured");
		}});
	}


	//Usage: RAINBOW.java <chain-num> <table-size>
	public static void main (String[] args) throws Exception {
		final int NUM_CHAIN = Integer.parseInt(args[0]);
		final int TABLE_SIZE = Integer.parseInt(args[1]);
		//final int TABLE_SIZE = 1;
		int n = 0;

		while (n<4){
			int initial = 0;
			System.out.println((n+1)+" table, seed = "+ (n));
			byte[] digest = null;
			int j = 0;

			while (j < TABLE_SIZE){
				
				ArrayList<String> chainOfWords = new ArrayList<String>();
				byte[] byteword = bytefy(initial);
				String firstword = Conversion.byteArrayToHex(byteword);
				chainOfWords.add(firstword);
				
				for (int i=0; i < NUM_CHAIN; i++) {
					digest = sha1(byteword);
					chainOfWords.add(Conversion.byteArrayToHex(digest));
					byteword =  reduce(digest, n, i);
					chainOfWords.add(Conversion.byteArrayToHex(byteword));
				}

				//store the last digest first word
				String digeststr = Conversion.byteArrayToHex(digest);
				String lastDig = "";
				for (int pos=0; pos < 40; pos += 4){
					lastDig = lastDig+digeststr.charAt(pos);
				}
				longdigest.put(digeststr, 1);
				trimmed.put(lastDig, 1);
				insertToArray(firstword, lastDig, 1);
				System.out.println(digeststr);
				chainOfWords.remove(chainOfWords.size()-1);
				chainOfWords.forEach(a -> {if ((chainOfWords.indexOf(a))%2 == 0){
											unique_words.put(a, 1);
											}});
				j++;
				initial += 1;
			}
			System.out.println(rainbow_table.size());
			System.out.println("unique words " + unique_words.size());

			n++;
		}

		System.out.println(longdigest.size()+" "+trimmed.size()+" ");
		encodeRT2File();
		System.out.println("total unique words " + unique_words.size());
	}
}


