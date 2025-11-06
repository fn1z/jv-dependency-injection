package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final String CLASS = "Class";
    private static final String IMPL = "Impl";
    private static final String SPACE = " ";

    private static final Injector injector = new Injector();

    private static final Map<Class<?>, Class<?>> interfaceToImplementation = Map.of(
            mate.academy.service.FileReaderService.class, mate.academy.service.impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductParser.class, mate.academy.service.impl.ProductParserImpl.class,
            mate.academy.service.ProductService.class, mate.academy.service.impl.ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            Class<?> implClass = findImplementation(interfaceClazz);

            if (!implClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(CLASS + SPACE + implClass.getName()
                        + " is missing @Component annotation");
            }

            if (instances.containsKey(implClass)) {
                return instances.get(implClass);
            }

            Constructor<?> constructor = implClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object implInstance = constructor.newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }

            instances.put(implClass, implInstance);
            return implInstance;

        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + interfaceClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceToImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
