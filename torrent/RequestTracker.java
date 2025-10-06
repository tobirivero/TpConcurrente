package torrent;

import java.util.List;

/*
  Es utilizada por los leechers con dos propositos:
  1)Solicitar informacion de un bloque al tracker
    Esta solicitud es de tipo 0, contiene el id del bloque que el leecher solicita. En este caso, bloques_to_update = NULL
  2)Pedirle al tracker que se actualice
    Esta solicitud es de tipo 1, contiene la lista de nuevos bloques diposnibles del peer. En este caso, index_bloque = NULL.

    Ambas solicitudes contienen la referencia del leecher que crea la solicitud.
 */

public class RequestTracker {
    private Leecher leecher;
    private Integer tipo; 
    private Integer index_bloque;
    private List < Integer > bloques_to_update;
    

    public RequestTracker( Leecher leecher, Integer tipo, Integer index_bloque, List<Integer> bloques_to_update) {
        this.index_bloque = index_bloque;
        this.tipo = tipo;
        this.leecher = leecher;
        this.bloques_to_update = bloques_to_update;
    }

    public Leecher getLeecher() {
        return leecher;
    }

    public Integer getIndex_bloque() {
        return index_bloque;
    }

    public void setLeecher(Leecher leecher) {
        this.leecher = leecher;
    }

    public void setIndex_bloque(Integer index_bloque) {
        this.index_bloque = index_bloque;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }
    public List<Integer> getBloques_to_update() {
        return bloques_to_update;
    }

    public void setBloques_to_update(List<Integer> bloques_to_update) {
        this.bloques_to_update = bloques_to_update;
    }
    
}
