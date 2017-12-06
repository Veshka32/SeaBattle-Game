

public class ShipTest {
    public void ShipIsDestroyed(){
        //arrange
        Ship ship=new Ship(true,1,3);

        //act
        for (int i = 0; i <3 ; i++) {
            ship.getShot();
        }

        //assert
        assert (ship.isDestroyed()); //add -ea to VM options in EditConfigurations
    }

    public static void main(String[] args) {
        ShipTest test=new ShipTest();
        test.ShipIsDestroyed();
    }
}
