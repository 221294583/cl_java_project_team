import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class CellView extends JEditorPane implements ListCellRenderer<Sentence> {

    private List<List<int[]>> highlightRange;
    private int font;

    public CellView(int font) {
        super();
        this.highlightRange=null;
        this.font=font;
        setContentType("text/html");
        setEditable(false);
    }

    public void setHighlightRange(List<List<int[]>> highlightRange) {
        this.highlightRange = highlightRange;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Sentence> list, Sentence value, int index, boolean isSelected, boolean cellHasFocus) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
            }
        });

        setText(value.toString(index,this.font));
        /**
        if (isSelected){
            setBackground(Color.MAGENTA);
        }
        else {
            if (index%2==1){
                setBackground(Color.gray);
            }
            else {
                setBackground(Color.CYAN);
            }
        }**/
        if (this.highlightRange!=null){
            Highlighter highlighter=this.getHighlighter();
            highlighter.removeAllHighlights();
            Highlighter.HighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
            for (int[] pair:this.highlightRange.get(index)){
                try {
                    highlighter.addHighlight(pair[0]+1, pair[1]+1, painter);
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }
            Word cur=value.getHead();
            while (cur!=null){
                if (cur.hasMatch()){
                    int[][] tmp=cur.getOffset();
                    try {
                        highlighter.addHighlight(tmp[0][0]+2,tmp[0][1],painter);
                        highlighter.addHighlight(tmp[1][0]+2,tmp[1][1],painter);
                        highlighter.addHighlight(tmp[2][0]+2,tmp[2][1],painter);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                cur=cur.getNextWord();
            }
        }
        return this;
    }
}
