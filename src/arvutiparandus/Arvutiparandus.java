package arvutiparandus;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arvutiparandus {

    public static void main(String[] args) throws Exception {
        List<Arvuti> ootelTood = loeFailist(args[0]);

        List<Arvuti> tehtudTood = new ArrayList<>();

        try (BufferedReader bf = new BufferedReader(new InputStreamReader(System.in))) {
            boolean running = true;
            while (running) {
                System.out.println("Kas soovid parandada (P), uut tööd registreerida (R) või lõpetada (L)?");
                String kask = bf.readLine();
                switch (kask) {
                    case "P":
                        paranda(bf, ootelTood, tehtudTood);
                        break;
                    case "R":
                        registreeritood(bf, ootelTood);
                        break;
                    case "L":
                        salvesta(ootelTood, tehtudTood);
                        kokkuvote(ootelTood, tehtudTood);
                        running = false;
                }
            }
        }
    }

    // rida = "Lenovo;tavatöö@2023-03-25T12:34:12"
    // rida =  Ordi;kiirtöö;monitoriga@2023-04-12T10:12:45
    public static Arvuti loeArvuti(String rida) throws FormaadiErind {
        Arvuti arvuti = null;
        LocalDateTime registreerimiseAeg = LocalDateTime.now();
        if (rida.contains("@")) {
            int eraldajaAsukoht = rida.lastIndexOf("@");
            registreerimiseAeg = LocalDateTime.parse(rida.substring(eraldajaAsukoht + 1));
            rida = rida.substring(0, eraldajaAsukoht);
        }
        String[] jupid = rida.split(";");
        if (jupid.length < 2 || jupid.length > 3) {
            throw new FormaadiErind("Väljade arv on vale!");
        }
        if (!jupid[1].equals("tavatöö") && !jupid[1].equals("kiirtöö"))
            throw new FormaadiErind("Töötüübiks võib olla kas tavatöö või kiirtöö!");

        String tootja = jupid[0];
        String tootuup = jupid[1];

        boolean onKiirtoo = parseOnKiirtoo(tootuup);

        if (jupid.length == 2) {
            arvuti = new Arvuti(tootja, onKiirtoo, registreerimiseAeg);
        }
        if (jupid.length == 3) {
            if (jupid[2].equals("monitoriga"))
                arvuti = new ValiseMonitorigaArvuti(tootja, onKiirtoo, registreerimiseAeg);
            else
                throw new FormaadiErind("Kolmanda välja väärtus peaks olema \"monitoriga\"");
        }
        return arvuti;
    }

    private static boolean parseOnKiirtoo(String tootuup) throws FormaadiErind {

        if (!tootuup.equals("kiirtöö") && !tootuup.equals("tavatöö"))
            throw new FormaadiErind("Töötüüp ei ole ega \"kiirtöö\" ega \"tavatöö\"");
        return (tootuup.equals("kiirtöö"));
    }

    private static List<Arvuti> loeFailist(String sisend) throws IOException, InterruptedException {
        List<Arvuti> arvutid = new ArrayList<>();
        boolean isUrl = sisend.startsWith("http://") || sisend.startsWith("https://");

        try (InputStream in = isUrl ? new URI(sisend).toURL().openStream() : new FileInputStream(sisend);
             BufferedReader bf = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String rida = bf.readLine();
            while (rida != null) {
                try {
                    arvutid.add(loeArvuti(rida));
                } catch (FormaadiErind ex) {
                    System.out.println("Rida: " + rida + " Vea selgitus: " +
                            ex.getMessage());
                }
                rida = bf.readLine();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return arvutid;
    }
    public static void registreeritood(BufferedReader bf, List<Arvuti> parandamataArvutid) throws IOException {
        while (true) {
            System.out.println("Sisesta töö kirjeldus: ");
            String rida = bf.readLine();
            try {
                parandamataArvutid.add(loeArvuti(rida));
                break;
            } catch (FormaadiErind ex) {
                System.out.println("Vigane sisestus: " + rida + " Vea selgitus: " +
                        ex.getMessage());
            }
        }
    }

    private static Map<String, Double> loeParandajad(String failinimi) throws Exception {
        Map<String, Double> map = new HashMap<>();

        try (DataInputStream in = new DataInputStream(new FileInputStream(failinimi))) {
            int arv = in.readInt();
            for (int i = 0; i < arv; i++) {
                map.put(in.readUTF(), in.readDouble());
            }
        }
        return map;
    }

    private static void salvesta(List<Arvuti> ootel, List<Arvuti> tehtud) throws Exception {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream("tehtud.dat"))) {
            out.writeInt(tehtud.size());
            for (Arvuti arvuti : tehtud) {
                out.writeUTF(arvuti.getTootja());
                out.writeUTF(arvuti.getRegistreerimiseAeg().toString());
                out.writeDouble(arvuti.getArveSumma());
            }
        }

        try (BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("ootel.txt"), StandardCharsets.UTF_8
        ))) {
            for (Arvuti arvuti : ootel) {
                bf.write(arvuti.toString() + System.lineSeparator());
            }
        }
    }

    private static void kokkuvote(List<Arvuti> ootelTood, List<Arvuti> tehtudTood) {
        System.out.println("Sessiooni kokkuvõte:");
        Map<String, Integer> tootjateJargi = new HashMap<>();
        for (Arvuti arvuti : tehtudTood) {
            if (!tootjateJargi.containsKey(arvuti.getTootja())) {
                tootjateJargi.put(arvuti.getTootja(), 1);
            } else {
                tootjateJargi.put(arvuti.getTootja(), tootjateJargi.get(
                        arvuti.getTootja()) + 1);
            }
        }
        for (String tootja : tootjateJargi.keySet()) {
            System.out.println(tootja + ": " + tootjateJargi.get(tootja) + "tk");
        }
        System.out.println("Ootele jäi " + ootelTood.size() + " arvutit.");
    }

    private static void paranda(BufferedReader bf, List<Arvuti> ootelTood, List<Arvuti> tehtudTood) throws Exception {

        if (ootelTood.isEmpty()) {
            System.out.println("Ootel töid pole!");
            return;
        }
        Map<String, Double> parandajad = loeParandajad("tunnitasud.dat");
        System.out.println("Parandajad:" + parandajad);

        Arvuti parandatav = ootelTood.getFirst();
        for (Arvuti arvuti : ootelTood) {
            if (arvuti.onKiirtoo()) {
                parandatav = arvuti;
                break;
            }
        }
        System.out.println("Arvuti info: " + parandatav);
        System.out.println("Sisesta parandamiseks kulunud aeg (täisminutites):");
        int aeg = Integer.parseInt(bf.readLine());
        System.out.println("Sisesta parandaja nimi:");
        String nimi = bf.readLine();
        double tunniHind = parandajad.containsKey(nimi) ? parandajad.get(nimi) : 15;
        double tooBaashind = aeg / 60.0 * tunniHind;
        parandatav.setArveSumma(parandatav.arvutaArveSumma(tooBaashind));
        System.out.println("Parandatud:" + parandatav + ", arve summa: " + parandatav.getArveSumma());
        ootelTood.remove(parandatav);
        tehtudTood.add(parandatav);
    }

}