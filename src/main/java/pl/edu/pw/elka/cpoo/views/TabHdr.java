package pl.edu.pw.elka.cpoo.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JSlider;

import pl.edu.pw.elka.cpoo.images.HdrImage;

public class TabHdr extends Container {

    protected HdrImage hdrImage;

    protected JSlider slider;
    protected TabImage tabImage;

    public TabHdr(HdrImage hdrImage) {
        super();

        this.hdrImage = hdrImage;
        tabImage = new TabImage(hdrImage.getExposedImage(1));

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        slider = new JSlider(0, 255, 16);
        slider.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent event) {
                tabImage.setImage(hdrImage.getExposedImage(slider.getValue() * 0.0625));
            }

            @Override
            public void mousePressed(MouseEvent event) {
            }

            @Override
            public void mouseExited(MouseEvent event) {
            }

            @Override
            public void mouseEntered(MouseEvent event) {
            }

            @Override
            public void mouseClicked(MouseEvent event) {
            }
        });

        add(slider, BorderLayout.NORTH);
        add(tabImage, BorderLayout.CENTER);
    }

}
