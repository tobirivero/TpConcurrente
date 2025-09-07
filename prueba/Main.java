package prueba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Main {
    private static Semaphore P1 = new Semaphore(1);
    private static Semaphore P2 = new Semaphore(1);
    private static Semaphore P3 = new Semaphore(1);
    private static Semaphore P4 = new Semaphore(1);
    private static Semaphore Tracker = new Semaphore(1);
    private static Semaphore Server = new Semaphore(1);
    private static Semaphore Printer = new Semaphore(1);
     private static Semaphore Registro = new Semaphore(1);


    //Lista de todos los semaforos
    private static ArrayList<Semaphore> semaphores = new ArrayList<>(Arrays.asList(P1, P2, P3, P4, Tracker, Server, Printer, Registro));


    public static void main(String[] args) {
        //Inicializamos todos los semaforos
        Semaforo allInstances = new Semaforo(semaphores);
        //Creamos el Registro
        RegistroServer registroServer = new RegistroServer();
        //Creamos el servidor
        Server server = new Server(registroServer, allInstances);
        //Creamos los leechers
        Leecher p0 = new Leecher(0, server, null, allInstances);
        Leecher p1 = new Leecher(1, server, null, allInstances);
        Leecher p2 = new Leecher(2, server, null, allInstances);
        Leecher p3 = new Leecher(3, server, null, allInstances);

        ArrayList<Leecher> leechers = new ArrayList<>(
            Arrays.asList(p0, p1, p2, p3)
        );

        //Creamos el tracker
        Tracker tracker = new Tracker(0, leechers,registroServer, allInstances);

        //Seteo el tracker en cada leecher
        p0.setTracker(tracker);
        p1.setTracker(tracker);
        p2.setTracker(tracker);
        p3.setTracker(tracker);

        

        new Thread(p0,"P0").start();
        new Thread(p1,"P1").start();
        new Thread(p2,"P2").start();
        new Thread(p3,"P3").start();
        new Thread(tracker,"Tracker").start();
        new Thread(server,"Server").start();
        
    }
}
/*
 * BitTorrent es un protocolo de intercambio de archivos que divide los datos en 
 * fragmentos pequeños distribuidos entre varios usuarios. En lugar de descargar 
 * desde un único servidor, cada participante comparte lo que ya posee, acelerando 
 * la transferencia. Su eficiencia lo hace popular para distribuir contenido grande 
 * en redes descentralizadas.
 */