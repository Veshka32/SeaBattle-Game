import java.util.Stack;

/* Implements SeaBattle game, Player vs Computer. Number of ships and its length is fix. Player can place ships manually or automatically (random)
 */
public class SeaBattle implements PlayerAction {
    private final int[] shipsSize = {0, 4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    private PlayerShipBoard playerBoard;
    private ShipBoard computerBoard;
    private Boolean isLeftFieldClicked;
    private boolean orient;
    private boolean[] usedCells; //for Player
    private int[] forRandomPick; //for computer
    private int i; // current index in shipsSize

    //for computer SmartMove
    private Stack<Integer> forSmartPick = new Stack<>();
    private int goodShotsSoFar = 0;
    private int goodShotSoFarMin = 1000;
    private int goodShotSoFarMax = 0;
    private boolean orientSoFar;
    private int prev;

    //current game state
    private State state;
    private enum State{DO_NOTHING,BUILD_SHIP,MAKE_MOVE,CHOOSE_MODE,CHOOSE_ORIENT,END}

    public SeaBattle() {
        StdDrawForSeaBattle.enableDoubleBuffering();
        StdDrawForSeaBattle.setCanvasSize(1000, 500);
        StdDrawForSeaBattle.setXscale(0.0, 23.00);
        StdDrawForSeaBattle.setYscale(0.0, 12);
    }

    @Override
    public void MouseClicked(double x, double y) {
        if (state == State.END) return;
        int coordinate;
        int x1 = (int) x;
        int y1 = (int) y;
        if (x1 < 11 && x1 > 0 && y1 > 0 && y1 < 11) isLeftFieldClicked = true;
        else if (x1 > 11 && x1 < 22 && y1 > 0 && y1 < 11) {
            isLeftFieldClicked = false;
            x1 -= 11;
        } else isLeftFieldClicked = null;

        if (isLeftFieldClicked != null) {
            y1 = 10 - y1 + 1;
            coordinate = 10 * (y1 - 1) + x1;
        } else return; // if user click outside the fields - do nothing

        if (state == State.BUILD_SHIP) {
            int size = shipsSize[i];
            if (!isLeftFieldClicked) return; // player should build ship only on left field
            if (!isValidClickForBuildShip(coordinate, size, orient)) {
                System.out.println("Invalid cell");
                return;
            }
            Ship ship = new Ship(orient, coordinate, size, 0);
            if (!playerBoard.isShipValid(ship, usedCells)) {
                System.out.println("You may not place ship here"); //if ship invalid, still in state 1;
                return;
            }
            state = State.DO_NOTHING;
            playerBoard.placeShip(ship, i, usedCells);
            System.out.println("Ship is built!");
            i++;
            if (i == shipsSize.length) { // end of building ships
                state = State.DO_NOTHING;
                System.out.println("Done!");
                System.out.println("Player, make shot! Click cell");
                state = State.MAKE_MOVE;
                return;
            }
            buildShip();
        } else if (state == State.MAKE_MOVE) {
            if (isLeftFieldClicked) return; // player should shot only to right field
            if (computerBoard.isCellChecked(coordinate)) {
                System.out.println("You already shot this cell. Click another cell");
                return;
            }
            boolean shot = computerBoard.getShot(coordinate);
            if (!shot) {
                System.out.println("You miss!");
                state = State.DO_NOTHING;
                computerMove();
                if (playerBoard.isAllShot()) {
                    System.out.println("Computer wins!");
                    state = State.END;
                    return;
                }
                System.out.println("Player, make shot! Click cell");
                state = State.MAKE_MOVE;
                return;
            }
            if (computerBoard.isAllShot()) {
                System.out.println("Player wins!");
                state = State.END;
                return;
            }
            System.out.println("Nice shot!"); // player continue to make moves
            System.out.println("Player, make shot! Click cell");
        }
    }

    @Override
    public void KeyTyped(char key) {
        if (state == State.CHOOSE_MODE) {
            if (key == 'a' || key=='A') {
                playerBoard.autoPlaceShips();
                System.out.println("Player, make shot! Click cell");
                state = State.MAKE_MOVE;
            } else if (key == 'm' || key=='M') {
                buildShip();
            } else System.out.println("Wrong input. Type m to place ships manually or a to place ships automatically");
        } else if (state == State.CHOOSE_ORIENT) {
            if (key == 'v'|| key=='V') {
                orient = false;
                System.out.println("Click ship start");
                state = State.BUILD_SHIP;
            } else if (key == 'h' || key=='H') {
                orient = true;
                System.out.println("Click ship start");
                state = State.BUILD_SHIP;
            } else System.out.println("Wrong input. Choose orientation of ship. Type h for horizontal, v for vertical");
        }
    }

    private void buildShip() {
        int size = shipsSize[i];
        System.out.println("Build ship of length " + size);
        if (size > 1) {
            System.out.println("Choose orientation of ship. Type h for horizontal, v for vertical");
            state = State.CHOOSE_ORIENT;
        } else {
            System.out.println("Click ship start");
            state = State.BUILD_SHIP;
        }
    }

    public void play() {
        playerBoard = new PlayerShipBoard();
        playerBoard.drawBoard();
        computerBoard = new ShipBoard();
        computerBoard.drawBoard();
        computerBoard.autoPlaceShips();
        isLeftFieldClicked = null;
        orient = true;
        usedCells = new boolean[101];
        i = 1;
        state = State.DO_NOTHING;
        resetSmartFields();
        forRandomPick = new int[101];
        for (int j = 1; j < forRandomPick.length; j++) {
            forRandomPick[j] = j;
        }
        forRandomPick[0] = 1; //start for random pick
        StdDrawForSeaBattle.addPlayerActions(this);
        System.out.println("Type m to place ships manually or a to place ships automatically");
        state = State.CHOOSE_MODE;
    }

    private boolean isValidClickForBuildShip(int start, int size, boolean orient) {
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

    private void computerMove() {
        state = State.DO_NOTHING;
        int shot;
        if (goodShotsSoFar == 0) shot = computerMakesRandomShot();
        else shot = computerMakesSmartShot();
        while (shot >= 0) {
            if (shot == 0) {
                System.out.println("Oh! You are shot!");
                shot = computerMakesSmartShot();
            } else {
                System.out.println("One of your ships is completely destroyed!");
                if (playerBoard.isAllShot()) {
                    return;
                }
                Ship destroyed = new Ship(orientSoFar, goodShotSoFarMin, goodShotSoFarMax); //constructor for start - end
                for (int n : destroyed.getSurrounded()) {
                    playerBoard.playerGetPseudoShot(n);
                }
                resetSmartFields();
                shot = computerMakesRandomShot();
            }
        }
        state = State.MAKE_MOVE;
    }

    private int computerMakesSmartShot() {
        if (goodShotsSoFar == 1 && forSmartPick.isEmpty()) {
            int[] possible = {prev - 1, prev + 1, prev - 10, prev + 10};
            for (int m : possible) {
                if (m > 0 && m < 101 && !playerBoard.isCellChecked(m)) {
                    forSmartPick.push(m);
                }
            }
        } else if (goodShotsSoFar >= 2) {
            forSmartPick = new Stack<>();
            int[] possible;
            if (orientSoFar) possible = new int[]{goodShotSoFarMin - 1, goodShotSoFarMax + 1};
            else possible = new int[]{goodShotSoFarMin - 10, goodShotSoFarMax + 10};
            for (int m : possible) {
                if (m > 0 && m < 101 && !playerBoard.isCellChecked(m)) {
                    forSmartPick.push(m);
                }
            }
        }
        int n = forSmartPick.pop();
        int shot = playerBoard.playerGetShot(n);
        if (shot >= 0) handleGoodShot(n);
        System.out.println("Computer shoots to cell " + n);
        return shot;
    }

    private void resetSmartFields() {
        goodShotsSoFar = 0;
        goodShotSoFarMin = 1000;
        goodShotSoFarMax = 0;
        forSmartPick = new Stack<>();
        prev = -1;
    }


    private int getValidRandom(int[] forRandomPick) {
        int position = computerBoard.getRandom(forRandomPick[0], 101);
        int validRandom = forRandomPick[position];
        computerBoard.update(forRandomPick, position);
        return validRandom;
    }

    private int computerMakesRandomShot() {
        int n = getValidRandom(forRandomPick);
        while (playerBoard.isCellChecked(n)) {
            n = getValidRandom(forRandomPick); // catch pseudoshots and shots from smartshot
        }
        int shot = playerBoard.playerGetShot(n);
        System.out.println("Computer shoots to cell " + n);
        if (shot >= 0) handleGoodShot(n);
        return shot;
    }

    private void handleGoodShot(int n) {
        prev = n;
        goodShotsSoFar++;
        goodShotSoFarMin = Math.min(goodShotSoFarMin, n);
        goodShotSoFarMax = Math.max(goodShotSoFarMax, n);
        if (goodShotsSoFar == 2) {
            orientSoFar = goodShotSoFarMax - goodShotSoFarMin == 1;
        }
    }

    public static void main(String[] args) {
        SeaBattle test = new SeaBattle();
        test.play();
        test.play();
    }
}
