package basics;

public class ClassLoaderDemo {

    public static void main(String[] args) {
        printLoader(String.class);
        printLoader(java.sql.Driver.class);
        printLoader(ClassLoaderDemo.class);
    }

    static void printLoader(Class<?> type) {
        System.out.println(type.getName());
        System.out.println(type.getClassLoader());
        System.out.println("---");
    }
}
