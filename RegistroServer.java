import java.util.*;

public class RegistroServer {
    private Map<Integer, ArrayList<Integer>> registro;

    public RegistroServer() {
        this.registro = new HashMap<>(); // inicializar primero
        for (int i = 0; i < 15; i++) {
            this.registro.put(i, new ArrayList<Integer>());
        }
    }

    public ArrayList<Integer> getPids(Integer index) {
        return this.registro.getOrDefault(index, null);
    }

    public void addPid(Integer index, Integer pid) {
        this.registro.get(index).add(pid);
    }

    public boolean archivo_distribuido() {
        for (Map.Entry<Integer, ArrayList<Integer>> entry : this.registro.entrySet()) {
            if (entry.getValue().size() < 4)
                return false; // Esto significa que ese bloque index, no es poseido por todos los leechers
        }
        return true;
    }
}
