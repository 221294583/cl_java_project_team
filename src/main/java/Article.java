import opennlp.tools.sentdetect.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Article implements Iterable<Paragraph>{

    private String url=null;
    private List<Paragraph> raw;
    private String sentStreamName;
    private String tokenStreamName;
    private String posStreamName;
    private String lemmaStreamName;
    private boolean done=false;
    private static Article INSTANCE;

    private Article(String sentStreamName, String tokenStreamName, String posStreamName, String lemmaStreamName) {
        this.raw = new ArrayList<>();
        this.sentStreamName = sentStreamName;
        this.tokenStreamName = tokenStreamName;
        this.posStreamName = posStreamName;
        this.lemmaStreamName = lemmaStreamName;
    }

    public static Article getINSTANCE() {
        if (INSTANCE==null){
            try(BufferedReader bf=new BufferedReader(new FileReader(Article.class.getResource("config").getPath()))){
                String buffer;
                String sentStreamName=null;
                String tokenStreamName=null;
                String posStreamName=null;
                String lemmaStreamName=null;
                while((buffer=bf.readLine())!=null){
                    String[] dict=buffer.split("=");
                    if (dict[0].equals("sent")){
                        sentStreamName=dict[1];
                    }
                    if (dict[0].equals("token")){
                        tokenStreamName=dict[1];
                    }
                    if (dict[0].equals("pos")){
                        posStreamName=dict[1];
                    }
                    if (dict[0].equals("lemma")){
                        lemmaStreamName=dict[1];
                    }
                }
                INSTANCE=new Article(sentStreamName,tokenStreamName,posStreamName,lemmaStreamName);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public static void purge(){
        INSTANCE=null;
    }

    public void addParagraph(Paragraph paragraph){
        this.raw.add(paragraph);
    }

    public void processSentences(){
        try (InputStream sentSteam=getClass().getResourceAsStream(this.sentStreamName)){
            SentenceModel sm=new SentenceModel(sentSteam);
            SentenceDetectorME sentenceDetector=new SentenceDetectorME(sm);
            for (Paragraph paragraph:this.raw){
                paragraph.preProcess(sentenceDetector);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void processAt(int indexOutside,int indexInside){
        this.raw.get(indexOutside).get(indexInside).process(this.tokenStreamName,this.posStreamName,this.lemmaStreamName);
    }

    public void processAll(){
        for (Paragraph p:this.raw) {
            for (Sentence s:p){
                s.process(this.tokenStreamName,this.posStreamName,this.lemmaStreamName);
            }
        }
        done=true;
    }

    public Sentence sentenceAt(int indexOutside,int indexInside){
        return this.raw.get(indexOutside).get(indexInside);
    }

    public boolean isDone() {
        return done;
    }

    public String toString(boolean showToken,boolean showPOS,boolean showLemma){
        String res="";
        for (Paragraph p:this.raw){
            for (Sentence s:p){
                res+=String.format("<%1$s>%2$s</%1$s>",p.getTag(),s.getContent());
                if (showToken){
                    res+=String.format("<%1$s style=\"background-color:yellow;\">TOKEN: ",p.getTag());
                    for (String i:s.getTokens()){
                        res+=i+", ";
                    }
                    res+=String.format("</%1$s>\n",p.getTag());
                }
                if (showPOS){
                    res+=String.format("<%1$s style=\"background-color:blue;\">POS: ",p.getTag());
                    for (String i:s.getPos()){
                        res+=i+", ";
                    }
                    res+=String.format("</%1$s>\n",p.getTag());
                }
                if (showLemma){
                    res+=String.format("<%1$s style=\"background-color:red;\">LEMMA: ",p.getTag());
                    for (String i:s.getLemmas()){
                        res+=i+", ";
                    }
                    res+=String.format("</%1$s>\n",p.getTag());
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "Article{" +
                "raw=" + raw +
                '}';
    }

    @Override
    public Iterator<Paragraph> iterator() {
        return this.raw.iterator();
    }
}
