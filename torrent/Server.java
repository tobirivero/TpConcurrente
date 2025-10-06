package torrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Server implements  Runnable{
    private Semaforos semaforos;
    private Queue <RequestBloque> requests; 
    private ArrayList < Bloque > archivo;
    private boolean signal_to_kill;

    public Server(Semaforos semaforos){
        this.semaforos = semaforos;
        this.archivo = new ArrayList<>();
        this.requests = new LinkedList<>();
        //Creo el archivo completo
        List<String> contenido_archivo = new ArrayList<>(Arrays.asList(
        "TPConcurrente", "cada", "leecher", "procesa", "bloques",
            "en", "paralelo", "usando", "semaforos", "para",
            "sincronizar", "accesos", "y", "evitar", "condiciones"
        ));
        for(int i=0 ; i<15 ; i++){
            Bloque bloque = new Bloque(i,contenido_archivo.get(i));
            this.archivo.add(bloque);
        }
        this.signal_to_kill=false;
    }

    public void addRequest(RequestBloque request){
        /*
            Utilizado por los procesos leechers. Carga la solicitud en el buffer de solicitudes.
            Se accede como region critica (desde el leecher).
         */ 

        this.requests.add(request);
    }

    public Bloque returnBloque(RequestBloque request){
        /*
         Devuelve el bloque solicitado en la request
         */

        Integer index_bloque = request.getIndex_bloque();
        String content_bloque = this.archivo.get(index_bloque).getContent();

        Bloque bloque = new Bloque(index_bloque, content_bloque);

        return bloque;
    }

    public void killServer(){
        /*
          Utilizado por el proceso tracker, marca como true el fin de la ejecucion del proceso server.
         */

        this.signal_to_kill = true;
    }

    @Override
    public void run(){
        //Obtengo las instancias de los semaforos a utilizar, llamando a los metodos de la clase semaforos
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_server = semaforos.getSemaforoServerRequest();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();

        //Flag booleana que pone fin al run
        boolean stop = this.signal_to_kill;

        while(!stop){
            /*En primera instancia, si no hay ninguna solicitud el proceso quedara bloqueado. 
            Cuando algun leecher envie una solicitud o el tracker pone en true signal_to_kill, levantara al server*/
            s_server.acquireUninterruptibly();

            //Accedo a la region critica para verificar la flag de signal_to_kill por si fue puesta en true por el tracker.
            s_m_server.acquireUninterruptibly();
            stop = this.signal_to_kill;
            s_m_server.release();

            if(stop){
                /*El prroceso servidor fue levantado para finalizar su ejecucion por el tracker.
                Accedo a la region critica de la consola, e informo que el proceso server se esta por hacer kill */

                s_m_printer.acquireUninterruptibly();
                System.out.println("Proceso servidor muriendo...");
                s_m_printer.release();
                break;

            }else{
                //El proceso servidor no fue levantado para finalizar su ejecucion.

                //Accedo a la region critica del buffer de solicitudes del server y obtengo una request
                s_m_server.acquireUninterruptibly();
                RequestBloque request = requests.poll();
                s_m_server.release();

                if(request != null){
                    // Obtengo la referencia del leecher que origino la request, y su id
                    Leecher leecher = request.getLeecher();
                    Integer id_leecher = leecher.getId();

                    //Llamo al metodo returnBloque con la request, devuelve el bloque solicitado
                    Bloque answer_server = returnBloque(request);

                    //Obtengo el semaforo mutex asociado al buffer de respuestas de bloques del leecher
                    Semaphore s_m_leecher = semaforos.getMutexBbLeecherX(id_leecher);
                    
                    //Accedo a la region critica y cargo la respuesta
                    s_m_leecher.acquireUninterruptibly();
                    leecher.addAnswerBlock(answer_server);
                    s_m_leecher.release();

                    //Levanto al leecher
                    Semaphore s_leecher = semaforos.getSemaforoLeecherX(id_leecher);
                    s_leecher.release(); 
                }
            }
            
        }
    }
    

}
