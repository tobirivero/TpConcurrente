package torrent;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Semaforos {
    private ArrayList < Semaphore > semaforos;

    public Semaforos() {
        //Asumimos 4 leechers -> 1 semaforo x c/leecher (semaforo asociado al run)
        Semaphore s_l0 = new Semaphore(0,true); // i=0
        Semaphore s_l1 = new Semaphore(0,true); // i=1
        Semaphore s_l2 = new Semaphore(0,true); // i=2
        Semaphore s_l3 = new Semaphore(0,true); // i=3
        //Asumimos 4 seeders -> 1 semaforo x c/seeder (semaforo asociado al run)
        Semaphore s_s0 = new Semaphore(0,true); // i=4
        Semaphore s_s1 = new Semaphore(0,true); // i=5
        Semaphore s_s2 = new Semaphore(0,true); // i=6
        Semaphore s_s3 = new Semaphore(0,true); // i=7
        
        //Seeder y Leecher comparten 1 archivo, entonces 1 mutex por el archivo compartido
        Semaphore s_m_file0 = new Semaphore(1,true); // i=8
        Semaphore s_m_file1 = new Semaphore(1,true); // i=9
        Semaphore s_m_file2 = new Semaphore(1,true); // i=10
        Semaphore s_m_file3 = new Semaphore(1,true); // i=11

        //Cada seeder tiene un mutex para su cola de requests
        Semaphore s_m_s0 = new Semaphore(1,true); // i=12
        Semaphore s_m_s1 = new Semaphore(1,true); // i=13
        Semaphore s_m_s2 = new Semaphore(1,true); // i=14
        Semaphore s_m_s3 = new Semaphore(1,true); // i=15
        //Cada leecher tiene un mutex para su cola de buffer_tracker
        Semaphore s_m_buffer_tracker_l0 = new Semaphore(1,true);// i=16
        Semaphore s_m_buffer_tracker_l1 = new Semaphore(1,true);// i=17
        Semaphore s_m_buffer_tracker_l2 = new Semaphore(1,true);// i=18
        Semaphore s_m_buffer_tracker_l3 = new Semaphore(1,true);// i=19
        //Cada leecher tiene un mutex para su cola de buffer_bloques
        Semaphore s_m_buffer_bloques_l0 = new Semaphore(1,true); //i=20
        Semaphore s_m_buffer_bloques_l1 = new Semaphore(1,true); //i=21
        Semaphore s_m_buffer_bloques_l2 = new Semaphore(1,true); //i=22
        Semaphore s_m_buffer_bloques_l3 = new Semaphore(1,true); //i=23

        
        //Semaforo mutex para la flag kill de c/seeder 
        Semaphore s_m_seeder0_kill = new Semaphore(1,true); //i=24
        Semaphore s_m_seeder1_kill = new Semaphore(1,true); //i=25
        Semaphore s_m_seeder2_kill = new Semaphore(1,true); //i=26
        Semaphore s_m_seeder3_kill = new Semaphore(1,true); //i=27

        //El servidor posee un mutex para su cola de solicitudes 
        Semaphore s_m_server = new Semaphore(1,true); // i=28
        //El servidor posee un semaforo asociado a su run
        Semaphore s_server = new Semaphore(0,true); //i=29
        //Semaforo mutex para la flag kill del server
        Semaphore s_m_server_kill = new Semaphore(1,true); //i=30

        //El tracker posee un mutex para su cola de solicitudes 
        Semaphore s_m_tracker = new Semaphore(1,true); //i=31
        //El tracker posee un semaforo asociado a su run
        Semaphore s_tracker = new Semaphore(0,true); //i=32

        //Semaforo mutex para escribir por la consola
        Semaphore s_m_printer = new Semaphore(1,true); //i=33

        //Semaforo mutex para escribir por el resumen
        Semaphore s_m_resume = new Semaphore(1,true); //i=34

        
        
        
        ArrayList < Semaphore > list = new ArrayList<>();
        list.add(s_l0);
        list.add(s_l1);
        list.add(s_l2);
        list.add(s_l3);
        list.add(s_s0);
        list.add(s_s1);
        list.add(s_s2);
        list.add(s_s3);
        list.add(s_m_file0);
        list.add(s_m_file1);
        list.add(s_m_file2);
        list.add(s_m_file3);
        list.add(s_m_s0);
        list.add(s_m_s1);
        list.add(s_m_s2);
        list.add(s_m_s3);
        list.add(s_m_buffer_tracker_l0);
        list.add(s_m_buffer_tracker_l1);
        list.add(s_m_buffer_tracker_l2);
        list.add(s_m_buffer_tracker_l3);
        list.add(s_m_buffer_bloques_l0);
        list.add(s_m_buffer_bloques_l1);
        list.add(s_m_buffer_bloques_l2);
        list.add(s_m_buffer_bloques_l3);
        list.add(s_m_seeder0_kill);
        list.add(s_m_seeder1_kill);
        list.add(s_m_seeder2_kill);
        list.add(s_m_seeder3_kill);
        list.add(s_m_server);
        list.add(s_server);
        list.add(s_m_server_kill);
        list.add(s_m_tracker);
        list.add(s_tracker);
        list.add(s_m_printer);
        list.add(s_m_resume);
        this.semaforos = list;
    }

    //Devuelve el semaforo asociado al run del leecher x
    public Semaphore getSemaforoLeecherX(int index){
        return semaforos.get(index);
    }
    //Devuelve el semaforo asociado al run del seeder x
    public Semaphore getSemaforoSeederX(int index){
        return semaforos.get(4+index);
    }
    //Devuelve el semaforo mutex del archivo compartido por el peer con id=index
    public Semaphore getMutexArchivo(int index){
        return semaforos.get(8 + index);
    }
    //Devuelve el semaforo asociado al mutex del buffer de solicitudes del seeder x
    public Semaphore getMutexSeederX(int index){
        return semaforos.get(12 + index);
    }
    //Devuelve el semaforo asociado al mutex del buffer tracker del leecher x
    public Semaphore getMutexBtLeecherX(int index){
        return semaforos.get(16 + index);
    }
    //Devuelve el semaforo asociado al mutex del buffer bloques del leecher x
    public Semaphore getMutexBbLeecherX(int index){
        return semaforos.get(20 + index);
    }
    //Devuelve el semaforo mutex asociado a la flag kill de c/seeder
    public Semaphore getMutexKillSeederX(int index){
        return semaforos.get(24+index);
    }
    //Devuelve el semaforo mutex asociado al buffer de solicitudes del server
    public Semaphore getSemaforoServerRequest(){
        return semaforos.get(28);
    }
    //Devuelve el semaforo asociado al run del server
    public Semaphore getSemaforoServer(){
        return semaforos.get(29);
    }
    //Devuelve el semaforo mutex asociado a la flag kill del server
    public Semaphore getMutexKillServer(){
        return semaforos.get(30);
    }
    //Devuelve el semaforo mutex asociado al buffer de solicitudes del tracker 
    public Semaphore getSemaforoTrackerRequest(){
        return semaforos.get(31);
    }
    //Devuelve el semaforo asociado al run del tracker
    public Semaphore getSemaforoTracker(){
        return semaforos.get(32);
    }
    //Devuelve el semaforo mutex asociado al printer
    public Semaphore getSemaforoPrinter(){
        return semaforos.get(33);
    }
    //Devuelve el semaforo mutex asociado al resumen
    public Semaphore getMutexResume(){
        return  semaforos.get(34);
    }
}
