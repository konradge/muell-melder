import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    Assistent assistent = new Assistent();

    public Server(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server läuft");
            while (true) {
                try {
                    Socket client = server.accept();
                    handleClient(client);
                } catch (Exception ex) {
                    System.out.println("Verbindung zu Client fehlgeschlagen");
                }
            }

            // server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(3000);
    }

    private void handleClient(Socket client) throws Exception {
        System.out.println("Neue Verbindung zu Client");
        Scanner clientScanner = new Scanner(client.getInputStream());
        PrintWriter clientOut = new PrintWriter(client.getOutputStream());
        sendMessage(clientOut, "Willkommen zu ihrem Müllassistenten");
        sendMessage(clientOut, "Folgende Commandos sind möglich");
        sendMessage(clientOut, "~mail \\E-Mail Adresse\\");
        sendMessage(clientOut, "~add \\Datum(Tag.Monat.<Jahr = 2020>) Mülltyp(Rest, Gelber, Papier)\\");
        sendMessage(clientOut, "\tDabei sind auch mehrere Eingaben getrennt von einem Zeilenumbruch möglich");
        sendMessage(clientOut, "~quit");
        sendMessage(clientOut, "~json \t Lade JSON Datei");

        while (true) {
            // 1. Warte & empfange Botschaft vom Client
            String in = clientScanner.nextLine();
            if (in.equals("quit")) {
                clientOut.close();
                clientScanner.close();
                client.close();
                System.out.println("Trenne Verbindung zu Client");
                break;
            }
            try {
                String[] command = in.split(" ", 2);
                System.out.println(Arrays.toString(command));
                switch (command[0]) {
                    case "mail":
                        assistent.addMail(command[1]);
                        sendMessage(clientOut, command[1] + " wurde hinzugefügt.");
                        break;
                    case "add":
                        assistent.add(command[1]);
                        sendMessage(clientOut, "'" + command[1] + "' wurde hinzugefügt.");
                        break;
                    case "save":
                        assistent.saveDates(assistent.dates);
                        assistent.saveMails(assistent.mails);
                        sendMessage(clientOut, "Mails und Daten wurden gespeichert");
                        break;
                    case "list":
                        assistent.saveDates(assistent.dates);
                        sendMessage(clientOut, "Folgende Abfuhrdaten wurden bereits gespeichert:");
                        sendMessage(clientOut, assistent.getDatesFile().replace("\n", ", "));
                        break;
                    case "json":
                        assistent.loadDatesFromJson(command[1]);
                        break;
                    default:
                        sendMessage(clientOut, "Ungültiger Befehl");
                }
            } catch (Exception e) {
                sendMessage(clientOut, "Es ist ein Fehler aufgetreten!");
            }
        }
    }

    void sendMessage(PrintWriter client, String message) {
        client.println(message);
        client.flush();
    }
}