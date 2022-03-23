public class QRCode{
    private int id;
    //pessoa a quem vai ser paga amount
    private int userID;
    private double amount;
    private int requestID;

    public QRCode(int id, int useriD, double amount, int requestID){
        this.id = id;
        this.userID = userID;
        this.amount = amount;
        this.requestID = requestID;
    }

   /*TODO in database public void obtainQRCode(){
        return a imagem
   }
   */ 
   public int getRequestID(){
       return requestID;
   }

}