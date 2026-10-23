package basics;

class OrderConfiguration {

    static {
        // The static block runs when the static method triggers initialization.
        System.out.println("OrderConfiguration initialized");
    }

    static void configure() {
        System.out.println("Configuration applied");
    }
}

public class ClassLifecycleDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("Application started");

        // OrderConfiguration order = new OrderConfiguration(); // Force class loading and initialization

        ClassLoader loader = ClassLifecycleDemo.class.getClassLoader(); // Loading and initialization are different lifecycle stages.

        Class.forName("OrderConfiguration", false, loader); // requests loading without initialization

        System.out.println("Class loaded without requested initialization");

        OrderConfiguration.configure();
    }
}
