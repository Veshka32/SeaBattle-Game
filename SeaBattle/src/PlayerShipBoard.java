import java.awt.*;

public class PlayerShipBoard extends ShipBoard{

    public PlayerShipBoard() {
        this.leftCornerX=0;
    }

    //just for computer needs
    public void playerGetPseudoShot(int n){
        field[n]=-1;
    }

    //manually place ship
    public void placeShip(Ship ship, int id, boolean[] usedCells) {
        //ship.draw();
        for (int n : ship.getSurrounded()) {
            usedCells[n] = true;
        }
        ships[id] = ship;
        for (int n : ships[id].position()) field[n] = id;
    }
}
