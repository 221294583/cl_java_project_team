import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.*;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.tokenize.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorpusBuilder {

    String text;
    String[] sentences;
    List<List<String>> tokens;
    List<List<String>> lemmas;
    List<List<String>> posTags;
    /**
     * Create a CorpusBuilder which generates POS tags and Lemmas for text.
     * @param text The text which should be annotated.
     */
    CorpusBuilder(String text){
        this.text=text;
        try (InputStream sentStream= Files.newInputStream(Paths.get(
                "src/main/resources/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin"));
        InputStream tokenStream=Files.newInputStream(Paths.get(
                "src/main/resources/opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin"));
        InputStream posStream=Files.newInputStream(Paths.get(
                "src/main/resources/opennlp-en-ud-ewt-pos-1.0-1.9.3.bin"));
        InputStream lemmaStream=Files.newInputStream(Paths.get(
                "src/main/resources/en-lemmatizer.bin"))){
            SentenceModel sm=new SentenceModel(sentStream);
            SentenceDetectorME sentenceDetector=new SentenceDetectorME(sm);
            this.sentences=sentenceDetector.sentDetect(this.text);

            TokenizerModel tm=new TokenizerModel(tokenStream);
            Tokenizer tokenizer=new TokenizerME(tm);
            this.tokens=new ArrayList<>();
            for (String s:this.sentences){
                this.tokens.add(new ArrayList<>(Arrays.asList(tokenizer.tokenize(s))));
            }

            POSModel pm=new POSModel(posStream);
            POSTaggerME tagger=new POSTaggerME(pm);
            this.posTags=new ArrayList<>();
            for (List<String> token:this.tokens){
                this.posTags.add(Arrays.asList(tagger.tag(token.toArray(new String[0]))));
            }

            LemmatizerModel lm=new LemmatizerModel(lemmaStream);
            LemmatizerME lemmatizer=new LemmatizerME(lm);
            this.lemmas=new ArrayList<>();
            for (int i=0;i<this.tokens.size();i++){
                this.lemmas.add(Arrays.asList(lemmatizer.lemmatize(this.tokens.get(i).toArray(new String[0]),
                        this.posTags.get(i).toArray(new String[0]))));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Returns the text of this CorpusBuilder
     * @return The text of this CorpusBuilder
     */
    public String getText() {
        return this.text;
    }

    /**
     * Return an array with the sentences of the CorpusBuilder
     * @return An array with the sentences of the CorpusBuildr
     */
    public String[] getSentences() {
        return this.sentences;
    }

    /**
     * Return a List of List with the tokens/words of the text of CorpusBuilder. The first list holds the words of the
     * first sentence, the second list holds the words of the second sentence and so on.
     * @return A List of List the tokens/words of the text of the CorpusBuilder.
     */
    public List<List<String>> getTokens() {
        return this.tokens;
    }

    /**
     * Return a List of List with the POS tags of the text of CorpusBuilder. The first list holds the POS tags of the
     * first sentence, the second list holds the POS tags of the second sentence and so on.
     * @return A List of List with the POS tags of the text of CorpusBuilder.
     */
    public List<List<String>> getPosTags() {
        return this.posTags;
    }

    /**
     * Return a List of List with the Lemmas of the text of CorpusBuilder. The first list holds the lemmas of the
     * first sentence, the second list holds the Lemmas of the second sentence and so on.
     * @return A List of List with the Lemmas of the text of CorpusBuilder.
     * @return
     */
    public List<List<String>> getLemmas() {
        return this.lemmas;
    }

    public static void main(String[] args) {
        System.out.println("111");
    }
}
