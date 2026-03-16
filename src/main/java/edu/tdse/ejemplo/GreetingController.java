package edu.tdse.ejemplo;

import edu.tdse.annotations.GetMapping;
import edu.tdse.annotations.RequestParam;
import edu.tdse.annotations.RestController;

@RestController
public class GreetingController {

    @GetMapping("/")
    public String index() {
        return """
                <html>
                <head>
                    <title>Micro Framework Tomás Espitia</title>
                </head>
                <body>
                    <h1>Mi micro framework funciona</h1>
                    <p>Proyecto desplegado con Docker y AWS</p>
                    <ul>
                        <li><a href="/greeting">/greeting</a></li>
                        <li><a href="/greeting?name=Tomas">/greeting?name=Tomas</a></li>
                        <li><a href="/status">/status</a></li>
                    </ul>
                </body>
                </html>
                """;
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }

    @GetMapping("/status")
    public String status() {
        return "Servidor activo";
    }
}