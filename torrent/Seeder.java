package torrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Seeder implements Runnable{
    private Integer id;
    private Semaforos semaforos;
    private Archivo mi_archivo;
    private Queue <RequestBloque> requests; //buffer de solicitudes
    private Tracker tracker;
    private Server server;
    private boolean signal_to_kill;

    public Seeder(Integer id, Semaforos semaforos, Archivo mi_archivo, Tracker tracker, Server server){
        this.id = id;
        this.semaforos = semaforos;
        this.mi_archivo = mi_archivo;
        this.requests = new LinkedList<>();
        this.tracker = tracker;
        this.server = server;
        this.signal_to_kill = false;
    }

    public void setTracker(Tracker tracker){
        this.tracker = tracker;
    }

    public void addRequest(RequestBloque requestBloque){
        /*
            Utilizado por los procesos leechers. Carga la solicitud en el buffer de solicitudes.
            Se accede como region critica (desde el leecher).
         */

        this.requests.add(requestBloque);
    }
    
    public Integer getId() {
        return id;
    }
    
    public Bloque returnBloque(RequestBloque request, Semaphore s_m_file){
        /*
            Devuelve el bloque solicitado en la request. Recibe el mutex asociado al archivo compartido del peer.
            Decidimos que reciba el semaforo como parametro y ejecute el acquired dentro del metodo para que tome el recurso,
            y ejecute la menor cantidad de instrucciones posibles. Luego libera el recurso.
         */
        Integer index_bloque = request.getIndex_bloque();

        s_m_file.acquireUninterruptibly();
        Bloque bloque = mi_archivo.getBloque(index_bloque);
        s_m_file.release();

        return bloque;
    }

    public void killSeeder(){
        /*
          Utilizado por el proceso tracker, marca como true el fin de la ejecucion del proceso server.
         */

        this.signal_to_kill = true;
    }
    
    @Override
    public void run(){
        //Obtengo las instancias de los semaforos a utilizar, llamando a los metodos de la clase semaforos
        Semaphore s_seeder = semaforos.getSemaforoSeederX(id);
        Semaphore s_m_seeder = semaforos.getMutexSeederX(id);
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        Semaphore s_m_seeder_kill = semaforos.getMutexKillSeederX(id);
        Semaphore s_m_file = semaforos.getMutexArchivo(id);
        
        //Flag booleana que pone fin al run
        boolean stop = this.signal_to_kill;

        while(!stop){
            /*En primera instancia, si no hay ninguna solicitud el proceso quedara bloqueado. 
            Cuando algun leecher envie una solicitud o el tracker pone en true signal_to_kill, levantara al seeder*/
            s_seeder.acquireUninterruptibly();

            //Accedo a la region critica para verificar la flag de signal_to_kill por si fue puesta en true por el tracker.
            s_m_seeder_kill.acquireUninterruptibly();
            stop = this.signal_to_kill;
            s_m_seeder_kill.release();

            if(stop){
                /*El proceso seeder fue levantado para finalizar su ejecucion por el tracker.
                Accedo a la region critica de la consola, e informo que el proceso seeder se esta por hacer kill */
                s_m_printer.acquireUninterruptibly();
                System.out.println("Proceso seeder ID "+this.id+" muriendo...");
                s_m_printer.release();
            }
            else{
                //El proceso seeder no fue levantado para finalizar su ejecucion.

                //Accedo a la region critica del buffer de solicitudes del seeder y obtengo una request
                s_m_seeder.acquireUninterruptibly();
                RequestBloque request = requests.poll();
                s_m_seeder.release();

                if(request != null){
                    // Obtengo la referencia del leecher que origino la request, y su id
                    Leecher leecher = request.getLeecher();
                    Integer id_leecher = leecher.getId();

                    //Llamo al metodo returnBloque con la request, devuelve el bloque solicitado
                    Bloque bloque_answer = returnBloque(request,s_m_file);

                    //Obtengo el semaforo mutex asociado al buffer de respuestas de bloques del leecher
                    Semaphore s_m_leecher = semaforos.getMutexBbLeecherX(id_leecher);
                    
                    //Accedo a la region critica y cargo la respuesta
                    s_m_leecher.acquireUninterruptibly();
                    leecher.addAnswerBlock(bloque_answer);
                    s_m_leecher.release();

                    //Levanto al leecher
                    Semaphore s_leecher = semaforos.getSemaforoLeecherX(id_leecher);
                    s_leecher.release();
                }
            }

        }
    }

}
