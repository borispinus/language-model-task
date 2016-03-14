import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.File;

/**
 * Created by boris on 12.03.16.
 */
public class Main {
    public static void read(String path) {
        Porter porter = new Porter();
        System.out.println(porter.stem("заигравшийся"));
    }

    public static void main(String[] args){
        String path = "http://az.lib.ru/g/gercen_a_i/text_0010.shtml";
        read(path);
    }
}
