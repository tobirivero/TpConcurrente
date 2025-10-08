package torrent;

/*
  Utilizado por los leechers para generar solicitudes de bloque al server o a los seeders.
  Esta request contiene la referencia del leecher que creo la request, y el id del bloque que solicita.
 */

public class RequestBloque {
    private Leecher leecher;
    private Integer index_bloque;

    public RequestBloque(Leecher leecher, Integer index_bloque) {
        this.leecher = leecher;
        this.index_bloque = index_bloque;
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
}
