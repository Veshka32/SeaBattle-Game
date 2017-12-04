public class EqualsTest {
    public static void main(String[] args) {
        String x = null;
        String y = null;
        String test = "Test";
        try {
            x.equals("Test");
        } catch (NullPointerException e) {
            System.out.println("null object cannot equals smth");
        }

        try {
            test.equals(null);
            System.out.println("Object equals null "+test.equals(null));
        } catch (NullPointerException a) {
            System.out.println("object cannot equals null");
        }


        try {
            y.equals(x);
        } catch (NullPointerException b) {
            System.out.println("null object cannot equals null object");
        }

        try {
            x.equals(null);
        } catch (NullPointerException c) {
            System.out.println("null object cannot equals null");
        }


        try{
           if (x==null) System.out.println("null object == null"); ;

        } catch (NullPointerException c) {
            System.out.println("null object cannot == null object");
        }

        try{
            if (x==y) System.out.println("null object == null object"); ;

        } catch (NullPointerException c) {
            System.out.println("null object cannot == null object");
        }

    }
}

