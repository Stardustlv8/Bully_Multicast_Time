
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author avi
 */
public class Peer implements Runnable{
    private MulticastSocket socket;
    private InetAddress host;
    private int port = 5000;
    
    private int id;
    private boolean Coordinador=false;
    private boolean Elector_lock=false;
    private boolean imp=true;
    
    private int count=0;
    
    private JTextArea ar1=null;
    private String mensaje="";
    
    public Peer(int id,boolean c){
        this.id=id;
        this.Coordinador=c;
        
        try {
            socket = new MulticastSocket(port);
            host = InetAddress.getByName("230.0.0.5");
            socket.joinGroup(host);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Se envia el mensaje para informar a todos los peer que es el coordinador actual
     * @param id identificador del peer que se declara coordinador.
     */
    private void msg_Coordinador(int id){
        LocalTime hora= LocalTime.now();
        byte buffer []= ("Coordinador "+id+" "+hora).getBytes();
        DatagramPacket paquete = new DatagramPacket(buffer,buffer.length,host,port);
        try {
            socket.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Se envia el mensaje para el proceso de eleccion a todos los peer informando
     * que es un posible candidato a coordinador
     * @param id identificador del peer que pretende ser coordinador
     */
    private void msg_Eleccion(int id){
        LocalTime hora= LocalTime.now();
        byte buffer[] = ("Eleccion "+id+" "+hora).getBytes();
        DatagramPacket paquete = new DatagramPacket(buffer,buffer.length,host,port);
        try {
            socket.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void set_Area(JTextArea ar){
        ar1=ar;
    }
    
    @Override
    public void run() {
        /**
         * Thread-1
         * 
         * Si es coordinador
         * Encargado de enviar el mensaje notificando a todos los peers
         * 
         * Si no es coordinador
         * Encargado de esperar el mensaje del coordinador actual
         * monitorear tanto mensajes Coordinador como mensajes Eleccion 
         */
        Runnable T_coordinador = new Runnable(){
            
            @Override
            public void run() {
                try {
                    DatagramPacket paquete;
                    LinkedList pack;
                    socket.setTimeToLive(0);
                    while(true){
                            if(Coordinador){
                                msg_Coordinador(id);
                                
                            }
                            if(!Coordinador){
                                try {
                                
                                    byte buffer[]= new byte[30];
                                    paquete=new DatagramPacket(buffer,buffer.length);
                                    socket.setSoTimeout(3000);
                                    socket.receive(paquete);
                                    
                                    pack = to_Split_Datagram(paquete.getData());
                                
                                    String msg = String.valueOf(pack.get(0));
                                    int id_rec = Integer.parseInt(String.valueOf(pack.get(1)));
                                    String hora = String.valueOf(pack.get(2));
                                    
                                    if(msg.equalsIgnoreCase("Coordinador")){
                                        Elector_lock=false;
                                            mensaje=("Hora actual: "+hora+"\n");
                                            if(ar1!=null)
                                                ar1.setText(mensaje);
                                            
                                    }
                                    if(msg.equalsIgnoreCase("Eleccion")){
                                        if(count>2){
                                            Coordinador=true;
                                                mensaje+=("Coordinador");
                                                if(ar1!=null)
                                                    ar1.setText(mensaje);                                            
                                        }
                                        if(id<id_rec){
                                            Elector_lock=true;
                                        }
                                        if(id==id_rec){
                                            count++;
                                        }
                                    }
                                } catch (IOException ex) {
                                    
                                    if(!Elector_lock){
                                        msg_Eleccion(id);
                                    }
                                }
                            }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };new Thread(T_coordinador).start();
        
        /**
         * Thread-2
         * Encargado de monitorear los mensajes de posibles candidatos a coordinador
         * si recibe un id mayor este se queda a la espera del  nuevo coordinador
         */
    }
    /**
     * Separa la informacion del datagrama
     * @param cad arreglo de bytes 
     * @return LinkedList usada para almacenar el resultado del cast
     */
    private LinkedList to_Split_Datagram(byte []cad){
        LinkedList lista= new LinkedList();
        String split[]=new String(cad).split(" ");
        
        lista.add( split[0]);
        lista.add(Integer.parseInt(split[1]));
        lista.add(split[2]);
        
        return lista;
    }
    
}
