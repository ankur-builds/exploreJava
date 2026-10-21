package basics;

import java.lang.annotation.*;
import java.util.Arrays;

// Three annotations, identical except for @Retention
@Retention(RetentionPolicy.SOURCE)
@interface SourceMarker {}

@Retention(RetentionPolicy.CLASS)
@interface ClassMarker {}

@Retention(RetentionPolicy.RUNTIME)
@interface RuntimeMarker {}

// All three applied to the same class
@SourceMarker
@ClassMarker
@RuntimeMarker
class Example {}

public class RetentionDemo {
    public static void main(String[] args) {
        Class<?> type = Example.class;

        System.out.println("=== isAnnotationPresent (runtime reflection) ===");
        System.out.println("SourceMarker  : " + type.isAnnotationPresent(SourceMarker.class));
        System.out.println("ClassMarker   : " + type.isAnnotationPresent(ClassMarker.class));
        System.out.println("RuntimeMarker : " + type.isAnnotationPresent(RuntimeMarker.class));

        System.out.println();
        System.out.println("=== Everything reflection can see on Example ===");
        Annotation[] all = type.getAnnotations();
        System.out.println("count = " + all.length);
        System.out.println(Arrays.toString(all));
    }
}
