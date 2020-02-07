import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TestSpriteSheet {

    public static void main(String[] args) {
        new TestSpriteSheet();
    }

    public TestSpriteSheet() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        private Spritesheet spritesheet;
        private BufferedImage currentFrame;
        private int frame;

        public TestPane() {
            spritesheet = new Spritesheet("test.gif", 240, 220);
            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentFrame = spritesheet.getSprite(frame % spritesheet.getFrameCount());
                    repaint();
                    frame++;
                }
            });
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(240, 220);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentFrame != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                int x = (getWidth() - currentFrame.getWidth()) / 2;
                int y = (getHeight() - currentFrame.getHeight()) / 2;
                g2d.drawImage(currentFrame, x, y, this);
                g2d.dispose();
            }
        }

    }

    public class Spritesheet {

        //Instance Variables
        private String path;
        private int frameWidth;
        private int frameHeight;
        private BufferedImage sheet = null;
        private BufferedImage[] frameImages;

        //Constructors
        public Spritesheet(String aPath, int width, int height) {

            path = aPath;
            frameWidth = width;
            frameHeight = height;

            try {
                sheet = ImageIO.read(new File(aPath));
                frameImages = getAllSprites();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public BufferedImage getSprite(int frame) {
            return frameImages[frame];
        }

        //Methods
        public int getHeight() {
            return frameHeight;
        }

        public int getWidth() {
            return frameWidth;
        }

        public int getColumnCount() {
            return sheet.getWidth() / getWidth();
        }

        public int getRowCount() {
            return sheet.getHeight() / getHeight();
        }

        public int getFrameCount() {
            int cols = getColumnCount();
            int rows = getRowCount();
            return cols * rows;
        }

        private BufferedImage getSprite(int x, int y, int h, int w) {
            BufferedImage sprite = sheet.getSubimage(x, y, h, w);
            return sprite;
        }

        public BufferedImage[] getAllSprites() {
            int cols = getColumnCount();
            int rows = getRowCount();
            int frameCount =  getFrameCount();
            BufferedImage[] sprites = new BufferedImage[frameCount];
            int index = 0;
            System.out.println("cols = " + cols);
            System.out.println("rows = " + rows);
            System.out.println("frameCount = " + frameCount);
            for (int row = 0; row < getRowCount(); row++) {
                for (int col = 0; col < getColumnCount(); col++) {
                    int x = col * getWidth();
                    int y = row * getHeight();
                    System.out.println(index + " " + x + "x" + y);
                    BufferedImage currentSprite = getSprite(x, y, getWidth(), getHeight());
                    sprites[index] = currentSprite;
                    index++;
                }
            }
            return sprites;

        }

    }
}