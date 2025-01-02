/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.utils;

@SuppressWarnings("unused")
public class MathUtils {

    public static final double NEGATIVE_MAX_DOUBLE = -Double.MAX_VALUE;

    private final static float EPSILON_FLOAT = 0.000001f;
    private final static double EPSILON_DOUBLE = 0.000001;

    public static boolean equals(float a, float b) {
        return a == b || Math.abs(a - b) < EPSILON_FLOAT;
    }

    public static boolean equals(float a, float b, float epsilon) {
        return a == b || Math.abs(a - b) < epsilon;
    }

    public static boolean greaterThan(float a, float b) {
        return greaterThan(a, b, EPSILON_FLOAT);
    }

    public static boolean greaterThan(float a, float b, float epsilon) {
        return a - b > epsilon;
    }

    public static boolean greaterThanOrEquals(float a, float b) {
        return greaterThanOrEquals(a, b, EPSILON_FLOAT);
    }

    public static boolean greaterThanOrEquals(float a, float b, float epsilon) {
        return a - b > epsilon || equals(a, b, epsilon);
    }

    public static boolean lessThan(float a, float b) {
        return lessThan(a, b, EPSILON_FLOAT);
    }

    public static boolean lessThan(float a, float b, float epsilon) {
        return b - a > epsilon;
    }

    public static boolean lessThanOrEquals(float a, float b) {
        return lessThanOrEquals(a, b, EPSILON_FLOAT);
    }

    public static boolean lessThanOrEquals(float a, float b, float epsilon) {
        return b - a > epsilon || equals(a, b, epsilon);
    }

    public static boolean equals(double a, double b) {
        return a == b || Math.abs(a - b) < EPSILON_DOUBLE;
    }

    public static boolean equals(double a, double b, double epsilon) {
        return a == b || Math.abs(a - b) < epsilon;
    }

    public static boolean greaterThan(double a, double b) {
        return greaterThan(a, b, EPSILON_DOUBLE);
    }

    public static boolean greaterThan(double a, double b, double epsilon) {
        return a - b > epsilon;
    }

    public static boolean greaterThanOrEquals(double a, double b) {
        return greaterThanOrEquals(a, b, EPSILON_DOUBLE);
    }

    public static boolean greaterThanOrEquals(double a, double b, double epsilon) {
        return a - b > epsilon || equals(a, b, epsilon);
    }

    public static boolean lessThan(double a, double b) {
        return lessThan(a, b, EPSILON_DOUBLE);
    }

    public static boolean lessThan(double a, double b, double epsilon) {
        return b - a > epsilon;
    }

    public static boolean lessThanOrEquals(double a, double b) {
        return lessThanOrEquals(a, b, EPSILON_DOUBLE);
    }

    public static boolean lessThanOrEquals(double a, double b, double epsilon) {
        return b - a > epsilon || equals(a, b, epsilon);
    }

}
