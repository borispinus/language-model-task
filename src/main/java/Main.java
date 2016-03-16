import java.io.FileNotFoundException;

/**
 * Created by boris on 12.03.16.
 */
public class Main {

    public static void main(String[] args){
        Separator porter = new Separator();
        try {
            porter.separate("/home/boris/corpus.txt", 2);
            System.out.println();
            porter.insertWord("у меня <SKIP>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
