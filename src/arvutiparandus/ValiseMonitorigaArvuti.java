package arvutiparandus;

import java.time.LocalDateTime;

public class ValiseMonitorigaArvuti extends Arvuti {

    public ValiseMonitorigaArvuti(String tootja, boolean onKiirtoo, LocalDateTime registreerimiseAeg) {
        super(tootja, onKiirtoo, registreerimiseAeg);
    }
    @Override
    public double arvutaArveSumma(double baashind) {
        return super.arvutaArveSumma(baashind)+ 1 ;
    }

    public String toString(){
        String too = onKiirtoo() ? "kiirtöö" : "tavatöö";
        return getTootja() + ";" + too + ";monitoriga" + "@" + getRegistreerimiseAeg().toString();
    }
}