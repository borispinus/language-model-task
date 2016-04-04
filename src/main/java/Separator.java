/**
 * Created by boris on 14.03.16.
 */

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Separator {

    public static final String SPECIAL_SIGN = "SPECIAL_SIGN";
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
    private ModelTable modelTable = new ModelTable();
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    private static final Pattern UNION = Pattern.compile("(и|более|менее|очень|крайне|скоре|некотор|кажд|други|котор|когда|однако|если|чтоб|хот|смотря|кактакже|так|зато|что|или|потом|эт|тог|тоже|словно|ежели|кабы|коли|ничем|чем)$");
    private static final Pattern PRETEXT = Pattern.compile("(без|близ|благодаря|в|во|для|до|за|из|изо|к|ко|на|о|от|по|ради|со|с|у|через|чрез)$");
    private static final Pattern PARTICLE = Pattern.compile("(не|ага|аж|бишь|будто|буквально|бы|ведь|вот|вон|вроде|вряд|всё-таки|да|давай|де|дескать|дык|едва|ещё|ж|именно|ладно|конечно|мол|навряд|нет|ни|очевидно|пожалуй|пожалуйста|поди|полноте|якобы|что-то|это|уж|уже|ужели|хоть|хотя|только|так|типа|таки|словно|собственно|спасибо)$");
    private int amount = 0;
    private int nGram = 2;
    private String wordType = "";
    private String string;

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


    public ModelTable buildModel(String path, String charset, String wordType, int n, int unknownWordFreq, String sPath, String... smoothing) throws IOException {

        long t = System.currentTimeMillis();
        System.out.println("Start analyzing...");
        this.wordType = wordType;
        nGram = n;
        ArrayList<String> prevs = new ArrayList<String>();
        File file = new File(path);
        Scanner scanner = new Scanner(file, charset);
        while (scanner.hasNext()) {
            boolean endFlag = false;
            String str = scanner.next();
            if (str.matches(".*[?!\\.;'].*")) {
                amount++;
                endFlag = true;
            }
            str = scanToken(str);
            if (!str.equals("")) {
                amount++;
                String sign = "";
                if (endFlag) {
                    if (wordType.equals("surface_all")) {
                        sign = String.valueOf(str.charAt(str.length() - 1));
                    } else {
                        sign = SPECIAL_SIGN;
                    }

                }
                str = str.replaceAll("[?!\\.,:\";']", "");
                if (!str.equals("")) {
                    String key = buildKey(prevs);
                    addWord(key, str);
                    saveStringForFutureKey(str, prevs);
                    if (endFlag) {
                        addWord(buildKey(prevs), sign);
                        saveStringForFutureKey(sign, prevs);
                    }
                }

            }
            printDots();
        }
        String sm = smoothing.length > 0 ? smoothing[0] : "";
        countP(sm);
        removeRareWords(unknownWordFreq);
        printInfo(t);

        //modelTable.saveModelToFile(sPath);
        return modelTable;
    }

    private void saveStringForFutureKey(String str, ArrayList<String> prevs) {
        if (prevs.size() >= nGram - 1) {
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
        }
        return key;
    }

    private void addWord(String key, String str) {
        if (wordCount.containsKey(key)) {
            wordCount.put(key, wordCount.get(key) + 1);
        } else {
            wordCount.put(key, 1);
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
                    word.setP((double) (word.getAmount() + 1) / (wordCount.get(key) + wordCount.size()));
                } else {
                    word.setP((double) word.getAmount() / wordCount.get(key));
                }
            }
        }
    }

    private void printDots() {
        if (amount % 10000 == 0) {
            if (amount % 100000 == 0) {
                System.out.println(".");
            } else {
                System.out.print(".");
            }
        }
    }

    private void removeRareWords(int freq) {
        for (String key : wordCount.keySet()) {
            if (wordCount.get(key) < freq) {
                wordCount.remove(key);
                modelTable.getModelTable().remove(key);
            }
        }
    }

    private String scanToken(String str) {
        str = str.toLowerCase();
        if (wordType.equals("stem")) {
            str = stem(removePM(str));
        } else if (wordType.contains("suffix_")) {
            str = removePM(str);
            int suffixLength = Integer.parseInt(wordType.substring(1 + wordType.indexOf(("_"))));
            if (str.length() >= suffixLength)
                str = str.substring(str.length() - suffixLength);
        }
        return str;
    }

    public String insertWords(int n, String string) {
        ArrayList<String> prevs = new ArrayList<String>();
        String res = string;
        for (int number = 0; number < n; number++) {
            Scanner scanner = new Scanner(string);
            res = string;
            while (scanner.hasNext()) {
                String str = scanner.next();
                str = scanToken(str);
                if (str.contains("skip")) {
                    ArrayList<Word> list = modelTable.getModelTable().get(buildKey(prevs));
                    try {
                        Collections.sort(list);
                        res = res.replaceFirst("<SKIP>", list.get(number).getWord());
                    } catch (Exception e) {
                        res = res.replaceFirst("<SKIP>", "<NO MATCHING>");
                    }
                } else {
                    prevs.add(str);
                    if (prevs.size() > nGram - 1) {
                        prevs.remove(0);
                    }
                }
            }
            System.out.println(res);
        }
        return res;
    }

    public String buildSentence() {
        ArrayList<String> potentialStarts = new ArrayList<String>();
        for (String key : modelTable.getModelTable().keySet()) {
            if (!wordType.equals("surface_all")) {
                if (key.indexOf(SPECIAL_SIGN) == 0) {
                    potentialStarts.add(key);
                }
            } else {
                if (key.length() > 0 && String.valueOf(key.charAt(0)).matches("[?!\\.']")) {
                    potentialStarts.add(key);
                }
            }
        }
        Random random = new Random();
        String sentence = potentialStarts.get(Math.abs(random.nextInt() % potentialStarts.size()));
        String key = "";
        String adding = "";
        int n = 0;
        sentence = sentence.replaceAll(SPECIAL_SIGN, SPECIAL_SIGN + " ");
        while (!adding.matches("[?!\\.']|" + SPECIAL_SIGN) && n < 50) {
            n++;
            if (n == 2 && wordType.equals("surface_all")) {
                sentence = sentence.substring(1);
            }
            String[] tokens = sentence.split(" ");
            int limit = tokens.length - nGram;
            if (n == 1) {
                limit++;
            }
            for (int j = tokens.length - 1; j > limit; j--) {
                key = tokens[j] + key;
            }
            ArrayList<Word> list = modelTable.getModelTable().get(key);
            try {
                Collections.sort(list);
                sentence += " ";
                adding = list.get(0).getWord();
                sentence += adding;

            } catch (Exception e) {

            }
            key = "";
        }
        sentence = sentence.replaceAll(SPECIAL_SIGN, "");
        System.out.println(sentence);
        return sentence;
    }
    private void generateBegins(ArrayList<int[]> list, int pos, int[] sequence, int[] result, boolean[] used) {
        if (pos == nGram - 1) {
            list.add(result);
            return;
        }
        for (int i = 0; i < sequence.length; i++) {
            if (!used[i]) {
                used[i] = true;
                result[pos] = sequence[i];
                generateBegins(list, pos + 1, sequence, result.clone(), used);
                used[i] = false;
            }
        }
    }

    private String getPartOfSpeech(String word) {
        word = word.toLowerCase();
        word = word.replace('ё', 'е');
        if (UNION.matcher(word).matches()) {
            return "union";
        }
        if (PRETEXT.matcher(word).matches()) {
            return "pretext";
        }
        if (PARTICLE.matcher(word).matches()) {
            return "particle";
        }
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
                    if (!temp.equals(rv)) {
                        return "participle";
                    } else {
                        return "adjective";
                    }
                } else {
                    temp = VERB.matcher(rv).replaceFirst("");
                    if (temp.equals(rv)) {
                        return "noun";
                    } else {
                        return "verb";
                    }
                }
            } else {
                return "participle";
            }
        }
        return "unknown";
    }

    private boolean moreThanUnknown(HashMap<String, Integer> partsStatistics) {
        for (String partName: partsStatistics.keySet()) {
            if (!partName.equals("unknown")) {
                return true;
            }
        }
        return false;
    }

    public String sentenceRecovery(String mixedSentence) {
        String[] tokens = mixedSentence.split(" ");
        if (tokens.length < nGram) {
            return mixedSentence;
        }
        int length = tokens.length;
        String theEnd = "";
        int theEndIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].matches(".*[?!\\.]")) {
                theEnd = tokens[i];
                theEndIndex = i;
                length--;

            }
        }
        String[] words = new String[length];
        int cnt = 0;
        for (int i = 0; i < tokens.length; i++) {
            if (i != theEndIndex) {
                words[cnt++] = tokens[i];
            }
        }
        // assign
        ArrayList<int[]> sequenceList = new ArrayList<int[]>();
        int[] initialSequence = new int[words.length];
        boolean[] used = new boolean[words.length];
        int[] resultSequence = new int[nGram - 1];

        // init
        for (int i = 0; i < words.length; i++) {
            initialSequence[i] = i;
        }
        Arrays.fill(used, false);

        generateBegins(sequenceList, 0, initialSequence, resultSequence, used);


        String sentence = mixedSentence;
        double maxP = -1.0;
        HashMap<String, Integer> partsStatistics = new HashMap<String, Integer>();

        // get data about parts of speech of the given sentence
        ArrayList<String> sentencePartsOfSpeech = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
            String partName = getPartOfSpeech(removePM(words[i]));
            sentencePartsOfSpeech.add(partName);
        }

        // try any begin of the possible sentence
        for (int[] begin: sequenceList) {
            // init
            ArrayList<
                    String> prevs = new ArrayList<String>();
            Arrays.fill(used, false);
            String tempSentence = "";
            // begins init
            for (int i = 0; i < begin.length; i++) {
                tempSentence += (words[begin[i]] + " ");
                saveStringForFutureKey(removePM(words[begin[i]]), prevs);
                used[begin[i]] = true;
            }
            // possible sentence building
            double sequenceP = 0.0;
            for (int i = nGram - 1; i < words.length; i++) {
                String key = buildKey(prevs).toLowerCase();
                double localMaxP = 0.0;
                String localWord = "";
                int wordIndex = -1;
                partsStatistics.clear();
                boolean wordIsFound = false;

                // if we have experience with this collocation
                if (wordCount.containsKey(key)) {
                    ArrayList<Word> list = modelTable.getModelTable().get(key);
                    // get parts of speech statistics
                    for (Word word: list) {
                        String partName = getPartOfSpeech(word.getWord());
                        if (partsStatistics.containsKey(partName)) {
                            partsStatistics.put(partName, partsStatistics.get(partName) + 1);
                        } else {
                            partsStatistics.put(partName, 1);
                        }
                    }
                    // try to find the most probable word
                    for (int j = 0; j < words.length; j++) {
                        if (!used[j]) {
                            for (Word word: list) {
                                if (removePM(words[j]).equalsIgnoreCase(word.getWord())) {
                                    double wordP = word.getP();
                                    if (Double.compare(word.getP(), localMaxP) > 0) {
                                        localMaxP = word.getP();
                                        localWord = words[j];
                                        wordIndex = j;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // if we found its
                    if (wordIndex > -1) {
                        used[wordIndex] = true;
                        wordIsFound = true;
                    }
                    else if (moreThanUnknown(partsStatistics)) {
                        int theMostStatisticPart = 0;
                        String partName = "unknown";
                        for (String speechPart: partsStatistics.keySet()) {
                            if (!speechPart.equals("unknown") && partsStatistics.get(speechPart) > theMostStatisticPart) {
                                theMostStatisticPart = partsStatistics.get(speechPart);
                                partName = speechPart;
                            }
                        }
                        if (!partName.equals("unknown")) {
                            for (int j = 0; j < words.length; j++) {
                                if (!used[j] && partName.equals(sentencePartsOfSpeech.get(j))) {
                                    localMaxP = 0.0004;
                                    localWord = words[j];
                                    used[j] = true;
                                    wordIsFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!wordIsFound) {
                    for (int j = 0; j < words.length; j++) {
                        if (!used[j]) {
                            localWord = words[j];
                            used[j] = true;
                            break;
                        }
                    }
                }
                sequenceP += localMaxP;
                tempSentence += (localWord + " ");
                saveStringForFutureKey(localWord, prevs);
            }


            if (Double.compare(sequenceP, maxP) > 0) {
                maxP = sequenceP;
                sentence = tempSentence;
            }
        }

        System.out.println(sentence + theEnd);
        return sentence + theEnd;
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