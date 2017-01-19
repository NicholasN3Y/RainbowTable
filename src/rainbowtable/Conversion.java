package rainbowtable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class Conversion {
	static String toBase64(byte[] a){
		return Base64.getEncoder().encodeToString(a);
	}
	
	static String toBase64(String a){
		return toBase64(hexToByteArray(a));
	}
	
	static String byteArrayToHex(byte[] input){
		return javax.xml.bind.DatatypeConverter.printHexBinary(input);
	} 

	static byte[] hexToByteArray(String s){
		return javax.xml.bind.DatatypeConverter.parseHexBinary(s);
	}
	
	public static boolean compareBase64(){
		return true;
	}
	
	  private static void decompressGzipFile(String gzipFile, String newFile) {
	        try {
	            FileInputStream fis = new FileInputStream(gzipFile);
	            GZIPInputStream gis = new GZIPInputStream(fis);
	            FileOutputStream fos = new FileOutputStream(newFile);
	            byte[] buffer = new byte[1024];
	            int len;
	            while((len = gis.read(buffer)) != -1){
	                fos.write(buffer, 0, len);
	            }
	            //close resources
	            fos.close();
	            gis.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	    }

	public static void compressGZIPfile(String file, String gzip) {
		try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzip);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}
