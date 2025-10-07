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
    private Queue <RequestTracker> requests;  // buffer de solicitudes 
    private Map < Integer, ArrayList<Integer> > registro;   // registro del tracker, se indexa como bloque_id -> {seeders que lo poseen}
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
        /*
         Utilizado por los procesos leechers. Carga la solicitud en el buffer de solicitudes.
         Se accede como region critica (desde el leecher). 
         */

        this.requests.add(request);
    }

    public Integer findSomeSeeder(Integer target_index){
        /*
          Consultando el registro, devuelve de forma aleatoria un seeder que posea el bloque con id=target_index(null si no existe). 
         */

        ArrayList < Integer > filtro_indice = this.registro.get(target_index);

        int size_target = filtro_indice.size();
        if(size_target == 0){
            return null;
        }
        Random rand = new Random();
        int indice = rand.nextInt(size_target);
        
        return filtro_indice.get(indice);
    }

    public boolean updateMyRegistro(RequestTracker request){
        /*
          Recibe una request de tipo 1 generada por un leecher. Toma los nuevos bloques disponibles del peer y actualiza el registro.
          Luego de actualizar el registro, lo recorre y devuelve true si todos los seeders poseen todos los bloques. False en caso contrario.
         */

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
        //Obtengo las instancias de los semaforos a utilizar, llamando a los metodos de la clase semaforos
        Semaphore s_tracker = semaforos.getSemaforoTracker();
        Semaphore s_m_tracker = semaforos.getSemaforoTrackerRequest();
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_server = semaforos.getSemaforoServerRequest();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        Semaphore s_m_server_kill = semaforos.getMutexKillServer();
        //Flag booleana que pone fin al run, es true cuando se detecta que el registro esta completo.
        boolean stop = false;

        while(!stop){
            /*En primera instancia, si no hay ninguna solicitud el proceso quedara bloqueado. 
            Cuando algun leecher envie una solicitud, levantara al tracker*/
            s_tracker.acquireUninterruptibly();

            //Accedo a la region critica del buffer de solicitudes del tracker y obtengo una request
            s_m_tracker.acquireUninterruptibly();
            RequestTracker request = requests.poll();
            s_m_tracker.release();
            
            if(request == null) continue;
            else if(request.getTipo().equals((Integer)0)){
                //La request es de tipo 0, es decir, el leecher solicita informacion sobre un bloque

                // Obtengo el id del bloque se solicita, y el id del leecher que origino la request
                Integer target_index = request.getIndex_bloque();
                Integer id_leecher = request.getLeecher().getId();
    

                //Si hay algun seeder que tenga el bloque, devuelvo la referencia
                Integer id_seeder = findSomeSeeder(target_index);

                /*Creo una respuesta de acuerdo a id_seeder:
                    -Si es NULL, entonces answer_status = -1 y la referencia al seeder es NULL (no hay seeder en la red con el bloque),
                    -Si no es NULL, answer_status = id_seeder y devuelvo en answer_seeder la referencia al seeder
                */
                AnswerTracker answer_tracker = (id_seeder== null) ? new AnswerTracker(-1, null) : new AnswerTracker(id_seeder, getSeederById(id_seeder));

                //Obtengo el semaforo mutex asociado al buffer de respuestas del tracker del leecher 
                Semaphore s_m_leecher = semaforos.getMutexBtLeecherX(id_leecher);

                //Accedo a la region critica y cargo la respuesta
                s_m_leecher.acquireUninterruptibly();
                request.getLeecher().addAnswerTracker(answer_tracker);
                s_m_leecher.release();
                
                //Levanto al leecher
                Semaphore s_leecher= semaforos.getSemaforoLeecherX(id_leecher);

                s_leecher.release();
                
            }
            else if (request.getTipo().equals((Integer)1)) {
                //La request es de tipo 1, es decir, el leecher solicita al tracker que se actualice

                //Llamo al metodo updateMyRegistro con la request, y asigno la flag stop al retorno del metodo
                stop = updateMyRegistro(request);

                if(stop){
                    //El registro del tracker esta completo, debo ponerle fin a la ejecucion del servidor y los seeders

                    //Accedo a la region critica de la consola, e informo que el proceso tracker se esta por hacer kill
                    s_m_printer.acquireUninterruptibly();
                    System.out.println("Proceso tracker muriendo...");
                    s_m_printer.release();

                    //Accedo a la region critica de la flag kill del servidor, y llamo al metodo killServer
                    s_m_server_kill.acquireUninterruptibly();
                    server.killServer();
                    s_m_server_kill.release();

                    //Levanto al servidor para que termine su ejecucion
                    s_server.release();


                    //Para cada seeder llamo al metodo killSeeder, accediendo a la region critica del seeder
                    for(int i=0 ; i<seeders.size() ; i++){
                        Seeder seeder_i = seeders.get(i);
                        Semaphore s_m_seeder_kill = semaforos.getMutexKillSeederX(i);

                        s_m_seeder_kill.acquireUninterruptibly();
                        seeder_i.killSeeder();
                        s_m_seeder_kill.release();
                    }

                    //Levanto seeders, por si alguno quedo bloqueado
                    for(int i=0 ; i<4 ; i++){
                        Seeder seeder_i = seeders.get(i);
                        semaforos.getSemaforoSeederX(seeder_i.getId()).release();
                    }
                    //Levanto leechers, por si alguno quedo bloqueado
                    for(int i=0 ; i<4; i++){
                        semaforos.getSemaforoLeecherX(i).release();
                    }
                }

                

            }
        }
    }
}
