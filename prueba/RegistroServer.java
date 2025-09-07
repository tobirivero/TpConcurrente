package prueba;
import java.util.*;

public class RegistroServer {
    private Map <Integer, ArrayList<Integer>> registro;
    public RegistroServer(){
        this.registro = new HashMap<>(); // inicializar primero
        for(int i=0 ; i<15 ; i++){
            this.registro.put(i, new ArrayList<Integer>());
        }
    }
    public ArrayList<Integer> getPids(Integer index){
        return this.registro.getOrDefault(index, null);
    }

    public void addPid(Integer index, Integer pid){
        this.registro.get(index).add(pid);
    }
}
