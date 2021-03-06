import java.io.Serializable;

/**
 * Created by boris on 15.03.16.
 */
public class Word implements Serializable, Comparable {
    private String word;
    private int amount;
    private double p;

    public Word(int amount, String word) {
        this.amount = amount;
        this.word = word;
    }

    public double getP() {

        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public String getWord() {

        return word;
    }

    @Override
    public boolean equals(Object obj) {
        Word w = (Word) obj;
        return this.word == w.getWord();
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

    public int compareTo(Object object) {
        Word word = (Word) object;
        if (this.getP() > word.getP()) {
            return -1;
        } else if (this.getP() < word.getP()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return word;
    }
}
