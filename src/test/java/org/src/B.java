package org.src;

public class B {
    // cyclic dependency broke here
    int x;

    public B() {
        this.x = 10;
    }

    public B(int x) {
        this.x = x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        return B.class.equals(o.getClass()) && this.x == ((B) o).x;
    }

}
