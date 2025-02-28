/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BDUtility {
    public static void setImage(JFrame frame, String imagePath, int newWidth, int newHeight){
        try {
            BufferedImage originalImage = ImageIO.read(BDUtility.class.getResource(imagePath));
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.createGraphics().drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0,0,null);
            
            ImageIcon backgroundImage = new ImageIcon(resizedImage);
            JLabel backgroundLabel =new JLabel(backgroundImage);
            backgroundLabel.setBounds(0,0,newWidth, newHeight);
            frame.getContentPane().add(backgroundLabel, BorderLayout.CENTER);
            frame.validate();
            
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    private static HashMap<String, JFrame> formsMap = new HashMap<>();
    
    public static void openForm(String formName, JFrame formInstance){
        JFrame existingForm = formsMap.get(formName);
        if(existingForm == null || !existingForm.isVisible()){
            formsMap.put(formName, formInstance);
            formInstance.setVisible(true);
        }
        else{
            existingForm.toFront();
            
        }
            
    }
    
    public static String getPath(String finalPath){
        String projectPath = System.getProperty("user.dir");
        return projectPath+"\\src\\"+finalPath;
    }
    
    public static String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
        return fileName.substring(lastDotIndex + 1);
    }
    return "";
}

    
    public static BufferedImage scaleImage(BufferedImage originalImage) {
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();
    BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = scaledImage.createGraphics();
    g.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
    g.dispose();
    return scaledImage;
}

}
