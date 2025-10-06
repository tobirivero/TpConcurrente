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
    private Resume resume;
    public Leecher (Integer id, Semaforos semaforos, Archivo archivo, Tracker tracker, Server server, Resume resume){
        this.id = id;
        this.semaforos = semaforos;
        this.buffer_tracker = new LinkedList<>();
        this.buffer_bloques = new LinkedList<>();
        this.mi_archivo = archivo;
        this.tracker = tracker;
        this.server = server;
        this.resume = resume;
    }

    public Bloque bloque_faltante(){
        /*
            Devuelve de forma aleatoria un bloque faltante del archivo (null si el archivo esta completo). 
            Utiliza el semaforo mutex asociado al archivo del peer para acceder a la seccion critica, luego libera el recurso.
         */


        Semaphore s_m_file = semaforos.getMutexArchivo(id);
        
        s_m_file.acquireUninterruptibly();
        List < Bloque > faltantes = mi_archivo.faltantes();
        s_m_file.release();

        int size_faltantes = faltantes.size();
        if(size_faltantes == 0){
            return null;
        }
        Random rand = new Random();
        int indice = rand.nextInt(size_faltantes);
        
        return faltantes.get(indice);
    }

    public void updateArchivo(Bloque bloque){
        /*
          Actualiza el bloque del archivo (sobreescribe el bloque vacio). Llama al metodo writeBloque de la instancia del archivo.
           Utiliza el semaforo mutex asociado al archivo del peer para acceder a la seccion critica, luego libera el recurso.
         */

        Semaphore s_m_file = semaforos.getMutexArchivo(this.id);
        
        s_m_file.acquireUninterruptibly();
        mi_archivo.writeBloque(bloque);
        s_m_file.release();
    }

    public Integer getId(){
        return this.id;
    }

    public void addAnswerTracker(AnswerTracker answer){
        /*
         Utilizado por el proceso tracker. Escribe la respuesta del tracker en el buffer_tracker.
         Se accede como region critica (desde el tracker).
         */

        this.buffer_tracker.add(answer);
    }

    public void addAnswerBlock(Bloque bloque){
        /*
         Utilizado por el proceso server o seeder. Escribe la respuesta de bloque el buffer_bloques.
         Se accede como region critica (desde el server o seeder).
         */
        this.buffer_bloques.add(bloque);
    }

    @Override
    public void run(){
        //Obtengo las instancias de los semaforos a utilizar, llamando a los metodos de la clase semaforos
        Semaphore s_leecher = semaforos.getSemaforoLeecherX(this.getId());
        Semaphore s_m_buffer_bloques = semaforos.getMutexBbLeecherX(this.id);
        Semaphore s_m_buffer_tracker = semaforos.getMutexBtLeecherX(this.id);
        Semaphore s_m_tracker = semaforos.getSemaforoTrackerRequest();
        Semaphore s_tracker = semaforos.getSemaforoTracker();
        Semaphore s_m_server = semaforos.getSemaforoServerRequest(); 
        Semaphore s_server = semaforos.getSemaforoServer();
        Semaphore s_m_printer = semaforos.getSemaforoPrinter();
        Semaphore s_m_file = semaforos.getMutexArchivo(this.getId());
        Semaphore s_m_resume = semaforos.getMutexResume();

        //Flag booleana que pone fin al run, es true cuando no hay mas bloques faltantes
        boolean stop = false;


        while(!stop){

            Bloque next_bloque = bloque_faltante();
            stop = (next_bloque == null);
            String source_answer = "Server";

            if(!stop){
                //Existe un bloque faltante, genero request de tipo 0 para enviar al tracker
                RequestTracker request_tracker = new RequestTracker(this, 0, next_bloque.getId_bloque(),null);

                //Accedo a la region critica del buffer de solicitudes del tracker y cargo la request.
                s_m_tracker.acquireUninterruptibly();
                tracker.addRequest(request_tracker);
                s_m_tracker.release();

                //Levanto al tracker
                s_tracker.release();

                //Me duermo hasta que el tracker responda mi request
                s_leecher.acquireUninterruptibly();

                //Accedo a la region critica de mi buffer de respuestas del tracker, y tomo una respuesta.
                s_m_buffer_tracker .acquireUninterruptibly();
                AnswerTracker answer_tracker = this.buffer_tracker.poll();
                s_m_buffer_tracker .release();

                //Transferencia
                if(answer_tracker != null && answer_tracker.getAnswerStatus().equals(-1)){
                    //Como la status de la respuesta es -1, no hay seeders en la red que posean el bloque 
                     
                    //Genero request de bloque al server
                    RequestBloque request_server = new RequestBloque(this, next_bloque.getId_bloque());

                    //Accedo a la region critica del buffer de solicitudes del server y cargo la request.
                    s_m_server.acquireUninterruptibly();
                    server.addRequest(request_server);
                    s_m_server.release();

                    //Levanto al servidor
                    s_server.release();

                    //Me duermo hasta que el servidor responda mi request
                    s_leecher.acquireUninterruptibly();

                }else if(answer_tracker != null){
                    //Como el status de la respuesta no es -1, hay seeders en la red que poseen el bloque

                    /*De la respuesta del tracker agarro:
                        -la referencia del seeder, 
                        -su id 
                        -el semaforo asociado al buffer de solicitudes de dicho seeder */

                    Seeder seeder_answer = answer_tracker.getSeeder();
                    Integer id_seeder_answer = answer_tracker.getSeeder().getId();
                    Semaphore s_m_seeder_answer = semaforos.getMutexSeederX(id_seeder_answer);

                    //String del id del seeder
                    source_answer = "" + id_seeder_answer; 


                    //Genero request de bloque al seeder
                    RequestBloque request_seeder = new RequestBloque(this, next_bloque.getId_bloque());
                    
                    //Accedo a la region critica del buffer de solicitudes del seeder y cargo la request.
                    s_m_seeder_answer.acquireUninterruptibly();
                    seeder_answer.addRequest(request_seeder);
                    s_m_seeder_answer.release();

                    //Obtengo el semaforo asociado al run del seeder
                    Semaphore s_seeder = semaforos.getSemaforoSeederX(id_seeder_answer);

                    //Levanto al seeder
                    s_seeder.release();

                    //Me duermo hasta que el seeder responda mi request
                    s_leecher.acquireUninterruptibly();
                }

                /*
                Entre la linea , ocurre:
                    1) Me hago de la region critica de mi buffer de respuestas de bloques
                    2) Iterio y vacio dicho buffer, almacenando los nuevos bloques en una lista
                    3) En cada iteracion escribo en el resumen mediante el metodo writeTransfer() y actualizo mi archivo,
                    4) Llamo al metodo de isDone del resumen, si es true llamo al metodo saveToFile
                    5) Libero el recurso 1)
                    -*/
                List < Integer > new_bloques = new ArrayList<>();
                
                s_m_buffer_bloques.acquireUninterruptibly();
                while(!buffer_bloques.isEmpty()){
                    Bloque bloque = buffer_bloques.poll();

                    updateArchivo(bloque);
                    new_bloques.add(bloque.getId_bloque());

                    s_m_buffer_bloques.release();

                    s_m_resume.acquireUninterruptibly();
                    resume.writeTransfer(this.id, bloque.getId_bloque(), source_answer);
                    if(resume.isDone()){
                        resume.saveToFile("resumen.txt");
                    }
                    s_m_resume.release();
                    
                    s_m_buffer_bloques.acquireUninterruptibly();
                }
                s_m_buffer_bloques.release();

                //genero request de tipo 1 para enviar al tracker
                RequestTracker update_request = new RequestTracker(this, 1, null,new_bloques);
                
                //Accedo a la region critica del buffer de solicitudes del tracker y cargo la request.
                s_m_tracker.acquireUninterruptibly();
                tracker.addRequest(update_request);
                s_m_tracker.release();
            
                //Levanto al tracker
                s_tracker.release();

            }else{
                //No existen bloques faltantes

                //Accedo a la region critica del archivo compartido del peer, y llamo al metodo printFile
                s_m_file.acquireUninterruptibly();
                mi_archivo.printFile(this.id, s_m_printer);
                s_m_file.release();

                
                break;
                
            }
        }
    }
}