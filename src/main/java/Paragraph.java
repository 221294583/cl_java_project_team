import opennlp.tools.sentdetect.SentenceDetectorME;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Paragraph implements Iterable<Sentence>{
    private String tag;
    private String content;
    private List<Sentence> sentences;

    public Paragraph(String tag, String content) {
        this.tag = tag;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "tag='" + tag + '\'' +
                ", content='" + content + '\'' +
                ", sentences=" + sentences +
                "}\n";
    }

    public void preProcess(SentenceDetectorME sd){
        this.sentences= Stream.of(sd.sentDetect(this.content)).map(temp -> {
            try {
                return new Sentence(temp);
            } catch (KException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    public String getTag() {
        return tag;
    }

    public String getContent() {
        return content;
    }

    public Sentence get(int num){
        return this.sentences.get(num);
    }

    @Override
    public Iterator<Sentence> iterator() {
        return this.sentences.iterator();
    }

    public int ratioOf(Sentence s){
        return this.sentences.indexOf(s)*100/this.sentences.size();
    }
}
