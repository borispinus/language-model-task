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
            porter.buildModel("C:\\Users\\asus\\Downloads\\Telegram Desktop\\corpus.txt", "UTF-8", "stem", 2, 0, "C:\\Users\\asus\\IdeaProjects\\language-model-task\\model.txt");
            System.out.println("Model builded");
            //test = new ModelTable("C:\\Users\\asus\\IdeaProjects\\language-model-task\\model.txt");
            //porter.insertWord("Серию статей об <SKIP> я начал с <SKIP>");
            //System.out.println((System.currentTimeMillis()-t));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
