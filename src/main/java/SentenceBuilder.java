import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by boris on 20.04.16.
 */
public class SentenceBuilder {
    private ModelTable modelTable = null;
    public String buildSentence(String modelPath) throws IOException, ClassNotFoundException {
        modelTable = new ModelTable(modelPath);
        ArrayList<String> potentialStarts = new ArrayList<String>();
        for (String key : modelTable.getModelTable().keySet()) {
            if (!modelTable.getWordType().equals("surface_all")) {
                if (key.indexOf(ModelTable.SPECIAL_SIGN) == 0) {
                    potentialStarts.add(key);
                } else if (modelTable.getnGram() == 2) {
                    potentialStarts.add(ModelTable.SPECIAL_SIGN);
                }
            } else if (key.length() > 0 && String.valueOf(key.charAt(0)).matches("[?!\\.']")) {
                potentialStarts.add(key);
            }
        }
        //System.out.println("size = " + potentialStarts.size());
        Random random = new Random();
        String sentence = "";
        sentence = potentialStarts.get(Math.abs(random.nextInt() % potentialStarts.size()));
        String key = "";
        String adding = "";
        int n = 0;
        while (!adding.matches("[?!\\.']|" + ModelTable.SPECIAL_SIGN) && n < 50) {
            n++;
            String[] tokens = sentence.split(" ");
            int limit = tokens.length - modelTable.getnGram();
            int k = 0;
            for (int j = tokens.length - 1; j >= limit; j--) {
                k++;
                if (j >= 0 && k < modelTable.getnGram()) {
                    key = tokens[j] + " " + key;
                }
            }
            if (key.contains(" ")) {
                key = key.substring(0, key.length() - 1);
            }
            ArrayList<Word> list = modelTable.getModelTable().get(key);
            try {
                //System.out.println("key =" + key);

                adding = generateAdding(list);
                if (!adding.equals("")) {
                    sentence += " ";
                    sentence += adding;
                }

            } catch (Exception e) {

            }
            key = "";
        }
        sentence = sentence.replaceAll(ModelTable.SPECIAL_SIGN, "");
        if (modelTable.getWordType().equals("surface_all")) {
            sentence = sentence.substring(2);
        }
        sentence = String.valueOf(sentence.charAt(0)).toUpperCase() + sentence.substring(1);
        System.out.println(sentence);
        //System.out.println("adding = " + adding);
        return sentence;
    }

    private String generateAdding(ArrayList<Word> list) {
        Collections.sort(list);
        ArrayList<Integer> structure = new ArrayList<Integer>();
        double base = list.get(list.size() - 1).getP();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < (int) (list.get(i).getP() / base); j++) {
                if (!list.get(i).getWord().matches("\\s*")) {
                    structure.add(i);
                }
            }
        }
        Random random = new Random();
        String adding = list.get(Math.abs(random.nextInt() % structure.size())).getWord();
        return adding.replaceAll("\\s", "");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new SentenceBuilder().buildSentence(args[0]);
    }
}
