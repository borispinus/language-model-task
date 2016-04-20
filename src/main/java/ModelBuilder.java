import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by boris on 20.04.16.
 */
public class ModelBuilder {

    private ModelTable modelTable = new ModelTable();
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

    public ModelTable buildModel(String path, String charset, String wordType, int n, int unknownWordFreq, String sPath, String... smoothing) throws IOException {

        long t = System.currentTimeMillis();
        System.out.println("Start analyzing...");
        modelTable.setWordType(wordType);
        modelTable.setnGram(n);
        ArrayList<String> prevs = new ArrayList<String>();
        File file = new File(path);
        Scanner scanner = new Scanner(file, charset);
        while (scanner.hasNext()) {
            boolean endFlag = false;
            boolean signFlag = false;
            String str = scanner.next();
            if (str.matches(".*[?!\\.;'].*")) {
                endFlag = true;
            }
            if (str.matches(".*[:,].*")) {
                signFlag = true;
            }
            str = scanToken(str);
            if (!str.equals("")) {
                String sign = "";
                if (endFlag) {
                    if (wordType.equals("surface_all")) {
                        sign = String.valueOf(str.charAt(str.length() - 1));
                    } else {
                        sign = ModelTable.SPECIAL_SIGN;
                    }

                } else if (signFlag && wordType.equals("surface_all")) {
                    sign = String.valueOf(str.charAt(str.length() - 1));
                }
                str = str.replaceAll("[?!\\.,:\";']", "");
                if (!str.equals("")) {
                    String key = buildKey(prevs);
                    addWord(key, str);
                    saveStringForFutureKey(str, prevs);
                    if (endFlag || (wordType.equals("surface_all") && signFlag)) {
                        addWord(buildKey(prevs), sign);
                        saveStringForFutureKey(sign, prevs);
                    }
                }

            }
        }
        String sm = smoothing.length > 0 ? smoothing[0] : "";
        countP(sm);
        printInfo(t);

        modelTable.saveModelToFile(sPath);
        return modelTable;
    }

    private void saveStringForFutureKey(String str, ArrayList<String> prevs) {
        if (prevs.size() >= modelTable.getnGram() - 1) {
            prevs.remove(0);
        }
        prevs.add(str);
    }

    private void printInfo(long t) {
        System.out.print("Build model worktime: ");
        System.out.print((System.currentTimeMillis() - t));
        System.out.println(" ms.");
    }

    private String buildKey(ArrayList<String> prevs) {
        String key = "";
        for (String word : prevs) {
            key += word;
            key += " ";
        }
        if (prevs.size() != 0) {
            key = key.substring(0, key.length() - 1);
        }
        return key;
    }

    private void addWord(String key, String str) {
        if (modelTable.getWordCount().containsKey(key)) {
            modelTable.getWordCount().put(key, modelTable.getWordCount().get(key) + 1);
        } else {
            modelTable.getWordCount().put(key, 1);
        }
        if (modelTable.getModelTable().containsKey(key)) {
            boolean isContaining = false;
            ArrayList<Word> keyArrayList = modelTable.getModelTable().get(key);
            for (int i = 0; i < keyArrayList.size(); i++) {
                Word word = keyArrayList.get(i);
                if (word.getWord().equals(str)) {
                    isContaining = true;
                    word.setAmount(word.getAmount() + 1);
                }
            }
            if (!isContaining) {
                keyArrayList.add(new Word(1, str));
            }
        } else {
            ArrayList<Word> wordArrayList = new ArrayList<Word>();
            wordArrayList.add(new Word(1, str));
            modelTable.getModelTable().put(key, wordArrayList);
        }
    }

    private void countP(String smoothing) {
        for (String key : modelTable.getModelTable().keySet()) {
            for (Word word : modelTable.getModelTable().get(key)) {
                if (smoothing.equals("laplace")) {
                    word.setP((double) (word.getAmount() + 1) / (modelTable.getWordCount().get(key) + modelTable.getWordCount().size()));
                } else {
                    word.setP((double) word.getAmount() / modelTable.getWordCount().get(key));
                }
            }
        }
    }

    private String scanToken(String str) {
        str = str.toLowerCase();
        if (modelTable.getWordType().equals("stem")) {
            str = stem(removePM(str));
        } else if (modelTable.getWordType().contains("suffix_")) {
            str = removePM(str);
            int suffixLength = Integer.parseInt(modelTable.getWordType().substring(1 + modelTable.getWordType().indexOf(("_"))));
            if (str.length() >= suffixLength)
                str = str.substring(str.length() - suffixLength);
        }
        return str;
    }

    public static String stem(String word) {
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


    public static void main(String[] args) throws IOException {
        if (args.length < 7){
            new ModelBuilder().buildModel(args[0], args[1],args[2],Integer.parseInt(args[3]), Integer.parseInt(args[4]),args[5]);
        } else {
            new ModelBuilder().buildModel(args[0], args[1],args[2],Integer.parseInt(args[3]), Integer.parseInt(args[4]),args[5],args[6]);
        }
    }
}
