import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class ParagraphTest {

    @Test
    public void iterator() {
        Paragraph p=new Paragraph("p","Is that for real? Yes, it is for real!");
        try (InputStream sentSteam=getClass().getResourceAsStream("opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin")) {
            SentenceModel sm = new SentenceModel(sentSteam);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(sm);
            p.preProcess(sentenceDetector);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (Sentence s:p){
            System.out.println(s);
        }
    }
}