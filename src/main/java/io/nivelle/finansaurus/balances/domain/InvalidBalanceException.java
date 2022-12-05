package io.nivelle.finansaurus.balances.domain;

public class InvalidBalanceException extends RuntimeException {
    private int month;
    private int year;

    public InvalidBalanceException(int month, int year) {
        this.month = month;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
