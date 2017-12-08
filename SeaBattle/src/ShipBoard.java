import java.awt.*;
import java.util.Random;
import java.util.stream.IntStream;

public class ShipBoard implements Drawable{
    int[] field = new int[GameConstant.CELLS_COUNT+1]; //0 - unchecked cell, >0 - cell occupied with ship, -1 - checked
    private final int TOTAL_SHIP_LENGTH = IntStream.of(GameConstant.SHIPS_SIZE).sum();
    private int damagedCellsCount = 0;
    Ship[] ships = new Ship[GameConstant.SHIPS_COUNT+1];

    public ShipBoard() {    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        ((Graphics2D)g).setStroke(new BasicStroke(0.0f));

        for (int i = GameConstant.CELL_SIZE; i < GameConstant.CELL_SIZE *GameConstant.DIMENSION; i+=GameConstant.CELL_SIZE) {
            g.drawLine(0, i, GameConstant.CELL_SIZE *GameConstant.DIMENSION, i);
        }

        for (int i = GameConstant.CELL_SIZE; i < GameConstant.CELL_SIZE *GameConstant.DIMENSION; i+=GameConstant.CELL_SIZE) {
            g.drawLine(i, 0, i, GameConstant.CELL_SIZE *GameConstant.DIMENSION);
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
            damagedCellsCount++;
            return id;
        }
        damagedCellsCount++;
        return 0;
    }
    //detect end of the game
    public boolean isAllShot() {
        return damagedCellsCount == TOTAL_SHIP_LENGTH;
    }

    static class Miss implements Drawable{
        int n;
        public Miss(int n){
            this.n=n;
        }

        public void draw(Graphics g){
            g.setColor(Color.BLACK);
            int diameter=(int)(GameConstant.CELL_SIZE *0.3); //0.3 = size of object/size of board cell
            ((Graphics2D)g).setStroke(new BasicStroke(4.0f));
            g.drawOval(getX(n)+(GameConstant.CELL_SIZE -diameter)/2,getY(n)+(GameConstant.CELL_SIZE -diameter)/2,diameter,diameter);
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
        public void draw(Graphics g) {
            g.setColor(Color.RED);
            ((Graphics2D)g).setStroke(new BasicStroke(4.0f));
            int diameter=(int)(GameConstant.CELL_SIZE *0.5); //0.5 - size of object/size of board cell
            g.fillOval(getX(n)+(GameConstant.CELL_SIZE -diameter)/2,getY(n)+(GameConstant.CELL_SIZE -diameter)/2,diameter,diameter);
        }
    }

    public static int getX(int n) {
        if (n % GameConstant.DIMENSION == 0) return (GameConstant.DIMENSION -1)*GameConstant.CELL_SIZE;
        else return (n % GameConstant.DIMENSION -1)*GameConstant.CELL_SIZE;
    }

    public static int getY(int n) {
        if (n % GameConstant.DIMENSION == 0) return (n / GameConstant.DIMENSION -1)*GameConstant.CELL_SIZE;
        else return n / GameConstant.DIMENSION *GameConstant.CELL_SIZE;
    }

    public void autoPlaceShips() {
        boolean[] used = new boolean[GameConstant.CELLS_COUNT+1];
        int[] forRandomPickX = new int[GameConstant.CELLS_COUNT+1];
        int[] forRandomPickY = new int[GameConstant.CELLS_COUNT+1];
        for (int i = 1; i < forRandomPickX.length; i++) {
            forRandomPickX[i] = i;
            forRandomPickY[i] = i;
        }
        forRandomPickX[0] = 1; //lower bound for random pick
        forRandomPickY[0] = 1;
        Random random = new Random();
        for (int i = 1; i < GameConstant.SHIPS_SIZE.length; i++) {
            boolean choice = random.nextBoolean();
            if (choice) autoBuildShip(GameConstant.SHIPS_SIZE[i], i, GameConstant.HORIZONTAL, forRandomPickX, used);
            else autoBuildShip(GameConstant.SHIPS_SIZE[i], i, GameConstant.VERTICAL, forRandomPickY, used);
        }
        System.out.println("Done!");
    }

    public int getRandom(int from, int to) {
        Random r = new Random();
        return r.nextInt(to - from) + from;
    }

    private int getValidShipStart(int size, boolean orient, int[] forRandomPick) {
        int position = getRandom(forRandomPick[0], GameConstant.CELLS_COUNT+1);
        int start = forRandomPick[position];
        update(forRandomPick, position);

        while (!isValidStartForBuildShip(start, size, orient)) {
            position = getRandom(forRandomPick[0], GameConstant.CELLS_COUNT+1);
            start = forRandomPick[position];
            update(forRandomPick, position);
        }
        return start;
    }

    private boolean isValidStartForBuildShip(int start, int size, boolean orient) {
        if (start < 1 || start > GameConstant.CELLS_COUNT+1) return false;
        int distToEdge;
        if (orient) {
            distToEdge = GameConstant.DIMENSION - start % GameConstant.DIMENSION;
        } else {
            int row;
            if (start % GameConstant.DIMENSION == 0) row = start / GameConstant.DIMENSION;
            else row = start / GameConstant.DIMENSION + 1;
            distToEdge = GameConstant.DIMENSION - row;
        }
        return size - 1 <= distToEdge && distToEdge != GameConstant.DIMENSION;
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
        Ship[] allShips=new Ship[ships.length-1];
        System.arraycopy(ships,1,allShips,0,ships.length-1);
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
