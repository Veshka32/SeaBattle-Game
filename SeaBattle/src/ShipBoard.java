import java.awt.*;
import java.util.Random;

public class ShipBoard implements Drawable{
    int[] field = new int[101]; //0 - unchecked cell, >0 - cell occupied with ship, -1 - checked
    private final int TOTAL_SHIP_LENGTH = 20;
    int numberOfDestroyedCells = 0;
    Ship[] ships = new Ship[11];
    public static final int GAMEBOARD_DIMENTION=10;

    public ShipBoard() {    }

    public void draw(Graphics g,int scale) {
        g.setColor(Color.BLACK);
        ((Graphics2D)g).setStroke(new BasicStroke(0.0f));

        for (int i = scale; i < scale*GAMEBOARD_DIMENTION; i+=scale) {
            g.drawLine(0, i, scale*GAMEBOARD_DIMENTION, i);
        }

        for (int i = scale; i < scale*GAMEBOARD_DIMENTION; i+=scale) {
            g.drawLine(i, 0, i, scale*GAMEBOARD_DIMENTION);
        }
    }

    //get shoot from opponent
    public int getShot(int n) {//-1 for miss, 0 for shot, id if completely destroyed
        int id = field[n];
        field[n] = -1;
        if (id == 0) {
            return -1;
        }
        ships[id].getShot();
        if (ships[id].isDestroyed()) {
            numberOfDestroyedCells++;
            return id;
        }
        numberOfDestroyedCells++;
        return 0;
    }
    //detect end of the game
    public boolean isAllShot() {
        return numberOfDestroyedCells == TOTAL_SHIP_LENGTH;
    }

    static class Miss implements Drawable{
        int n;
        public Miss(int n){
            this.n=n;
        }

        public void draw(Graphics g,int scale){
            g.setColor(Color.BLACK);
            int diameter=(int)(scale*0.3); //0.3 = size of object/size of board cell
            ((Graphics2D)g).setStroke(new BasicStroke(4.0f));
            g.drawOval(getX(n,scale)+(scale-diameter)/2,getY(n,scale)+(scale-diameter)/2,diameter,diameter);
        }
    }

    public Ship getDestroyedShip(int id){
        return ships[id];
    }

    static class Shot implements Drawable{
        int n;
        public Shot(int n){
            this.n=n;
        }
        @Override
        public void draw(Graphics g,int scale) {
            g.setColor(Color.RED);
            ((Graphics2D)g).setStroke(new BasicStroke(4.0f));
            int diameter=(int)(scale*0.5); //0.5 - size of object/size of board cell
            g.fillOval(getX(n,scale)+(scale-diameter)/2,getY(n,scale)+(scale-diameter)/2,diameter,diameter);
        }
    }

    public static int getX(int n,int scale) {
        if (n % GAMEBOARD_DIMENTION == 0) return (GAMEBOARD_DIMENTION-1)*scale;
        else return (n % GAMEBOARD_DIMENTION-1)*scale;
    }

    public static int getY(int n, int scale) {
        if (n % GAMEBOARD_DIMENTION == 0) return (n / GAMEBOARD_DIMENTION-1)*scale;
        else return n / GAMEBOARD_DIMENTION *scale;
    }

    public void autoPlaceShips() {
        int[] shipsSize = new int[]{0, 4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        boolean[] used = new boolean[101];
        int[] forRandomPickX = new int[101];
        int[] forRandomPickY = new int[101];
        for (int i = 1; i < forRandomPickX.length; i++) {
            forRandomPickX[i] = i;
            forRandomPickY[i] = i;
        }
        forRandomPickX[0] = 1; //lower bound for random pick
        forRandomPickY[0] = 1;
        Random random = new Random();
        for (int i = 1; i < shipsSize.length; i++) {
            boolean choice = random.nextBoolean();
            if (choice) autoBuildShip(shipsSize[i], i, true, forRandomPickX, used);
            else autoBuildShip(shipsSize[i], i, false, forRandomPickY, used);
        }
        System.out.println("Done!");
    }

    public int getRandom(int from, int to) {
        Random r = new Random();
        return r.nextInt(to - from) + from;
    }

    private int getValidShipStart(int size, boolean orient, int[] forRandomPick) {
        int position = getRandom(forRandomPick[0], 101);
        int start = forRandomPick[position];
        update(forRandomPick, position);

        while (!isValidStartForBuildShip(start, size, orient)) {
            position = getRandom(forRandomPick[0], 101);
            start = forRandomPick[position];
            update(forRandomPick, position);
        }
        return start;
    }

    private boolean isValidStartForBuildShip(int start, int size, boolean orient) {
        if (start < 1 || start > 100) return false;
        int distToEdge;
        if (orient) {
            distToEdge = GAMEBOARD_DIMENTION - start % GAMEBOARD_DIMENTION;
        } else {
            int row;
            if (start % GAMEBOARD_DIMENTION == 0) row = start / GAMEBOARD_DIMENTION;
            else row = start / GAMEBOARD_DIMENTION + 1;
            distToEdge = GAMEBOARD_DIMENTION - row;
        }
        return size - 1 <= distToEdge && distToEdge != GAMEBOARD_DIMENTION;
    }

    private void autoBuildShip(int size, int id, boolean orient, int[] forRandomPick, boolean[] used) {
        //get random start for ship
        int start = getValidShipStart(size, orient, forRandomPick);
        //build ship
        ships[id] = new Ship(orient, start, size);
        while (!isShipValid(ships[id], used)) {
            start = getValidShipStart(size, orient, forRandomPick);
            ships[id] = new Ship(orient, start, size);
        }
        //update usedCells
        for (int n : ships[id].getSurrounded()) {
            used[n] = true;
        }
        for (int n : ships[id].position()) field[n] = id;
    }

    public Ship[] getAllShips(){
        Ship[] allShips=new Ship[10];
        System.arraycopy(ships,1,allShips,0,10);
        return allShips;
    }

    public boolean isShipValid(Ship ship, boolean[] usedCells) {
        for (int n : ship.position()) {
            if (usedCells[n]) return false;
        }
        return true;
    }

    public boolean isCellChecked(int n) { //true if this cell is already checked
        return field[n] == -1;
    }

    public void update(int[] array, int b) {
        int a=array[0];
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
        array[0]++;
    }
}
