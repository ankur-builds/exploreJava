/*
 * Copyright (c) 2020.
 * File : Date.java
 * Author : Ankur
 * Last modified : 28/11/2020
 * Problem Statement at the end of the code
 *
 * All code is for practice purpose only and strictly non-commercial.
 * All rights reserved.
 * Please refer to apache license terms in the project.
 */

package basics;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Date {

    static String date = "2020-11-26 12:57:37.0";
    static StringBuilder str = new StringBuilder(date);

    static String convert() {
        str.setCharAt(10, 'T');
        str.append("00+0000");
        return str.toString();
    }

    static void getSeconds(String givenTime) {
        System.out.println(
            ChronoUnit.MILLIS.between(Instant.parse(givenTime), Instant.now())
        );
    }

    public static void main(String[] args) {
        System.out.println("#1 Convert : " + convert());
        getSeconds("2020-11-29T12:59:52.893Z");

        IO.println("Local Date : %s".formatted(LocalDate.now()));
        IO.println("Local Time : %s".formatted(LocalTime.now()));
    }
}
