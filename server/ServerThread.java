import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.List;
import java.sql.Connection;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;

public class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<ServerThread> threadList;
    private ObjectOutputStream output;
    private PaqueteEnvio paquete_recibido;
    private PaqueteEnvio paquete_enviar;
    private MarcoServidor mimarco;
    public String nombre_socket;
    public ArrayList<String> lista_mandar;
    public String estado = "";
    public ArrayList<Thread> lista_hilos_funciones;
    public ExecutorService ex;
    public List<Mis_hilos> tasks_;
    public Base_datos bd_postgres;
    public Connection conexion;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads, MarcoServidor mimarco, ExecutorService ex,
            Base_datos bd_postgres, Connection conexion) {
        this.socket = socket;
        this.threadList = threads;
        this.mimarco = mimarco;
        this.ex = ex;
        this.bd_postgres = bd_postgres;
        this.conexion = conexion;
    }

    @Override
    public void run() {
        try {

            // System.out.println("Entra0");
            // Esto es lo que nos está enviando el cliente
            ObjectInputStream input = new ObjectInputStream(this.socket.getInputStream());


            // Esto es lo que le vamos a enviar al cliente
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();

            while (true) {

                paquete_recibido = (PaqueteEnvio) input.readObject();
                List<Double> resultados = new ArrayList<>();
                ;
                String mensaje = paquete_recibido.getMensaje();
                String nick = paquete_recibido.getNick();
                String ip_destino = paquete_recibido.getIp();

                if (Objects.nonNull(mensaje)) {
                    if (mensaje.equals("quit")) {
                        paquete_enviar = new PaqueteEnvio();
                        paquete_enviar.setMensaje("quit");
                        output.writeObject(paquete_enviar);
                    }
                }

                if (Objects.nonNull(ip_destino) && Objects.nonNull(mensaje)) {

                    Boolean uno = mensaje.contains(",");
                    String[] parts = new String[2];
                    Boolean m = false;

                    if (uno) {
                        parts = mensaje.split(",");

                        try{
                        if(((Integer.valueOf(parts[1])) >= 0 && Integer.valueOf(parts[1]) < 1000) && (parts[0].equals("std") ||  parts[0].equals("min") ||  parts[0].equals("max") ||  parts[0].equals("count") 
                        ||  parts[0].equals("mean"))){
                            mimarco.areatexto.append("\n" + nick + ": pidió: " + mensaje);
                            mensaje = parts[0];
                        }
                        }
                        catch (NumberFormatException e){
                            mimarco.areatexto.append("\n" + nick + ": " + mensaje + " para " + ip_destino);
                        }

                        
                    }
                    else{
                        if((mensaje.equals("std") || mensaje.equals("min") ||  mensaje.equals("max") ||  mensaje.equals("count") 
                        ||  mensaje.equals("mean"))){
                            mimarco.areatexto.append("\n" + nick + ": pidió: " + mensaje);
                        }
                        else{
                            mimarco.areatexto.append("\n" + nick + ": " + mensaje + " para " + ip_destino);
                        }
                    }

                    

                    if (mensaje.equals("std")) {
                        Mis_hilos hilo_ = new Mis_hilos();
                        hilo_.set_funcion("std", bd_postgres, conexion);
                        hilo_.set_resultado(resultados);
                        if (uno) {
                            hilo_.set_numero(Integer.parseInt(parts[1]));
                        }

                        Future<?> future = ex.submit(hilo_);
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Maneja las excepciones si es necesario
                        }

                    } else if (mensaje.equals("min")) {
                        Mis_hilos hilo_ = new Mis_hilos();
                        hilo_.set_funcion("min", bd_postgres, conexion);
                        hilo_.set_resultado(resultados);
                        if (uno) {
                            hilo_.set_numero(Integer.parseInt(parts[1]));
                        }

                        Future<?> future = ex.submit(hilo_);
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Maneja las excepciones si es necesario
                        }
                    } else if (mensaje.equals("max")) {
                        Mis_hilos hilo_ = new Mis_hilos();
                        hilo_.set_funcion("max", bd_postgres, conexion);
                        hilo_.set_resultado(resultados);
                        if (uno) {
                            hilo_.set_numero(Integer.parseInt(parts[1]));
                        }
                        Future<?> future = ex.submit(hilo_);
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Maneja las excepciones si es necesario
                        }
                    } else if (mensaje.equals("count")) {
                        Mis_hilos hilo_ = new Mis_hilos();
                        hilo_.set_funcion("count", bd_postgres, conexion);
                        hilo_.set_resultado(resultados);
                        if (uno) {
                            hilo_.set_numero(Integer.parseInt(parts[1]));
                        }
                        Future<?> future = ex.submit(hilo_);
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Maneja las excepciones si es necesario
                        }
                    } else if (mensaje.equals("mean")) {
                        Mis_hilos hilo_ = new Mis_hilos();
                        hilo_.set_funcion("mean", bd_postgres, conexion);
                        hilo_.set_resultado(resultados);
                        if (uno) {
                            hilo_.set_numero(Integer.parseInt(parts[1]));
                        }
                        Future<?> future = ex.submit(hilo_);
                        try {
                            future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Maneja las excepciones si es necesario
                        }
                    } else {
                        //System.out.println("Mensaje.");
                        m = true;
                    }

                    if (!m) {
                        paquete_enviar = new PaqueteEnvio();
                        // paquete_enviar.setMensaje(Double.toString((resultados.get(0))));

                        StringBuilder mensaje_ = new StringBuilder();
                        if (resultados.size() == 1) {
                            paquete_enviar.setMensaje(Double.toString((resultados.get(0))));
                        } else {
                            for (int i = 0; i < resultados.size(); i++) {
                                double resultado = resultados.get(i);
                                mensaje_.append(i + 1).append(": ").append(Double.toString(resultado));

                                if (i < resultados.size() - 1) {
                                    mensaje_.append(",");
                                }
                            }
                            paquete_enviar.setMensaje(mensaje_.toString());
                        }

                        paquete_enviar.setNick("Servidor: ");
                        // paquete_recibido.setMensaje(Double.toString((resultados.get(0))));
                        output.writeObject(paquete_enviar);

                    } else {
                
                        for (ServerThread hilos : threadList) {
                            String nombre_enviar = hilos.nombre_socket;
                            if (ip_destino.trim().equals(nombre_enviar.trim())) {
                                hilos.output.writeObject(paquete_recibido);
                            }
                        }
                    }
                    // mimarco.areatexto.append("\n" + nick + ": " + mensaje + " para " +
                    // ip_destino);
                    // for (ServerThread hilos : threadList) {
                    // String nombre_enviar = hilos.nombre_socket;
                    // // InetAddress ip_socket = hilos.socket.getInetAddress();
                    // // System.out.println("Un elemento de la lista: " +
                    // ip_socket.getHostAddress());
                    // if (ip_destino.trim().equals(nombre_enviar.trim())) {
                    // // System.out.println("Si se parecen");
                    // // hilos.output.writeUTF( "El servidor contestó esto: " + mensaje);
                    // hilos.output.writeObject(paquete_recibido);
                    // }
                    // }
                } else {
                    nombre_socket = nick;
                    Integer bandera_existe = 0;
                    for (ServerThread hilos : threadList) {
                        if ((nombre_socket.trim().equals(hilos.nombre_socket))) {
                            bandera_existe = bandera_existe + 1;
                        }
                    }

                    if (bandera_existe > 1) {
                        estado = "abortado";
                        paquete_enviar = new PaqueteEnvio();
                        paquete_enviar.setMensaje("abort");
                        output.writeObject(paquete_enviar);
                    } else {
                        mimarco.areatexto.append("\n" + "Cliente condectado: " + nick);
                        estado = "creado";
                        lista_mandar = new ArrayList<>();

                        for (ServerThread hilos : threadList) {
                            lista_mandar.add(hilos.nombre_socket);

                        }
                        // System.out.println(lista_mandar.size());

                        for (ServerThread hilos : threadList) {
                            // System.out.println("Se está mandando");
                            paquete_enviar = new PaqueteEnvio();
                            paquete_enviar.setLista(lista_mandar);
                            paquete_enviar.setMensaje("vacio");
                            hilos.output.writeObject(paquete_enviar);
                        }

                    }

                }

            }

        } catch (IOException | ClassNotFoundException ex) {

            InetAddress ip_borrar = socket.getInetAddress();
            String identificador_borrar = socket.getInetAddress().toString().trim()
                    + Integer.toString(socket.getPort()).trim();
            String nombre_borrar = nombre_socket.trim();
            int numero = -1;
            int bandera = -1;

            if (estado.equals("abortado")) {
                mimarco.areatexto.append("\n" + "Se rechazó la conexión con un nombre repetido: " + nombre_borrar);
            } else {
                mimarco.areatexto.append("\n" + "Cliente desconectado: " + nombre_borrar);
            }

            for (ServerThread hilos : threadList) {

                String nombre_hilos = hilos.nombre_socket.trim();
                // System.out.println("Un elemento de la lista: " + ip_socket.getHostAddress());
                numero = numero + 1;
                if ((hilos.socket.getInetAddress().toString().trim() + Integer.toString(hilos.socket.getPort()).trim())
                        .equals(identificador_borrar)) {
                    bandera = 0;
                    break;
                }
            }

            if (bandera == 0) {
                threadList.remove(numero);
            }

            // System.out.println(threadList.size());
            lista_mandar = new ArrayList<>();

            for (ServerThread hilos : threadList) {
                lista_mandar.add(hilos.nombre_socket);
            }

            // System.out.println(lista_mandar.size());
            for (ServerThread hilos : threadList) {
                try {
                    // System.out.println("Se está mandando salida a: " + hilos.nombre_socket);
                    paquete_enviar = new PaqueteEnvio();
                    paquete_enviar.setLista(lista_mandar);
                    paquete_enviar.setMensaje("vacio");
                    hilos.output.writeObject(paquete_enviar);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // ex.printStackTrace();
        }
    }
}

class PaqueteEnvio implements Serializable { // implements Serializable -> Todas las instancias de esta clase podrán
    // convertirse en una serie de bytes para ser enviados por la red.
    private String nick, ip, mensaje;

    private LocalDateTime hora;

    private ArrayList<String> lista_thread;

    public void setLista(ArrayList<String> lista_thread) {
        this.lista_thread = lista_thread;
    }

    public void setHora(LocalDateTime hora) {
        this.hora = hora;
    }

    public LocalDateTime getHora() {
        return this.hora;
    }

    public ArrayList<String> getLista() {
        return this.lista_thread;
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