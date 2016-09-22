package rainbowtable;
import java.util.Base64;


public class Conversion {
	public static String toBase64(byte[] a){
		return Base64.getEncoder().encodeToString(a);
	}
	
	static String byteArrayToHex(byte[] input){
		return javax.xml.bind.DatatypeConverter.printHexBinary(input);
	} 

	static byte[] hexToByteArray(String s){
		return javax.xml.bind.DatatypeConverter.parseHexBinary(s);
	}
	
	public static void main(String[] args){
		String hex = "D1725392ADFAF1361C9015546FFB4FC44391B1A7";
	}
	
	public static boolean compareBase64(){
		return true;
	}
}
