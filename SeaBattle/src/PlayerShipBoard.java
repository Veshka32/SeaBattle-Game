public class PlayerShipBoard extends ShipBoard{

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
