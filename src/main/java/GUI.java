import org.apache.batik.swing.JSVGCanvas;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;

public class GUI {

    private static String langConfig;
    private static Clipboard clipboard;

    private static JMenuItem languageEN=new JMenuItem("");
    private static JMenuItem languageDE=new JMenuItem("");
    private static JMenuItem languageFR=new JMenuItem("");

    private static JPopupMenu popupMenu=new JPopupMenu("edit");
    private static JMenuItem cut = new JMenuItem("cut");
    private static JMenuItem copy = new JMenuItem("copy");
    private static JMenuItem paste = new JMenuItem("paste");
    private static JMenuItem empty = new JMenuItem("empty");

    private static Popup popup;
    private static PopupFactory popupFactory=new PopupFactory();
    private static JEditorPane popupContent=new JEditorPane();

    private static JTextField wikiUrl=new JTextField("paste your wiki url here!",20);
    private static JButton entryButton=new JButton("GO!");
    private static JEditorPane result=new JEditorPane();
    private static JScrollPane resultScroll;
    private static JProgressBar progressBar;

    private static JPanel searchBarPanel=new JPanel();
    private static JTextField searchBar;
    private static JButton regexButton;
    private static JButton shutSearchButton;

    public static void main(String[] args) {

        ArrayList<Article> articles=new ArrayList<>();
        final Timer[] timer = new Timer[1];
        getSettings();
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
        AppendMouseHover amh=new AppendMouseHover(result);

        JFrame frame=new JFrame("Wiki Spider");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar=new JMenuBar();
        JMenu menuSettings=new JMenu("Settings");
        menuBar.add(menuSettings);

        popupContent.setContentType("text/html");

        JMenu languageMenu=new JMenu("change the language for tokenizer");
        languageEN.setText((langConfig.equals("config-EN")?"✓":"")+"EN");
        languageDE.setText((langConfig.equals("config-DE")?"✓":"")+"DE");
        languageFR.setText((langConfig.equals("config-FR")?"✓":"")+"FR");
        languageEN.setActionCommand("EN");
        languageDE.setActionCommand("DE");
        languageFR.setActionCommand("FR");
        languageEN.addActionListener(new ChangeConfig());
        languageDE.addActionListener(new ChangeConfig());
        languageFR.addActionListener(new ChangeConfig());
        languageMenu.add(languageEN);
        languageMenu.add(languageDE);
        languageMenu.add(languageFR);
        menuSettings.add(languageMenu);

        popupMenu.add(copy);
        popupMenu.add(cut);
        popupMenu.add(paste);
        popupMenu.add(empty);

        Container container=frame.getContentPane();
        GroupLayout groupLayout=new GroupLayout(container);
        container.setLayout(groupLayout);

        JPanel inputPanel=new JPanel();

        progressBar=new JProgressBar(0,100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("PENDING");
        inputPanel.add(wikiUrl);
        inputPanel.add(entryButton);
        inputPanel.add(progressBar);
        GroupLayout inputLayout=new GroupLayout(inputPanel);
        inputPanel.setLayout(inputLayout);
        inputLayout.setHorizontalGroup(
                inputLayout.createParallelGroup().
                        addGroup(inputLayout.createSequentialGroup().addComponent(wikiUrl).addComponent(entryButton)).
                        addGroup(inputLayout.createSequentialGroup().addComponent(progressBar)));
        inputLayout.setVerticalGroup(
                inputLayout.createSequentialGroup().
                        addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                                        addComponent(wikiUrl).addComponent(entryButton)).
                        addComponent(progressBar));

        JPanel outputPanel=new JPanel();

        result.setContentType("text/html");
        result.setFocusable(true);
        result.setEditable(false);
        resultScroll=new JScrollPane(result);
        outputPanel.add(resultScroll);
        GroupLayout outputLayout=new GroupLayout(outputPanel);
        outputPanel.setLayout(outputLayout);

        result.addMouseListener(new ShowMenu());

        searchBar=new JTextField("",20);

        regexButton=new JButton();
        regexButton.setToolTipText("apply regex search");
        shutSearchButton=new JButton();
        shutSearchButton.setToolTipText("close search bar");

        ImageIcon regexIcon=new ImageIcon(GUI.class.getResource("regex.png"));
        ImageIcon regexPressedIcon=new ImageIcon(GUI.class.getResource("regex_pressed.png"));
        ImageIcon shutIcon=new ImageIcon(GUI.class.getResource("shut.png"));
        regexButton.setIcon(regexIcon);
        shutSearchButton.setIcon(shutIcon);
        searchBar.setPreferredSize(new Dimension(300,28));
        searchBarPanel.add(searchBar);
        searchBarPanel.add(regexButton);
        searchBarPanel.add(shutSearchButton);
        FlowLayout searchLayout=new FlowLayout();
        GroupLayout searchBarLayout=new GroupLayout(searchBarPanel);
        searchBarPanel.setLayout(searchBarLayout);
        searchBarLayout.setHorizontalGroup(searchBarLayout.createSequentialGroup().
                addComponent(searchBar).addComponent(regexButton).addComponent(shutSearchButton));
        searchBarLayout.setVerticalGroup(searchBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(searchBar).addComponent(regexButton).addComponent(shutSearchButton).
                addGroup(searchBarLayout.createBaselineGroup(false,false)));
        searchBarPanel.setVisible(false);;

        outputLayout.setHorizontalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));
        outputLayout.setVerticalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));

        result.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F,KeyEvent.CTRL_MASK),"SEARCH");
        result.getActionMap().put("SEARCH", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBarPanel.setVisible(true);
                searchBar.requestFocus();
                amh.forceCease();
            }
        });

        JPanel combinedPanel=new JPanel();
        combinedPanel.add(inputPanel);
        combinedPanel.add(outputPanel);
        GroupLayout combinedLayout=new GroupLayout(combinedPanel);
        combinedPanel.setLayout(combinedLayout);
        combinedLayout.setHorizontalGroup(
                combinedLayout.createParallelGroup().
                        addComponent(inputPanel).addComponent(searchBarPanel).addComponent(outputPanel));
        combinedLayout.setVerticalGroup(
                combinedLayout.createSequentialGroup().
                        addComponent(inputPanel).addComponent(searchBarPanel).addComponent(outputPanel));

        JPanel checkboxPanel=new JPanel();
        JCheckBox isTokenShow=new JCheckBox("show tokens?");
        JCheckBox isPosShow=new JCheckBox("show POS?");
        JCheckBox isLemmaShow=new JCheckBox("show lemmas?");
        checkboxPanel.add(isTokenShow);
        checkboxPanel.add(isPosShow);
        checkboxPanel.add(isLemmaShow);
        GroupLayout checkboxLayout=new GroupLayout(checkboxPanel);
        checkboxPanel.setLayout(checkboxLayout);
        checkboxLayout.setHorizontalGroup(
                checkboxLayout.createParallelGroup().
                        addComponent(isTokenShow).addComponent(isPosShow).addComponent(isLemmaShow));
        checkboxLayout.setVerticalGroup(
                checkboxLayout.createSequentialGroup().
                        addComponent(isTokenShow).addComponent(isPosShow).addComponent(isLemmaShow));

        groupLayout.setHorizontalGroup(
                groupLayout.createSequentialGroup().
                        addComponent(combinedPanel).addComponent(checkboxPanel));
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup().
                        addComponent(combinedPanel).addComponent(checkboxPanel));

        wikiUrl.addMouseListener(new ShowMenu());
        wikiUrl.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,KeyEvent.SHIFT_MASK),"GO");
        wikiUrl.getActionMap().put("GO", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("GO===============");
                try {
                    NetWork netWork=new NetWork(wikiUrl.getText(),langConfig);
                    netWork.start();
                    amh.dismiss();
                    ActionListener updateOutput=new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Article article=Article.getINSTANCE();
                            if (articles.size()==0){
                                articles.add(article);
                            }
                            else if (article!=articles.get(articles.size()-1)) {
                                articles.add(article);
                            }
                            if (!(article.isValid())){
                                result.setText("");
                                timer[0].stop();    //show a warning!
                            }
                            if (articles.get(articles.size()-1).isDone()){
                                System.out.println("DONE!!!");
                                JList<String> list=new JList<>();
                                result.setText(article.toString(
                                        isTokenShow.isSelected(),isPosShow.isSelected(),isLemmaShow.isSelected()));
                                timer[0].stop();
                                frame.repaint();
                                //result.addMouseMotionListener(new MouseHover(result));
                                if (result.getCaretListeners().length==2){
                                    amh.trigger();
                                    result.addMouseListener(amh);
                                }
                                progressBar.setValue(100);
                                progressBar.setString("DONE");
                            }
                            else {
                                progressBar.setValue(article.getProgress());
                                progressBar.setString("PROCESSING");
                            }
                        }
                    };
                    timer[0] =new Timer(250,updateOutput);
                    timer[0].start();
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });

        wikiUrl.setForeground(Color.gray);
        wikiUrl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (wikiUrl.getText().equals("paste your wiki url here!")){
                    wikiUrl.setText("");
                    wikiUrl.setForeground(Color.black);
                }
            }
        });

        entryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("GO===============");
                try {
                    NetWork netWork=new NetWork(wikiUrl.getText(),langConfig);
                    netWork.start();
                    amh.dismiss();
                    ActionListener updateOutput=new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Article article=Article.getINSTANCE();
                            if (articles.size()==0){
                                articles.add(article);
                            }
                            else if (article!=articles.get(articles.size()-1)) {
                                articles.add(article);
                            }
                            if (!(article.isValid())){
                                result.setText("");
                                timer[0].stop();    //show a warning!
                            }
                            if (articles.get(articles.size()-1).isDone()){
                                System.out.println("DONE!!!");
                                JList<String> list=new JList<>();
                                result.setText(article.toString(
                                        isTokenShow.isSelected(),isPosShow.isSelected(),isLemmaShow.isSelected()));
                                timer[0].stop();
                                frame.repaint();
                                //result.addMouseMotionListener(new MouseHover(result));
                                if (result.getCaretListeners().length==2){
                                    amh.trigger();
                                    result.addMouseListener(amh);
                                    /**result.addCaretListener(new CaretListener() {
                                        @Override
                                        public void caretUpdate(CaretEvent e) {
                                            Article article=Article.getINSTANCE();
                                            String toUpdate=article.popupString(result.getCaretPosition());
                                            popupContent.setText(toUpdate==null ? "":toUpdate);
                                            popup=popupFactory.getPopup(result,popupContent,
                                                    MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
                                            if (toUpdate!=null) {
                                                popup.show();
                                            }
                                        }
                                    });**/
                                }
                                progressBar.setValue(100);
                                progressBar.setString("DONE");
                            }
                            else {
                                progressBar.setValue(article.getProgress());
                                progressBar.setString("PROCESSING");
                            }
                        }
                    };
                    timer[0] =new Timer(500,updateOutput);
                    timer[0].start();
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });

        shutSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchBarPanel.setVisible(false);
            }
        });

        regexButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (regexButton.getIcon()==regexIcon){
                    regexButton.setIcon(regexPressedIcon);
                }
                else {
                    regexButton.setIcon(regexIcon);
                }
            }
        });

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update(){
                Article article=Article.getINSTANCE();

                java.util.List<int[]> render=new ArrayList<>();
                try {
                    render=article.find(
                            searchBar.getText(),regexButton.getIcon()==regexPressedIcon,
                            result.getDocument().getText(0,result.getDocument().getLength()));
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
                Highlighter highlighter=result.getHighlighter();
                highlighter.removeAllHighlights();

                Highlighter.HighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                for (int[] pair:render){
                    try{
                        highlighter.addHighlight(pair[0],pair[1],painter);
                    }
                    catch (Exception exception){
                        exception.printStackTrace();
                    }
                }
            }
        });

        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setVisible(true);
    }

    private static void getSettings(){
        try(BufferedReader br=new BufferedReader(new FileReader(GUI.class.getResource("config").getPath()))){
            String  buffer;
            while ((buffer=br.readLine())!=null){
                String[] dict=buffer.split("=");
                if (dict[0].equals("language")){
                    langConfig=dict[1];
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class ChangeConfig implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            try (PrintWriter pw=new PrintWriter(new FileOutputStream(getClass().getResource("config").getPath(),
                    false));)
            {
                pw.write(String.format("language=config-%1$s",e.getActionCommand()));
                pw.close();
                getSettings();
                languageEN.setText((langConfig.equals("config-EN")?"✓":"")+"EN");
                languageDE.setText((langConfig.equals("config-DE")?"✓":"")+"DE");
                languageFR.setText((langConfig.equals("config-FR")?"✓":"")+"FR");
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    private static class AppendMouseHover extends MouseAdapter{
        private JEditorPane component;
        private MouseHover hoverF;
        private UpdateCaret updateCaret;

        public AppendMouseHover(JEditorPane component) {
            this.component=component;

            this.updateCaret=new UpdateCaret(this.component,true);
        }

        public void trigger(){
            this.hoverF=new MouseHover(this.component);

            this.component.addMouseMotionListener(this.hoverF);
            this.component.addCaretListener(this.updateCaret);
        }

        public void dismiss(){
            this.component.removeMouseMotionListener(this.hoverF);
            this.component.removeCaretListener(this.updateCaret);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.component.removeMouseMotionListener(this.hoverF);
            this.updateCaret.setStatus(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.component.addMouseMotionListener(this.hoverF);
            updateCaret.setStatus(true);
            /**Timer temp= new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateCaret.setStatus(true);
                }
            });
            temp.start();**/
        }

        @Override
        public void mouseExited(MouseEvent e) {
            try{
                this.hoverF.forceCease();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void forceCease(){
            try {
                this.hoverF.forceCease();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        private class UpdateCaret implements CaretListener{

            JEditorPane c;
            boolean status;

            public UpdateCaret(JEditorPane c,boolean status) {
                this.c = c;
                this.status=status;
            }

            public void setStatus(boolean status) {
                this.status = status;
            }

            public boolean isStatus() {
                return status;
            }

            @Override
            public void caretUpdate(CaretEvent e) {
                if (this.c.getSelectedText()==null){
                    Article article=Article.getINSTANCE();
                    String toUpdate=article.popupString(result.getCaretPosition());
                    popupContent.setText(toUpdate==null ? "":toUpdate);
                    popup=popupFactory.getPopup(result,popupContent,
                            MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
                    if (toUpdate!=null&&(!toUpdate.equals(""))) {
                        popup.show();
                    }
                }
            }
        }
    }

    private static class MouseHover extends MouseMotionAdapter{
        private JEditorPane component;
        final Timer[] timerMouseMotion=new Timer[1];

        public MouseHover(JEditorPane component) {
            super();
            this.component=component;
        }

        public void forceCease(){
            try {
                timerMouseMotion[0].stop();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                timerMouseMotion[0].stop();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (popup!=null){
                popup.hide();
            }
            try {
                Robot bot =new Robot();
                ActionListener simulation=new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    }
                };
                if (timerMouseMotion[0]!=null){
                    timerMouseMotion[0].stop();
                }
                timerMouseMotion[0]=new Timer(1500,simulation);
                timerMouseMotion[0].setRepeats(false);
                if (this.component.getSelectedText()==null&&(!searchBarPanel.isVisible())){
                    timerMouseMotion[0].start();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class ShowMenu extends MouseAdapter{

        @Override
        public void mousePressed(MouseEvent e) {
            showMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showMenu(e);
        }

        public void showMenu(MouseEvent e) {
            for (ActionListener a:copy.getActionListeners()){
                copy.removeActionListener(a);
            }
            for (ActionListener a:cut.getActionListeners()){
                cut.removeActionListener(a);
            }
            for (ActionListener a:paste.getActionListeners()){
                paste.removeActionListener(a);
            }
            for (ActionListener a:empty.getActionListeners()){
                empty.removeActionListener(a);
            }
            copy.addActionListener(new EditText((JTextComponent) e.getComponent()));
            cut.addActionListener(new EditText((JTextComponent) e.getComponent()));
            paste.addActionListener(new EditText((JTextComponent) e.getComponent()));
            empty.addActionListener(new EditText((JTextComponent) e.getComponent()));
            if(e.isPopupTrigger()){
                if (e.getSource()==result){
                    cut.setEnabled(false);
                    paste.setEnabled(false);
                    empty.setEnabled(false);
                }
                else {
                    cut.setEnabled(true);
                    paste.setEnabled(true);
                    copy.setEnabled(true);
                    empty.setEnabled(true);
                }
                popupMenu.show(e.getComponent(),e.getX(),e.getY());
            }
        }
    }

    private static class EditText implements ActionListener{
        private JTextComponent component;

        public EditText(JTextComponent component) {
            this.component=component;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("cut")){
                clipboard.setContents(new StringSelection(this.component.getSelectedText()),null);
                this.component.replaceSelection("");
            }
            if (e.getActionCommand().equals("paste")){
                try{
                    this.component.replaceSelection((String) clipboard.getData(DataFlavor.stringFlavor));
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            if (e.getActionCommand().equals("copy")){
                clipboard.setContents(new StringSelection(this.component.getSelectedText()),null);
            }
            if (e.getActionCommand().equals("empty")){
                this.component.setText("");
            }
        }
    }
}