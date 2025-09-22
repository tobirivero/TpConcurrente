package torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Tracker implements  Runnable{
    private Semaforos semaforos;
    private Queue <RequestTracker> requests;
    private Map < Integer, ArrayList<Integer> > registro;
    private List <Seeder> seeders;
    private Server server;

    public Tracker(Semaforos semaforos, Server server, List < Seeder> seeders){
        this.semaforos = semaforos;
        this.requests = new LinkedList<>();
        this.registro = new HashMap<>();
        this.server = server;
        //Lista integer vacia
        for(int i=0 ; i<15 ; i++){
            registro.put(i, new ArrayList<>());
        }
        this.seeders = seeders;
    }

    public void addRequest(RequestTracker request){
        this.requests.add(request);
    }

    public Integer findSomeSeeder(Integer target_index){
        ArrayList < Integer > filtro_indice = this.registro.get(target_index);

        //Random return
        int size_target = filtro_indice.size();
        if(size_target == 0){
            return null;
        }
        Random rand = new Random();
        int indice = rand.nextInt(size_target);
        
        return filtro_indice.get(indice);
    }

    public boolean updateMyRegistro(RequestTracker request){
    
        Integer id_peer = request.getLeecher().getId();
        List < Integer > lista_bloques = request.getBloques_to_update();
        
        for(Integer e : lista_bloques){
            registro.get(e).add(id_peer);
        }

        for(int i=0 ; i<15; i++){
            if(registro.get(i).size() < 4) return false;
        }
        return true;
    }

    public Seeder getSeederById(Integer id){
        return seeders.get(id);
    }

    public void run(){
        boolean stop = false;
        Semaphore s_tracker_semaforo = semaforos.getSemaforoTracker();
        Semaphore s_m_tracker = semaforos.getSemaforoTrackerRequest();
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_server = semaforos.getSemaforoServerRequest();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        
        while(!stop){
            s_tracker_semaforo.acquireUninterruptibly();

            s_m_tracker.acquireUninterruptibly();
            RequestTracker request = requests.poll();
            s_m_tracker.release();
            
            if(request == null) continue;
            else if(request.getTipo().equals((Integer)0)){
                Integer target_index = request.getIndex_bloque();
                Integer id_leecher = request.getLeecher().getId();
    

                //Si hay algun seeder que tenga el bloque, devuelvo la referencia
                Integer id_seeder = findSomeSeeder(target_index);
                AnswerTracker answer_tracker = (id_seeder== null) ? new AnswerTracker(-1, null) : new AnswerTracker(id_seeder, getSeederById(id_seeder));

                Semaphore s_m_leecher = semaforos.getMutexBtLeecherX(id_leecher);

                //Guardo la respuesta
                s_m_leecher.acquireUninterruptibly();
                request.getLeecher().addAnswerTracker(answer_tracker);
                s_m_leecher.release();
                
                //Levanto al leecher

                semaforos.getSemaforoLeecherX(id_leecher).release();
                
            }
            else if (request.getTipo().equals((Integer)1)) {
                stop = updateMyRegistro(request);

                if(stop){
                    s_m_printer.acquireUninterruptibly();
                    System.out.println("Proceso tracker muriendo...");
                    s_m_printer.release();
                    //Marco en true para eliminar servidor
                    s_m_server.acquireUninterruptibly();
                    server.killServer();
                    s_m_server.release();
                    
                    s_server.release();

                    //Marco en true para eliminar seeders
                    for(int i=0 ; i<seeders.size() ; i++){
                        Seeder seeder_i = seeders.get(i);
                        seeder_i.killSeeder();
                    }
                    //Libero  seeders
                    for(int i=0 ; i<4 ; i++){
                        Seeder seeder_i = seeders.get(i);
                        semaforos.getSemaforoSeederX(seeder_i.getId()).release();
                    }
                    //Libero leechers
                    for(int i=0 ; i<4; i++){
                        semaforos.getSemaforoLeecherX(i).release();
                    }
                }

                

            }
        }
    }
}
