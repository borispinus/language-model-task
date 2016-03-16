import java.io.Serializable;

/**
 * Created by boris on 15.03.16.
 */
public class Word implements Serializable{
    private String word;
    private int amount;

    public Word(String word, int amount) {
        this.word = word;
        this.amount = amount;
    }

    public String getWord() {

        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
