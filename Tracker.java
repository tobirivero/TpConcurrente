import java.util.*;

public class Tracker {
    private int id;
    private ArrayList<Leecher> leechers;

    public Tracker(int id) {
        this.id = id;
        this.leechers = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public ArrayList<Leecher> getPeers() {
        return leechers;
    }

    public void addPeer(Leecher leecher) {
        this.leechers.add(leecher);
    }

    public Integer consultarBloque(Integer key, int id) {

        for (Leecher leecher : leechers) {

            if (id != leecher.getId()) { // me fijo en todos menos en el que consulto

                if (leecher.enviarDato(key) != null) {

                    return leecher.enviarDato(key);
                }

            }

        }
        return null;
    }

    public void actualizarArr(Leecher leecher) {
        for (int i = 0; i < leechers.size(); i++) {
            if (leechers.get(i).getId() == leecher.getId()) {
                leechers.set(i, leecher);
                break;
            }
        }
    }
}
