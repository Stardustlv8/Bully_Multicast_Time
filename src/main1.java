
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author avi
 */
public class main1 {
    public static void main(String args[]){
        Date fecha = new Date();
        LocalTime hora = LocalTime.now();
        
        DatagramPacket paquete;
        MulticastSocket socket;
        
        InetAddress host;
        System.out.println(""+hora);
        
        try {
            socket =  new MulticastSocket(5000);
            host = InetAddress.getByName("224.0.0.5");
            socket.joinGroup(host); 
            
            byte buffer[]=("Coordinador "+10+" "+hora.minusHours(0)).getBytes();
            paquete = new DatagramPacket(buffer,buffer.length,host,5000);
            
            socket.send(paquete);
            
            buffer=new byte[30];
            paquete = new DatagramPacket(buffer,buffer.length);
            socket.receive(paquete);
            
            String mensaje= new String(paquete.getData());
            String msg[]=mensaje.split(" ");
            
            for(int i=0;i<msg.length;i++){
                System.out.println(" "+msg[i]);
            }
            
            
        } catch (IOException ex) {
        }
    }
}
