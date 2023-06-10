import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.tokenize.*;
import opennlp.tools.postag.*;
import opennlp.tools.lemmatizer.*;
import org.jsoup.nodes.Element;

public class Sentence {

    private String content;
    private static String[] tokens=null;
    private static String[] pos=null;
    private static String[] lemmas=null;
    private String tag;
    private boolean showToken;
    private boolean showPOS;

    public void setShowToken(boolean showToken) {
        this.showToken = showToken;
    }

    public void setShowPOS(boolean showPOS) {
        this.showPOS = showPOS;
    }

    public void setShowLemma(boolean showLemma) {
        this.showLemma = showLemma;
    }

    private boolean showLemma;

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

    public String toString(boolean pureText,boolean textOnly){
        String result="";
        if (pureText){
            result+=String.format("%1$s\n",this.content);
            if (showToken){
                result+=String.format("       ",this.tag);
                for (String s:this.tokens) {
                    result+=s+", ";
                }
            }
            if (showPOS){
                result+=String.format("     ",this.tag);
                for (String s:this.pos) {
                    result+=s+", ";
                }
                result+="\n";
            }
            if (showLemma){
                result+=String.format("       ",this.tag);
                for (String s:this.lemmas) {
                    result+=s+", ";
                }
                result+="\n";
            }
        }
        else {
            result+=String.format("<%1$s>%2$s</%1$s>",this.tag,this.content);
            if (showToken){
                result+=String.format("<%1$s style=style=\"color:green;\">TOKEN: ",this.tag);
                for (String s:this.tokens) {
                    result+=String.format("<b>%1$s</b>",s)+", ";
                }
                result+=String.format("</%1$s>",this.tag);
            }
            if (showPOS){
                result+=String.format("<%1$s style=style=\"color:red;\">POS: ",this.tag);
                for (String s:this.pos) {
                    result+=String.format("<b>%1$s</b>",s)+", ";
                }
                result+=String.format("</%1$s>",this.tag);
            }
            if (showLemma){
                result+=String.format("<%1$s style=style=\"color:blue;\">LEMMA: ",this.tag);
                for (String s:this.lemmas) {
                    result+=String.format("<b>%1$s</b>",s)+", ";
                }
                result+=String.format("</%1$s>",this.tag);
            }
        }
        return result;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.toString(false,false);
    }

    public List<int[]> find(String toFind,boolean isRegex,boolean isGlobal){
        String re=isRegex ? toFind : Pattern.quote(toFind);
        List<int[]> result=new ArrayList<>();
        if (!toFind.isEmpty()){
            if (isGlobal){
                Pattern pattern=Pattern.compile(re);
                Matcher matcher= pattern.matcher(this.toString(true,false));
                while (matcher.find()){
                    result.add(new int[]{matcher.start()+1,matcher.end()+1});
                }
            }
            else {
                Pattern pattern=Pattern.compile(re);
                Matcher matcher=pattern.matcher(this.toString(true,true));
                while (matcher.find()){
                    result.add(new int[]{matcher.start()+1,matcher.end()+1});
                }
            }
        }
        return result;
    }

    public String popupString(){
        String buffer="";
        if (!this.showToken){
            buffer+=String.format("<%1$s style=\"color:green;\">TOKEN: ","p");
            for (String i:this.getTokens()){
                buffer+=String.format("<b>%1$s</b>",i)+", ";
            }
            buffer+=String.format("</%1$s>\n","p");
        }
        if (!this.showPOS){
            buffer+=String.format("<%1$s style=\"color:blue;\">POS: ","p");
            for (String i:this.getPos()){
                buffer+=String.format("<b>%1$s</b>",i)+", ";
            }
            buffer+=String.format("</%1$s>\n","p");
        }
        if (!this.showLemma){
            buffer+=String.format("<%1$s style=\"color:red;\">LEMMA: ","p");
            for (String i:this.getLemmas()){
                buffer+=String.format("<b>%1$s</b>",i)+", ";
            }
            buffer+=String.format("</%1$s>\n","p");
        }
        return buffer.equals("") ? null : buffer;
    }

    public void toXML(Element sent){
        for(int i=0;i<this.tokens.length;i++){
            Element t=sent.appendElement("token");
            t.text(this.tokens[i]);
            Element p=sent.appendElement("POS");
            p.text(this.pos[i]);
            Element l=sent.appendElement("lemma");
            l.text(this.lemmas[i]);
        }
    }
}
