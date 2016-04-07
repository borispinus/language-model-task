import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by boris on 12.03.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Separator porter = new Separator();
        ModelTable test;
        try {
            long t = System.currentTimeMillis();
            ModelTable modelTable =  porter.buildModel("/home/boris/corpus.txt", "UTF-8", "stem", 3, 0, "/home/boris/model.txt");
            System.out.println("Model is built");
            int n = 0;
            //for (String key: modelTable.getModelTable().keySet()){
              //  System.out.println(key);
            //}


            porter.insertWords(4, "я очень <SKIP>, давно не <SKIP>");
            //porter.buildSentence();
            System.out.println((System.currentTimeMillis() - t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
