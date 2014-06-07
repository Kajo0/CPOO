package pl.edu.pw.elka.cpoo.views;

import java.awt.event.MouseEvent;

import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg2;
import pl.edu.pw.elka.cpoo.images.HdrImage;

public class TabDrago extends TabHdr {

    public TabDrago(HdrImage hdrImage) {
        super(hdrImage);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        ToneMappingAlg2 tm = new ToneMappingAlg2();
        tm.setBiasP(slider.getValue() * 0.0625);
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
