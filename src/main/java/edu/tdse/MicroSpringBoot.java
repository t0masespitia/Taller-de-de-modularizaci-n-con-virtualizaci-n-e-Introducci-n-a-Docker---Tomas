package edu.tdse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import edu.tdse.annotations.GetMapping;
import edu.tdse.annotations.RequestParam;
import edu.tdse.annotations.RestController;
import edu.tdse.http.HttpServer;

public class MicroSpringBoot {

    private static final Map<String, Function<Map<String, String>, String>> services = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Debe enviar el nombre de la clase controlador.");
        }

        String className = args[0];
        loadComponent(className);

        HttpServer server = new HttpServer(8080, services);
        server.start();
    }

    public static void loadComponent(String className) throws Exception {
        Class<?> c = Class.forName(className);

        if (!c.isAnnotationPresent(RestController.class)) {
            throw new IllegalArgumentException("La clase no está anotada con @RestController");
        }

        Object instance = c.getDeclaredConstructor().newInstance();

        for (Method method : c.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                String path = getMapping.value();

                services.put(path, params -> {
                    try {
                        Object[] argsToInvoke = buildArguments(method, params);
                        Object result = method.invoke(instance, argsToInvoke);
                        return result != null ? result.toString() : "";
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Error interno: " + e.getMessage();
                    }
                });
            }
        }
    }

    private static Object[] buildArguments(Method method, Map<String, String> queryParams) {
        Parameter[] parameters = method.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String paramName = requestParam.value();
                String defaultValue = requestParam.defaultValue();

                values[i] = queryParams.getOrDefault(paramName, defaultValue);
            } else {
                values[i] = null;
            }
        }
        return values;
    }
}