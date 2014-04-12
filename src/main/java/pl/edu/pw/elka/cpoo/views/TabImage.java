package pl.edu.pw.elka.cpoo.views;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

public class TabImage extends JComponent implements MouseMotionListener, MouseListener,
        MouseWheelListener {

    public static final float ZOOM_VALUE = 0.2f;
    public static final float ZOOM_MIN_VALUE = 0.05f;

    protected Image image;

    protected float scale;

    protected int imgWidth;
    protected int imgHeight;

    protected int xPos;
    protected int yPos;

    protected int xTmp;
    protected int yTmp;

    public TabImage() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public TabImage(final Image img) {
        this();

        image = img;

        reset();
    }

    public void zoomIn() {
        scale += ZOOM_VALUE;
        repaint();
    }

    public void zoomOut() {
        scale -= ZOOM_VALUE;
        if (scale < 0)
            scale = ZOOM_MIN_VALUE;
        repaint();
    }

    public void resetZoom() {
        scale = 1;
        repaint();
    }

    public void reset() {
        xPos = 0;
        yPos = 0;
        xTmp = -1;
        yTmp = -1;
        resetZoom();
        calcSize();
    }

    public void setImage(final Image image) {
        this.image = image;
        reset();
    }

    protected void calcSize() {
        if (image != null) {
            imgWidth = image.getWidth(null);
            imgHeight = image.getHeight(null);

            if (getWidth() != 0 && getHeight() != 0) {
                int w = getWidth() - imgWidth;
                int h = getHeight() - imgHeight;

                if (w < 0 || h < 0) {
                    if (h < w) {
                        imgWidth = imgWidth * getHeight() / imgHeight;
                        imgHeight = getHeight();
                    } else {
                        imgHeight = imgHeight * getWidth() / imgWidth;
                        imgWidth = getWidth();
                    }
                }
            }
        } else {
            imgWidth = 0;
            imgHeight = 0;
        }
    }

    public Image getImage() {
        return image;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, xPos, yPos, (int) (imgWidth * scale), (int) (imgHeight * scale),
                    null);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        if (xTmp != -1) {
            xPos -= xTmp - event.getX();
            yPos -= yTmp - event.getY();
            xTmp = event.getX();
            yTmp = event.getY();

            repaint();
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
    }

    @Override
    public void mouseExited(final MouseEvent event) {
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        switch (event.getButton()) {
        case MouseEvent.BUTTON1:
            xTmp = event.getX();
            yTmp = event.getY();
            break;
        case MouseEvent.BUTTON2:
            reset();
            break;

        default:
            // ignore
            break;
        }

    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        switch (event.getButton()) {
        case MouseEvent.BUTTON1:
            xTmp = -1;
            yTmp = -1;
            break;

        default:
            // ignore
            break;
        }
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent event) {
        int notches = event.getWheelRotation();
        if (notches < 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    }

}
