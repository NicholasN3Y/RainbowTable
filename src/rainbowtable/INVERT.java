package rainbowtable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import com.sun.javafx.collections.SortableList;
import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;

import javafx.animation.RotateTransitionBuilder;
import jdk.internal.dynalink.beans.StaticClass;
import rainbowtable.RAINBOW;
import sun.swing.text.TextComponentPrintable;

/*
 * Program such that on input of 5000 digests, can make use of RAINBOW.java to quickly find a preimage
 */
public class INVERT {
	static HashMap<String, ArrayList<Integer>> RTTable = new HashMap<String, ArrayList<Integer>>();
	static ArrayList<String> RTchainend = new ArrayList<String>();
	static int num_hashes = 0;
	static ArrayList<String> permuchain1 = null;
	static ArrayList<String> permuchain2 = null;
	static ArrayList<String> permuchain3 = null;
	private static void loadR_Table(String RTfilepath) throws Exception{
		String filename = RTfilepath;
		if (RTfilepath == null){
			filename = "rainbow.dat";
		}
		FileInputStream input = new FileInputStream(new File(filename));
		GZIPInputStream r = new GZIPInputStream(input);
		int i= 1;
		while(true){
			try{
				byte[] digest = new byte[10];
				int a = r.read(digest, 0, 10);
				if (a == -1){
					break;
				}
				String chain_end = new String(digest);
				RTchainend.add(chain_end);
			}catch (Exception e){
				System.out.println(e);
				break;
			}
			i++;
		}
		System.out.println("Done building rainbowtable of size"+ (i-1));
		//invertlisting of the terms;

		for (int c = 0; c < RTchainend.size(); c++){
			String dg = RTchainend.get(c);
			if (RTTable.containsKey(dg)){
				RTTable.get(dg).add(c);
			}else{
				RTTable.put(dg, new ArrayList<Integer>());
				RTTable.get(dg).add(c);
			}
			if (RTTable.size() == 1){
			System.out.println(RTchainend.get(0));
			System.out.print(dg + " " + c);
			}

		}

		r.close();
	}

	private static void initiateQuerySequence(FileReader queries) throws IOException{
		BufferedReader r = new BufferedReader(queries);
		String a = null;
		while ((a = r.readLine()) != null){
			a = a.trim();
			String[] tokens = a.split(" ");
			String digest = String.join("", tokens);
			num_hashes = 0;
			String result = queryTable(digest);
			System.out.println(result+" hashes to "+ digest);
			//printResultToFile(result);
		}
	}

	private static String queryTable(String digest) throws UnsupportedEncodingException{
		//while yet to find a end of chain
		ArrayList<Integer> candi = check(Conversion.hexToByteArray(digest));
		if (candi != null){
			String result = scanRT(candi,digest);
			if (result != null){
				return result;
			}
		}

		for (int i = 0; i < 199; i++) {
			byte[] word1 = RAINBOW.reduce(Conversion.hexToByteArray(digest), 1, i); // % = 0
			byte[] word2 = RAINBOW.reduce(Conversion.hexToByteArray(digest), 1, i+1); // % = 1
			byte[] word3 = RAINBOW.reduce(Conversion.hexToByteArray(digest), 1, i+2); // % = 2
			byte[] digest1 =  RAINBOW.sha1(word1);
			num_hashes++;
			byte[] digest2 = RAINBOW.sha1(word2);
			num_hashes++;
			byte[] digest3 = RAINBOW.sha1(word3);
			num_hashes++;
			ArrayList<Integer> ismatch = null;
			String found = null;
			if (i%3 == 1){
				ismatch = check(digest1);
				if (ismatch != null){
					found = scanRT(ismatch, digest);
				}
			}else if ((i+1)%3 == 1){
				ismatch = check(digest2);
				if (ismatch != null){
					found = scanRT(ismatch, digest);
				}
			}else if ((i+2)%3 == 1){
				ismatch = check(digest3);
				if (ismatch != null){
					found = scanRT(ismatch, digest);
				}
			}
			if (found != null){
				return found;
			}
		}
		return null;
	}

	private static ArrayList<Integer> check(byte[] candidate){
		String digest = Conversion.byteArrayToHex(candidate);
		String comparable = "";
		for (int pos=0; pos < 40; pos += 4){
			comparable = comparable+digest.charAt(pos);
		}
		return RTTable.get(comparable);
	}

	private static String scanRT(ArrayList<Integer> candidateChains, String query) throws UnsupportedEncodingException{
		for (int a:candidateChains){
			int initial = a * 2;
			int k = 0;
			byte[] byteword = RAINBOW.bytefy(initial);
			System.out.println(Conversion.byteArrayToHex(byteword));
			byte[] digest = RAINBOW.sha1(byteword);
			for (int i=0; i < 200 ; i++) {
				k++;
				String candidate = Conversion.byteArrayToHex(digest);
				System.out.println(Conversion.byteArrayToHex(byteword));
				if (candidate.equals(query)){
					return Conversion.byteArrayToHex(byteword);
				}
				byteword =  RAINBOW.reduce(digest, 1, k);
				digest = RAINBOW.sha1(byteword);
			}
		}
		return null;
	}

	public static void main(String[] args){
		try{
			if (args.length < 1 || args.length > 2){
				throw new IllegalArgumentException("\n\tHINT:-usage: INVERT.java <path/to/queries>");
			}
			FileReader queries = new FileReader(new File(args[0]));
			if (args.length == 2){
				loadR_Table(args[1]);
			}else{
				loadR_Table(null);
			}
			initiateQuerySequence(queries);
		}catch (Exception e){
			System.out.println(e.getMessage());
		}

	}

}
