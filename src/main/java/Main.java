import java.io.IOException;


/**
 * Created by boris on 12.03.16.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Separator porter = new Separator();
        ModelTable test;
        try {
            long t = System.currentTimeMillis();
            ModelTable modelTable =  porter.buildModel("/home/boris/corpus.txt", "UTF-8", "surface_no_pm", 3, 0, "/home/boris/model.txt");
            System.out.println("Model is built");
            int n = 0;


            //porter.insertWords(4, "я очень <SKIP>, давно не <SKIP>");
            //porter.buildSentence();
            porter.sentenceRecovery("очень придумать сложно предложение мне");
            System.out.println((System.currentTimeMillis() - t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
