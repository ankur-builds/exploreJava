package basics;

import java.lang.reflect.Method;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface LabComponent {}

@LabComponent
class OrderProcessor {
    public void process() {
        System.out.println("Order processed");
    }
}

@LabComponent
class OrderService {
    public void getOrderDetails() {
        System.out.println("Order details");
    }
}

public class ReflectionDemo {
    public static void main(String[] args) throws Exception {
        Class<?> type = OrderProcessor.class;

        if (type.isAnnotationPresent(LabComponent.class)) {
            Constructor<?> constructor =
                type.getDeclaredConstructor();

            Object instance = constructor.newInstance();

            type.getMethod("process").invoke(instance);
        }

        for (Method method : OrderService.class.getDeclaredMethods()) {
            System.out.println(
                method.getName() + " -> " +
                method.getReturnType().getSimpleName()
            );
        }
    }
}
