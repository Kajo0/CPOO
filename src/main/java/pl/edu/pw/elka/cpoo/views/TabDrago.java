package pl.edu.pw.elka.cpoo.views;

import java.awt.event.MouseEvent;

import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg2;
import pl.edu.pw.elka.cpoo.images.HdrImage;

public class TabDrago extends TabHdr {

    public TabDrago(HdrImage hdrImage) {
        super(hdrImage);
        slider.setValue(200);
        mouseReleased(null);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        double value = (double) slider.getValue() / 255;
        slider.setToolTipText(value + "");

        ToneMappingAlg2 tm = new ToneMappingAlg2();
        tm.setBiasP(value);
        tabImage.setImage(tm.doTonalMappingAlg2(hdrImage));
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
