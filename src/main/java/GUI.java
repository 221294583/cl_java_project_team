import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUI {

    private static java.util.List<ArticleRoot> articles=new ArrayList<>();

    private static String langConfig;
    private static int font;
    private static Clipboard clipboard;

    private static JFrame frame=new JFrame("Wiki Spider");

    private static JMenuBar menuBar=new JMenuBar();
    private static JMenu menuSettings=new JMenu("Settings");
    private static JMenuItem languageEN=new JMenuItem("");
    private static JMenuItem languageDE=new JMenuItem("");
    private static JMenuItem languageFR=new JMenuItem("");
    private static CFileChooser fileChooser;
    private static JMenuItem saveXML=new JMenuItem("save to XML file");
    private static JMenuItem changeFont=new JMenuItem("change font");

    private static JPopupMenu popupMenu=new JPopupMenu("edit");
    private static JMenuItem cut = new JMenuItem("cut");
    private static JMenuItem copy = new JMenuItem("copy");
    private static JMenuItem paste = new JMenuItem("paste");
    private static JMenuItem empty = new JMenuItem("empty");

    private static Popup popup;
    private static PopupFactory popupFactory=new PopupFactory();
    private static JEditorPane popupContent=new JEditorPane();

    private static JTextField wikiUrl=new JTextField("paste your wiki url here!",19);
    private static JButton entryButton=new JButton("GO!");
    private static DefaultListModel<Sentence> listModel=new DefaultListModel<>();
    private static JList<Sentence> resultList=new JList<>(listModel);
    private static CellView cellRenderer;
    private static JScrollPane resultScroll=new JScrollPane(resultList);
    private static JPanel outputPanel=new JPanel();

    private static JProgressBar progressBar;

    private static JPanel searchBarPanel=new JPanel();
    private static JTextField searchBar;
    private static JButton relationButton;
    private static JButton regexButton;
    private static JButton shutSearchButton;
    private static ImageIcon relationIcon;
    private static ImageIcon relationPressedIcon;
    private static ImageIcon regexIcon;
    private static ImageIcon regexPressedIcon;
    private static ImageIcon shutIcon;


    private static JCheckBox isTokenShow=new JCheckBox("show tokens?");
    private static JCheckBox isPosShow=new JCheckBox("show POS?");
    private static JCheckBox isLemmaShow=new JCheckBox("show lemmas?");

    public static void main(String[] args) {


        final Timer[] timer = new Timer[1];
        getSettings();
        cellRenderer=new CellView(font);
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        menuSettings.add(saveXML);
        menuSettings.add(changeFont);
        fileChooser=new CFileChooser(System.getProperty("user.dir"));

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

        wikiUrl.setFont(new Font("Serif",Font.BOLD,font));
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

        AppendMouseHover amh=new AppendMouseHover();
        outputPanel.add(resultScroll);
        GroupLayout outputLayout=new GroupLayout(outputPanel);
        outputPanel.setLayout(outputLayout);

        resultList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F,KeyEvent.CTRL_MASK),"SEARCH");
        resultList.getActionMap().put("SEARCH", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBarPanel.setVisible(true);
                searchBar.requestFocus();
            }
        });
        resultList.addMouseListener(new ShowMenu());

        searchBar=new JTextField("",20);

        searchBar.addMouseListener(new ShowMenu());

        relationButton=new JButton();
        relationButton.setToolTipText("text only");
        regexButton=new JButton();
        regexButton.setToolTipText("apply regex search");
        shutSearchButton=new JButton();
        shutSearchButton.setToolTipText("close search bar");

        relationIcon=new ImageIcon(GUI.class.getResource("global.png"));
        relationPressedIcon=new ImageIcon(GUI.class.getResource("text_only.png"));
        regexIcon=new ImageIcon(GUI.class.getResource("regex.png"));
        regexPressedIcon=new ImageIcon(GUI.class.getResource("regex_pressed.png"));
        shutIcon=new ImageIcon(GUI.class.getResource("shut.png"));

        relationButton.setIcon(relationIcon);
        regexButton.setIcon(regexIcon);
        shutSearchButton.setIcon(shutIcon);
        searchBar.setPreferredSize(new Dimension(300,28));
        searchBarPanel.add(searchBar);
        searchBar.add(regexButton);
        searchBarPanel.add(regexButton);
        searchBarPanel.add(shutSearchButton);
        FlowLayout searchLayout=new FlowLayout();
        GroupLayout searchBarLayout=new GroupLayout(searchBarPanel);
        searchBarPanel.setLayout(searchBarLayout);
        searchBarLayout.setHorizontalGroup(searchBarLayout.createSequentialGroup().
                addComponent(searchBar).addComponent(relationButton).addComponent(regexButton).addComponent(shutSearchButton));
        searchBarLayout.setVerticalGroup(searchBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(searchBar).addComponent(relationButton).addComponent(regexButton).addComponent(shutSearchButton).
                addGroup(searchBarLayout.createBaselineGroup(false,false)));
        searchBarPanel.setVisible(false);

        outputLayout.setHorizontalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));
        outputLayout.setVerticalGroup(outputLayout.createSequentialGroup().addComponent(resultScroll));

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

        UpdateOutput updateOutput=new UpdateOutput(timer,frame,amh);

        wikiUrl.addMouseListener(new ShowMenu());
        wikiUrl.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,KeyEvent.SHIFT_MASK),"GO");
        wikiUrl.getActionMap().put("GO", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("GO===============");
                try {
                    NetWork netWork=new NetWork(wikiUrl.getText(),langConfig);
                    netWork.start();

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
                    timer[0] =new Timer(250,new UpdateOutput(timer,frame,amh));
                    timer[0].start();
                }
                catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });

        saveXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Timer[] timers=new Timer[1];
                fileChooser.showSaveDialog(frame);
                timers[0]=new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int status=fileChooser.getStatus();
                        if (status!=0){
                            timers[0].stop();
                            if (status!=-1){
                                try {
                                    CreateXML xml = new CreateXML(status,
                                            fileChooser.getLastDir(),
                                            fileChooser.getFilename());
                                    if (!xml.check()){
                                        throw new KException("");
                                    }
                                    else {
                                        xml.save();
                                    }
                                }
                                catch (Exception ex){
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(frame,"INVALID PATH");
                                }
                            }
                        }
                    }
                });
                timers[0].start();
            }
        });

        changeFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Integer newFont=Integer.parseInt(JOptionPane.showInputDialog(frame,"give a number"));
                    System.out.println(newFont);
                    //InputStream inputStream=new
                }
                catch (Exception exception){
                    JOptionPane.showMessageDialog(frame,"Invalid number!");
                }
            }
        });

        shutSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchBarPanel.setVisible(false);
                cellRenderer.setHighlightRange(null);
                update(true);
            }
        });

        regexButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (regexButton.getIcon()==regexIcon){
                    regexButton.setIcon(regexPressedIcon);
                    regexButton.setToolTipText("dismiss regex search");
                }
                else {
                    regexButton.setIcon(regexIcon);
                    regexButton.setToolTipText("apply regex search");
                }
                update(false);
            }
        });

        relationButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (relationButton.getIcon()==relationIcon){
                    relationButton.setIcon(relationPressedIcon);
                    relationButton.setToolTipText("global");
                    cellRenderer.setGlobal(false);
                }
                else {
                    relationButton.setIcon(relationIcon);
                    relationButton.setToolTipText("text only");
                    cellRenderer.setGlobal(true);
                }
                update(false);
            }
        });

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(false);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update(false);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update(false);
            }
        });

        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setVisible(true);
    }

    private static void update(boolean cancel){
        Article article=Article.getINSTANCE();

        java.util.List<java.util.List<int[]>> render=new ArrayList<>();
        try {
            for (int i=0;i<listModel.getSize();i++){

                 render.add(listModel.getElementAt(i).find(
                 searchBar.getText(),
                 regexButton.getIcon()==regexPressedIcon,
                 relationButton.getIcon()==relationIcon));

            }
            cellRenderer.setHighlightRange(render);
            resultList.repaint();
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
    }

    private static class CreateXML{
        private Document doc=Document.createShell("");
        private int status;
        private String path;
        private String filename;

        public CreateXML(int status,String path,String filename) {
            this.status=status;
            this.path=path;
            this.filename=filename;
            doc.select("html").remove();
        }

        public boolean check(){
            Pattern pattern= Pattern.compile("[\\w_].*\\.xml");
            Matcher matcher= pattern.matcher(filename);
            if (matcher.find()){
                return true;
            }
            else {
                return false;
            }
        }

        public void save(){
            Element db=doc.appendElement("db");
            if (status==1){
                Article.getINSTANCE().toXML(db,Article.getINSTANCE());
            }
            else if (status==2) {
                for (ArticleRoot a:articles) {
                    a.toXML(db,a);
                }
            }

            try (PrintWriter pw=new PrintWriter(path+"\\"+filename,"UTF-8")){
                pw.write(doc.toString());
                System.out.println(doc.select("db").first());
            }
            catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    private static class AppendMouseHover extends MouseAdapter {
        private MouseHover mh;
        public AppendMouseHover() {
            System.out.println("AMH CREATE");
            mh=new MouseHover();
        }

        public void trigger(){
            resultList.addMouseMotionListener(mh);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (popup!=null){
                popup.hide();
            }
            resultList.removeMouseMotionListener(mh);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            resultList.addMouseMotionListener(mh);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            resultList.addMouseMotionListener(mh);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (popup!=null){
                popup.hide();
            }
            try{
                mh.forceCease();
            }
            catch (Exception exception){
                exception.printStackTrace();
            }
            resultList.removeMouseMotionListener(mh);
        }
    }

    private static class MouseHover extends MouseMotionAdapter{

        private Timer[] timer=new Timer[1];


        public MouseHover() {
            System.out.println("MOUSE HOVER CREATE");
        }

        public void forceCease(){
            try {
                timer[0].stop();
            }
            catch (Exception exception){
                exception.printStackTrace();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (popup!=null){
                popup.hide();
            }
            if (timer[0]!=null){
                timer[0].stop();
            }
            int length=resultList.getModel().getSize();
            Rectangle base=SwingUtilities.convertRectangle(resultList,resultList.getBounds(),frame);
            int cur=resultScroll.getVerticalScrollBar().getValue();
            for (int i=0;i<length;i++){
                Rectangle temp=new Rectangle(base.x+resultList.getCellBounds(i,i).x,
                        base.y+resultList.getCellBounds(i,i).y+cur,
                        (int) resultScroll.getViewportBorderBounds().getWidth(),
                        resultList.getCellBounds(i,i).height);
                if (temp.contains(MouseInfo.getPointerInfo().getLocation())){
                    timer[0]=new Timer(1500,new ShowPopup(i));
                    timer[0].setRepeats(false);
                    timer[0].start();
                    break;
                }
            }
        }
    }

    private static class ShowPopup implements ActionListener{
        private int index;

        public ShowPopup(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            popup=popupFactory.getPopup(resultList,popupContent,
                    MouseInfo.getPointerInfo().getLocation().x+10,
                    MouseInfo.getPointerInfo().getLocation().y+10);
            popupContent.setText(listModel.getElementAt(index).popupString());
            if (!searchBarPanel.isVisible()){
                System.out.println("POPUP");
                popup.show();
            }
        }
    }

    private static class UpdateOutput implements ActionListener {
        private Timer[] timer;
        private JFrame frame;
        private AppendMouseHover amh;

        public UpdateOutput(Timer[] timer, JFrame frame, AppendMouseHover amh) {
            this.timer = timer;
            this.frame = frame;
            this.amh = amh;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Article article=Article.getINSTANCE();
            if (articles.size()==0){
                articles.add(new ArticleRoot(article));
            }
            else if (!article.getUrl().equals(articles.get(articles.size()-1).getUrl())) {
                articles.add(new ArticleRoot(article));
            }
            if (!(article.isValid())){
                //result.setText("");
                timer[0].stop();    //show a warning!
            }
            if (article.isDone()){
                articles.get(articles.size()-1).addRaw(article);
                System.out.println("DONE!!!");
                listModel.removeAllElements();
                article.setShowToken(isTokenShow.isSelected(),isPosShow.isSelected(),isLemmaShow.isSelected());
                for (Sentence s:article.getAllSentence(article)){
                    listModel.addElement(s);
                }
                cellRenderer=new CellView(font);
                resultList.setCellRenderer(cellRenderer);
                resultList.repaint();

                timer[0].stop();
                frame.repaint();
                progressBar.setValue(100);
                progressBar.setString("DONE");
                if (!(resultList.getMouseListeners()[resultList.getMouseListeners().length-1]==amh)){
                    System.out.println("BIND");
                    resultList.addMouseListener(amh);
                    //resultList.addMouseMotionListener(new MouseHover());
                    amh.trigger();
                    /**for (MouseListener m:resultList.getMouseListeners()){
                        System.out.println(m);
                    }
                    System.out.println("------------");
                    for (MouseMotionListener m:resultList.getMouseMotionListeners()){
                        System.out.println(m);
                    }**/
                }
            }
            else {
                progressBar.setValue(article.getProgress());
                progressBar.setString("PROCESSING");
            }
        }
    };

    private static void getSettings(){
        try(BufferedReader br=new BufferedReader(new FileReader(GUI.class.getResource("config").getPath()))){
            String  buffer;
            while ((buffer=br.readLine())!=null){
                String[] dict=buffer.split("=");
                if (dict[0].equals("language")){
                    langConfig=dict[1];
                }
                if (dict[0].equals("font")){
                    font= Integer.parseInt(dict[1]);
                    System.out.println(font);
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
            if (e.getSource()==resultList){
                int length=resultList.getModel().getSize();
                Rectangle base=SwingUtilities.convertRectangle(resultList,resultList.getBounds(),frame);
                int cur=resultScroll.getVerticalScrollBar().getValue();
                String toProcess="";
                for (int i=0;i<length;i++){
                    Rectangle temp=new Rectangle(base.x+resultList.getCellBounds(i,i).x,
                            base.y+resultList.getCellBounds(i,i).y+cur,
                            (int) resultScroll.getViewportBorderBounds().getWidth(),
                            resultList.getCellBounds(i,i).height);
                    if (temp.contains(MouseInfo.getPointerInfo().getLocation())){
                        toProcess=resultList.getModel().getElementAt(i).toString(true,0,0);
                        break;
                    }
                }
                copy.addActionListener(new EditText(toProcess));
            }
            else {
                copy.addActionListener(new EditText((JTextComponent) e.getComponent()));
                cut.addActionListener(new EditText((JTextComponent) e.getComponent()));
                paste.addActionListener(new EditText((JTextComponent) e.getComponent()));
                empty.addActionListener(new EditText((JTextComponent) e.getComponent()));
            }
            if(e.isPopupTrigger()){
                if (e.getSource()==resultList){
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
        private String toCopy;

        public EditText(JTextComponent component) {
            this.component=component;
        }

        public EditText(String toCopy) {
            this.toCopy = toCopy;
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
                if (this.component==null){
                    clipboard.setContents(new StringSelection(this.toCopy),null);
                }
                else {
                    clipboard.setContents(new StringSelection(this.component.getSelectedText()),null);
                }
            }
            if (e.getActionCommand().equals("empty")){
                this.component.setText("");
            }
        }
    }
}