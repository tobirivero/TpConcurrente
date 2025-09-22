package torrent;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Semaforos {
    private ArrayList < Semaphore > semaforos;

    public Semaforos() {
        //Asumimos 4 leechers -> 1 semaforo x c/leecher
        Semaphore l0 = new Semaphore(0); // i=0
        Semaphore l1 = new Semaphore(0); // i=1
        Semaphore l2 = new Semaphore(0); // i=2
        Semaphore l3 = new Semaphore(0); // i=3
        //Asumimos 4 seeders -> 1 semaforo x c/seeder
        Semaphore s0 = new Semaphore(0); // i=4
        Semaphore s1 = new Semaphore(0); // i=5
        Semaphore s2 = new Semaphore(0); // i=6
        Semaphore s3 = new Semaphore(0); // i=7
        
        //Seeder y Leecher comparten 1 archivo, entonces 1 mutex por el archivo
        Semaphore m0 = new Semaphore(1); // i=8
        Semaphore m1 = new Semaphore(1); // i=9
        Semaphore m2 = new Semaphore(1); // i=10
        Semaphore m3 = new Semaphore(1); // i=11

        //Cada seeder tiene un mutex para sus requests
        Semaphore s0_m = new Semaphore(1); // i=12
        Semaphore s1_m = new Semaphore(1); // i=13
        Semaphore s2_m = new Semaphore(1); // i=14
        Semaphore s3_m = new Semaphore(1); // i=15
        //Cada leecher tiene un mutex para su respuesta de buffer_tracker
        Semaphore l0_bt_m = new Semaphore(1);// i=16
        Semaphore l1_bt_m = new Semaphore(1);// i=17
        Semaphore l2_bt_m = new Semaphore(1);// i=18
        Semaphore l3_bt_m = new Semaphore(1);// i=19
        //Cada leecher tiene un mutex para su respuesta de buffer_bloques
        Semaphore l0_bb_m = new Semaphore(1); //i=20
        Semaphore l1_bb_m = new Semaphore(1); //i=21
        Semaphore l2_bb_m = new Semaphore(1); //i=22
        Semaphore l3_bb_m = new Semaphore(1); //i=23

        //Semaforo server-queue y server 
        Semaphore sq = new Semaphore(1); // i=24
        Semaphore server = new Semaphore(0); //i=25

        //Semaforos tracker-queue y tracker
        Semaphore tq = new Semaphore(1); //i=26
        Semaphore tracker = new Semaphore(0); //i=27

        //Semaforo para mostrar por consola el archivo
        Semaphore m_printer = new Semaphore(1); //i=28

        ArrayList < Semaphore > list = new ArrayList<>();
        list.add(l0);
        list.add(l1);
        list.add(l2);
        list.add(l3);
        list.add(s0);
        list.add(s1);
        list.add(s2);
        list.add(s3);
        list.add(m0);
        list.add(m1);
        list.add(m2);
        list.add(m3);
        list.add(s0_m);
        list.add(s1_m);
        list.add(s2_m);
        list.add(s3_m);
        list.add(l0_bt_m);
        list.add(l1_bt_m);
        list.add(l2_bt_m);
        list.add(l3_bt_m);
        list.add(l0_bb_m);
        list.add(l1_bb_m);
        list.add(l2_bb_m);
        list.add(l3_bb_m);
        list.add(sq);
        list.add(server);
        list.add(tq);
        list.add(tracker);
        list.add(m_printer);

        this.semaforos = list;
    }

    public Semaphore getSemaforoLeecherX(int index){
        return semaforos.get(index);
    }
    public Semaphore getSemaforoSeederX(int index){
        return semaforos.get(4+index);
    }
    public Semaphore getMutexArchivo(int index){
        return semaforos.get(8 + index);
    }
    public Semaphore getMutexSeederX(int index){
        return semaforos.get(12 + index);
    }
    public Semaphore getMutexBtLeecherX(int index){
        return semaforos.get(16 + index);
    }
    public Semaphore getMutexBbLeecherX(int index){
        return semaforos.get(20 + index);
    }
    
    public Semaphore getSemaforoServerRequest(){
        return semaforos.get(24);
    }
    public Semaphore getSemaforoServer(){
        return semaforos.get(25);
    }
    public Semaphore getSemaforoTrackerRequest(){
        return semaforos.get(26);
    }
    public Semaphore getSemaforoTracker(){
        return semaforos.get(27);
    }
    public Semaphore getSemaforoPrinter(){
        return semaforos.get(28);
    }
    
}
