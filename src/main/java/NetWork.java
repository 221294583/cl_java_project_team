import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

public class NetWork extends Thread{
    private String wikiURL;

    public NetWork(String wikiURL) {
        this.wikiURL = wikiURL;
    }

    @Override
    public void run(){

        Article article=Article.getINSTANCE();
        if(!(this.wikiURL.equals(article.getUrl()))){
            Article.purge();
            article=Article.getINSTANCE();
            article.setUrl(this.wikiURL);
            try {
                Document document= Jsoup.connect(this.wikiURL).get();
                Element title=document.select("head > title").first();
                article.addParagraph(new Paragraph(title.tagName(),title.text()));
                Element rawContainer=document.select("#bodyContent").first();
                Element contentContainer=rawContainer.select("#mw-content-text").first();
                Element parserContainer=contentContainer.select(".mw-parser-output").first();
                Elements toRemove=parserContainer.select("sup");
                toRemove.remove();
                Elements subContent=parserContainer.children();
                for (Element e:subContent){
                    if (e.tagName().equals("p")||e.tagName().matches("h\\d")){
                        if (!(e.text().equals(""))){
                            article.addParagraph(new Paragraph(e.tagName(),e.text()));
                        }
                    }
                }
                article.processSentences();
                article.processAll();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}