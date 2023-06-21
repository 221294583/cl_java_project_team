import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.codepoetics.protonpack.StreamUtils;
import opennlp.tools.tokenize.*;
import opennlp.tools.postag.*;
import opennlp.tools.lemmatizer.*;
import opennlp.tools.util.Span;
import org.jsoup.nodes.Element;

public class Sentence {

    private String content;
    private Word head;
    //private List<int[]> highlight;
    private String tag;
    private boolean showToken;
    private boolean showPOS;
    private boolean showLemma;

    public void setShowToken(boolean showToken) {
        this.showToken = showToken;
    }

    public void setShowPOS(boolean showPOS) {
        this.showPOS = showPOS;
    }

    public void setShowLemma(boolean showLemma) {
        this.showLemma = showLemma;
    }

    public Sentence(String content) throws KException {
        if (!(content instanceof String)) {
            throw new KException("null was given!");
        }
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public Word getHead() {
        return head;
    }

    public void process(String tokenName, String posName, String lemmaName){
        try(InputStream tokenStream=getClass().getResourceAsStream(tokenName);
        InputStream posStream=getClass().getResourceAsStream(posName);
        InputStream lemmaStream=getClass().getResourceAsStream(lemmaName))
        {
            TokenizerModel tm=new TokenizerModel(tokenStream);
            POSModel pm=new POSModel(posStream);
            LemmatizerModel lm=new LemmatizerModel(lemmaStream);
            Tokenizer tokenizer=new TokenizerME(tm);
            Span[] spanTemp= tokenizer.tokenizePos(this.content);
            String[] tokenTMP= tokenizer.tokenize(this.content);
            POSTaggerME tagger=new POSTaggerME(pm);
            String[] posTMP=tagger.tag(tokenTMP);
            LemmatizerME l=new LemmatizerME(lm);
            String[] lemmaTMP=l.lemmatize(tokenTMP,posTMP);
            Word temp = null;
            for (int i=0;i<tokenTMP.length;i++){
                if (i==0){
                    temp=new Word(tokenTMP[i],posTMP[i],lemmaTMP[i],spanTemp[i]);
                    this.head=temp;
                    System.out.println("SET HEAD");
                }
                else {
                    temp.setNextWord(new Word(tokenTMP[i],posTMP[i],lemmaTMP[i],spanTemp[i]));
                    temp=temp.getNextWord();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String toString(boolean toCopy,int index,int font){
        String result="";
        if (toCopy){
            String tokenString="TOKEN: ";
            String posString="POS: ";
            String lemmaString="LEMMA: ";
            result+=this.content;
            Word cur=this.head;
            while(cur!=null){
                String[] temp=cur.toString(true);
                tokenString+=temp[0];
                posString+=temp[1];
                lemmaString+=temp[2];
                cur=cur.getNextWord();
            }
            /**
            for (Word word:this.set){
                String[] temp=word.toString(true);
                tokenString+=temp[0];
                posString+=temp[1];
                lemmaString+=temp[2];
            }**/
            result+=("\n"+tokenString+"\n"+posString+"\n"+lemmaString);
        }
        else {
            String tokenString=String.format("<%1$s style=\"color:green;\">TOKEN: ",this.tag);
            String posString=String.format("<%1$s style=\"color:red;\">POS: ",this.tag);
            String lemmaString=String.format("<%1$s style=\"color:blue;\">LEMMA: ",this.tag);
            result+=String.format("<div style=\"background-color:%1$s;font-size:%2$dpx\">",
                    index%2==1 ? "#b5d4f5":"#ada161",font);
            result+=String.format("<%1$s>%2$s</%1$s>",this.tag,this.content);
            int offset=this.content.length();
            int tokenLength=7;
            int posLength=5;
            Word cur=this.head;
            while(cur!=null){
                String[] temp=cur.toString(false);
                tokenString+=temp[0];
                posString+=temp[1];
                lemmaString+=temp[2];
                temp=cur.toString(true);
                tokenLength+=temp[0].length();
                posLength+=temp[1].length();
                cur=cur.getNextWord();
            }
            /**
            for (Word word:this.set){
                String[] temp=word.toString(false);
                tokenString+=temp[0];
                posString+=temp[1];
                lemmaString+=temp[2];
            }**/
            tokenString+=String.format("</%1$s>",this.tag);
            posString+=String.format("</%1$s>",this.tag);
            lemmaString+=String.format("</%1$s>",this.tag);
            result+=((this.showToken?tokenString:"")+
                    (this.showPOS?posString:"")+
                    (this.showLemma?lemmaString:""));
            result+="</div>";
            this.head.setOffset(new int[]{offset+7,
                    offset+tokenLength+5+1,
                    offset+tokenLength+posLength+7+2});
        }

        return result;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.toString(false,0,0);
    }

    public String toString(int index,int font) {
        return this.toString(false,index,font);
    }


    public List<int[]> find(String toFind,boolean isRegex,boolean isGlobal){
        String re=isRegex ? toFind : Pattern.quote(toFind);
        List<int[]> result=new ArrayList<>();
        Word cur=this.head;
        while(cur!=null){
            cur.setHasMatch(false);
            cur=cur.getNextWord();
        }
        if (!toFind.isEmpty()){
            Pattern pattern=Pattern.compile(re);
            Matcher matcher=pattern.matcher(this.content);
            while (matcher.find()){
                result.add(new int[]{matcher.start(),matcher.end()});
            }
            int count=0;
            cur=this.head;
            while(cur!=null){
                if (count==result.size()){
                    break;
                }
                if (cur.getSpan().getStart()==result.get(count)[0]&&cur.getSpan().getEnd()==result.get(count)[1]){
                    cur.setHasMatch(true);
                    count+=1;
                }
                else if (result.get(count)[0]<cur.getSpan().getStart()){
                    count+=1;
                }
                cur=cur.getNextWord();
            }
            /**
            for (Word word:this.set){
                if (count==result.size()){
                    break;
                }
                if (word.getSpan().getStart()==result.get(count)[0]&&word.getSpan().getEnd()==result.get(count)[1]){
                    word.setHasMatch(true);
                    count+=1;
                }
                if (result.get(count)[0]<word.getSpan().getStart()){
                    count+=1;
                }
            }**/
        }
        return result;
    }

    public String popupString(){
        String result="";
        String tokenString=String.format("<%1$s style=\"color:green;\">TOKEN: ",this.tag);
        String posString=String.format("<%1$s style=\"color:red;\">POS: ",this.tag);
        String lemmaString=String.format("<%1$s style=\"color:blue;\">LEMMA: ",this.tag);
        Word cur=this.head;
        while(cur!=null){
            String[] temp=cur.toString(false);
            tokenString+=temp[0];
            posString+=temp[1];
            lemmaString+=temp[2];
            cur=cur.getNextWord();
        }
        /**
        for (Word word:this.set){
            String[] temp=word.toString(false);
            tokenString+=temp[0];
            posString+=temp[1];
            lemmaString+=temp[2];
        }**/
        tokenString+=String.format("</%1$s>",this.tag);
        posString+=String.format("</%1$s>",this.tag);
        lemmaString+=String.format("</%1$s>",this.tag);
        result+=((this.showToken?"":tokenString)+
                (this.showPOS?"":posString)+
                (this.showLemma?"":lemmaString));
        return result.equals("") ? null : result;
    }

    public void toXML(Element sent){

        Word cur=this.head;
        while(cur!=null){
            Element w=sent.appendElement("word");
            cur.toXML(w);
            cur=cur.getNextWord();
        }
        /**
        for (Word word:this.set){
            Element w=sent.appendElement("word");
            word.toXML(w);
        }**/
    }
}
