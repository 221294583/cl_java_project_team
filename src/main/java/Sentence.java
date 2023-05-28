import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.tokenize.*;
import opennlp.tools.postag.*;
import opennlp.tools.lemmatizer.*;

public class Sentence {

    private String content;
    private String[] tokens=null;
    private String[] pos=null;
    private String[] lemmas=null;

    public Sentence(String content) throws KException {
        if (!(content instanceof String)) {
            throw new KException("null was given!");
        }
        this.content = content;
    }

    private void setTokens(TokenizerModel tm){
        Tokenizer tokenizer=new TokenizerME(tm);
        this.tokens= tokenizer.tokenize(this.content);
    }

    private void setPos(POSModel pm){
        POSTaggerME tagger=new POSTaggerME(pm);
        this.pos=tagger.tag(this.tokens);
    }

    private void setLemmas(LemmatizerModel lm){
        LemmatizerME l=new LemmatizerME(lm);
        this.lemmas=l.lemmatize(this.tokens,this.pos);
    }

    public String getContent() {
        return this.content;
    }

    public String[] getTokens() {
        return this.tokens;
    }

    public String[] getPos() {
        return this.pos;
    }

    public String[] getLemmas() {
        return this.lemmas;
    }

    public void process(String tokenName,String posName,String lemmaName){
        try(InputStream tokenStream=getClass().getResourceAsStream(tokenName);
        InputStream posStream=getClass().getResourceAsStream(posName);
        InputStream lemmaStream=getClass().getResourceAsStream(lemmaName))
        {
            TokenizerModel tm=new TokenizerModel(tokenStream);
            POSModel pm=new POSModel(posStream);
            LemmatizerModel lm=new LemmatizerModel(lemmaStream);
            this.setTokens(tm);
            this.setPos(pm);
            this.setLemmas(lm);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Sentence{" +
                "content='" + content + '\'' +
                ", tokens=" + Arrays.toString(tokens) +
                ", pos=" + Arrays.toString(pos) +
                ", lemmas=" + Arrays.toString(lemmas) +
                "}\n";
    }
}
