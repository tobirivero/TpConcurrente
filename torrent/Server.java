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
        //Array aux
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
        this.requests.add(request);
    }

    public Bloque returnBloque(RequestBloque request){
        Integer index_bloque = request.getIndex_bloque();
        String content_bloque = this.archivo.get(index_bloque).getContent();

        Bloque bloque = new Bloque(index_bloque, content_bloque);

        return bloque;
    }

    public void killServer(){
        this.signal_to_kill = true;
    }

    @Override
    public void run(){
        boolean stop = this.signal_to_kill;
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_server = semaforos.getSemaforoServerRequest();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        while(!stop){
            s_server.acquireUninterruptibly();

            s_m_server.acquireUninterruptibly();
            RequestBloque request = requests.poll();
            s_m_server.release();

            if(request != null){
                Leecher leecher = request.getLeecher();
                Integer id_leecher = leecher.getId();

                Bloque answer_server = returnBloque(request);

                Semaphore s_m_leecher = semaforos.getMutexBbLeecherX(id_leecher);
                //Accedo al buffer de respuesta de el leecher X
                s_m_leecher.acquireUninterruptibly();
                leecher.addAnswerBlock(answer_server);
                s_m_leecher.release();

                Semaphore s_leecher = semaforos.getSemaforoLeecherX(id_leecher);
                
                //Levanto al leecher
                s_leecher.release(); 
            }

            stop = this.signal_to_kill;
            if(stop){
                s_m_printer.acquireUninterruptibly();
                System.out.println("Proceso servidor muriendo...");
                s_m_printer.release();
            }
        }
    }
    

}
