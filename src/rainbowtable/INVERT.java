package rainbowtable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.org.apache.bcel.internal.generic.StackInstruction;

import rainbowtable.RAINBOW;

/*
 * Program such that on input of 5000 digests, can make use of RAINBOW.java to quickly find a preimage
 */
public class INVERT {
	static HashMap<String, ArrayList<Integer>> RTTable = new HashMap<String, ArrayList<Integer>>();
	static ArrayList<String> RTchainend1 = new ArrayList<String>();
	static ArrayList<String> RTchainend2 = new ArrayList<String>();
	static ArrayList<String> RTchainend3 = new ArrayList<String>();
	static ArrayList<String> RTchainend4 = new ArrayList<String>();
	static int num_hashes = 0;
	static int global_success_total = 0;
	static int global_hashes = 0;

	private static void loadR_Table(String RTfilepath) throws Exception{
		String filename = RTfilepath;
		if (RTfilepath == null){
			filename = "rainbow.dat";
		}
		FileInputStream input = new FileInputStream(new File(filename));
		InputStream uncompressed = new GZIPInputStream(input);
		Reader reader = new InputStreamReader(uncompressed);
		char[]dig = new char[10];
		int pointer =0;
		int k = 0;;
		for (int length = 0;(length = reader.read(dig)) > 0;){
			pointer+=length;
			
			String digest = null;
			digest = new String(dig);
			assert (digest.length()==10);
			if (k<56500){
				RTchainend1.add(digest);
			}else if (k< 2*56500){
				RTchainend2.add(digest);
			}else if(k < 3*56500){
				RTchainend3.add(digest);
			}else{
				RTchainend4.add(digest);
			}
			k++;
		}
				
		System.out.println("Done building rainbow table");
		System.out.println(RTchainend1.get(0));
		//invertlisting of the terms
	}

	private static void initiateQuerySequence(FileReader queries) throws IOException{
		
		BufferedReader r = new BufferedReader(queries);
		String a = null;
		int solved= 0;
		int cardinality = 0;
		try{
		while ((a = r.readLine()) != null && (a.length() > 15)){
			cardinality++;
			a = a.trim();
			String[] tokens = a.split(" ");
			String digest = String.join("", tokens);
			num_hashes = 0;
			String result = "0";
			byte[] word = queryTable(digest);
			try {
				result = Conversion.byteArrayToHex(word);
			}catch(Exception e){
				;
			}
			global_hashes +=num_hashes;
			if (!result.equals("0")){
				solved++;
				global_success_total+= num_hashes;
			}
			System.out.println(result +" -> "+ digest +" => "+ num_hashes+ " hashes");
			//System.out.println(result);
		}
		}catch (Exception e){
			
		}
		System.out.println("solved = "+solved);
		System.out.println("total hashes invoked for successful =" + global_success_total);
		System.out.println("total hashes invoked overall =" + global_hashes);
		double F = solved * Math.pow(23,2) / global_success_total ;
		System.out.println("F = "+F);
	}

	private static byte[] queryTable(String digest_query) throws UnsupportedEncodingException{
		byte[] res = new byte[3];

		for (int i = 199; i>=0; i--){
			String chain1 = findChainFirstWord(digest_query,0, i);
			int indexOnRT1 = RTchainend1.indexOf(chain1);
			if ( indexOnRT1 > -1){
				res = resolveQuery(digest_query, indexOnRT1, 0);
				if(res != null){
					return res;
				}
			}
		}
		for (int i = 199; i>=0; i--){
			String chain2 = findChainFirstWord(digest_query,1, i);
			int indexOnRT2 = RTchainend2.indexOf(chain2);
			if (indexOnRT2 > -1){
				res = resolveQuery(digest_query, indexOnRT2, 1);
				if(res != null){
					return res;
				}
			}
		}
		for (int i = 199; i>=0; i--){
			String chain3 = findChainFirstWord(digest_query,2, i);
			int indexOnRT3 = RTchainend3.indexOf(chain3);
			if (indexOnRT3 > -1){
				res = resolveQuery(digest_query, indexOnRT3, 2);
				if(res != null){
					return res;
				}
			}
		}
		for (int i = 199; i>=0; i--){
			String chain4 = findChainFirstWord(digest_query,3, i);
			int indexOnRT4 = RTchainend4.indexOf(chain4);
			if (indexOnRT4 > -1){
				res = resolveQuery(digest_query, indexOnRT4, 3);
				if(res != null){
					return res;
				}
			}
		}
		return null;

	}

	private static String findChainFirstWord(String query, int seed ,int start) throws UnsupportedEncodingException{
		byte[]word = new byte[3];
		byte[]digest = Conversion.hexToByteArray(query);
		for (int i = start; i < 199; i++){
			word = RAINBOW.reduce(digest, seed, i);
			digest = RAINBOW.sha1(word);
			num_hashes++;
		}
		String candidate = Conversion.byteArrayToHex(digest);
		String comparable = "";
		//System.out.print(RTchainend1.get(30201));
		for (int pos = 0; pos < 40; pos += 4){
			comparable = comparable+candidate.charAt(pos);
		}
		return comparable;
	}

	private static byte[] resolveQuery(String goaldigest, int words, int seed) throws UnsupportedEncodingException{
		byte[] byteword = RAINBOW.bytefy(words);
		for (int i = 0; i< 200; i++){
			byte[] digest = RAINBOW.sha1(byteword);
			num_hashes++;
			String candid = Conversion.byteArrayToHex(digest);
			//System.out.println("digest "+Conversion.byteArrayToHex(digest));
			if (candid.equals(goaldigest)){
				return byteword;
			}
			byteword = RAINBOW.reduce(digest,seed, i);
			//System.out.println("reduced word "+Conversion.byteArrayToHex(byteword));
		}
		//System.out.println("not on this chain");
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
			e.printStackTrace();
		}

	}

}
