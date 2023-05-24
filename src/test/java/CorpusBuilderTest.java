import junit.framework.TestCase;

public class CorpusBuilderTest extends TestCase {

    public void testGetSentences() {
        CorpusBuilder cb=new CorpusBuilder("Okay, is that for real? It's for real!");
        System.out.println(cb.getText());
        String[] temp=cb.getSentences();
        for (String i: temp){
            System.out.println(i);
        }
        System.out.println(cb.getTokens());
        System.out.println(cb.getPosTags());
        System.out.println(cb.getLemmas());
    }
}