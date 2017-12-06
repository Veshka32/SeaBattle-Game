import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class ShipBoard implements Drawable{
    int[] field = new int[101]; //0 - unchecked cell, >0 - cell occupied with ship, -1 - checked
    private final int totalShipsLength = 20;
    int numberOfDestroyedCells = 0;
    int leftCornerX=11;
    Ship[] ships = new Ship[11];

    public ShipBoard() {    }

    public void draw(Graphics g) {


        g.setColor(Color.BLACK);
        ((Graphics2D)g).setStroke(new BasicStroke(0.0f));

        for (int i = 0; i < 501; i+=50) {
            g.drawLine(0, i, 500, i);
        }

        for (int i = 0; i < 501; i+=50) {
            g.drawLine(i, 0, i, 500);
        }
    }

    //get shoot from opponent
    public boolean getShot(int n) {
        int id = field[n];
        field[n] = -1;
        if (id == 0) {
            drawMiss(n);
            return false;
        }
        ships[id].getShot();
        if (ships[id].isDestroyed()) {
            System.out.println("This ship is DONE");
            //ships[id].draw();
        }
        drawShot(n);
        numberOfDestroyedCells++;
        return true;
    }
    //detect end of the game
    public boolean isAllShot() {
        return numberOfDestroyedCells == totalShipsLength;
    }

    public void drawMiss(int n) {
        StdDrawForSeaBattle.setPenRadius(0.01);
        StdDrawForSeaBattle.circle((getX(n) + leftCornerX + 0.5), (11 - getY(n) + 0.5), 0.2);
        StdDrawForSeaBattle.show();
    }

    public void drawShot(int n) {
        StdDrawForSeaBattle.setPenColor(Color.RED);
        StdDrawForSeaBattle.setPenRadius(0.01);
        double xx = getX(n) + 0.5 + leftCornerX;
        double yy = 11 - getY(n) + 0.5;
        StdDrawForSeaBattle.line(xx - 0.2, yy - 0.2, xx + 0.2, yy + 0.2);
        StdDrawForSeaBattle.line(xx - 0.2, yy + 0.2, xx + 0.2, yy - 0.2);
        StdDrawForSeaBattle.show();
        StdDrawForSeaBattle.setPenColor();
    }

    public int getX(int n) {
        if (n % 10 == 0) return 10;
        else return n % 10;
    }

    public int getY(int n) {
        if (n % 10 == 0) return n / 10;
        else return n / 10 + 1;
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
            distToEdge = 10 - start % 10;
        } else {
            int row;
            if (start % 10 == 0) row = start / 10;
            else row = start / 10 + 1;
            distToEdge = 10 - row;
        }
        return size - 1 <= distToEdge && distToEdge != 10;
    }

    private void autoBuildShip(int size, int id, boolean orient, int[] forRandomPick, boolean[] used) {
        //get random start for ship
        int start = getValidShipStart(size, orient, forRandomPick);
        //build ship
        ships[id] = new Ship(orient, start, size, leftCornerX);
        while (!isShipValid(ships[id], used)) {
            start = getValidShipStart(size, orient, forRandomPick);
            ships[id] = new Ship(orient, start, size, leftCornerX);
        }
        //if (leftCornerX == 0) ships[id].draw();

        //update usedCells
        for (int n : ships[id].getSurrounded()) {
            used[n] = true;
        }
        for (int n : ships[id].position()) field[n] = id; //
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

    public static void main(String[] args) {

    }


}
