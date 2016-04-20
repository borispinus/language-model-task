import java.io.IOException;
import java.util.*;

/**
 * Created by boris on 20.04.16.
 */
public class WordInserter {
    private ModelTable modelTable = null;
    private  <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public String insertWords(int n, String string, String modelPath) throws IOException, ClassNotFoundException {
        modelTable = new ModelTable(modelPath);
        ArrayList<String> prevs = new ArrayList<String>();
        String res = string;
        for (int number = 0; number < n; number++) {
            Scanner scanner = new Scanner(string);
            res = string;
            boolean skipFlag = false;
            ArrayList<Word> beforeList = null;
            while (scanner.hasNext()) {
                String str = scanner.next();
                str = scanToken(str);
                if (skipFlag) {
                    skipFlag = false;
                    try {
                        prevs.remove(0);
                        Collections.sort(beforeList);
                        TreeMap<String, Double> inserts = new TreeMap<String, Double>();
                        for (int i = 0; i < beforeList.size(); i++) {
                            String key = buildKey(prevs).equals("") ? beforeList.get(i).getWord() : buildKey(prevs) + " "
                                    + beforeList.get(i).getWord();
                            for (Word word : modelTable.getModelTable().get(key)) {
                                if (str.equals(word.getWord())) {
                                    inserts.put(beforeList.get(i).getWord(), word.getP() * beforeList.get(i).getP());
                                }
                            }
                        }
                        SortedSet<Map.Entry<String,Double>> entrySortedSet= entriesSortedByValues(inserts);

                        Iterator<Map.Entry<String,Double>>  iterator = entrySortedSet.iterator();
                        int k = 0;
                        String key = "";
                        double p = 0;
                        while(number >= k) {

                            Map.Entry<String, Double> entry = iterator.next();
                            key = entry.getKey();
                            k++;
                        }
                        res = res.replaceFirst("<SKIP>", key);
                        prevs.add(key);
                    } catch (Exception e) {
                        res = res.replaceFirst("<SKIP>", "<NO MATCHING>");
                    }
                }
                if (str.contains("skip")) {
                    skipFlag = true;
                    beforeList = modelTable.getModelTable().get(buildKey(prevs));
                } else {
                    prevs.add(str);
                    if (prevs.size() > modelTable.getnGram() - 1) {
                        prevs.remove(0);
                    }
                }
            }
            if (skipFlag){
                try{
                    String j = prevs.get(0);
                    beforeList =  modelTable.getModelTable().get(buildKey(prevs));
                    res = res.replaceFirst("<SKIP>", beforeList.get(number).getWord());
                }
                catch (Exception e){
                    res = res.replaceFirst("<SKIP>", "<NO MATCHING>");
                }
            }
            System.out.println(res);
        }
        return res;
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

    private String scanToken(String str) {
        str = str.toLowerCase();
        if (modelTable.getWordType().equals("stem")) {
            str = ModelBuilder.stem(removePM(str));
        } else if (modelTable.getWordType().contains("suffix_")) {
            str = removePM(str);
            int suffixLength = Integer.parseInt(modelTable.getWordType().substring(1 + modelTable.getWordType().indexOf(("_"))));
            if (str.length() >= suffixLength)
                str = str.substring(str.length() - suffixLength);
        }
        return str;
    }

    private String removePM(String word) {
        return word.replaceAll("[^\\p{L}]", "");
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new WordInserter().insertWords(Integer.parseInt(args[0]), args[1], args[2]);
    }
}
