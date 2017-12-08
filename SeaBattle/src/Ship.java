import edu.princeton.cs.algs4.Queue;


import java.awt.*;
/*Create a ship places on ShipBoard for SeaBattle game.
 */

public class Ship implements Drawable{
    private final int size;
    private final int start;
    private final boolean isHorizontal;
    private int damagedCellCount = 0; //number of damage cells in ship so far;

    public Ship(boolean isHorizontal, int start, int size) {
        this.isHorizontal = isHorizontal;
        this.start = start;
        this.size = size;
    }

    //return cells ship occupies
    public int[] position() {
        if (size == 1) return new int[]{start};
        int[] location = new int[size];
        int j;
        if (isHorizontal) j = 1;
        else j = GameConstant.DIMENSION;
        for (int i = 0; i < location.length; i++) {
            location[i] = start + j * i;
        }
        return location;
    }

    //return cells  ship occupies and surrounded cells
    public Queue<Integer> getSurrounded() {
        Queue<Integer> s = new Queue<>();
        int j = 0;
        int k = 1;
        boolean topLeft = true;
        boolean bottomRight = true;
        if (isHorizontal) {
            if (start % GameConstant.DIMENSION == 1) j = 1;
            else if ((start + size - 1) % GameConstant.DIMENSION == 0) k = 0;
            if (start <=GameConstant.DIMENSION) topLeft = false;
            else if (start > GameConstant.CELLS_COUNT-GameConstant.DIMENSION) bottomRight = false;
            for (int i = -1 + j; i < size + k; i++) {
                if (topLeft) s.enqueue(start + i - GameConstant.DIMENSION);
                if (bottomRight) s.enqueue(start + i + GameConstant.DIMENSION);
                s.enqueue(start + i);
            }
        } else {
            if (start <=GameConstant.DIMENSION) j = 1;
            else if (start + (size - 1) * GameConstant.DIMENSION > GameConstant.CELLS_COUNT-GameConstant.DIMENSION) k = 0;
            if (start % GameConstant.DIMENSION == 1) topLeft = false;
            else if (start % GameConstant.DIMENSION == 0) bottomRight = false;
            for (int i = -GameConstant.DIMENSION + j * GameConstant.DIMENSION; i < (size + k) *GameConstant.DIMENSION; i += GameConstant.DIMENSION) {
                if (topLeft) s.enqueue(start + i - 1);
                if (bottomRight) s.enqueue(start + i + 1);
                s.enqueue(start + i);
            }
        }
        return s;
    }

    public boolean isDestroyed() {
        return damagedCellCount == size;
    }

    public void getShot() {
        damagedCellCount++;
    }

    public void draw(Graphics g) {
        int scale=GameConstant.CELL_SIZE;
        ((Graphics2D)g).setStroke(new BasicStroke(5.0f));
        g.setColor(Color.BLACK);
        int xLeft;
        if (start % GameConstant.DIMENSION == 0) xLeft = (GameConstant.DIMENSION -1)*scale;
        else xLeft = (start % GameConstant.DIMENSION -1)*scale;
        int yTop;
        if (start % GameConstant.DIMENSION == 0) yTop = (start / GameConstant.DIMENSION -1)*scale;
        else yTop = start / GameConstant.DIMENSION *scale;

        int xRight;
        int yBottom;
        if (isHorizontal){
            xRight = xLeft + size*scale;
            yBottom = yTop - scale;
        }
        else  {
            xRight = xLeft + scale;
            yBottom = yTop - size*scale;
        }
        int width=(xRight-xLeft);
        int height=(yTop-yBottom);
        g.drawRect(xLeft,yTop,width,height);
    }
}



