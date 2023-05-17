
import javax.swing.*;

import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.awt.Label;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.*;

public class Main {

    public static void main(String[] args) {
        // try (Socket socket = new Socket("localhost", 5000)){
        
        JTextField ip_field = new JTextField();
        JTextField puerto_field = new JTextField();
        JTextField nombre_field = new JTextField();
        boolean err=true;
        
        Object[] fields = {"Ip del server: ",ip_field, "Puerto del server: ", puerto_field, "Nombre del cliente: ", nombre_field};
        do{
            JOptionPane.showConfirmDialog(null, fields, "Datos cliente", JOptionPane.OK_CANCEL_OPTION);
            Pattern patt1 = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher m1 = patt1.matcher(ip_field.getText());
            if(!m1.find()){
                JOptionPane.showMessageDialog(null, "Ingrese una ip correcta. ");
                err=true;
            }
            else{
                err=false;
            }

            Pattern patt2 = Pattern.compile("^(102[4-9]|10[3-9][0-9]|1[1-9][0-9]{2}|[2-9][0-9]{3}|[1-3][0-9]{4}|4[0-8][0-9]{3}|49[0-1][0-9]|491[0-4][0-9]|4915[0-1])$");
            Matcher m2 = patt2.matcher(puerto_field.getText());
            if(!m2.find()){
                JOptionPane.showMessageDialog(null, "Ingrese un puerto. ");
                err=true;
            }
            else{
                err=false;
            }

        }
        while(err);




        if(!ip_field.getText().equals("") && !puerto_field.getText().equals("") && !nombre_field.getText().equals("")){
            
            if(ip_field.getText().equals("")){
                System.out.println("yes");
            }

        try (Socket socket = new Socket(ip_field.getText(), Integer.parseInt(puerto_field.getText()))) {
            // Esto es lo que vamos a recibir del servidor

            // Esto es lo que le vamos a enviar al servidor
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();

            // Paquete que se le enviará al servidor
            // PaqueteEnvio datos = new PaqueteEnvio();
            PaqueteEnvio datos = null;


            MarcoCliente mimarco = new MarcoCliente(socket, output, datos, nombre_field.getText());
            mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ClientRunnable clientRun = new ClientRunnable(socket, mimarco, nombre_field.getText());

            new Thread(clientRun).start();
            // loop closes when user enters exit command

            while(true){
                
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }
}

// Esta clase sirve para enviar un objeto donde esté toda la información
class PaqueteEnvio implements Serializable { // implements Serializable -> Todas las instancias de esta clase podrán
                                             // convertirse en una serie de bytes para ser enviados por la red.
    private String nick, ip, mensaje;

    private ArrayList<String> lista_thread;

    private LocalDateTime hora;

    public void setLista(ArrayList<String> lista_thread) {
        this.lista_thread = lista_thread;
    }

    public ArrayList<String> getLista(){
        return this.lista_thread;
    }

    public void setHora(LocalDateTime hora){
        this.hora = hora;
    }

    public LocalDateTime getHora(){
        return this.hora;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

class MarcoCliente extends JFrame {
    public String nombre;
    public Socket socket;
    public ObjectOutputStream output;
    public PaqueteEnvio datos;
    public LaminaMarcoCliente milamina;

    public MarcoCliente(Socket socket, ObjectOutputStream output, PaqueteEnvio datos, String nombre) {
        this.socket = socket;
        this.output = output;
        this.datos = datos;
        this.nombre = nombre;

        setBounds(600, 300, 400, 350);

        milamina = new LaminaMarcoCliente(socket, output, datos, nombre);

        add(milamina);

        setVisible(true);

        addWindowListener(new EnvioOnline(socket, output, datos, nombre));
    }
}

class EnvioOnline extends WindowAdapter{
    public String nombre;
    public Socket socket;
    public ObjectOutputStream output;
    public PaqueteEnvio datos;

    public EnvioOnline(Socket socket, ObjectOutputStream output, PaqueteEnvio datos, String nombre){
        this.socket = socket;
        this.output = output;
        this.datos = datos;
        this.nombre = nombre;
    }

    public void windowOpened(WindowEvent e){
        try {
            datos = new PaqueteEnvio();
            // campochat.append("\nYo: " + campo1.getText());
            datos.setNick(nombre);
            output.writeObject(datos);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}

class LaminaMarcoCliente extends JPanel {

    public LaminaMarcoCliente(Socket socket, ObjectOutputStream output, PaqueteEnvio datos, String nombre) {
        this.socket = socket;
        this.output = output;
        this.datos = datos;
        this.nombre = nombre;

        JLabel n_nick = new JLabel("Nombre: ");
        add(n_nick);

        nick = new JLabel();
        nick.setText(nombre);
        add(nick);

        JLabel texto = new JLabel("     Clientes online: ");

        add(texto);

        ip = new JComboBox();

        add(ip);

        campochat = new JTextArea(12, 30);

        add(campochat);

        JScrollPane scroll = new JScrollPane (campochat, 
   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scroll);

        campo1 = new JTextField(20);

        add(campo1);

        miboton = new JButton("Enviar");

        EnviaTexto mievento = new EnviaTexto();

        miboton.addActionListener(mievento);

        add(miboton);

    }

    private class EnviaTexto implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);
            if(ip.getSelectedIndex() != -1){

            if((!campo1.getText().trim().equals("")))
            {
            campochat.append("\n" + formattedDate);
            campochat.append("\nYo: " + campo1.getText());
            if(campo1.getText().equals("quit")){
                System.exit(0);
            }
            
            try {
                datos = new PaqueteEnvio();
                // campochat.append("\nYo: " + campo1.getText());
                datos.setNick(nick.getText());
                datos.setIp(ip.getSelectedItem().toString());
                datos.setMensaje(campo1.getText());
                output.writeObject(datos);
                campo1.setText("");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            }
            else{
                campo1.setText("");
            }
        }
        }

    }

    public ArrayList<String> lista_combo;
    public JComboBox ip;
    public String nombre;
    public Socket socket;
    public ObjectOutputStream output;
    public PaqueteEnvio datos;
    public JLabel nick;
    public JTextField campo1;

    public JTextArea campochat;

    public JButton miboton;

}