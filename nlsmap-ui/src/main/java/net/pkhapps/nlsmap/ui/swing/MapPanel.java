package net.pkhapps.nlsmap.ui.swing;

import net.pkhapps.nlsmap.api.CoordinateReferenceSystems;
import net.pkhapps.nlsmap.api.raster.MapTileIdentifier;
import net.pkhapps.nlsmap.api.raster.MapTileProvider;
import net.pkhapps.nlsmap.ui.MockMapTileProvider;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Optional;

/**
 * TODO document me
 */
public class MapPanel extends JComponent {

    /**
     * TODO Document me
     */
    public static final String PROP_ANCHOR_POINT_POSITION = "anchorPointPosition";

    /**
     * TODO Document me
     */
    public abstract static class AnchorPoint {

        protected abstract DirectPosition toDirectPosition(DirectPosition anchorPointPosition, Rectangle bounds,
                                                           double scaleX, double scaleY, Point point);

        protected abstract DirectPosition toDirectPosition(DirectPosition anchorPointPosition, Rectangle bounds,
                                                           double scaleX, double scaleY, double offsetX,
                                                           double offsetY);

        protected abstract Envelope toEnvelope(DirectPosition anchorPointPosition, Rectangle bounds, double scaleX,
                                               double scaleY);
    }

    /**
     * TODO Document me
     */
    public static final AnchorPoint CENTER = new AnchorPoint() {

        @Override
        protected DirectPosition toDirectPosition(DirectPosition anchorPointPosition, Rectangle bounds, double scaleX,
                                                  double scaleY, Point point) {
            final double centerX = bounds.getWidth() / 2;
            final double centerY = bounds.getHeight() / 2;
            final double offsetX = point.getX() - centerX;
            final double offsetY = point.getY() - centerY;
            // TODO Take axises of CRS into account
            final double coordinateX = anchorPointPosition.getOrdinate(0) + scaleX * offsetX;
            final double coordinateY = anchorPointPosition.getOrdinate(1) + scaleY * offsetY;
            return new DirectPosition2D(anchorPointPosition.getCoordinateReferenceSystem(), coordinateX, coordinateY);
        }

        @Override
        protected DirectPosition toDirectPosition(DirectPosition anchorPointPosition, Rectangle bounds, double scaleX,
                                                  double scaleY, double offsetX, double offsetY) {
            final double centerX = bounds.getWidth() / 2;
            final double centerY = bounds.getHeight() / 2;
            // When we drag the map to the right, the center position moves to the left (west)
            // When we drag the map downwards, the center position moves upwards (north).
            final double x = centerX - offsetX;
            final double y = centerY - offsetY;
            return toDirectPosition(anchorPointPosition, bounds, scaleX, scaleY, new Point((int) x, (int) y));
        }

        @Override
        protected Envelope toEnvelope(DirectPosition anchorPointPosition, Rectangle bounds, double scaleX, double scaleY) {
            // TODO Implement me!
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public String toString() {
            return "center";
        }
    };

    // TODO Create additional anchor points

    private class MouseHandler implements MouseListener, MouseMotionListener {

        private Point dragStartMousePosition;
        private DirectPosition dragStartAnchorPointPosition;

        @Override
        public void mouseClicked(MouseEvent e) {
            // NOP
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragStartMousePosition = e.getPoint();
            dragStartAnchorPointPosition = getAnchorPointPosition();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragStartAnchorPointPosition = null;
            dragStartMousePosition = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // NOP
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // NOP
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            final double offsetX = e.getX() - dragStartMousePosition.getX();
            final double offsetY = e.getY() - dragStartMousePosition.getY();
            doSetAnchorPointPosition(anchorPoint.toDirectPosition(dragStartAnchorPointPosition, getBounds(), getScaleX(),
                    getScaleY(), offsetX, offsetY), true);
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // NOP
        }
    }

    private MapTileProvider mapTileProvider;
    private int zoomLevel;
    // Initial anchorPointPosition is a roundabout in my home town :-)
    private DirectPosition anchorPointPosition = new DirectPosition2D(CoordinateReferenceSystems.ETRS89_TM35FIN,
            240474.500, 6694820.500);
    private AnchorPoint anchorPoint = CENTER;

    /**
     * Default constructor for the {@code MapPanel}. Remember to set a
     * {@link #setMapTileProvider(MapTileProvider) MapTileProvider} before you start using the component.
     */
    public MapPanel() {
        final MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TODO Paint everything inside a separate buffer, then copy it over to the component buffer.
        if (mapTileProvider == null) {
            g.drawString("Please attach a MapTileProvider", 10, 20);
        } else {
            g.drawString(getAnchorPointPosition().toString(), 10, 20);
            //Optional<MapTileIdentifier> centerTile = mapTileProvider.getTileIdentifier(zoomLevel, centerCoordinates);
            //centerTile.flatMap(mapTileProvider::getTile).ifPresent(tile -> {
            // Calculate where to start painting the tile so that
            //});
            // TODO Continue here
        }
    }

    private void drawTile(MapTileIdentifier identifier, Graphics g) {
        System.out.println(identifier);
    }

    /**
     * TODO Document me
     */
    public AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }

