package prueba;

import java.util.*;

public class Tracker implements Runnable{
    private int id;
    private ArrayList<Leecher> leechers;
    private Map<Integer, ArrayList<Integer>> bloque_leecher;// representa bloque_index lo tiene {p1, ... , pn};
    private RegistroServer registroServer;
    private Semaforo semaforos;

    public Tracker(int id,  ArrayList<Leecher> leechers, RegistroServer registroServer, Semaforo semaforos) {
        this.id = id;
        this.leechers = leechers;
        this.bloque_leecher = new HashMap<>();
        for (int i = 0; i < 15; i++) {  // suponemos que son 15 bloques
            bloque_leecher.put(i, new ArrayList<>());
        }
        this.registroServer = registroServer;
        this.semaforos = semaforos;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Leecher> getPeers() {
        return leechers;
    }

    public void addPeer(Leecher leecher) {
        this.leechers.add(leecher);
    }

    public Leecher consultarBloque(Integer key, int id) {
        //Responde ante la solicitud de un leecher
        this.semaforos.getSemaforoRegistro().acquireUninterruptibly();
        ArrayList < Integer > pids = this.registroServer.getPids(key);
        this.semaforos.getSemaforoRegistro().release();
        /*if(this.bloque_leecher.get(key) == null) return null;

        ArrayList <Integer> pids = this.bloque_leecher.get(key);
        for(Integer i : pids){
            if( leechers.get(i).getId() != id && leechers.get(i).poseeDato(key)){
                return leechers.get(i);
            }
        }*/
        if(pids.size() == 0){
            return null;
        }
        
        Random rand = new Random();
        Integer aleatorio = rand.nextInt(pids.size());
        return leechers.get(pids.get(aleatorio));
    }


    public void actualizarTracker(Integer key, Integer pid){
        this.bloque_leecher.get(key).add(pid);

    }
    
    public boolean archivo_distribuido(){
        for(Map.Entry<Integer, ArrayList<Integer>> entry: this.bloque_leecher.entrySet()){
            if(entry.getValue().size() < 4) return false; //Esto significa que ese bloque index, no es poseido por todos los leechers
        }
        return true;
    }
    


    @Override
    public void run(){
        boolean flag = false;
        while(!flag){
            flag = archivo_distribuido();
            if(flag){
                System.out.println();
                System.out.println();
                System.out.println("TRACKER DISTRIBUYO EL 100% DE LOS BLOQUES EN LOS PROCESOS. MATANDO PROCESO TRACKER...");
                System.out.println();
                System.out.println();
            }
        }
    }

    public void setLeechers(ArrayList<Leecher> leechers) {
        this.leechers = leechers;
    }
}
