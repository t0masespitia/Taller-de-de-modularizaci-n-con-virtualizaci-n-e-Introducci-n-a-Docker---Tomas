package edu.tdse.ejemplo;

import edu.tdse.annotations.GetMapping;
import edu.tdse.annotations.RequestParam;
import edu.tdse.annotations.RestController;

@RestController
public class GreetingController {

    @GetMapping("/")
    public String index() {
        return "<html><body><h1>Mi micro framework funciona</h1></body></html>";
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}