package torrent;

/*
  Un bloque esta definido por el par {id_bloque, contenido}
 */

public class Bloque {
    private Integer id_bloque;
    private String content;

    public Bloque(Integer id_bloque, String content) {
        this.id_bloque = id_bloque;
        this.content = content;
    }

    public Integer getId_bloque() {
        return id_bloque;
    }

    public String getContent() {
        return content;
    }

    public void setId_bloque(Integer id_bloque) {
        this.id_bloque = id_bloque;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
