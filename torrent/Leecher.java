package torrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Leecher implements  Runnable{
    private Integer id;
    private Queue <AnswerTracker> buffer_tracker;
    private Queue <Bloque> buffer_bloques;
    private Semaforos semaforos;
    private Archivo mi_archivo;
    private Tracker tracker;
    private Server server;
    public Leecher (Integer id, Semaforos semaforos, Archivo archivo, Tracker tracker, Server server){
        this.id = id;
        this.semaforos = semaforos;
        this.buffer_tracker = new LinkedList<>();
        this.buffer_bloques = new LinkedList<>();
        this.mi_archivo = archivo;
        this.tracker = tracker;
        this.server = server;
    }

    public Bloque bloque_faltante(){
        //Obtiene un bloque random de los faltantes
        Semaphore mutex_archivo = semaforos.getMutexArchivo(id);
        
        mutex_archivo.acquireUninterruptibly();
        List < Bloque > faltantes = mi_archivo.faltantes();
        mutex_archivo.release();

        int size_faltantes = faltantes.size();
        if(size_faltantes == 0){
            return null;
        }
        Random rand = new Random();
        int indice = rand.nextInt(size_faltantes);
        
        return faltantes.get(indice);
    }

    public void updateArchivo(Bloque bloque){
        Semaphore mutex_archivo = semaforos.getMutexArchivo(this.id);
        
        mutex_archivo.acquireUninterruptibly();
        mi_archivo.writeBloque(bloque);
        mutex_archivo.release();
    }
    public Integer getId(){
        return this.id;
    }

    public void addAnswerTracker(AnswerTracker answer){
        this.buffer_tracker.add(answer);
    }

    public void addAnswerBlock(Bloque bloque){
        this.buffer_bloques.add(bloque);
    }

    @Override
    public void run(){

        Semaphore mi_semaforo = semaforos.getSemaforoLeecherX(this.getId());
        Semaphore mutex_buffer_bloques = semaforos.getMutexBbLeecherX(this.id);
        Semaphore mutex_buffer_tracker = semaforos.getMutexBtLeecherX(this.id);
        Semaphore s_tracker_request = semaforos.getSemaforoTrackerRequest();
        Semaphore s_tracker = semaforos.getSemaforoTracker();
        Semaphore s_server_request = semaforos.getSemaforoServerRequest(); 
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        Semaphore s_m_file = semaforos.getMutexArchivo(this.getId());
        boolean stop = false;
        while(!stop){
            Bloque next_bloque = bloque_faltante();
            stop = (next_bloque == null);

            if(!stop){
                //Genero request de tipo 0 al tracker
                RequestTracker request_tracker = new RequestTracker(this, 0, next_bloque.getId_bloque(),null);


                s_tracker_request.acquireUninterruptibly();
                tracker.addRequest(request_tracker);
                s_tracker_request.release();

                //Levanto al tracker
                s_tracker.release();

                //Me duermo hasta que el tracker responda mi request
                mi_semaforo.acquireUninterruptibly();

                //Procedo con la transferencia
                mutex_buffer_tracker.acquireUninterruptibly();
                AnswerTracker answer_tracker = this.buffer_tracker.poll();
                mutex_buffer_tracker.release();

                if(answer_tracker != null && answer_tracker.getAnswerStatus().equals(-1)){
                    s_server_request.acquireUninterruptibly();
                    RequestBloque request_server = new RequestBloque(this, next_bloque.getId_bloque());
                    server.addRequest(request_server);
                    s_server_request.release();

                    //Levanto al servidor
                    s_server.release();

                    //Me duermo hasta que el servidor responda mi request
                    mi_semaforo.acquireUninterruptibly();
                }else if(answer_tracker != null){
                    Seeder seeder_answer = answer_tracker.getSeeder();
                    Integer id_seeder_answer = answer_tracker.getSeeder().getId();
                    Semaphore s_m_seeder_answer = semaforos.getMutexSeederX(id_seeder_answer);

                    //Cargo la request en el seeder
                    s_m_seeder_answer.acquireUninterruptibly();
                    RequestBloque request_seeder = new RequestBloque(this, next_bloque.getId_bloque());
                    seeder_answer.addRequest(request_seeder);
                    s_m_seeder_answer.release();

                    Semaphore s_seeder_answer = semaforos.getSemaforoSeederX(id_seeder_answer);

                    //Levanto al seeder
                    s_seeder_answer.release();

                    //Me duermo hasta que el seeder responda mi request
                    mi_semaforo.acquireUninterruptibly();
                }

                //Vacio mi buffer de las respuestas que recibi tanto de server o seeders
                List < Integer > new_bloques = new ArrayList<>();
                
                mutex_buffer_bloques.acquireUninterruptibly();
                while(!buffer_bloques.isEmpty()){
                    Bloque bloque = buffer_bloques.poll();
                    updateArchivo(bloque);
                    new_bloques.add(bloque.getId_bloque());
                }
                mutex_buffer_bloques.release();

                //Llamo al tracker a que se actualice con request de tipo 1
                s_tracker_request.acquireUninterruptibly();
                RequestTracker update_request = new RequestTracker(this, 1, next_bloque.getId_bloque(),new_bloques);
                tracker.addRequest(update_request);
                s_tracker_request.release();

                s_tracker.release();
            }else{
                //Leecher obtuvo todos los bloques del archivo
                //Muestro por consola el archivo
                s_m_file.acquireUninterruptibly();
                mi_archivo.printFile(this.id, s_m_printer);
                s_m_file.release();
                break;
                
            }
        }
    }
}