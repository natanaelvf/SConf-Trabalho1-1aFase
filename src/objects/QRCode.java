package objects;

import java.io.IOException;
import java.nio.file.Path;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class QRCode{
    private int id;
    private int userID;
    private double amount;


    public QRCode(int id, double amount , int userID){
        this.id = id;
        this.userID = userID;
        this.amount = amount;
    }

   public int getID(){
       return id;
   }
   
   public double getAmount() {
	   return amount;
   }
      
   public static void generateQRcode(String data, Path path, String charset, int h, int w) throws WriterException, IOException  
   { 
   BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);  
   MatrixToImageWriter.writeToPath(matrix, charset, path);
   }  

}