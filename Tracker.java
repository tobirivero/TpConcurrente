import java.util.*;

public class Tracker {
    private int id;
    private ArrayList<Peer> peers;

    public Tracker(int id) {
        this.id = id;
        this.peers = new ArrayList<>();
    }
    public int getId() {
        return id;
    }
    public ArrayList<Peer> getPeers() {
        return peers;
    }
    public void addPeer(Peer peer) {
        this.peers.add(peer);
    }
    public boolean buscarDato(int dato){
        for(Peer peer : peers){
            if(peer.tieneDato(dato)){
                peer.enviarDato(dato);
                return true;
            }
        }
        return false;
    }
    public void actualizarArr(Peer peer){
        for(int i = 0; i < peers.size(); i++){
            if(peers.get(i).getId() == peer.getId()){
                peers.set(i, peer);
                return;
            }
        }
    }
}
