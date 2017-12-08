import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

/* Implements SeaBattle game, Player vs Computer. Number of ships and its length is fix. Player can place ships manually or automatically (random). Computer make strategy shots while biggest ship (of length 4) is not destroyed. Once computer shot one of the ships, it tries to end up with this ship, and only after this continue to make random (strategy effective) shots.
*/
public class SeaBattle implements PlayerAction {
    private PlayerShipBoard playerBoard;
    private ShipBoard computerBoard;
    private boolean[] cellsUsedForShipBuilding; //for building ships by player
    private int[] forRandomPick; //for computer
    private ArrayList<Integer> forPreferredPick=new ArrayList<>();
    private int i; // current index in shipsSize
    private GameWindow gameWindow;

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
            if (i+80<=GameConstant.DIMENSION) forPreferredPick.add(i+GameConstant.DIMENSION);
        }
    }
    //init
    public void play() {
        if (gameWindow == null) {
            gameWindow = new GameWindow();
            gameWindow.setHandler(this);
        }
        playerBoard = new PlayerShipBoard();
        computerBoard = new ShipBoard();
        computerBoard.autoPlaceShips();
        cellsUsedForShipBuilding = new boolean[GameConstant.CELLS_COUNT +1];
        i = 1;
        resetSmartFields();
        forRandomPick = new int[GameConstant.CELLS_COUNT +1];
        for (int j = 1; j < forRandomPick.length; j++) {
            forRandomPick[j] = j;
        }
        buildPreferredPick4();
        forRandomPick[0] = 1; //set start for random pick
        state = State.DO_NOTHING;
    }

    public void onMouseClicked(int x, int y, State s) {
        state = s;
        int xx = x / GameConstant.CELL_SIZE + 1;
        int yy = y / GameConstant.CELL_SIZE + 1;
        switch (state) {
            case BUILD_HORIZONTAL_SHIP:
                buildShip(xx, yy, GameConstant.HORIZONTAL);
                break;
            case BUILD_VERTICAL_SHIP:
                buildShip(xx, yy, GameConstant.VERTICAL);
                break;
            case MAKE_MOVE:
                processUserMove(xx, yy);
                break;
        }
    }

    public void onPassState(State s) {
        state = s;
        switch (state) {
            case NEW_GAME:
                this.play();
                break;
            case CHOOSE_ORIENT:
                if (i == GameConstant.SHIPS_SIZE.length) gameWindow.startShooting();
                else {
                    gameWindow.updateMessage("Build ship of length " + GameConstant.SHIPS_SIZE[i], "");
                    if (GameConstant.SHIPS_SIZE[i] == 1) {
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
        int coordinate = (y - 1) * GameConstant.DIMENSION + x;
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

    private void buildShip(int x, int y, boolean orientation)  {
        int coordinate = (y - 1) * GameConstant.DIMENSION + x;
        int size = GameConstant.SHIPS_SIZE[i];
        Ship ship = new Ship(orientation, coordinate, size);
        if (!playerBoard.isShipValid(ship, cellsUsedForShipBuilding) || !isValidClickForBuildShip(coordinate, size, orientation)) {
            state=State.DO_NOTHING;
            gameWindow.showException();
            if (size==1) gameWindow.updateState(State.BUILD_HORIZONTAL_SHIP);
            else gameWindow.chooseOrientation();
            return;
        }
        state = State.DO_NOTHING;
        playerBoard.placeShip(ship, i, cellsUsedForShipBuilding);
        gameWindow.drawOnLeft(ship);
        i++;
    }

    private boolean isValidClickForBuildShip(int start, int size, boolean isHorizontal) {
        if (size==1) return true;
        int dim=GameConstant.DIMENSION;
        int distToEdge;
        if (isHorizontal) {
            distToEdge = dim - start % dim;
        } else {
            int row;
            if (start % dim == 0) row = start / dim;
            else row = start / dim + 1;
            distToEdge = dim - row;
        }
        return size - 1 <= distToEdge && distToEdge != dim;
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
            int[] possible = {prev - 1, prev + 1, prev - GameConstant.DIMENSION, prev + GameConstant.DIMENSION};
            for (int m : possible) {
                if (m > 0 && m <=GameConstant.CELLS_COUNT && !playerBoard.isCellChecked(m)) {
                    forSmartPick.push(m);
                }
            }
        } else if (goodShotsSoFar >= 2) {
            forSmartPick.clear();
            int[] possible;
            if (orientSoFar) possible = new int[]{goodShotSoFarMin - 1, goodShotSoFarMax + 1};
            else possible = new int[]{goodShotSoFarMin - GameConstant.DIMENSION, goodShotSoFarMax + GameConstant.DIMENSION};
            for (int m : possible) {
                if (m > 0 && m <=GameConstant.CELLS_COUNT && !playerBoard.isCellChecked(m)) {
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
        int position = computerBoard.getRandom(forRandomPick[0], GameConstant.CELLS_COUNT +1);
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
}
