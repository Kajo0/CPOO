package pl.edu.pw.elka.cpoo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg1;
import pl.edu.pw.elka.cpoo.algorithms.ToneMappingAlg2;
import pl.edu.pw.elka.cpoo.images.ImageWrapper;
import pl.edu.pw.elka.cpoo.interfaces.HdrProcessor;
import pl.edu.pw.elka.cpoo.views.ButtonTabComponent;
import pl.edu.pw.elka.cpoo.views.FileDrop;
import pl.edu.pw.elka.cpoo.views.TabImage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class View implements KeyListener, ActionListener {

    private static final String COMMAND_ALG_1 = "alg1";
    private static final String COMMAND_ALG_2 = "alg2";
    private static final String COMMAND_ZOOM_IN = "zoom_in";
    private static final String COMMAND_ZOOM_OUT = "zoom_out";
    private static final String COMMAND_RESET_ZOOM = "reset_zoom";

    private JFrame mainFrame;
    private JPanel barPanel;
    private JTabbedPane tabPane;
    private JButton alg1Button;
    private JButton alg2Button;
    private Map<Image, ExifSubIFDDirectory> imageToExifMap = new HashMap<>();

    public View() {
        mainFrame = new JFrame("CPOO, HDR");
        barPanel = new JPanel();
        tabPane = new JTabbedPane();

        init();
    }

    private void init() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;

        mainFrame.setLocation(width / 2, height / 2);
        mainFrame.setSize(width, height);
        // mainFrame.setAlwaysOnTop(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.addKeyListener(this);

        //

        barPanel.setLayout(new GridLayout());

        initButtons();

        //

        new FileDrop(tabPane, new FileDrop.Listener() {

            @Override
            public void filesDropped(final File[] files) {
                for (File file : files) {
                    if (file.isDirectory())
                        continue;

                    createImageTab(file.getAbsolutePath(), true);
                }
            }
        });

        //

        mainFrame.add(barPanel, BorderLayout.NORTH);
        mainFrame.add(tabPane, BorderLayout.CENTER);

        mainFrame.setVisible(true);
        mainFrame.requestFocus();
        mainFrame.invalidate();
    }

    private void initButtons() {
        alg1Button = addButtonToPanel(new ToneMappingAlg1().getName(), COMMAND_ALG_1, this);
        alg2Button = addButtonToPanel(new ToneMappingAlg2().getName(), COMMAND_ALG_2, this);
        addButtonToPanel("Zoom in", COMMAND_ZOOM_IN, this);
        addButtonToPanel("Zoom out", COMMAND_ZOOM_OUT, this);
        addButtonToPanel("Reset zoom", COMMAND_RESET_ZOOM, this);
    }

    protected JButton addButtonToPanel(final String label, final String command,
            final ActionListener listener) {
        JButton button = new JButton(label);
        button.setActionCommand(command);
        button.addActionListener(listener);

        barPanel.add(button);

        return button;
    }

    private void createImageTab(final Image img, final String name, boolean checkedTab) {
        tabPane.addTab(name, new TabImage(img));
        int tabIndex = tabPane.getTabCount() - 1;
        ButtonTabComponent tabButton = new ButtonTabComponent(tabPane);
        tabButton.setChecked(checkedTab);
        tabPane.setTabComponentAt(tabIndex, tabButton);
        tabPane.setSelectedIndex(tabIndex);
    }

    private void createImageTab(final String path, boolean checkedTab) {
        try {
            File jpgFile = new File(path);
            Metadata metadata = ImageMetadataReader.readMetadata(jpgFile);
            ExifSubIFDDirectory exif = metadata.getDirectory(ExifSubIFDDirectory.class);
            Image image = new ImageIcon(path).getImage();
            imageToExifMap.put(image, exif);
            createImageTab(image, jpgFile.getName(), checkedTab);

        } catch (IOException | ImageProcessingException e) {
        }
    }

    @Override
    public void keyTyped(final KeyEvent event) {
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            mainFrame.dispose();
        case KeyEvent.VK_LEFT: {
            if (tabPane.getTabCount() == 0) {
                return;
            }
            int index = tabPane.getSelectedIndex();
            index -= 1;
            if (index < 0) {
                index = tabPane.getTabCount() - 1;
            }
            index %= tabPane.getTabCount();
            tabPane.setSelectedIndex(index);
        }
            break;
        case KeyEvent.VK_RIGHT: {
            if (tabPane.getTabCount() == 0) {
                return;
            }
            int index = tabPane.getSelectedIndex();
            index += 1;
            index %= tabPane.getTabCount();
            tabPane.setSelectedIndex(index);
        }
            break;
        case KeyEvent.VK_DELETE:
            if (tabPane.getTabCount() == 0) {
                return;
            }
            tabPane.remove(tabPane.getSelectedIndex());
            break;
        case KeyEvent.VK_R:
            getCurrentTabImage().reset();
            break;

        default:
            // ignore
            break;
        }
    }

    @Override
    public void keyPressed(final KeyEvent event) {
    }

    protected TabImage getCurrentTabImage() {
        TabImage ti = (TabImage) tabPane.getSelectedComponent();
        return ti == null ? new TabImage(null) : ti;
    }

    protected Image getCurrentImage() {
        return getCurrentTabImage().getImage();
    }

    protected List<TabImage> getSelectedTabImages() {
        List<TabImage> tabs = new ArrayList<>();

        for (int i = 0; i < tabPane.getTabCount(); ++i)
            if (((ButtonTabComponent) tabPane.getTabComponentAt(i)).isChecked())
                tabs.add((TabImage) tabPane.getComponentAt(i));

        return tabs;
    }

    protected List<Image> getSelectedImages() {
        List<Image> imgs = new ArrayList<>();

        for (TabImage img : getSelectedTabImages())
            imgs.add(img.getImage());

        return imgs;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        switch (event.getActionCommand()) {
        case COMMAND_ALG_1:
            processAlgorithm(new ToneMappingAlg1());
            break;
        case COMMAND_ALG_2:
            processAlgorithm(new ToneMappingAlg2());
            break;
        case COMMAND_ZOOM_IN:
            getCurrentTabImage().zoomIn(this.mainFrame.getWidth() / 2, mainFrame.getHeight() / 2);
            break;
        case COMMAND_ZOOM_OUT:
            getCurrentTabImage().zoomOut(this.mainFrame.getWidth() / 2, mainFrame.getHeight() / 2);
            break;
        case COMMAND_RESET_ZOOM:
            getCurrentTabImage().reset();
            break;

        default:
            // ignore
            break;
        }
        mainFrame.requestFocus();
    }

    protected void processAlgorithm(final HdrProcessor processor) {
        if (tabPane.getTabCount() == 0)
            return;

        alg1Button.setEnabled(false);
        alg2Button.setEnabled(false);

        new Thread(new Runnable() {

            @Override
            public void run() {
                List<Image> images = getSelectedImages();
                if (images.isEmpty() == false) {
                    Image img = processor.process(new ImageWrapper(images, imageToExifMap));
                    createImageTab(img, processor.getName(), false);
                }

                alg1Button.setEnabled(true);
                alg2Button.setEnabled(true);
            }
        }).start();
    }

}
