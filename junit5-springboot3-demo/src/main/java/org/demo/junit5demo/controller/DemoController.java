package org.demo.junit5demo.controller;

import org.demo.junit5demo.service.CalculatorService;
import org.demo.junit5demo.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final GreetingService greetingService;
    private final CalculatorService calculatorService;

    public DemoController(GreetingService greetingService, CalculatorService calculatorService) {
        this.greetingService = greetingService;
        this.calculatorService = calculatorService;
    }

    @GetMapping("/api/hello")
    public String hello(@RequestParam(defaultValue = "JUnit5") String name) {
        return greetingService.greet(name);
    }

    @GetMapping("/api/sum")
    public int sum(@RequestParam int a, @RequestParam int b) {
        return a + b;
    }

    @GetMapping("/is-even/{number}")
    public boolean isEven(@PathVariable int number) {
        return calculatorService.isEven(number);
    }
}