import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    public WeatherAppGui() {
        // Set frame properties
        setTitle("Weather App");
        setSize(450, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window on the screen
        setResizable(false);
        setLayout(new BorderLayout());  // Use a layout manager instead of null layout

        // Create panel and set its background color
        JPanel panel = new JPanel();
        panel.setBackground(Color.lightGray);

        // Add components to the panel if needed (e.g., labels, buttons, etc.)
        addGuiComponents();

        // Add the panel to the frame
        add(panel, BorderLayout.CENTER);


        // Make the frame visible
        setVisible(true);
    }
    // Custom JTextField with rounded corners
    class RoundedTextField extends JTextField {
        private int cornerRadius;

        public RoundedTextField(int columns) {
            super(columns);
            this.cornerRadius = 50;  // Set corner radius (adjust as needed)
            setOpaque(false);  // Makes the background transparent for custom painting
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded rectangle background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Call the super method to handle the text drawing inside the field
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the border with rounded corners
            g2.setColor(Color.GRAY);  // Set border color
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
        }

        @Override
        public Insets getInsets() {
            return new Insets(10, 10, 10, 10);  // Adjust insets to control text padding
        }
    }




    private void addGuiComponents() {
        RoundedTextField searchTextField = new RoundedTextField(20);
        searchTextField.setBounds(15, 15, 351, 45);

        searchTextField.setFont(new Font("Dialog", Font.BOLD, 24));
        searchTextField.setBorder(new LineBorder(Color.GRAY,1,true));

        add(searchTextField);


        JLabel weatherConditionImage = new JLabel(loadImage("src/main/assets/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        //temperature text
       JLabel temperatureText = new JLabel("10 C");
       temperatureText.setBounds(0, 350,450,54);
       temperatureText.setFont(new Font("Dialog",Font.BOLD,48));

       //center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //weather condition description
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog",Font.PLAIN,32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //humidity image
        JLabel humidityImage = new JLabel(loadImage("src/main/assets/humidity.png"));
        humidityImage.setBounds(15,500,70,66);
        add(humidityImage);

        //humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(79,522,85,55);
        humidityText.setFont(new Font("Dialog",Font.BOLD,13));
        add(humidityText);

        //windspeed image
        JLabel windspeedImage = new JLabel(loadImage("src/main/assets/wind.png"));
        windspeedImage.setBounds(220,500,74,66);
        add(windspeedImage);

        //windspeed text
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310,500,85,55);
        windspeedText.setFont(new Font("Dialog",Font.BOLD,16));
        add(windspeedText);


        JButton searchBtn = new JButton(loadImage("src/main/assets/search.png"));

        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.setBounds(375,13,47,45);
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get location from user
                String userInput = searchTextField.getText();
                //validate input -remove whitespace to ensure non-empty text
                if(userInput.replaceAll("\\s","").length() <= 0){
                    return ;
                }
                //retrieve weather data
                weatherData = WeatherApplication.getWeatherData(userInput);
                //update gui
                //update weather image
                String weatherCondition = (String) weatherData.get("weather_condition");

                switch (weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/main/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/main/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/main/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/main/assets/snow.png"));
                        break;
                }
                //Update temperature text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + "C");
                //update weather condition
                weatherConditionDesc.setText(weatherCondition);
                //update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b>" +humidity+"%</html>");
                //update windspeed text
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> "+ windspeed +"km/h</html>");

            }
        });

        add(searchBtn);

    }

   private ImageIcon loadImage(String resourcePath){
        try{
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
    }catch (IOException e){
            e.printStackTrace();
        }
       System.out.println("Could not find resource.");
        return null;
    }


}