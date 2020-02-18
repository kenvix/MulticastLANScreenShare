//--------------------------------------------------
// Class ClientUI
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ClientUI {
    private static final ClientUI INSTANCE = new ClientUI();
    private JFrame frame;
    private ImagePanel panel;

    public static ClientUI getInstance() {
        return INSTANCE;
    }

    public void show(int width, int height) {
        frame = new JFrame();
        panel = new ImagePanel();
        setSize(width, height);

        frame.setResizable(true);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void update(BufferedImage image) {
        Dimension size = panel.getSize();
        panel.setImg(resize(image, size.width, size.height));
        panel.repaint();
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public void setSize(int width, int height) {
        Dimension dimension = new Dimension(width, height);
        frame.setPreferredSize(dimension);
    }
}
