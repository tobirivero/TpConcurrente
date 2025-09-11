import java.util.*;

public class Tracker implements Runnable {
    private int id;
    private ArrayList<Leecher> leechers;
    private RegistroServer registroServer;
    private Semaforo semaforos;

    public Tracker(int id, ArrayList<Leecher> leechers, RegistroServer registroServer, Semaforo semaforos) {
        this.id = id;
        this.leechers = leechers;
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

    public Leecher consultarBloque(Integer key, Integer id) {
        // Responde ante la solicitud de un leecher
        this.semaforos.downSemaforo("Registro");
        ArrayList<Integer> pids = this.registroServer.getPids(key);
        this.semaforos.upSemaforo("Registro");

        if (pids.size() == 0) {
            return null;
        }

        Random rand = new Random();
        Integer aleatorio = rand.nextInt(pids.size());
        return leechers.get(pids.get(aleatorio));
    }

    public void informarSoyPoseedor(Integer key, Integer pid) {
        // leecher informa al tracker que posee un bloque
        this.semaforos.downSemaforo("Registro");
        this.registroServer.addPid(key, id);
        this.semaforos.upSemaforo("Registro");
    }

    @Override
    public void run() {
        boolean flag = false;
        while (!flag) {
            flag = this.registroServer.archivo_distribuido();
            if (flag) {
                System.out.println();
                System.out.println();
                System.out.println(
                        "TRACKER DISTRIBUYO EL 100% DE LOS BLOQUES EN LOS PROCESOS. MATANDO PROCESO TRACKER...");
                System.out.println();
                System.out.println();
            }
        }
        return;
    }

    public void setLeechers(ArrayList<Leecher> leechers) {
        this.leechers = leechers;
    }
}
