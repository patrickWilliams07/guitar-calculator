package uk.ac.cam.pgfw2.analysis;

public class Complex {
    private double real;
    private double imag;

    public Complex() {
        set(0, 0);
    }

    public Complex(double theta) {
        cis(theta);
    }

    public Complex set(Complex z) {
        this.real = z.real;
        this.imag = z.imag;
        return this;
    }

    public Complex set(double real, double imag) {
        this.real = real;
        this.imag = imag;
        return this;
    }

    public Complex cis(double theta) {
        this.real = Math.cos(theta);
        this.imag = Math.sin(theta);
        return this;
    }

    public Complex add(Complex z) {
        this.real += z.real;
        this.imag += z.imag;
        return this;
    }

    public Complex minus(Complex z) {
        this.real -= z.real;
        this.imag -= z.imag;
        return this;
    }

    public Complex multiply(Complex z) {
        double newReal = this.real * z.real - this.imag * z.imag;
        this.imag = this.real * z.imag + this.imag * z.real;
        this.real = newReal;
        return this;
    }

    public Complex multiply(double scalar) {
        this.real *= scalar;
        this.imag *= scalar;
        return this;
    }

    public double squareModulus() {
        return this.real * this.real + this.imag * this.imag;
    }

    @Override
    public String toString() {
        return "(" + real + "+i" + imag + ")";
    }
}
