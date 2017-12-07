import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdDraw;


import java.awt.*;
import java.util.Arrays;
import java.util.Scanner;
/*Create a ship places on ShipBoard for SeaBattle game.
 */

public class Ship implements Drawable{
    private final int size;
    private final int start;
    private final boolean orientation; //true - horizontal, false - vertical
    private int damaged = 0; //number of damage shiparts so far; <=size

    public Ship(boolean orientation, int start, int size) {
        this.orientation = orientation;
        this.start = start;
        this.size = size;
    }

    //return cells ship occupies
    public int[] position() {
        if (size == 1) return new int[]{start};
        int[] location = new int[size];
        int j;
        if (orientation) j = 1;
        else j = 10;
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
        if (orientation) {
            if (start % 10 == 1) j = 1;
            else if ((start + size - 1) % 10 == 0) k = 0;
            if (start < 11) topLeft = false;
            else if (start > 90) bottomRight = false;
            for (int i = -1 + j; i < size + k; i++) {
                if (topLeft) s.enqueue(start + i - 10);
                if (bottomRight) s.enqueue(start + i + 10);
                s.enqueue(start + i);
            }
        } else {
            if (start < 11) j = 1;
            else if (start + (size - 1) * 10 > 90) k = 0;
            if (start % 10 == 1) topLeft = false;
            else if (start % 10 == 0) bottomRight = false;
            for (int i = -10 + j * 10; i < (size + k) * 10; i += 10) {
                if (topLeft) s.enqueue(start + i - 1);
                if (bottomRight) s.enqueue(start + i + 1);
                s.enqueue(start + i);
            }
        }
        return s;
    }

    public boolean isDestroyed() {
        return damaged == size;
    }

    public void getShot() {
        damaged++;
    }

    public void draw(Graphics g) {
        ((Graphics2D)g).setStroke(new BasicStroke(5.0f));
        g.setColor(Color.BLACK);
        int xLeft;
        if (start % 10 == 0) xLeft = (10-1)*50;
        else xLeft = (start % 10-1)*50;
        int yTop;
        if (start % 10 == 0) yTop = (start / 10-1)*50;
        else yTop = start / 10 *50;

        int xRight;
        int yBottom;
        if (!orientation) {
            xRight = xLeft + 50;
            yBottom = yTop - size*50;
        } else {
            xRight = xLeft + size*50;
            yBottom = yTop - 1*50;
        }

        int width=(xRight-xLeft);
        int height=(yTop-yBottom);
        g.drawRect(xLeft,yTop,width,height);

    }

    public static void main(String[] args) {

    }
}



