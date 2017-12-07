import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

/* Implements SeaBattle game, Player vs Computer. Number of ships and its length is fix. Player can place ships manually or automatically (random). Computer make strategy shots while biggest ship (of length 4) is not destroyed. Once computer shot one of the ships, it tries to end up with this ship, and only after this continue to make random (strategy effective) shots.
*/
public class SeaBattle implements PlayerAction {
    public static final int GAMEBOARD_DIMENTION=10;
    public static final int NUMBER_OF_CELLS_IN_GAMEBOARD=100;
    private final int[] shipsSize = {0, 4, 3, 3, 2, 2, 2, 1, 1, 1, 1}; //ships length
    private PlayerShipBoard playerBoard;
    private ShipBoard computerBoard;
    private boolean[] usedCells; //for Player
    private int[] forRandomPick; //for computer
    private ArrayList<Integer> forPreferredPick=new ArrayList<>();
    private int i; // current index in shipsSize 
    private StartWindow gameWindow;

    //for computer SmartMove
    private ArrayDeque<Integer> forSmartPick = new ArrayDeque<>();
    private int goodShotsSoFar = 0;
    private int goodShotSoFarMin = 1000;
    private int goodShotSoFarMax = 0;
    private boolean orientSoFar;
    private int prev;
    private State state; //current game state

    public SeaBattle() {
        play();
    }

    private void buildPreferredPick4(){
        int[] temp={1,5,9,12,16,20,23,27,34,38};
        for (int i:temp){
            forPreferredPick.add(i);
            forPreferredPick.add(i+40);
            if (i+80<=NUMBER_OF_CELLS_IN_GAMEBOARD) forPreferredPick.add(i+80);
        }
    }

    //init
    public void play() {
        if (gameWindow == null) {
            gameWindow = new StartWindow();
            gameWindow.setHandler(this);
        }
        playerBoard = new PlayerShipBoard();
        computerBoard = new ShipBoard();
        computerBoard.autoPlaceShips();
        usedCells = new boolean[NUMBER_OF_CELLS_IN_GAMEBOARD+1];
        i = 1;
        resetSmartFields();
        forRandomPick = new int[NUMBER_OF_CELLS_IN_GAMEBOARD+1];
        for (int j = 1; j < forRandomPick.length; j++) {
            forRandomPick[j] = j;
        }
        buildPreferredPick4();
        forRandomPick[0] = 1; //set start for random pick
        state = State.DO_NOTHING;
    }

    public void passCoordinates(int x, int y, State s,int scale) {
        state = s;
        int xx = x / scale + 1;
        int yy = y / scale + 1;
        switch (state) {
            case BUILD_HORIZONTAL_SHIP:
                buildShip(xx, yy, true);
                break;
            case BUILD_VERTICAL_SHIP:
                buildShip(xx, yy, false);
                break;
            case MAKE_MOVE:
                processUserMove(xx, yy);
                break;
        }
    }