    // TODO Setter for anchorPoint

    /**
     * TODO Document me
     */
    public DirectPosition getAnchorPointPosition() {
        return anchorPointPosition;
    }

    private void doSetAnchorPointPosition(DirectPosition anchorPointPosition, boolean fireEvent) {
        DirectPosition old = null;
        if (fireEvent) {
            old = new DirectPosition2D(this.anchorPointPosition);
        }
        this.anchorPointPosition = new DirectPosition2D(anchorPointPosition);
        if (fireEvent) {
            firePropertyChange(PROP_ANCHOR_POINT_POSITION, old, anchorPointPosition);
        }
    }

    private double getScaleX() {
        return requireMapTileProvider().getScaleX(getZoomLevel());
    }

    private double getScaleY() {
        return requireMapTileProvider().getScaleY(getZoomLevel());
    }

    /**
     * TODO Document me
     */
    public Optional<DirectPosition> getMousePositionCoordinates() {
        return Optional.ofNullable(getMousePosition()).map(point -> getAnchorPoint().toDirectPosition(
                getAnchorPointPosition(), getBounds(), getScaleX(), getScaleY(), point));
    }

    /**
     * TODO Document me
     */
    public Envelope getEnvelope() {
        return getAnchorPoint().toEnvelope(getAnchorPointPosition(), getBounds(), getScaleX(), getScaleY());
    }

    /**
     * TODO Document me
     */
    public MapTileProvider getMapTileProvider() {
        return mapTileProvider;
    }

    /**
     * TODO Document me
     */
    public void setMapTileProvider(MapTileProvider mapTileProvider) {
        this.mapTileProvider = mapTileProvider;
        if (mapTileProvider != null) {
            zoomLevel = mapTileProvider.getMinZoomLevel();
        }
        // TODO Check if we need to do something else as well
        repaint();
    }

    private MapTileProvider requireMapTileProvider() {
        if (mapTileProvider == null) {
            throw new IllegalStateException("No MapTileProvider has been set");
        }
        return mapTileProvider;
    }

    /**
     * TODO Document me
     */
    public int getZoomLevel() {
        return zoomLevel;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 300);
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("MapPanel Demo");
            frame.setLayout(new BorderLayout());
            final MapPanel panel = new MapPanel();
            final JLabel statusBar = new JLabel();
            panel.setMapTileProvider(new MockMapTileProvider());

            final Runnable updateStatusBar = () -> statusBar.setText(String.format("Mouse cursor coordinates: %s, anchor point coordinates: %s, anchor point: %s",
                    panel.getMousePositionCoordinates().map(MapPanel::directPositionToString).orElse("(none)"),
                    directPositionToString(panel.getAnchorPointPosition()), panel.getAnchorPoint()));

            panel.addPropertyChangeListener(evt -> updateStatusBar.run());
            panel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateStatusBar.run();
                }
            });
            frame.add(panel, BorderLayout.CENTER);
            frame.add(statusBar, BorderLayout.SOUTH);
            updateStatusBar.run();
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private static String directPositionToString(DirectPosition directPosition) {
        if (directPosition == null) {
            return null;
        } else {
            return String.format("%s,%s", directPosition.getOrdinate(0),
                    directPosition.getOrdinate(1));
        }
    }
}
