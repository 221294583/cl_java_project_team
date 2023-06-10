import sun.swing.FilePane;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CFileChooser extends JFileChooser {

    private static JFileChooser fileChooser=new JFileChooser();
    private static int status;
    private static String lastDir;
    private static String filename;
    private static JTextComponent c;

    public CFileChooser(String currentDirectoryPath) {
        fileChooser=new JFileChooser(currentDirectoryPath);
        status=0;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        status=0;
        JFrame frame = new JFrame("test");
        Container container = frame.getContentPane();
        GroupLayout frameLayout = new GroupLayout(container);
        container.setLayout(frameLayout);

        frame.add(fileChooser);
        JButton buttonSaveAll = new JButton("Save All");

        frameLayout.setHorizontalGroup(frameLayout.createParallelGroup().addComponent(fileChooser));
        frameLayout.setVerticalGroup(frameLayout.createSequentialGroup().addComponent(fileChooser));

        JPanel layer1=(JPanel) fileChooser.getComponents()[fileChooser.getComponents().length-1];
        JPanel layer2=(JPanel) layer1.getComponents()[layer1.getComponents().length-1];
        JButton buttonSave=(JButton) layer2.getComponents()[0];
        fileChooser.setApproveButtonText("Save");
        JButton buttonNO=(JButton) layer2.getComponents()[1];

        layer2.add(buttonSaveAll);

        c= (JTextComponent) ((Container)layer1.getComponents()[0]).getComponents()[1];

        GroupLayout buttonsLayout=new GroupLayout(layer2);
        layer2.setLayout(buttonsLayout);
        buttonsLayout.setHorizontalGroup(
                buttonsLayout.createSequentialGroup().
                        addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE).
                        addComponent(buttonSave).addComponent(buttonSaveAll).addComponent(buttonNO));
        buttonsLayout.setVerticalGroup(
                buttonsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                        addComponent(buttonSave).addComponent(buttonSaveAll).addComponent(buttonNO));


        buttonNO.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                status =-1;
                set();
                frame.dispose();
            }
        });

        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                status=1;
                set();
                frame.dispose();
            }
        });

        buttonSaveAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                status=2;
                set();
                frame.dispose();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                status=(status==0 ? -1 : status);
                frame.dispose();
            }
        });

        fileChooser.setFileFilter(new FileNameExtensionFilter("xml file","xml"));
        fileChooser.setSelectedFile(new File("1.xml"));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        return status;
    }

    public void set(){
        lastDir= String.valueOf(fileChooser.getCurrentDirectory());

        System.out.println(lastDir);
        filename=c.getText();
        System.out.println(filename);
    }

    public String getLastDir() {
        return lastDir;
    }

    public String getFilename() {
        return filename;
    }

    public int getStatus() {
        return status;
    }
}