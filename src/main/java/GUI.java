import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
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

    public static void main(String[] args) {

        ArrayList<Article> articles=new ArrayList<>();
        final Timer[] timer = new Timer[1];
        getSettings();
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();

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

        inputPanel.add(wikiUrl);
        inputPanel.add(entryButton);
        GroupLayout inputLayout=new GroupLayout(inputPanel);
        inputPanel.setLayout(inputLayout);
        inputLayout.setHorizontalGroup(
                inputLayout.createSequentialGroup().addComponent(wikiUrl).addComponent(entryButton));
        inputLayout.setVerticalGroup(
                inputLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                        addComponent(wikiUrl).addComponent(entryButton)
        );
        wikiUrl.addMouseListener(new ShowMenu());

        JPanel outputPanel=new JPanel();

        result.setContentType("text/html");
        result.setFocusable(true);
        result.setEditable(false);
        resultScroll=new JScrollPane(result);
        outputPanel.add(resultScroll);
        GroupLayout outputLayout=new GroupLayout(outputPanel);
        outputPanel.setLayout(outputLayout);
        outputLayout.setHorizontalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));
        outputLayout.setVerticalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));

        result.addMouseListener(new ShowMenu());

        JPanel combinedPanel=new JPanel();
        combinedPanel.add(inputPanel);
        combinedPanel.add(outputPanel);
        GroupLayout combinedLayout=new GroupLayout(combinedPanel);
        combinedPanel.setLayout(combinedLayout);
        combinedLayout.setHorizontalGroup(
                combinedLayout.createParallelGroup().
                        addComponent(inputPanel).addComponent(outputPanel));
        combinedLayout.setVerticalGroup(
                combinedLayout.createSequentialGroup().
                        addComponent(inputPanel).addComponent(outputPanel));

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
                try {
                    NetWork netWork=new NetWork(wikiUrl.getText(),langConfig);
                    netWork.start();
                    ActionListener updateOutput=new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            for (MouseMotionListener m:result.getMouseMotionListeners()){
                                result.removeMouseMotionListener(m);
                            }
                            for (CaretListener c:result.getCaretListeners()){
                                result.removeCaretListener(c);
                            }
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
                                JList<String> list=new JList<>();
                                result.setText(article.toString(
                                        isTokenShow.isSelected(),isPosShow.isSelected(),isLemmaShow.isSelected()));
                                timer[0].stop();
                                result.addMouseMotionListener(new MouseHover(result));
                                result.addCaretListener(new CaretListener() {
                                    @Override
                                    public void caretUpdate(CaretEvent e) {
                                        System.out.println("caret event");
                                        Article article=Article.getINSTANCE();
                                        String toUpdate=article.popupString(result.getCaretPosition());
                                        popupContent.setText(toUpdate==null ? "":toUpdate);
                                        popup=popupFactory.getPopup(result,popupContent,
                                                MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y);
                                        if (toUpdate!=null) {
                                            popup.show();
                                        }
                                        System.out.println("================");
                                    }
                                });
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

    private static class MouseHover extends MouseMotionAdapter{
        private JEditorPane component;
        final Timer[] timerMouseMotion=new Timer[1];

        public MouseHover(JEditorPane component) {
            super();
            this.component=component;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            System.out.println("mouse motion");
            System.out.println(this.component.getCaret());
            try {
                popup.hide();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            try {
                Robot bot =new Robot();
                ActionListener simulation=new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("simulation!");
                        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    }
                };
                if (timerMouseMotion[0]!=null){
                    System.out.println("cancelled");
                    timerMouseMotion[0].stop();
                }
                timerMouseMotion[0]=new Timer(1500,simulation);
                timerMouseMotion[0].setRepeats(false);
                timerMouseMotion[0].start();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("=======================");
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