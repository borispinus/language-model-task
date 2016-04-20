import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by boris on 20.04.16.
 */
public class SentenceRecoverer {
    private static final Pattern PERFECTIVEGROUND = Pattern.compile("((ив|ивши|ившись|ыв|ывши|ывшись)|((?<=[ая])(в|вши|вшись)))$");
    private static final Pattern REFLEXIVE = Pattern.compile("(с[яь])$");
    private static final Pattern ADJECTIVE = Pattern.compile("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$");
    private static final Pattern PARTICIPLE = Pattern.compile("((ивш|ывш|ующ)|((?<=[ая])(ем|нн|вш|ющ|щ)))$");
    private static final Pattern VERB = Pattern.compile("((ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю)|((?<=[ая])(ла|на|ете|йте|ли|й|л|ем|н|ло|но|ет|ют|ны|ть|ешь|нно)))$");
    private static final Pattern RVRE = Pattern.compile("^(.*?[аеиоуыэюя])(.*)$");
    private static final Pattern UNION = Pattern.compile("(и|более|менее|очень|крайне|скоре|некотор|кажд|други|котор|когда|однако|если|чтоб|хот|смотря|кактакже|так|зато|что|или|потом|эт|тог|тоже|словно|ежели|кабы|коли|ничем|чем)$");
    private static final Pattern PRETEXT = Pattern.compile("(без|близ|благодаря|в|во|для|до|за|из|изо|к|ко|на|о|от|по|ради|со|с|у|через|чрез)$");
    private static final Pattern PARTICLE = Pattern.compile("(не|ага|аж|бишь|будто|буквально|бы|ведь|вот|вон|вроде|вряд|всё-таки|да|давай|де|дескать|дык|едва|ещё|ж|именно|ладно|конечно|мол|навряд|нет|ни|очевидно|пожалуй|пожалуйста|поди|полноте|якобы|что-то|это|уж|уже|ужели|хоть|хотя|только|так|типа|таки|словно|собственно|спасибо)$");
    private String string;
    private ModelTable modelTable = null;
    private void generateBegins(ArrayList<int[]> list, int pos, int[] sequence, int[] result, boolean[] used) {
        if (pos == modelTable.getnGram() - 1) {
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
        for (String partName : partsStatistics.keySet()) {
            if (!partName.equals("unknown")) {
                return true;
            }
        }
        return false;
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
    private String removePM(String word) {
        return word.replaceAll("[^\\p{L}]", "");
    }
    private void saveStringForFutureKey(String str, ArrayList<String> prevs) {
        if (prevs.size() >= modelTable.getnGram() - 1) {
            prevs.remove(0);
        }
        prevs.add(str);
    }
    public String sentenceRecovery(String mixedSentence, String modelPath) throws IOException, ClassNotFoundException {
        modelTable = new ModelTable(modelPath);
        String[] tokens = mixedSentence.split(" ");
        if (tokens.length < modelTable.getnGram()) {
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
        int[] resultSequence = new int[modelTable.getnGram() - 1];

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
        for (int[] begin : sequenceList) {
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
            for (int i = modelTable.getnGram() - 1; i < words.length; i++) {
                String key = buildKey(prevs).toLowerCase();
                double localMaxP = 0.0;
                String localWord = "";
                int wordIndex = -1;
                partsStatistics.clear();
                boolean wordIsFound = false;

                // if we have experience with this collocation
                if (modelTable.getWordCount().containsKey(key)) {
                    ArrayList<Word> list = modelTable.getModelTable().get(key);
                    // get parts of speech statistics
                    for (Word word : list) {
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
                            for (Word word : list) {
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
                    } else if (moreThanUnknown(partsStatistics)) {
                        int theMostStatisticPart = 0;
                        String partName = "unknown";
                        for (String speechPart : partsStatistics.keySet()) {
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
    public static void main(String[] args) throws IOException, ClassNotFoundException {
    new SentenceRecoverer().sentenceRecovery(args[0], args[1]);
    }
}
