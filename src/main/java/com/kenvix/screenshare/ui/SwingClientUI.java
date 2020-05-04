//--------------------------------------------------
// Class ClientUI
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.ui;

import com.kenvix.utils.lang.WeakRef;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
    public void update(WeakRef<BufferedImage> image, boolean shouldResize) {
        try {
            if (shouldResize) {
                Dimension size = panel.getSize();
                panel.setImg(resize(image.invoke(), size.width, size.height));
            } else {
                panel.setImg(image.invoke());
            }

            panel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) throws IOException {
        return Thumbnails.of(img).size(newW, newH).asBufferedImage();
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

    @Override
    public int getWidth() {
        return panel.getWidth();
    }

    @Override
    public int getHeight() {
        return panel.getHeight();
    }
}
