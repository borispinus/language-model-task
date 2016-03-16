import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by boris on 12.03.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Separator porter = new Separator();
        try {
            long t = System.currentTimeMillis();
            porter.buildModel("/home/boris/corpus.txt", "UTF-8","suffix_3",2, 0,"/home/boris/model.ser");
            System.out.println();
            porter.insertWord("Серию статей об <SKIP> я начал с <SKIP>");
            System.out.println((System.currentTimeMillis()-t)/1000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
