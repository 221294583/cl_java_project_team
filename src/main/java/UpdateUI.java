import javax.swing.*;

public class UpdateUI extends Thread{
    private JProgressBar progressBar;

    public UpdateUI(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        Article article=Article.getINSTANCE();

        while(!article.isDone()){
            System.out.println("update");
            this.progressBar.setString("PROCESSING");
            this.progressBar.setValue(article.getProgress());
        }
        this.progressBar.setString("DONE");
        this.progressBar.setValue(100);
    }
}
