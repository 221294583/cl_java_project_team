import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CellView extends JEditorPane implements ListCellRenderer<Sentence> {

    private List<List<int[]>> highlightRange;

    public CellView() {
        super();
        this.highlightRange=null;
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

        setText(value.toString(index));
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
                    highlighter.addHighlight(pair[0], pair[1], painter);
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }
        return this;
    }
}
