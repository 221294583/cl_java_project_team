import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;

public class GUI {
    public static void main(String[] args) {

        ArrayList<Article> articles=new ArrayList<>();
        final Timer[] timer = new Timer[1];

        JFrame frame=new JFrame("Wiki Spider");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container container=frame.getContentPane();
        GroupLayout groupLayout=new GroupLayout(container);
        container.setLayout(groupLayout);

        JPanel inputPanel=new JPanel();
        JTextField wikiUrl=new JTextField("paste your wiki url here!",20);
        JButton entryButton=new JButton("GO!");
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

        JPanel outputPanel=new JPanel();
        JEditorPane result=new JEditorPane();
        result.setContentType("text/html");
        result.setEditable(false);
        outputPanel.add(result);
        GroupLayout outputLayout=new GroupLayout(outputPanel);
        outputPanel.setLayout(outputLayout);
        outputLayout.setHorizontalGroup(outputLayout.createSequentialGroup().addComponent(result));
        outputLayout.setVerticalGroup(outputLayout.createSequentialGroup().addComponent(result));

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
                    NetWork netWork=new NetWork(wikiUrl.getText());
                    netWork.start();
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
                            if (articles.get(articles.size()-1).isDone()){
                                System.out.println(article);
                                result.setText(article.toString(
                                        isTokenShow.isSelected(),isPosShow.isSelected(),isLemmaShow.isSelected()));
                                timer[0].stop();
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



        frame.pack();
        frame.setVisible(true);
    }
}