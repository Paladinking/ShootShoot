package test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MyClass {
    public static void main(String[] args) {
        try {
            try {
                throw new IOException();
            } finally {
                System.out.println("Hello");
            }

        } catch (IOException e){
            System.out.println(e);
        }
    }
}
