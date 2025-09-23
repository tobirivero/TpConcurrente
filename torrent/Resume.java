package torrent;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Resume {
    private ArrayList < ArrayList< String> > matriz;
    
    public Resume() {
        this.matriz = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ArrayList<String> fila = new ArrayList<>();
            for (int j = 0; j < 15; j++) {
                fila.add("");
            }
            this.matriz.add(fila);
        }
    }

    public void writeTransfer(Integer peer_id, Integer index_bloque, String source){
        this.matriz.get(peer_id).set(index_bloque, source);
    }

    public boolean isDone(){
        for(ArrayList<String> fila: matriz){
            for(String celda : fila){
                if(celda.equals("")) return false;
            }
        }
        return true;
    }

    public void saveToFile(String filename){
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("--- Resumen de transferencias de bloques ---\n\n");
            fw.write("Peer X recibio del Peer Y (o server) el bloque K\n\n");
            // Cabecera de bloques
            fw.write(String.format("%-10s", "Bloque"));
            for(int i=0; i<matriz.size(); i++){
                fw.write(String.format("%-15s", "Peer " + i));
            }
            fw.write("\n");

            // Contenido
            for(int j=0; j<matriz.get(0).size(); j++){
                fw.write(String.format("%-10d", j)); // indice del bloque
                for(int i=0; i<matriz.size(); i++){
                    fw.write(String.format("%-15s", matriz.get(i).get(j)));
                }
                fw.write("\n");
            }

            fw.flush();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
