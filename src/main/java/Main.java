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
            porter.buildModel("/home/cherepashka-ninja/corpus.txt", "UTF-8", "surface_no_pm", 2, 0, "/home/cherepashka-ninja/model.txt");
            System.out.println("Model builded");
            //test = new ModelTable("C:\\Users\\asus\\IdeaProjects\\language-model-task\\model.txt");
            //porter.insertWord("Серию статей об <SKIP> я начал с <SKIP>");
            //System.out.println((System.currentTimeMillis()-t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
