import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class File {
    public static void main(String[] args) {
        // write("hello.txt", "Hello World\nasdfasdf\n213123213");
        read("hello.txt");
    }

    public static void write(String fileName, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);

            writer.close();
        } catch (Exception e) {
            System.out.println("File writing error!");
        }
    }

    public static String read(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String st, out = "";
            while ((st = br.readLine()) != null) {
                out += st + "\n";
            }
            return out;
        } catch (Exception e) {
            System.out.println("File reading error!");
            return "";
        }
    }
}
