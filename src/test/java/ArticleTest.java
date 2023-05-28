import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArticleTest {

    Article article;

    @Before
    public void setUp() throws Exception {
        article=Article.getINSTANCE();
    }

    @After
    public void tearDown() throws Exception {
        Article.purge();
    }

    @Test
    public void purge() {
        article.addParagraph(new Paragraph("p","Is that for real? Yes,it is for real!"));
        article.processSentences();
        article.processAll();
        System.out.println(article.sentenceAt(0,0));
        Article.purge();
        article=Article.getINSTANCE();
        try{
            System.out.println(article.sentenceAt(0,0));}
        catch (Exception e){
            System.out.println("purged");
        }
        article.addParagraph(new Paragraph("p","Are you okay? No, i'm not okay!"));
        article.processSentences();
        article.processAll();
        System.out.println(article.sentenceAt(0,0));
    }
}