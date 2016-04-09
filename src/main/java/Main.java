import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by boris on 12.03.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        try {
            //ModelTable test = new ModelTable("/home/boris/model.txt");
            Separator porter = new Separator(new ModelTable("/home/boris/model.txt"));
            long t = System.currentTimeMillis();
           // ModelTable modelTable = porter.buildModel("/home/boris/corpus.txt", "UTF-8", "surface_all", 2, 0, "/home/boris/model.txt", "laplace");
            System.out.println("Model is built");
            //porter.insertWords(4, "я очень <SKIP>, давно не <SKIP>");
            porter.buildSentence();
            //porter.sentenceRecovery("на вернула задержанного украину россия человека");
            System.out.println((System.currentTimeMillis() - t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
