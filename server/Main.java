import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Objects;
import java.util.regex.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.DoubleStream;


public class Main {

    public static void main(String[] args) {
        //using serversocket as argument to automatically close the socket
        //the port number is unique for each server
        boolean err=true;
        String input1;
        Integer puerto = -1;
        ServerSocket socket_verificador;
        JTextField puerto_conexion = new JTextField();
        JTextField numero_hilos = new JTextField();
        Object[] fields = {"Puerto del server: ", puerto_conexion, "Numero de hilos: ", numero_hilos};
        ArrayList<Thread> lista_hilos_funciones = new ArrayList<>();


        do{
            try{
                JOptionPane.showConfirmDialog(null, fields, "Datos cliente", JOptionPane.OK_CANCEL_OPTION);
                Pattern patt1 = Pattern.compile("^(102[4-9]|10[3-9][0-9]|1[1-9][0-9]{2}|[2-9][0-9]{3}|[1-3][0-9]{4}|4[0-8][0-9]{3}|49[0-1][0-9]|491[0-4][0-9]|4915[0-1])$");
                Matcher m1 = patt1.matcher(puerto_conexion.getText());
                if(!m1.find()){
                    JOptionPane.showMessageDialog(null, "Ingrese un puerto valido. ");
                    err=true;
                }
                else{
                    err=false;
                }

                Pattern patt2 = Pattern.compile("[1-9][0-9]*");
                Matcher m2 = patt2.matcher(numero_hilos.getText());
                if(!m2.find()){
                    JOptionPane.showMessageDialog(null, "Ingrese una cantidad de hilos valida ");
                    err=true;
                }
                else{
                    err=false;
                }
            }catch(NumberFormatException e){
                // e.printStackTrace();
            }catch(Exception e){
                err=false;

            }
        }while(err);


        
        
        puerto = Integer.parseInt(puerto_conexion.getText());
        //list to add all the clients thread
        if(puerto != -1){

        ArrayList<ServerThread> threadList = new ArrayList<>();
        try (ServerSocket serversocket = new ServerSocket(puerto)){

            MarcoServidor mimarco = new MarcoServidor();
            mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Truncate de la tabla de la base de datos
            Base_datos bd_postgres = new  Base_datos();
            Connection conexion = bd_postgres.conectar();
            mimarco.areatexto.append(bd_postgres.tabla_truncate(conexion));

            // Creación de hilos
            Integer num_hilos = Integer.valueOf(numero_hilos.getText());
            for(Integer indice1 =0; indice1 < num_hilos; indice1++){
                Thread hiloA = new Thread( new Mis_hilos(),"hiloA" );
                lista_hilos_funciones.add(hiloA);
            }

        
            while(true) {
                Socket socket = serversocket.accept();
                
                ServerThread serverThread = new ServerThread(socket, threadList, mimarco, lista_hilos_funciones);
                //starting the thread
                threadList.add(serverThread); 
                serverThread.start();

                //get all the list of currently running thread

            }

            
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
        }
    }
}

class MarcoServidor extends JFrame {

    public MarcoServidor() {

        setBounds(1200, 300, 280, 350);

        JPanel milamina = new JPanel();

        milamina.setLayout(new BorderLayout());

        areatexto = new JTextArea();

        milamina.add(areatexto, BorderLayout.CENTER);

        add(milamina);

        setVisible(true);

    }
    public JTextArea areatexto;
}

class Base_datos{
    String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    String username = "postgres";
    String password = "chat";
    Connection connection;


    public Connection conectar(){
        try{
            connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex);
        }
        return connection;
    }

    public String tabla_truncate(Connection conectar) throws SQLException{
        PreparedStatement statement = conectar.prepareStatement("TRUNCATE public.SOLICITUD ");
        try{
            return (String.valueOf(statement.execute()));
        }
        finally{
            statement.close();
        }
    }

    

}

class Mis_hilos extends Thread {

    String nombre = "";
    Base_datos bd_postgres;
    Connection conexion;
    

    public void set_funcion(String nombre, Base_datos bd_postgres, Connection conexion){
        this.nombre = nombre;
        this.bd_postgres = bd_postgres;
        this.conexion = conexion;
    }

    public void run() {


        List<Double> resultados = new ArrayList<>();
        List<Double> listaOpen;

        for(int indice_archivo =1; indice_archivo <= 999; indice_archivo++){
            String archivo = "archivos/index_data_" + indice_archivo + ".csv";

            try(BufferedReader br = new BufferedReader(new FileReader(archivo))){

                String encabezados = br.readLine();
                 // Crear listas para cada columna
                listaOpen = new ArrayList<>();
              // Leer cada línea del archivo y agregar los valores a las listas correspondientes
              String linea;
              while ((linea = br.readLine()) != null) {
                  String[] campos = linea.split(",");
                  listaOpen.add(Double.parseDouble(campos[1]));
              }

              if(this.nombre == "std"){
                DoubleStream filas = listaOpen.stream().mapToDouble(val -> val);
                double average_open = filas.average().getAsDouble();
                filas = listaOpen.stream().mapToDouble(val -> val);
                resultados.add(Math.sqrt(filas.map(num -> Math.pow(num - average_open, 2)).average().getAsDouble()));
              }
              else if(this.nombre == "min"){
                resultados.add(listaOpen.stream().mapToDouble(val -> val).min().orElse(0.0));
              }
              else if(this.nombre == "max"){
                resultados.add(listaOpen.stream().mapToDouble(val -> val).max().orElse(0.0));
              }
              else if(this.nombre == "count"){
                resultados.add(Double.valueOf(listaOpen.size()));
              }
              else if(this.nombre == "mean"){
                resultados.add(listaOpen.stream().mapToDouble(val -> val).average().orElse(0.0));
              }

            }
            catch (IOException e){
                System.err.println("Error al imprimir el archivo " + archivo +": " + e.getMessage());
            }
        }


    }

}