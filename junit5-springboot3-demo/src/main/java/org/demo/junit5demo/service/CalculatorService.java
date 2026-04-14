package org.demo.junit5demo.service;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int dividend, int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor must not be zero");
        }
        return dividend / divisor;
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }
}