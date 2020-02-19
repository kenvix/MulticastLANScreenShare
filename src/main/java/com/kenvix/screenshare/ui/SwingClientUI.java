//--------------------------------------------------
// Class ClientUI
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SwingClientUI implements BaseUI {
    private static final SwingClientUI INSTANCE = new SwingClientUI();
    private JFrame frame;
    private ImagePanel panel;

    public static SwingClientUI getInstance() {
        return INSTANCE;
    }

    @Override
    public void show(int width, int height) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        frame = new JFrame();
        panel = new ImagePanel();
        setSize(width, height);

        frame.setResizable(true);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
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

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    @Override
    public void setSize(int width, int height) {
        Dimension dimension = new Dimension(width, height);
        frame.setPreferredSize(dimension);
    }

    public JFrame getFrame() {
        return frame;
    }

    public ImagePanel getPanel() {
        return panel;
    }

    @NotNull
    @Override
    public String getTitle() {
        return frame.getTitle();
    }

    @Override
    public boolean isShowing() {
        return frame != null && frame.isShowing();
    }
}
