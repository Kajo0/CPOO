package pl.edu.pw.elka.cpoo.views;

import java.awt.event.MouseEvent;

import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg1;
import pl.edu.pw.elka.cpoo.images.HdrImage;

public class TabReinhard extends TabHdr {

    public TabReinhard(HdrImage hdrImage) {
        super(hdrImage);
        slider.setValue(64);
        mouseReleased(null);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        double value = (double) slider.getValue() / 32;
        slider.setToolTipText(value + "");

        ToneMappingAlg1 tm = new ToneMappingAlg1();
        tm.setFactor(value);
        tabImage.setImage(tm.doTonalMappingAlg1(hdrImage));
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

}
