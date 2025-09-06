import java.util.HashMap;
import java.util.Map;

public class Server {
    private Map<Integer, Integer> archivo;

    public Server() {
        this.archivo = new HashMap<>();
    }

    public Integer consultarBloque(Integer key) {
        return this.archivo.get(key); // asumo que el server tiene el bloque
    }
}
