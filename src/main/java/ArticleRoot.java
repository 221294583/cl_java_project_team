import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArticleRoot implements Iterable<Sentence> {

    private String url = null;
    private List<Sentence> raw;

    public ArticleRoot(Article a) {
        url=a.getUrl();
        raw=new ArrayList<>();
    }

    public void addRaw(Article a){
        for(Sentence s:a.getAllSentence(a)){
            this.raw.add(s);
        }
    }

    public void toXML(Element db, ArticleRoot art){
        Element a=db.appendElement("article");
        Element u=a.appendElement("url");
        u.text(url);
        for (Sentence sentence:raw){
            Element sentTemp=a.appendElement("sentence");
            sentTemp.text(sentence.getContent());
            sentence.toXML(sentTemp);
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public Iterator<Sentence> iterator() {
        return this.raw.iterator();
    }
}
