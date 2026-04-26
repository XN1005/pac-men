package pacmen.multiplayerlogic;
import pacmen.entities.Player;

public class MultiplayerManager {
    public Player p1;
    public Player p2;
    public long timer;
    public String state;

    MultiplayerManager(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.timer = 0;
        state = "PAUSE";
    }
    
    public void update() {
        // CONTINUOUSLY CHECK FOR CONDITIONS
        if (p1.state.equals("DEAD")) {
            System.out.println("P2 WINS!");
        }
        else if (p2.state.equals("DEAD")) {
            System.out.println("P1 WINS!"); 
        }
        else { // NO DEATHS YET
            // if time out, whoever gains more points win, if draw, then it's a rematch.
            if (p1.score > p2.score && timer >= 10000) {
                System.out.println("P1 WINS!");
            }
            else if (p1.score < p2.score) {
                System.out.println("P2 WINS!");
            }
            else if (p1.score == p2.score) {
                System.out.println("REMATCH");
            }
        }
    }
}
