package objects;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.naming.spi.DirStateFactory.Result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.common.HybridBinarizer;

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
      
   public static void generateQRcode(String data, Path path, String charset, int h, int w) throws WriterException, IOException  
   { 
   ByteMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);  
   MatrixToImageWriter.writeToPath(matrix, charset, path);
   }  

}