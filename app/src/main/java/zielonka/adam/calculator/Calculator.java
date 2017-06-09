package zielonka.adam.calculator;

public class Calculator {
    private double lastNumber;
    private String lastOperator;

    Calculator() {
        lastNumber = 0;
        lastOperator = "=";
    }

    public void clear() {
        lastNumber = 0;
        lastOperator = "=";
    }

    double getResult() {
        return lastNumber;
    }

    void calculate(double number, String operator) {
        switch (lastOperator) {
            case "+": lastNumber = lastNumber + number; break;
            case "-": lastNumber = lastNumber - number; break;
            case "*": lastNumber = lastNumber * number; break;
            case "/": lastNumber = lastNumber / number; break;
            default: lastNumber = number; break;
        }
        lastOperator = operator;
    }
}
