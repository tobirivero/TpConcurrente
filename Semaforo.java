package prueba;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Semaforo {
    private Map<String, Semaphore> semaforos = new HashMap<>();
    // semaforos.get("P4"); -> semaforo proceso 4.

    public Semaforo() {
        for (int i = 0; i < 4; i++) {
            semaforos.put("P" + i, new Semaphore(1));
        }
        semaforos.put("Tracker", new Semaphore(1));
        semaforos.put("Server", new Semaphore(1));
        semaforos.put("Printer", new Semaphore(1));
        semaforos.put("Registro", new Semaphore(1));
    }

    private Semaphore obtenerSemaforo(String nombreDeSemaforo) {
        Semaphore s = this.semaforos.get(nombreDeSemaforo);
        if (s == null)
            throw new IllegalArgumentException("Número de semáforo inválido: " + nombreDeSemaforo);
        return s;
    }

    public void upSemaforo(String nombreDeSemaforo) {
        obtenerSemaforo(nombreDeSemaforo).release();
    }

    public void downSemaforo(String nombreDeSemaforo) {
        obtenerSemaforo(nombreDeSemaforo).acquireUninterruptibly();
    }

}
