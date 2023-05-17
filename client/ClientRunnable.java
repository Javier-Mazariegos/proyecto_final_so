import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.net.*;
import java.util.Objects;

public class ClientRunnable implements Runnable {

    private Socket socket;
    private ObjectInputStream input;
    PaqueteEnvio paquete_recibido;
    MarcoCliente mimarco;
    LocalDateTime hora;
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String mi_nombre;
    // private PrintWriter output;

    public ClientRunnable(Socket s,  MarcoCliente mimarco, String mi_nombre) throws IOException {
        this.socket = s;
        this.input = new ObjectInputStream(socket.getInputStream());
        this.mimarco = mimarco;
        this.mi_nombre = mi_nombre;
        // this.output = new PrintWriter(socket.getOutputStream(),true);
    }
    @Override
    public void run() {
        
            try {
                while(true) {
                    paquete_recibido = (PaqueteEnvio) input.readObject();
                    String mensaje = paquete_recibido.getMensaje();
                    String nick = paquete_recibido.getNick();
                    String ip_destino = paquete_recibido.getIp();
                    if(Objects.nonNull(mensaje)){
                        if(mensaje.equals("vacio")){

                            mimarco.milamina.ip.removeAllItems();
                            for(String it: paquete_recibido.getLista()){
                                if(!it.trim().equals(mi_nombre.trim())){
                                    mimarco.milamina.ip.addItem(it);
                                }
                                
                            }
                            
                           
                        }
                        else if(mensaje.equals("quit")){
                            
                            mimarco.dispose();
                            mimarco.setVisible(false);
                            input.close();
                            socket.close();
                            //System.exit(0);
                            
                        }
                        else if(mensaje.equals("abort")){
                            mimarco.dispose();
                            mimarco.setVisible(false);
                            input.close();
                            socket.close();
                            System.exit(0);
                        }
                        else{
                            hora = LocalDateTime.now();
                            String hora_recepcion = hora.format(formato);
                            mimarco.milamina.campochat.append("\n" +hora_recepcion);
                            mimarco.milamina.campochat.append("\n" + nick + ": "+ mensaje);
                        }
                    }
                    
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
    
}