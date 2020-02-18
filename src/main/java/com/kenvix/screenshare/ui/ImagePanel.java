//--------------------------------------------------
// Class ImagePanel
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private  BufferedImage img;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1024, 768);
    }


    @Override
    protected void paintComponent(Graphics g) {
        // TODO Auto-generated method stub
        super.paintComponent(g);
        if(img != null)
            g.drawImage(img, 0, 0, this);
    }


    public BufferedImage getImg() {
        return img;
    }


    public void setImg(BufferedImage img) {
        this.img = img;
    }
}