    public void passState(State s) {
        state = s;
        switch (state) {
            case NEW_GAME:
                this.play();
                break;
            case CHOOSE_ORIENT:
                if (i == shipsSize.length) gameWindow.startShooting();
                else {
                    gameWindow.updateMessage("Build ship of length " + shipsSize[i], "");
                    if (shipsSize[i] == 1) {
                        gameWindow.updateMessage("", " ");
                        gameWindow.updateState(State.BUILD_HORIZONTAL_SHIP);
                    } else gameWindow.chooseOrientation();
                }
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
        int coordinate = (y - 1) * GAMEBOARD_DIMENTION + x;
        if (computerBoard.isCellChecked(coordinate)) {
            gameWindow.updateMessage("You already shot this cell. Click another cell", "");
            return;
        }
        int shot = computerBoard.getShot(coordinate);
        if (shot < 0) {
            gameWindow.drawOnRight(new ShipBoard.Miss(coordinate));
            gameWindow.updateMessage("", "You miss!");
            computerMove();
            if (playerBoard.isAllShot()) {
                gameWindow.updateMessage("GAME OVER", "Computer wins!");
                gameWindow.updateState(State.END);
                state = State.END;
            }
            return;
        }

        if (shot > 0) {
            gameWindow.drawOnRight(computerBoard.getDestroyedShip(shot));
            gameWindow.updateMessage("", "This ship is DONE");
        }
        gameWindow.drawOnRight(new ShipBoard.Shot(coordinate));
        gameWindow.updateMessage("Nice shot!", "Player, make next shot");
        gameWindow.updateState(State.MAKE_MOVE);
        if (computerBoard.isAllShot()) {
            gameWindow.updateMessage("GAME OVER", "Player wins!");
            gameWindow.updateState(State.END);
            state = State.END;
        }
    }

    public void buildShip(int x, int y, boolean orientation) {
        int coordinate = (y - 1) * GAMEBOARD_DIMENTION + x;
        int size = shipsSize[i];
        Ship ship = new Ship(orientation, coordinate, size);
        if (!playerBoard.isShipValid(ship, usedCells) || !isValidClickForBuildShip(coordinate, size, orientation)) {
            gameWindow.updateMessage("You may not place ship here", "");
            gameWindow.chooseOrientation();
            return;
        }
        state = State.DO_NOTHING;
        playerBoard.placeShip(ship, i, usedCells);
        gameWindow.drawOnLeft(ship);
        i++;
    }

    private boolean isValidClickForBuildShip(int start, int size, boolean orient) {
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

    private void computerMove() {
        gameWindow.updateState(State.DO_NOTHING);
        int shot;
        if (goodShotsSoFar == 0) shot = computerMakesRandomShot();
        else shot = computerMakesSmartShot();
        while (shot >= 0) {
            if (shot == 0) {
                gameWindow.updateMessage("", "Oh! You are shot!");
                shot = computerMakesSmartShot();
            } else {
                gameWindow.updateMessage("", "One of your ships is completely destroyed!");
                if (playerBoard.isAllShot()) {
                    return;
                }
                if (shot==1) forPreferredPick.clear();
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
            int[] possible = {prev - 1, prev + 1, prev - GAMEBOARD_DIMENTION, prev + GAMEBOARD_DIMENTION};
            for (int m : possible) {
                if (m > 0 && m <=NUMBER_OF_CELLS_IN_GAMEBOARD && !playerBoard.isCellChecked(m)) {
                    forSmartPick.push(m);
                }
            }
        } else if (goodShotsSoFar >= 2) {
            forSmartPick.clear();
            int[] possible;
            if (orientSoFar) possible = new int[]{goodShotSoFarMin - 1, goodShotSoFarMax + 1};
            else possible = new int[]{goodShotSoFarMin - GAMEBOARD_DIMENTION, goodShotSoFarMax + GAMEBOARD_DIMENTION};
            for (int m : possible) {
                if (m > 0 && m <=NUMBER_OF_CELLS_IN_GAMEBOARD && !playerBoard.isCellChecked(m)) {
                    forSmartPick.push(m);
                }
            }
        }
        int n = forSmartPick.pop();
        int shot = playerBoard.getShot(n);
        if (shot >= 0) handleGoodShot(n);
        else gameWindow.drawOnLeft(new ShipBoard.Miss(n));
        gameWindow.updateMessage("Computer shoots to cell " + n, "");
        return shot;
    }

    private void resetSmartFields() {
        goodShotsSoFar = 0;
        goodShotSoFarMin = 1000;
        goodShotSoFarMax = 0;
        forSmartPick.clear();
        prev = -1;
    }

    private int getValidRandom(int[] forRandomPick) {
        int position = computerBoard.getRandom(forRandomPick[0], NUMBER_OF_CELLS_IN_GAMEBOARD+1);
        int validRandom = forRandomPick[position];
        computerBoard.update(forRandomPick, position);
        return validRandom;
    }

    private int getPreferred(){
        Random r=new Random();
        int random=r.nextInt(forPreferredPick.size());
        int n=forPreferredPick.get(random);
        forPreferredPick.remove(random);
        return n;
    }

    private int computerMakesRandomShot() {
        int n;
        if (forPreferredPick.size()>0) n=getPreferred();
        else n = getValidRandom(forRandomPick);
        // catch pseudoShots and shots from smartShot
        while (playerBoard.isCellChecked(n)) {
            if (forPreferredPick.size()>0) n=getPreferred();
            else n = getValidRandom(forRandomPick);
        }
        int shot = playerBoard.getShot(n);
        gameWindow.updateMessage("Computer shoots to cell " + n, "");
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
        SeaBattle newGame = new SeaBattle();
    }
}
