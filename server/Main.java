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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.concurrent.locks.*;

public class Main {

    public static void main(String[] args) {
        // using serversocket as argument to automatically close the socket
        // the port number is unique for each server
        boolean err = true;
        String input1;
        Integer puerto = -1;
        Integer hilos_funciones = 1;
        ServerSocket socket_verificador;
        JTextField puerto_conexion = new JTextField();
        JTextField numero_hilos = new JTextField();
        Object[] fields = { "Puerto del server: ", puerto_conexion, "Numero de hilos: ", numero_hilos };
        ArrayList<Thread> lista_hilos_funciones = new ArrayList<>();

        do {
            try {
                JOptionPane.showConfirmDialog(null, fields, "Datos cliente", JOptionPane.OK_CANCEL_OPTION);
                Pattern patt1 = Pattern.compile(
                        "^(102[4-9]|10[3-9][0-9]|1[1-9][0-9]{2}|[2-9][0-9]{3}|[1-3][0-9]{4}|4[0-8][0-9]{3}|49[0-1][0-9]|491[0-4][0-9]|4915[0-1])$");
                Matcher m1 = patt1.matcher(puerto_conexion.getText());
                if (!m1.find()) {
                    JOptionPane.showMessageDialog(null, "Ingrese un puerto valido. ");
                    err = true;
                } else {
                    err = false;
                }

                Pattern patt2 = Pattern.compile("[1-9][0-9]*");
                Matcher m2 = patt2.matcher(numero_hilos.getText());
                if (!m2.find()) {
                    JOptionPane.showMessageDialog(null, "Ingrese una cantidad de hilos valida. ");
                    err = true;
                } else {
                    err = false;
                }
            } catch (NumberFormatException e) {
                // e.printStackTrace();
            } catch (Exception e) {
                err = false;

            }
        } while (err);

        puerto = Integer.parseInt(puerto_conexion.getText());
        hilos_funciones = Integer.parseInt(numero_hilos.getText());
        
        // list to add all the clients thread
        if (puerto != -1) {

            ArrayList<ServerThread> threadList = new ArrayList<>();
            try (ServerSocket serversocket = new ServerSocket(puerto)) {

                MarcoServidor mimarco = new MarcoServidor();
                mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Truncate de la tabla de la base de datos
                Base_datos bd_postgres = new Base_datos();
                Connection conexion = bd_postgres.conectar();
                mimarco.areatexto.append(bd_postgres.tabla_truncate(conexion));
                // Creación de hilos
                /* Integer num_hilos = Integer.valueOf(numero_hilos.getText());
                for (Integer indice1 = 0; indice1 < num_hilos; indice1++) {
                    Thread hiloA = new Thread(new Mis_hilos(), "hiloA");
                    lista_hilos_funciones.add(hiloA);
                } */

                // Prueba para condiciones bd
                /* Mis_hilos hiloA = new Mis_hilos();
                hiloA.set_funcion("count", bd_postgres, conexion);
                lista_hilos_funciones.add(hiloA);

                lista_hilos_funciones.get(0).start(); */

                // Prueba con threadpool
                ExecutorService ex = Executors.newFixedThreadPool(hilos_funciones);
                // List<Mis_hilos> tasks_ = new ArrayList<>();
                // List<String> funciones = new ArrayList<>();
                // funciones.add("std");
                // funciones.add("min");
                // funciones.add("max");
                // funciones.add("count");
                // funciones.add("mean");

                // for (int i = 0; i < 5; i++) {
                //     Mis_hilos hilo_ = new Mis_hilos();
                //     hilo_.set_funcion(funciones.get(i), bd_postgres, conexion);
                //     tasks_.add(hilo_);
                // }

                /* for (int i=0; i< tasks_.size(); i++) {
                    ex.execute(tasks_.get(i));
                }
                ex.shutdown(); */


                while (true) {
                    Socket socket = serversocket.accept();
                    ServerThread serverThread = new ServerThread(socket, threadList, mimarco, ex, bd_postgres, conexion);
                    // starting the thread
                    threadList.add(serverThread);
                    serverThread.start();

                    // get all the list of currently running thread

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

class Base_datos {
    String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    String username = "postgres";
    String password = "chat";
    Connection connection;

    public Connection conectar() {
        try {
            connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
        return connection;
    }

    public String tabla_truncate(Connection conectar) throws SQLException {
        PreparedStatement statement = conectar.prepareStatement("TRUNCATE public.SOLICITUD ");
        try {
            return (String.valueOf(statement.execute()));
        } finally {
            statement.close();
        }
    }

    public void tabla_access(Connection conectar, int file,  String columna, double valor) throws SQLException {

        escritura_bd.lock();

        try{
            if (columna.equals("std")){
                columna = "stdvalue";
            } else if (columna.equals("min")){
                columna = "minvalue";
            } else if (columna.equals("max")){
                columna = "maxvalue";
            } else if (columna.equals("count")){
                columna = "countvalue";
            } else if (columna.equals("mean")){
                columna = "meanvalue";
            }

            // select
            String query = "SELECT file FROM public.SOLICITUD WHERE file = ?";
            Boolean existe;
            
            try (PreparedStatement statement = conectar.prepareStatement(query)) {
                statement.setInt(1, file);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    existe = resultSet.next(); // True si hay resultado, falso si no existe
                }
            }

            // Update o insert
            if (existe) {
                query = "UPDATE public.SOLICITUD SET " + columna + " = ?, functions_processed = functions_processed + 1 WHERE file = ?";
            
                try (PreparedStatement statement = conectar.prepareStatement(query)) {
                    statement.setDouble(1, valor);
                    statement.setInt(2, file);
                    statement.executeUpdate();
                }
            } else {
                query = "INSERT INTO public.SOLICITUD (file, " + columna +", functions_processed) VALUES (?, ?, 1)";
        
                try (PreparedStatement statement = conectar.prepareStatement(query)) {
                    statement.setInt(1, file);
                    statement.setDouble(2, valor);
                    
                    String.valueOf(statement.executeUpdate());
            }
            }
        
        } finally{
            escritura_bd.unlock();
        }
    }



    private Lock escritura_bd = new ReentrantLock();

}

class Mis_hilos extends Thread {

    String nombre;
    Base_datos bd_postgres;
    Connection conexion;
    Integer no_archivos = 0;
    List<Double> resultados;

    public void set_funcion(String nombre, Base_datos bd_postgres, Connection conexion) {
        this.nombre = nombre;
        this.bd_postgres = bd_postgres;
        this.conexion = conexion;
    }

    public void set_numero(Integer no_archivos){
        this.no_archivos = no_archivos;
    }

    public void set_resultado(List<Double> resultados){
        this.resultados = resultados;
    }

    public void run() {
        List<Double> listaOpen;

        // limitando cantidad de archivos para pruebas
        for (int indice_archivo = 1; indice_archivo <= 999; indice_archivo++) {
            String archivo;
            if(this.no_archivos != 0){
                archivo = "archivos/index_data_" + this.no_archivos + ".csv";
            }
            else{
                archivo = "archivos/index_data_" + indice_archivo + ".csv";
            }
            

            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {

                String encabezados = br.readLine();
                // Crear listas para cada columna
                listaOpen = new ArrayList<>();
                // Leer cada línea del archivo y agregar los valores a las listas
                // correspondientes
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] campos = linea.split(",");
                    listaOpen.add(Double.parseDouble(campos[1]));
                }

                if (this.nombre == "std") {
                    DoubleStream filas = listaOpen.stream().mapToDouble(val -> val);
                    double average_open = filas.average().getAsDouble();
                    filas = listaOpen.stream().mapToDouble(val -> val);
                    this.resultados
                            .add(Math.sqrt(filas.map(num -> Math.pow(num - average_open, 2)).average().getAsDouble()));
                } else if (this.nombre == "min") {
                    this.resultados.add(listaOpen.stream().mapToDouble(val -> val).min().orElse(0.0));
                } else if (this.nombre == "max") {
                    resultados.add(listaOpen.stream().mapToDouble(val -> val).max().orElse(0.0));
                } else if (this.nombre == "count") {
                    resultados.add(Double.valueOf(listaOpen.size()));
                } else if (this.nombre == "mean") {
                    resultados.add(listaOpen.stream().mapToDouble(val -> val).average().orElse(0.0));
                }

                try {
                    // comprobaciones para la bd
                    /* Boolean existe = bd_postgres.tabla_select(conexion, indice_archivo);
                    // System.out.println(existe);
                    if (existe){
                        bd_postgres.tabla_update(conexion, indice_archivo, this.nombre, resultados.get(0));
                    } else {
                        bd_postgres.tabla_inserts(conexion, indice_archivo, this.nombre, resultados.get(0)); 
                    } */
                    if(this.no_archivos != 0){
                        bd_postgres.tabla_access(conexion, this.no_archivos, this.nombre, resultados.get(0));
                    }
                    else{
                        bd_postgres.tabla_access(conexion, indice_archivo, this.nombre, resultados.get(0));
                    }
                    

                } catch (SQLException e) {
                    System.err.println("Error al insertar en la base de datos: " + e.getMessage());
                }

            } catch (IOException e) {
                System.err.println("Error al imprimir el archivo " + archivo + ": " + e.getMessage());
            }
            
            if(this.no_archivos != 0){
                break;
            }
        }

        this.no_archivos =0;
    }

}