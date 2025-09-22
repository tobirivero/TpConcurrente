package torrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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
