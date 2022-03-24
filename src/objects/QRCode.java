public class QRCode{
    private int id;
    //pessoa a quem vai ser paga amount
    private int userID;
    private double amount;


    public QRCode(int id, double amount , int userID){
        this.id = id;
        this.userID = userID;
        this.amount = amount;
    }

   /*TODO in database public void obtainQRCode(){
        return a imagem
   }
   */ 

   public int getID(){
       return id;
   }

}