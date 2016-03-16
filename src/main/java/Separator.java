/**
 * Created by boris on 14.03.16.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Separator {

    private HashMap<String, ArrayList<Word>> modelTable = new HashMap<String, ArrayList<Word>>();
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


    public void separate(String path, int n) throws FileNotFoundException {
        nGram = n;
        ArrayList<String> prevs = new ArrayList<String>();
        File file = new File(path);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String str = removePM(scanner.next());
            if (!str.equals("")) {
                amount++;
                String key = "";
                for (String word : prevs) {
                    key += word;
                }
                if (wordCount.containsKey(key)) {
                    wordCount.put(key, wordCount.get(key) + 1);
                } else {
                    wordCount.put(key, 1);
                }
                if (modelTable.containsKey(key)) {
                    boolean isContaining = false;
                    ArrayList<Word> keyArrayList = modelTable.get(key);
                    for (int i = 0; i < keyArrayList.size(); i++) {
                        Word word = keyArrayList.get(i);
                        if (word.getWord().equals(str)) {
                            isContaining = true;
                            word.setAmount(word.getAmount() + 1);
                        }
                    }
                    if (!isContaining) {
                        keyArrayList.add(new Word(str, 1));
                    }
                } else {
                    ArrayList<Word> wordArrayList = new ArrayList<Word>();
                    wordArrayList.add(new Word(str, 1));
                    modelTable.put(key, wordArrayList);
                }

            }
            prevs.add(str);
            if (prevs.size() > nGram - 1) {
                prevs.remove(0);
            }
        }
    }

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
                ArrayList<Word> list = modelTable.get(key);
                Word maxWord = list.get(0);
                for (int i = 0; i < list.size(); i++) {
                    Word word = list.get(i);
                    if (word.getAmount() > maxWord.getAmount()) {
                        maxWord = word;
                    }
                }
                string = string.replaceFirst("<SKIP>", maxWord.getWord());
                System.out.println(string);
                System.out.println((double) maxWord.getAmount()/
                        wordCount.get(key));
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