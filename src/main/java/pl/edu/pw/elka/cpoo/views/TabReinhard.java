package pl.edu.pw.elka.cpoo.views;

import java.awt.event.MouseEvent;

import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg1;
import pl.edu.pw.elka.cpoo.images.HdrImage;

public class TabReinhard extends TabHdr {

    public TabReinhard(HdrImage hdrImage) {
        super(hdrImage);
        slider.setValue(100);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        ToneMappingAlg1 tm = new ToneMappingAlg1();
        tm.setFactor(slider.getValue() * 0.025);
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
