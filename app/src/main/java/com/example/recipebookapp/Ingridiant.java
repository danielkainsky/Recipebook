package com.example.recipebookapp;

public class Ingridiant {
    private String ingridiant_name;
    private String count;

    public Ingridiant(String ingridiant_name, String count) {
        this.ingridiant_name = ingridiant_name;
        this.count = count;
    }

    public Ingridiant() {
    }

    public String getIngridiant_name() {
        return ingridiant_name;
    }

    public void setIngridiant_name(String ingridiant_name) {
        this.ingridiant_name = ingridiant_name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return ingridiant_name + ' ' + count;
    }
}
