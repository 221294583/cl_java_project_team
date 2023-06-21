import opennlp.tools.util.Span;
import org.jsoup.nodes.Element;

public class Word {
    private String token;
    private String POS;
    private String lemma;
    private Span span;
    private boolean hasMatch=false;
    private int[][] offset;
    private Word nextWord;

    public Word(String token, String POS, String lemma,Span span) {
        this.token = token;
        this.POS = POS;
        this.lemma = lemma;
        this.span=span;
    }

    public void setNextWord(Word nextWord) {
        this.nextWord = nextWord;
    }

    public Word getNextWord() {
        return nextWord;
    }

    public void setHasMatch(boolean hasMatch) {
        this.hasMatch = hasMatch;
    }

    public boolean hasMatch() {
        return hasMatch;
    }

    public Span getSpan() {
        return span;
    }

    public int[][] getOffset() {
        return offset;
    }

    public void setOffset(int[] offset) {
        this.offset =new int[3][];
        this.offset[0]=new int[]{offset[0],offset[0]+this.token.length()+2};
        this.offset[1]=new int[]{offset[1],offset[1]+this.POS.length()+2};
        this.offset[2]=new int[]{offset[2],offset[2]+this.lemma.length()+2};
        if (this.nextWord!=null){
            this.nextWord.setOffset(new int[]{this.offset[0][1],this.offset[1][1],this.offset[2][1]});
        }
    }
    public String[] toString(boolean toCopy) {
        String[] result=new String[]{"","",""};
        if (toCopy){
            result[0]+=(this.token+", ");
            result[1]+=(this.POS+", ");
            result[2]+=(this.lemma+", ");
        }
        else {
            result[0]+=String.format(
                    "<b>%1$s</b>", this.token+", ");
            result[1]+=String.format(
                    "<b>%1$s</b>", this.POS+", ");
            result[2]+=String.format(
                    "<b>%1$s</b>", this.lemma+", ");
        }
        return result;
    }

    public void toXML(Element wNode){
        Element t=wNode.appendElement("token");
        t.text(this.token);
        Element p=wNode.appendElement("pos");
        p.text(this.POS);
        Element l=wNode.appendElement("lemma");
        l.text(this.lemma);
    }
}
