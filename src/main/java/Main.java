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
            porter.buildModel("/home/boris/corpus.txt", "UTF-8", "surface_no_pm", 3, 0, "/home/boris/model.txt","laplace");
            System.out.println("Model is built");
            porter.insertWords(9,"Серию статей об <SKIP> я начал с <SKIP>");
            System.out.println((System.currentTimeMillis() - t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
