package arvutiparandus;

import java.time.LocalDateTime;

public class Arvuti {
    private String tootja;
    private boolean onKiirtoo;
    private java.time.LocalDateTime registreerimiseAeg;
    private double arveSumma;
    public Arvuti(String tootja, boolean onKiirtoo, LocalDateTime registreerimiseAeg) {
        this.tootja = tootja;
        this.onKiirtoo = onKiirtoo;
        this.registreerimiseAeg = registreerimiseAeg;
    }
    public double getArveSumma() {
        return arveSumma;
    }
    public void setArveSumma(double arveSumma) {
        this.arveSumma = (int)(arveSumma*100)/100.00;
    }
    public String getTootja() {
        return tootja;
    }

    public boolean onKiirtoo() {
        return onKiirtoo;
    }
    public LocalDateTime getRegistreerimiseAeg() {
        return registreerimiseAeg;
    }

    public double arvutaArveSumma(double baashind) {
        return onKiirtoo() ? baashind + 12 : baashind + 2;
    }

    public String toString() {
        String too = onKiirtoo() ? "kiirtöö" : "tavatöö";
        return tootja + ";" + too + "@" + getRegistreerimiseAeg().toString();
    }
}
