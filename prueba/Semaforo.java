package prueba;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Semaforo {
    private ArrayList<Semaphore> semaforos;
    // semaforos.get(4); -> semaforo proceso 4. Por convencion el ultimo semaforo es el del tracker

    public Semaforo(ArrayList<Semaphore> semaforos){
        this.semaforos = semaforos;
    }    
    
    public void setSemaforos(ArrayList<Semaphore> semaforos) {
        this.semaforos = semaforos;
    }

    public ArrayList<Semaphore> getSemaforos() {
        return semaforos;
    }

    public Semaphore getSemaforoById(Integer id){
        return semaforos.get(id);
    }
    public Semaphore getSemaforoTracker(){
        return semaforos.get(4);
    }
    public Semaphore getSemaforoServer(){
        return semaforos.get(5);
    }
    public Semaphore getSemaforoPrinter(){
        return semaforos.get(6);
    }
    public Semaphore getSemaforoRegistro(){
        return semaforos.get(7);
    }

    //Capaz cambiar


}
