package torrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/*
 Es una lista de bloques. Es compartido por el leecher y seeder del mismo peer.
 Provee los metodos:
 -getBloque(index) -> devuelve el bloque en el indice = index
 -writeBloque(bloque) -> sobreescribe el bloque en el archivo (en primera instancia esta vacio)
 -faltantes() -> devuelve una lista de los bloques faltantes del archivo
 -printFile(peer_id, ,semaforo_printer)  -> Informa que muere el peer con peer_id y printea en la consola el archivo final.
    Utiliza el semaoforo mutex del printer para que en la consola un solo proceso escriba a la vez.
 */

public class Archivo {
    private ArrayList < Bloque > mi_archivo;

    public Archivo(){
        this.mi_archivo = new ArrayList<>();
        for(int i=0 ; i<15 ; i++){
            Bloque bloque = new Bloque(i,"");
            mi_archivo.add(bloque);
        }
    }

    public Bloque getBloque(Integer index){
        return mi_archivo.get(index);
    }
    public void writeBloque(Bloque bloque){
        Integer index = bloque.getId_bloque();
        
        this.mi_archivo.set(index, bloque);
    }

    public List<Bloque> faltantes(){
        List < Bloque > faltantes = new ArrayList<>();

        for(Bloque b : mi_archivo){
            if(b.getContent().equals("")){
                faltantes.add(b);
            }
        }
        return faltantes;
    }

    public void printFile(Integer peer_id, Semaphore s_printer){
        s_printer.acquireUninterruptibly();
        System.out.println("Proceso leecher ID " + peer_id + " muriendo... Archivo final:");
        for(int i=0 ; i<mi_archivo.size() ; i++){
            Bloque bloque_i = mi_archivo.get(i);
            System.out.print(bloque_i.getContent() + " ");
        }
        System.out.println();
        s_printer.release();
    }
}
