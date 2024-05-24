package org.src;


import java.util.Arrays;

public class A {
    Object a0 = null;
    int a1;
    long a2;
    float a3;
    double a4;
    boolean a5;
    byte a6;
    char a7;
    String a8;
    B[] a9;
    A a10;

    public A() {
        this(null);
    }

    @Override
    public boolean equals(Object object) {
        return (object != null &&
                object.getClass().equals(A.class)
                && ((A) object).a1 == this.a1
                && ((A) object).a2 == this.a2
                && ((A) object).a3 == this.a3
                && ((A) object).a4 == this.a4
                && ((A) object).a5 == this.a5
                && ((A) object).a6 == this.a6
                && ((A) object).a7 == this.a7
                && ((A) object).a8.equals(this.a8)
                && Arrays.equals(((A) object).a9, this.a9)
                && (((A) object).a10 == null && this.a10 == null || ((A) object).a10.equals(this.a10))
        );
    }

    public A(A a10) {
        this.a1 = 5;
        this.a2 = -10000000000000L;
        this.a3 = 1231.123123f;
        this.a4 = -12312.1233412313123131;
        this.a5 = true;
        this.a6 = 122;
        this.a7 = 'c';
        this.a8 = "Hel lo";
        this.a9 = new B[]{new B(), new B(-20), new B(10)};
        this.a10 = a10;
    }

    public void setA1(int a1) {
        this.a1 = a1;
    }

    public void setA2(long a2) {
        this.a2 = a2;
    }

    public void setA3(float a3) {
        this.a3 = a3;
    }

    public void setA4(double a4) {
        this.a4 = a4;
    }

    public void setA0(Object a0) {
        this.a0 = a0;
    }

    public void setA5(boolean a5) {
        this.a5 = a5;
    }

    public void setA6(byte a6) {
        this.a6 = a6;
    }

    public void setA7(char a7) {
        this.a7 = a7;
    }

    public void setA8(String a8) {
        this.a8 = a8;
    }

    public void setA9(B[] a9) {
        this.a9 = a9;
    }

    public void setA10(A a10) {
        this.a10 = a10;
    }
}
