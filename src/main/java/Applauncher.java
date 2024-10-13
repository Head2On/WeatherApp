import javax.swing.*;

public class Applauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WeatherAppGui weatherGui = new WeatherAppGui();
                weatherGui.setVisible(true);
            }
        });
    }
}
