import java.awt.*;

public class PlayerShipBoard extends ShipBoard {

    public PlayerShipBoard() {
        this.leftCornerX=0;
    }

    //get shot from computer
    public int playerGetShot(int n) { //-1 for miss, 0 for shot, 1 for completed destroy
        int id = field[n];
        field[n] = -1;
        if (id == 0) {
            drawMiss(n);
            return -1;
        }
        ships[id].getShot();
        if (ships[id].isDestroyed()) {
            drawShot(n);
            return 1;
        }
        drawShot(n);
        numberOfDestroyedCells++;
        return 0;
    }

    //just for computer needs
    public void playerGetPseudoShot(int n){
        field[n]=-1;
        StdDrawForSeaBattle.setPenColor(Color.GREEN);
        StdDrawForSeaBattle.circle((getX(n) + leftCornerX + 0.5), (11 - getY(n) + 0.5), 0.1);
        StdDrawForSeaBattle.show();
        StdDrawForSeaBattle.setPenColor();
    }

    //manually place ship
    public void placeShip(Ship ship, int id, boolean[] usedCells) {
        ship.draw();
        for (int n : ship.getSurrounded()) {
            usedCells[n] = true;
        }
        ships[id] = ship;
        for (int n : ships[id].position()) field[n] = id;
    }
}
