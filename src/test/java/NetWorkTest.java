import org.junit.Test;

import static org.junit.Assert.*;

public class NetWorkTest {

    @Test
    public void run() throws InterruptedException {
        NetWork netWork=new NetWork("https://en.wikipedia.org/wiki/James_Hogun","EN");
        netWork.start();
        Article article=Article.getINSTANCE();
        Thread.sleep(10000);
        article.processSentences();
        article.processAll();
        for (Paragraph p:article){
            System.out.println(p);
        }
    }
}