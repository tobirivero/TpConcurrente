package torrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
 
    public static void main(String [] args){
        //Creo el objeto semaforos
        Semaforos semaforos = new Semaforos();
        //Creo el objeto server
        Server server = new Server(semaforos);
        //Creo 4 archivos
        Archivo archivo_peer_0 = new Archivo();
        Archivo archivo_peer_1 = new Archivo();
        Archivo archivo_peer_2 = new Archivo();
        Archivo archivo_peer_3 = new Archivo();
        //Creo el resumen
        Resume resume = new Resume();
        //Creo los seeders y una lista
        Seeder seeder_0 = new Seeder(0,semaforos,archivo_peer_0,null,server);
        Seeder seeder_1 = new Seeder(1,semaforos,archivo_peer_1,null,server);
        Seeder seeder_2 = new Seeder(2,semaforos,archivo_peer_2,null,server);
        Seeder seeder_3 = new Seeder(3,semaforos,archivo_peer_3,null,server);
        List < Seeder > list_seeders = new ArrayList<>(Arrays.asList(seeder_0,seeder_1,seeder_2,seeder_3));
        //Creo el tracker
        Tracker tracker = new Tracker(semaforos, server, list_seeders);
        //Seteo el tracker a los seeders
        seeder_0.setTracker(tracker);
        seeder_1.setTracker(tracker);
        seeder_2.setTracker(tracker);
        seeder_3.setTracker(tracker);
        //Creo los leechers
        Leecher leecher_0 = new Leecher(0,semaforos,archivo_peer_0,tracker,server,resume);
        Leecher leecher_1 = new Leecher(1,semaforos,archivo_peer_1,tracker,server,resume);
        Leecher leecher_2 = new Leecher(2,semaforos,archivo_peer_2,tracker,server,resume);
        Leecher leecher_3 = new Leecher(3,semaforos,archivo_peer_3,tracker,server,resume);


        //Creo los threads
        new Thread(server, "Server").start(); 
        new Thread(tracker, "Tracker").start(); 
        new Thread(leecher_0, "L0").start(); 
        new Thread(leecher_1, "L1").start();
        new Thread(leecher_2, "L2").start(); 
        new Thread(leecher_3, "L3").start(); 
        new Thread(seeder_0, "S0").start(); 
        new Thread(seeder_1, "S1").start();
        new Thread(seeder_2, "S2").start(); 
        new Thread(seeder_3, "S3").start();

    }
}
