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
    private StartWindow gameWindow;

    //for computer SmartMove
    private Stack<Integer> forSmartPick = new Stack<>();
    private int goodShotsSoFar = 0;
    private int goodShotSoFarMin = 1000;
    private int goodShotSoFarMax = 0;
    private boolean orientSoFar;
    private int prev;

    //current game state
    private State state;

    public SeaBattle() {
        if (gameWindow != null) gameWindow.dispose();
        System.out.println("New game is created");
        playerBoard = new PlayerShipBoard();
        //playerBoard.drawBoard();
        computerBoard = new ShipBoard();
        //computerBoard.drawBoard();
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
        gameWindow = new StartWindow();
        gameWindow.setHandler(this);
        state = State.CHOOSE_MODE;
    }

    public void passCoordinates(int x, int y, State s) {
        state = s;
        int xx;
        int yy;
        switch (state) {
            case BUILD_SHIP:
                break;
            case MAKE_MOVE:
                xx = x / 50 + 1;
                yy = y / 50 + 1;
                processUserMove(xx, yy);
                break;
        }
    }

    public void passAction(State s) {
        state = s;
        switch (state) {
            case NEW_GAME:this.play();
                break;
            case CHOOSE_ORIENT:
                break;
            case AUTO_BUILD_SHIPS: {
                playerBoard.autoPlaceShips();
                for (Ship ship : playerBoard.getAllShips()) {
                    gameWindow.drawOnLeft(ship);
                }
                state = State.MAKE_MOVE;
            }
            break;
        }
    }

    private void processUserMove(int x, int y) {
        state = State.DO_NOTHING;
        int coordinate = (y - 1) * 10 + x;
        if (computerBoard.isCellChecked(coordinate)) {
            gameWindow.updateMessage("You already shot this cell. Click another cell");
            return;
        }
        int shot = computerBoard.getShot(coordinate);
        if (shot<0) {
            gameWindow.drawOnRight(new ShipBoard.Miss(coordinate));
            gameWindow.updateMessage("You miss!");
            computerMove();
            if (playerBoard.isAllShot()) {
                gameWindow.updateMessage("Computer wins!");
                gameWindow.updateState(State.END);
                state = State.END;
            }
            return;
        }

        if (shot>0){ gameWindow.drawOnRight(computerBoard.getDestroyedShip(shot)); gameWindow.updateMessage("This ship is DONE");}
        gameWindow.updateNumberOfDestroyed();
        gameWindow.drawOnRight(new ShipBoard.Shot(coordinate));
        gameWindow.updateMessage("Nice shot! Player, make next shot! Click cell");
        gameWindow.updateState(State.MAKE_MOVE);
        if (computerBoard.isAllShot()) {
            gameWindow.updateMessage("Player wins!");
            gameWindow.updateState(State.END);
            state = State.END;}
    }

    public void MouseClicked(double x, double y) {

        int coordinate=0;
        if (state == State.BUILD_SHIP) {
            int size = shipsSize[i];
            if (!isLeftFieldClicked) return; // player should build ship only on left field
            if (!isValidClickForBuildShip(coordinate, size, orient)) {
                System.out.println("Invalid cell");
                return;
            }
            Ship ship = new Ship(orient, coordinate, size);
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
        System.out.println("New game is created");
        playerBoard = new PlayerShipBoard();
        computerBoard = new ShipBoard();
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
        gameWindow = new StartWindow();
        gameWindow.setHandler(this);
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
        gameWindow.updateState(State.DO_NOTHING);
        int shot;
        if (goodShotsSoFar == 0) shot = computerMakesRandomShot();
        else shot = computerMakesSmartShot();
        while (shot >= 0) {
            if (shot == 0) {
                gameWindow.updateMessage("Oh! You are shot!");
                shot = computerMakesSmartShot();
            } else {
                gameWindow.updateMessage("One of your ships is completely destroyed!");
                if (playerBoard.isAllShot()) {
                    return;
                }
                Ship destroyed = playerBoard.getDestroyedShip(shot);
                for (int n : destroyed.getSurrounded()) {
                    playerBoard.playerGetPseudoShot(n);
                }
                resetSmartFields();
                shot = computerMakesRandomShot();
            }
        }
        gameWindow.updateState(State.MAKE_MOVE);
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
        int shot = playerBoard.getShot(n);
        if (shot >= 0) handleGoodShot(n);
        else gameWindow.drawOnLeft(new ShipBoard.Miss(n));
        gameWindow.updateMessage("Computer shoots to cell " + n);
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
        int shot = playerBoard.getShot(n);
        gameWindow.updateMessage("Computer shoots to cell " + n);
        if (shot >= 0) handleGoodShot(n);
        else gameWindow.drawOnLeft(new ShipBoard.Miss(n));
        return shot;
    }

    private void handleGoodShot(int n) {
        gameWindow.drawOnLeft(new ShipBoard.Shot(n));
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
    }
}
