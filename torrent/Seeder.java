package torrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Seeder implements Runnable{
    private Integer id;
    private Semaforos semaforos;
    private Archivo mi_archivo;
    private Queue <RequestBloque> requests;
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
        this.requests.add(requestBloque);
    }
    
    public Integer getId() {
        return id;
    }
    
    public Bloque returnBloque(RequestBloque request){
        Integer index_bloque = request.getIndex_bloque();

        Semaphore mutex_archivo = semaforos.getMutexArchivo(this.id);

        mutex_archivo.acquireUninterruptibly();
        Bloque bloque = mi_archivo.getBloque(index_bloque);
        mutex_archivo.release();

        return bloque;
    }

    public void killSeeder(){
        this.signal_to_kill = true;
    }
    @Override
    public void run(){
        Semaphore s_seeder = semaforos.getSemaforoSeederX(id);
        Semaphore s_m_seeder = semaforos.getMutexSeederX(id);
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        boolean stop = this.signal_to_kill;
        while(!stop){
            s_seeder.acquireUninterruptibly();

            s_m_seeder.acquireUninterruptibly();
            RequestBloque request = requests.poll();
            s_m_seeder.release();

            if(request != null){
                Bloque bloque_answer = returnBloque(request);

                Leecher leecher = request.getLeecher();
                Integer id_leecher = leecher.getId();

                Semaphore s_m_leecher = semaforos.getMutexBbLeecherX(id_leecher);
                //Mando respuesta al leecher
                s_m_leecher.acquireUninterruptibly();
                leecher.addAnswerBlock(bloque_answer);
                s_m_leecher.release();

                Semaphore s_leecher = semaforos.getSemaforoLeecherX(id_leecher);
                //Levanto al leecher
                s_leecher.release();
            }

            stop = this.signal_to_kill;
            if(stop){
                s_m_printer.acquireUninterruptibly();
                System.out.println("Proceso seeder ID "+this.id+" muriendo...");
                s_m_printer.release();
            }
        }
    }

}
