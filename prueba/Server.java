package prueba;

import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable{
    private Map<Integer, String> archivo;
    private boolean[] bloques_en_red;
    private RegistroServer registroServer;
    private Semaforo semaforos;
    private static int nro = 0;
    String[] oracion = {
    "El", "gato", "negro", "saltó", "rapido", 
    "sobre", "el", "techo", "y", "miro", 
    "la", "luna", "brillante", "con", "curiosidad"
    };


    public Server(RegistroServer registroServer, Semaforo semaforos) {
        this.archivo = new HashMap<>();
        for(int i=0 ; i<15 ; i++){
            this.archivo.put( (Integer)i, oracion[i]);
        }
        this.bloques_en_red  = new boolean[15];
        this.registroServer = registroServer;
        this.semaforos = semaforos;
        
    }

    public String consultarBloque(Integer key, Integer pid) {
        if(bloques_en_red[key]) return null;

        semaforos.getSemaforoRegistro().acquireUninterruptibly();
        marcar_bloque(key);
        this.registroServer.addPid(key, pid);
        semaforos.getSemaforoRegistro().release();
        
        System.out.println("El servidor devolvio el BLOQUE: " + key);

        return this.archivo.get(key); // asumo que el server tiene el bloque
    }

    public void marcar_bloque(Integer index){
        this.bloques_en_red[index]=true;
    }

    public boolean estanTodosEnRed(){
        for(int i=0 ; i<15 ; i++){
            if(bloques_en_red[i] == false) return false;
        }
        return true;
    }

    @Override
    public void run(){
        
        while (true) {
            if (estanTodosEnRed()) {
                System.out.println("EL SERVIDOR ENVIO EL 100%. MATANDO PROCESO SERVIDOR...");
                break; // salimos del bucle
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Map<Integer, String> getArchivo() {
        return archivo;
    }

    public void setArchivo(Map<Integer, String> archivo) {
        this.archivo = archivo;
    }

}
