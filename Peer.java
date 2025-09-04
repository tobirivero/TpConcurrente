public abstract class Peer {
    private int id;
    private int archivo[];

    public Peer(int id) {
        this.id = id;
        this.archivo = new int[15];
        for (int i = 0; i < 15; i++) {
            this.archivo[i] = 0; // Inicializa todos los chunks como no poseídos
        }
    }

    public int getId() {
        return id;
    }
    public int[] getArchivo() {
        return archivo;
    }
    public void setArchivo(int[] archivo) {
        this.archivo = archivo;
    }
    
    public abstract void descargarArchivo(Tracker tracker);
    public abstract void compartirArchivo(Tracker tracker);
    public abstract void consultar_Tracker(Tracker tracker);
    public abstract void imprimirEstado();
}
