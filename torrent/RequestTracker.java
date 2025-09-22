package torrent;

import java.util.List;

public class RequestTracker {
    private Leecher leecher;
    private Integer tipo; //Definimos tipo 0 para solicitar datos, 1 para actualizar tracker
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
