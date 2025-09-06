import java.util.Map;
import java.util.HashMap;

public class Leecher implements Runnable {
    private int id;
    private Map<Integer, Integer> archivo;
    private boolean soySeeder;
    private Server server;
    private Tracker tracker;

    public Leecher(int id, Server server, Tracker tracker) {
        this.id = id;
        this.archivo = new HashMap<>();

        for (int i = 0; i < 15; i++) {
            this.archivo.put(i, -1); // Inicializa todos los chunks como no poseídos
        }

        this.soySeeder = false;
        this.server = server;
    }

    public int getId() {
        return this.id;
    }

    public Map<Integer, Integer> getArchivo() {
        return this.archivo;
    }

    public void setArchivo(Map<Integer, Integer> archivo) {
        this.archivo = archivo;
    }

    private Integer obtenerIndiceFaltante() { // metodo para ver si hay algun bloque faltante,

        Integer key = null;
        if (this.archivo.containsValue(-1)) {
            for (Map.Entry<Integer, Integer> entry : this.archivo.entrySet()) {
                if (entry.getValue().equals(-1)) {
                    key = entry.getKey();
                    return key; // retorna el indice del valor
                }
            }
        }
        return null;

    }

    public Integer enviarDato(Integer key) {
        return this.archivo.getOrDefault(key, null); // envia el dato, si no esta envia nulo
    }

    public void run() {

        while (!soySeeder) {

            if (obtenerIndiceFaltante() == null) {

                System.out.println("Soy Seeder");
                this.soySeeder = true;

                continue;

            }

            Integer key = obtenerIndiceFaltante();

            if (tracker.consultarBloque(key, getId()) == null) { // si no hay ningun leecher o seeder que tenga el dato
                this.archivo.put(key, server.consultarBloque(key)); // consulto al servidor
            } else {
                this.archivo.put(key, tracker.consultarBloque(key, getId()));
            }
        }

        while (true) // como es seeder se queda escuchando a ver si alguien pide bloque
            ;

    }

    // public abstract void compartirArchivo(Tracker tracker);

    // public abstract void consultar_Tracker(Tracker tracker);

    // public abstract void imprimirEstado();
}
