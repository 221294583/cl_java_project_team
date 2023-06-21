import opennlp.tools.sentdetect.*;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Article implements Iterable<Paragraph>{

    private static String url=null;
    private static List<Paragraph> raw;
    private String sentStreamName;
    private String tokenStreamName;
    private String posStreamName;
    private String lemmaStreamName;
    private boolean done=false;
    private boolean valid=true;
    private List<DictItem> map;
    private boolean showToken=false;

    public static List<Paragraph> getRaw() {
        return raw;
    }

    private boolean showPOS=false;
    private boolean showLemma=false;
    private int progress;

    private static Article INSTANCE;
    private static String config;

    private Article(String sentStreamName, String tokenStreamName, String posStreamName, String lemmaStreamName) {
        this.raw = new ArrayList<>();
        this.sentStreamName = sentStreamName;
        this.tokenStreamName = tokenStreamName;
        this.posStreamName = posStreamName;
        this.lemmaStreamName = lemmaStreamName;
        this.map=new ArrayList<>();
        this.progress=0;
    }

    /**
     * name a specific language for tokenizer...
     * @param c
     * @return
     */
    public static Article getINSTANCE(String c) {
        config=c;
        if (INSTANCE==null){
            try(BufferedReader bf=new BufferedReader(new FileReader(Article.class.getResource(config).getPath()))){
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

    /**
     * simply get an instance
     * @return
     */
    public static Article getINSTANCE() {
        if (INSTANCE==null){
            try(BufferedReader bf=new BufferedReader(new FileReader(Article.class.getResource(config).getPath()))){
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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
                this.updateProgress((this.raw.indexOf(p)*100/this.raw.size())+(p.ratioOf(s)/this.raw.size()));
            }
        }
        done=true;
    }

    private void updateProgress(int progress){
        this.progress=progress;
    }

    public int getProgress() {
        return progress;
    }

    public Sentence sentenceAt(int indexOutside, int indexInside){
        return this.raw.get(indexOutside).get(indexInside);
    }

    public boolean isDone() {
        return done;
    }

    /**
    @Deprecated
    public String toString(boolean showToken,boolean showPOS,boolean showLemma){
        this.showToken=showToken;
        this.showPOS=showPOS;
        this.showLemma=showLemma;
        this.map=new ArrayList<>();
        String res="";
        boolean odd=false;
        for (Paragraph p:this.raw){
            if (!p.getTag().equals("title")) {
                for (Sentence s:p){
                    DictItem temp=new DictItem(s);
                    String buffer="";
                    int length=0;
                    buffer+=String.format("<div style=\"background-color:%1$s;\">",(odd=!odd) ? "#668cff" : "#6fdcdc");
                    buffer+=String.format("<%1$s>%2$s</%1$s>",p.getTag(),s.getContent());
                    length+=s.getContent().length();
                    temp.setRangeContent(this.map.size()==0 ? 1 : this.map.get(this.map.size()-1).getEndingIndex(),length);
                    if (this.showToken){
                        buffer+=String.format("<%1$s style=\"color:green;\">TOKEN: ",p.getTag());
                        int l=7;
                        for (String i:s.getTokens()){
                            buffer+=i+", ";
                            l+=(i.length()+2);
                        }
                        temp.setRangeToken(l);
                        length+=l;
                        buffer+=String.format("</%1$s>\n",p.getTag());
                    }
                    if (this.showPOS){
                        buffer+=String.format("<%1$s style=\"color:blue;\">POS: ",p.getTag());
                        int l=5;
                        for (String i:s.getPos()){
                            buffer+=i+", ";
                            l+=(i.length()+2);
                        }
                        temp.setRangePOS(l);
                        length+=l;
                        buffer+=String.format("</%1$s>\n",p.getTag());
                    }
                    if (this.showLemma){
                        buffer+=String.format("<%1$s style=\"color:red;\">LEMMA: ",p.getTag());
                        int l=7;
                        for (String i:s.getLemmas()){
                            buffer+=i+", ";
                            l+=(i.length()+2);
                        }
                        temp.setRangeLemma(l);
                        length+=l;
                        buffer+=String.format("</%1$s>\n",p.getTag());
                    }
                    buffer+="</div>";
                    res+=buffer;
                    temp.setRange(this.map.size()==0 ? 1 : this.map.get(this.map.size()-1).getEndingIndex(),length);
                    this.map.add(temp);
                }
            }
        }
        return res;
    }
    **/

    public void setShowToken(boolean showToken,boolean showPOS,boolean showLemma) {
        this.showToken = showToken;
        this.showPOS = showPOS;
        this.showLemma = showLemma;
    }

    public Sentence[] getAllSentence(Article a){
        List<Sentence> result=new ArrayList<>();
        for (Paragraph p:a){
            if (!p.getTag().equals("title")){
                for (Sentence s:p){
                    s.setTag(p.getTag());
                    s.setShowToken(this.showToken);
                    s.setShowPOS(this.showPOS);
                    s.setShowLemma(this.showLemma);
                    result.add(s);
                }
            }
        }
        return result.toArray(new Sentence[0]);
    }
    /**
    public String popupString(int caret){
        Sentence temp=null;
        String buffer="";
        for (DictItem d:this.map){
            if (caret<=d.getEndingIndex()){
                temp=d.getSentence();
                break;
            }
        }
        System.out.println(caret);
        if (temp!=null){
            if (!this.showToken){
                buffer+=String.format("<%1$s style=\"color:green;\">TOKEN: ","p");
                for (String i:temp.getTokens()){
                    buffer+=i+", ";
                }
                buffer+=String.format("</%1$s>\n","p");
            }
            if (!this.showPOS){
                buffer+=String.format("<%1$s style=\"color:blue;\">POS: ","p");
                for (String i:temp.getPos()){
                    buffer+=i+", ";
                }
                buffer+=String.format("</%1$s>\n","p");
            }
            if (!this.showLemma){
                buffer+=String.format("<%1$s style=\"color:red;\">LEMMA: ","p");
                for (String i:temp.getLemmas()){
                    buffer+=i+", ";
                }
                buffer+=String.format("</%1$s>\n","p");
            }
        }
        return temp==null ? null : buffer;
    }
     **/

    public void toXML(Element db,Article art){
        Element a=db.appendElement("article");
        Element u=a.appendElement("url");
        u.text(url);
        for (Sentence sentence:this.getAllSentence(art)){
            Element sentTemp=a.appendElement("sentence");
            sentTemp.text(sentence.getContent());
            sentence.toXML(sentTemp);
        }
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

    private class DictItem{
        private Sentence sentence;
        private int[] range;
        private int[] rangeContent;
        private int[] rangeToken;
        private int[] rangePOS;
        private int[] rangeLemma;

        public DictItem(Sentence sentence) {
            this.sentence = sentence;
        }

        public void setRange(int beginIndex,int length){
            range= new int[]{beginIndex + 1,beginIndex+1+length};
        }

        public int getEndingIndex() {
            return range[1];
        }

        public void setRangeContent(int beginIndex,int length) {
            this.rangeContent = new int[]{beginIndex+1,beginIndex+1+length};
        }

        public void setRangeToken(int length) {
            this.rangeToken = new int[]{this.rangeContent[1]+(length==0? 0:1),this.rangeContent[1]+length+(length==0? 0:1)};
        }

        public void setRangePOS(int length) {
            this.rangePOS = new int[]{this.rangeToken[1]+(length==0? 0:1),this.rangeToken[1]+length+(length==0? 0:1)};
        }

        public void setRangeLemma(int length) {
            this.rangeLemma = new int[]{this.rangePOS[1]+(length==0? 0:1),this.rangePOS[1]+length+(length==0? 0:1)};
        }

        public Sentence getSentence() {
            return sentence;
        }
    }
}