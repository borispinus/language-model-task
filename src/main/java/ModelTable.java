import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by asus on 21.03.2016.
 */
public class ModelTable implements Serializable {
    private HashMap<String, ArrayList<Word>> modelTable;
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    private int nGram = 2;
    private String wordType = "";

    public int getnGram() {
        return nGram;
    }

    public String getWordType() {
        return wordType;
    }

    public HashMap<String, Integer> getWordCount() {
        return wordCount;
    }

    public ModelTable(HashMap<String, ArrayList<Word>> modelTable) {
        this.modelTable = modelTable;
    }

    public ModelTable() {
        this.modelTable = new HashMap<String, ArrayList<Word>>();
    }

    public ModelTable(String sPath) throws IOException, ClassNotFoundException {
        this.modelTable = readModelFromFile(sPath).getModelTable();
        this.wordCount = readModelFromFile(sPath).getWordCount();
        this.nGram = readModelFromFile(sPath).getnGram();
        this.wordType = readModelFromFile(sPath).getWordType();

    }

    private ModelTable readModelFromFile(String sPath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(sPath);
        ObjectInputStream ios = new ObjectInputStream(fis);
        ModelTable read = (ModelTable) ios.readObject();
        return read;
    }

    public void saveModelToFile(String outputPath) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputPath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public void setnGram(int nGram) {
        this.nGram = nGram;
    }

    public void setWordCount(HashMap<String, Integer> wordCount) {
        this.wordCount = wordCount;
    }

    @Override
    public String toString() {
        StringBuffer toPrint = new StringBuffer();
        Set<String> keys = modelTable.keySet();
        for (String key : keys) {
            ArrayList<Word> wordsOfCurrentKey = modelTable.get(key);
            for (Word w : wordsOfCurrentKey) {
                toPrint.append("\t");
                toPrint.append(w.getWord());
                toPrint.append(" ");
                toPrint.append(w.getAmount());
                toPrint.append("\n");
            }
        }

        return toPrint.toString();
    }

    public HashMap<String, ArrayList<Word>> getModelTable() {
        return modelTable;
    }

    public void setModelTable(HashMap<String, ArrayList<Word>> modelTable) {
        this.modelTable = modelTable;
    }
}
