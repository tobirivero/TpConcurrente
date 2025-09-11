import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Leecher implements Runnable {
    private int id;
    private Map<Integer, String> archivo;
    private boolean soySeeder;
    private Server server;
    private Tracker tracker;
    private Semaforo semaforos;// atributo semaforos

    public Leecher(int id, Server server, Tracker tracker, Semaforo semaforos) {
        this.id = id;
        this.archivo = new HashMap<>();

        for (int i = 0; i < 15; i++) { // Asumimos que tiene 15 bloques el archivo
            this.archivo.put(i, ""); // Inicializa todos los chunks como no poseídos
        }

        this.semaforos = semaforos;
        this.soySeeder = false;
        this.server = server;
        this.tracker = tracker;
    }

    public int getId() {
        return this.id;
    }

    public Map<Integer, String> getArchivo() {
        return this.archivo;
    }

    public void setArchivo(Map<Integer, String> archivo) {
        this.archivo = archivo;
    }

    private Integer obtenerIndiceFaltante() { // metodo para ver si hay algun bloque faltante,
        List<Integer> faltantes = new ArrayList<>();

        // juntamos indices de bloques faltantes
        for (Map.Entry<Integer, String> entry : this.archivo.entrySet()) {
            if (entry.getValue().equals("")) {
                faltantes.add(entry.getKey());
            }
        }

        if (faltantes.isEmpty())
            return null; // No falta ningun indice del archivo

        Random rand = new Random();
        Integer aleatorio = rand.nextInt(faltantes.size());

        return faltantes.get(aleatorio); // Devolvemos algun indice random
    }

    public boolean poseeDato(Integer key) {
        return !(this.archivo.get(key).equals(""));
    }

    public String enviarDato(Integer key) {
        return this.archivo.getOrDefault(key, ""); // envia el dato, si no esta envia nulo
    }

    public void mostrar_archivo() {
        // Imprimir archivo ordenado por indice
        for (int i = 0; i < archivo.size(); i++) {
            System.out.print(archivo.get(i) + " ");
        }
        System.out.println();
    }

    public void actualizar_mi_archivo(Integer index, String content) {
        this.archivo.put(index, content);
    }

    @Override
    public void run() {

        while (!soySeeder) {
            Integer key = obtenerIndiceFaltante();

            if (key == null) {
                semaforos.downSemaforo("Printer");
                System.out.println();
                System.out.println("Proceso " + this.id + " con archivo al 100%:");
                mostrar_archivo();
                System.out.println();
                System.out.println("MATANDO PID " + this.id + "...");
                semaforos.upSemaforo("Printer");
                this.soySeeder = true;
                break;
            }

            semaforos.downSemaforo("Tracker");
            if (tracker.consultarBloque(key, this.id) == null) { // si no hay ningun leecher o seeder que tenga el dato,
                                                                 // vamos al servidor
                semaforos.upSemaforo("Tracker");

                semaforos.downSemaforo("Server");
                // System.out.println(this.id + " solicita al Servidor");
                String contenido_bloque = server.consultarBloque(key, this.id); // consulto al servidor
                semaforos.upSemaforo("Server");

                if (contenido_bloque == null)
                    continue;
                else {
                    // Actualizo mi archivo
                    this.actualizar_mi_archivo(key, contenido_bloque);
                }

            } else {
                // Tracker debe devolver el/los peers que tienen el bloque con index key
                Leecher consultar_a = this.tracker.consultarBloque(key, this.id);

                semaforos.upSemaforo("Tracker");

                semaforos.downSemaforo("P" + consultar_a.getId());
                // System.out.println("PID "+this.id + " solicita BLOQUE: "+key+" a " + "PID
                // "+consultar_a.getId());
                String contenido_bloque = consultar_a.enviarDato(key);
                semaforos.upSemaforo("P" + consultar_a.getId());

                // Actualizo mi archivo
                this.actualizar_mi_archivo(key, contenido_bloque);

                // informo al tracker que poseo un nuevo bloque
                semaforos.downSemaforo("Tracker");
                this.tracker.informarSoyPoseedor(key, this.id);
                semaforos.upSemaforo("Tracker");

            }
        }

    }

    public Semaforo getSemaforos() {
        return semaforos;
    }

    public void setSemaforos(Semaforo semaforos) {
        this.semaforos = semaforos;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }
}
