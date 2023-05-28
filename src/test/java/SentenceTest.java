import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SentenceTest {
    private Sentence sentence;

    @Before
    public void setUp() throws Exception {
        sentence=new Sentence("Is that for real?");
        sentence.process("opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin",
                "opennlp-en-ud-ewt-pos-1.0-1.9.3.bin","en-lemmatizer.bin");
    }

    @After
    public void tearDown() throws Exception {
        sentence=null;
    }

    @org.junit.Test
    public void getContent() {
        System.out.println(sentence.getContent());
    }

    @org.junit.Test
    public void getTokens() {
        for (String s:sentence.getTokens()){
            System.out.println(s);
        }
    }

    @org.junit.Test
    public void getPos() {
        for (String s:sentence.getPos()){
            System.out.println(s);
        }
    }

    @org.junit.Test
    public void getLemmas() {
        for (String s:sentence.getLemmas()){
            System.out.println(s);
        }
    }
}