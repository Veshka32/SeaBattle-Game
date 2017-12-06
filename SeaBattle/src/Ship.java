import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdDraw;


import java.awt.*;
import java.util.Arrays;
import java.util.Scanner;
/*Create a ship places on ShipBoard for SeaBattle game.
 */

public class Ship {
    private final int size;
    private final int start;
    private final int leftX; // 0 - ship would be drawn on the Player board, 11 - on the opponent (computer) board
    private final boolean orientation; //true - horizontal, false - vertical
    private int damaged = 0; //number of damage shiparts so far; <=size

    public Ship(boolean orientation, int start, int size, int leftX) {
        this.orientation = orientation;
        this.leftX = leftX;
        this.start = start;
        this.size = size;
    }

    public Ship(boolean orientation,int start, int end){
        this.orientation=orientation;
        this.leftX=0;
        this.start=start;
        if (orientation) size = end - start + 1;
        else size = (end - start) / 10 + 1;
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

    public void draw() {
        StdDrawForSeaBattle.setPenRadius(0.01);
        int xLeft;
        if (start % 10 == 0) xLeft = 10 + leftX;
        else xLeft = start % 10 + leftX;
        int yTop;
        if (start % 10 == 0) yTop = 12 - start / 10;
        else yTop = 12 - (start / 10 + 1);
        int xRight;
        int yBottom;
        if (!orientation) {
            xRight = xLeft + 1;
            yBottom = yTop - size;
        } else {
            xRight = xLeft + size;
            yBottom = yTop - 1;
        }
        double centerX=(xLeft+xRight)/2.0;
        double centerY=(yBottom+yTop)/2.0;
        double halfWidth=(xRight-xLeft)/2.0;
        double haldHeight=(yTop-yBottom)/2.0;

        StdDrawForSeaBattle.rectangle(centerX,centerY,halfWidth,haldHeight);
//        StdDrawForSeaBattle.line(xLeft, ybottom, xright, ybottom);
//        StdDrawForSeaBattle.line(xLeft, ytop, xright, ytop);
//        StdDrawForSeaBattle.line(xLeft, ybottom, xLeft, ytop);
//        StdDrawForSeaBattle.line(xright, ybottom, xright, ytop);
        StdDrawForSeaBattle.show();
    }

    public void paint(Graphics g,int x,int y){
        Graphics2D gg= (Graphics2D) g;
        gg.setColor(Color.RED);
        gg.drawRect(x,y,50,50);
    }

    public static void main(String[] args) {

    }
}



