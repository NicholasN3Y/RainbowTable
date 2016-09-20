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
import java.util.zip.GZIPInputStream;

import rainbowtable.RAINBOW;

/*
 * Program such that on input of 5000 digests, can make use of RAINBOW.java to quickly ind a preimage
 */
public class INVERT {
	static ArrayList<String> RTchainhead = new ArrayList<String>();
	static ArrayList<String> RTchainend = new ArrayList<String>();
	static int num_hashes = 0;
	/*
	 * args = path of input file
	 */
	
	/*
	 * usage: INVERT.java </path/to/queries>
	 */
	private static void loadR_Table(String RTfilepath) throws Exception{
		String filename = RTfilepath;
		if (RTfilepath == null){
			filename = "rainbow.dat";
		}
		FileInputStream input = new FileInputStream(new File(filename));
		Reader r = new InputStreamReader(new GZIPInputStream(input),"UTF-8");
		int i = 0;
		while(true){
			try{
				char[] word = new char[6];
				char[] enddigest = new char[40];
				int a = r.read(word, 0, 6);
				if (a == -1){
					break;
				}
				r.skip(1);
				r.read(enddigest, 0, 40);
				r.skip(1);
				String chain_head = new String(word);
				String chain_end = new String(enddigest);
				RTchainhead.add(chain_head);
				RTchainend.add(chain_end);
				//System.out.println(chain_end + " digest chain starts from " + chain_head);
				i++;
			}catch (Exception e){
				System.out.println(e);
				break;
			}
		}
		System.out.println("Done building rainbowtable of size"+ (i+1));
		
		r.close();
	}
	
	private static void initiateQuerySequence(FileReader queries) throws IOException{
		BufferedReader r = new BufferedReader(queries);
		String a = null;
		while ((a = r.readLine()) != null){
			a = a.trim();
			String[] tokens = a.split(" ");
			String digest = String.join("", tokens);
			String result = findWord(digest);
			System.out.println(result);
			//printResultToFile(result);
		}
	}
	
	//query is the SHA1-digest
	private static String findWord(String query) throws UnsupportedEncodingException{
		num_hashes = 0;
		int chains_tried = 0;
		String digest = query;
		//find the end of the chain
		ArrayList<String> chains = findNextChain(digest);
		//found end of chain
		int k = 0;
		while (k < 5){
			for(String chainhead:chains.subList(1, chains.size())){
				String result = attemptDecoding(chainhead, query);
				if (result != null){
					return result;
				}
			}
		//all this query failed, continue on chain.	
			chains = findNextChain(chains.get(0));
			k++;
		}
		return null;
	}
	
	private static String attemptDecoding(String head, String query) throws UnsupportedEncodingException{
			byte[] querybyte = RAINBOW.hexToByteArray(query);
			byte[] wordbyte = RAINBOW.hexToByteArray(head);
			int internalhash = 0;
			byte[] bytedigest = RAINBOW.sha1(head);
			internalhash++;
			while (internalhash < 1005){
				//System.out.println(RAINBOW.byteArrayToHex(bytedigest) + " is to "+ RAINBOW.byteArrayToHex(querybyte));
				if	(!bytedigest.equals(querybyte)){
					wordbyte = RAINBOW.reduce(bytedigest,1);
					bytedigest = RAINBOW.sha1(wordbyte);
					internalhash++;
				}else{
					break;
				}
			}
			if	(bytedigest.equals(querybyte)){
				System.out.println("yay succeed");
				return RAINBOW.byteArrayToHex(wordbyte);
			}else{
				return null;
			}
			
			
	}
	
	private static ArrayList<String> findNextChain(String digest) throws UnsupportedEncodingException{
		ArrayList<String> chainHeads = new ArrayList<String>();
		//while yet to find a end of chain
		while (!RTchainend.contains(digest)){
			byte[] word = RAINBOW.reduce(RAINBOW.hexToByteArray(digest),1);
			digest = RAINBOW.byteArrayToHex(RAINBOW.sha1(word));
			num_hashes++;
		}
		chainHeads.add(digest);
		//found an end of chain, extract all the index that corresponds to such end of chain.
		for (int i = 0; i < RTchainend.size(); i++){
			if (RTchainend.get(i).equals(digest)){
				chainHeads.add(RTchainhead.get(i));
			}
		}
		return chainHeads;
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
