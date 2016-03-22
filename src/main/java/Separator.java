
/**
 * Created by boris on 14.03.16.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Separator {

    private ModelTable modelTable = new ModelTable();
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    private int amount = 0;
    private int nGram = 2;

    private String string;

    private static final Pattern PERFECTIVEGROUND = Pattern.compile("((ив|ивши|ившись|ыв|ывши|ывшись)|((?<=[ая])(в|вши|вшись)))$");

    private static final Pattern REFLEXIVE = Pattern.compile("(с[яь])$");

    private static final Pattern ADJECTIVE = Pattern.compile("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$");

    private static final Pattern PARTICIPLE = Pattern.compile("((ивш|ывш|ующ)|((?<=[ая])(ем|нн|вш|ющ|щ)))$");

    private static final Pattern VERB = Pattern.compile("((ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю)|((?<=[ая])(ла|на|ете|йте|ли|й|л|ем|н|ло|но|ет|ют|ны|ть|ешь|нно)))$");

    private static final Pattern NOUN = Pattern.compile("(а|ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$");

    private static final Pattern RVRE = Pattern.compile("^(.*?[аеиоуыэюя])(.*)$");

    private static final Pattern DERIVATIONAL = Pattern.compile(".*[^аеиоуыэюя]+[аеиоуыэюя].*ость?$");

    private static final Pattern DER = Pattern.compile("ость?$");

    private static final Pattern SUPERLATIVE = Pattern.compile("(ейше|ейш)$");

    private static final Pattern I = Pattern.compile("и$");
    private static final Pattern P = Pattern.compile("ь$");
    private static final Pattern NN = Pattern.compile("нн$");

    public String stem(String word) {
        word = word.toLowerCase();
        word = word.replace('ё', 'е');
        Matcher m = RVRE.matcher(word);
        if (m.matches()) {
            String pre = m.group(1);
            String rv = m.group(2);
            String temp = PERFECTIVEGROUND.matcher(rv).replaceFirst("");
            if (temp.equals(rv)) {
                rv = REFLEXIVE.matcher(rv).replaceFirst("");
                temp = ADJECTIVE.matcher(rv).replaceFirst("");
                if (!temp.equals(rv)) {
                    rv = temp;
                    rv = PARTICIPLE.matcher(rv).replaceFirst("");
                } else {
                    temp = VERB.matcher(rv).replaceFirst("");
                    if (temp.equals(rv)) {
                        rv = NOUN.matcher(rv).replaceFirst("");
                    } else {
                        rv = temp;
                    }
                }

            } else {
                rv = temp;
            }

            rv = I.matcher(rv).replaceFirst("");

            if (DERIVATIONAL.matcher(rv).matches()) {
                rv = DER.matcher(rv).replaceFirst("");
            }

            temp = P.matcher(rv).replaceFirst("");
            if (temp.equals(rv)) {
                rv = SUPERLATIVE.matcher(rv).replaceFirst("");
                rv = NN.matcher(rv).replaceFirst("н");
            } else {
                rv = temp;
            }
            word = pre + rv;

        }

        return word;
    }

    private String removePM(String word) {
        return word.replaceAll("[^\\p{L}]", "");
    }

    private String removeSpaces(String word) {
        return word.replaceAll("\\s", " ");
    }

    private String getWord(String word, String wordType) {

        switch (WordType.getType(wordType)) {

            case SURFACE_ALL:
                return word;

            case SURFACE_NO_PM:
                return removePM(word);

            case STEM:
                word = removePM(word);
                return stem(word);

            case SUFFIX_X:
                word = removePM(word);
                int suffixLength = Integer.parseInt(word.substring(1 + word.indexOf(("_"))));
                if (word.length() >= suffixLength) {
                    return word.substring(word.length() - suffixLength);
                } else {
                    return word;
                }

            default:
                throw new RuntimeException("Where is your miiind? Wheeere is your miind?..uu-uuu");
        }
    }

    /**
     * Founded minuses: 
     * 1? reading is made line by line so on line breaks we
     *    lose some words.
     */
    public void buildModel(String path, String charset, String wordType, int n, int unknownWordFreq, String sPath) throws IOException {
        long t = System.currentTimeMillis();
        System.out.println("Start analyzing...");

        nGram = n;
        List<String> grams = new LinkedList<String>();
        StringBuilder builderKey = new StringBuilder();
        String key = "";
        BufferedReader br = new BufferedReader(new FileReader(path));

        String line;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("(\\s){2,}", " ");
            if (!line.isEmpty()) {

                String[] words = line.split("\\s");
                for (String word : words) {
                    if (!word.isEmpty()) {

                        // init
                        word = getWord(word, wordType);
                        builderKey = new StringBuilder();
                        for (String part : grams) {
                            builderKey.append(part);
                        }
                        key = builderKey.toString();

                        //word amounts and counts 
                        amount++;
                        if (wordCount.containsKey(key)) {
                            wordCount.put(key, wordCount.get(key) + 1);
                        } else {
                            wordCount.put(key, 1);
                        }

                        // modelTable
                        if (modelTable.getModelTable().containsKey(key)) {

                            ArrayList<Word> valueWords = modelTable.getModelTable().get(key);

                            boolean isContaining = false;
                            for (Word valueWord : valueWords) {
                                if (valueWord.getWord().equals(word)) {
                                    valueWord.setAmount(valueWord.getAmount() + 1);
                                    isContaining = true;
                                    break;
                                }
                            }
                            if (!isContaining) {
                                valueWords.add(new Word(word, 1));
                            }
                        } else {
                            ArrayList<Word> valueWords = new ArrayList<Word>();
                            valueWords.add(new Word(word, 1));
                            modelTable.getModelTable().put(key, valueWords);
                        }
                        grams.add(word);
                        if (grams.size() > nGram - 1) {
                            grams.remove(0);
                        }

                        //beaty points
                        if (amount % 10000 == 0) {
                            if (amount % 100000 == 0) {
                                System.out.println(".");
                            } else {
                                System.out.print(".");
                            }
                        }
                    }
                }
            }
        }

        removeRareWords(unknownWordFreq);

        System.out.print("Build model worktime: ");
        System.out.print((System.currentTimeMillis() - t));
        System.out.println(" ms.");

        modelTable.saveModelToFile(sPath);
    }

    private void removeRareWords(int freq) {
        for (String key : wordCount.keySet()) {
            if (wordCount.get(key) < freq) {
                wordCount.remove(key);
                modelTable.getModelTable().remove(key);
            }
        }
    }

    /*private String scanToken(Scanner scanner, String wordType) {
        String str = "";
        if (wordType.equals("surface_all")) {
            str = scanner.next();
        } else if (wordType.equals("surface_no_pm")) {
            str = removePM(scanner.next());
        } else if (wordType.equals("stem")) {
            str = stem(removePM(scanner.next()));
        } else if (wordType.contains("suffix_")) {
            str = removePM(scanner.next());
            int suffixLength = Integer.parseInt(wordType.substring(1 + wordType.indexOf(("_"))));
            if (str.length() >= suffixLength) {
                str = str.substring(str.length() - suffixLength);
            }
        }
        return str;
    }*/

    public String insertWord(String string) {
        Scanner scanner = new Scanner(string);
        ArrayList<String> prevs = new ArrayList<String>();
        while (scanner.hasNext()) {
            String str = scanner.next().toLowerCase();
            if (str.equals("<skip>")) {
                String key = "";
                for (String word : prevs) {
                    key += word;
                }
                ArrayList<Word> list = modelTable.getModelTable().get(key);
                try {
                    Word maxWord = list.get(0);
                    for (int i = 0; i < list.size(); i++) {
                        Word word = list.get(i);
                        if (word.getAmount() > maxWord.getAmount()) {
                            maxWord = word;
                        }
                    }
                    string = string.replaceFirst("<SKIP>", maxWord.getWord());
                    System.out.println(string);
                    System.out.println((double) maxWord.getAmount()
                            / wordCount.get(key));
                } catch (NullPointerException e) {
                    System.out.println("No matching");
                }
            } else {
                prevs.add(str);
                if (prevs.size() > nGram - 1) {
                    prevs.remove(0);
                }
            }
        }

        return string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String continueString(String string) {

        return string;
    }

}
