import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Fid.Fiv;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private int width, height;

	public void showImage(Fid image) {
		Fiv view = image.getViews()[0];
		width = view.getWidth();
		height = view.getHeight();
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		this.image.getRaster().setDataElements(0, 0, width, height, view.getImageData());
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int x = (getWidth() - width) / 2;
			int y = (getHeight() - height) / 2;
			g.drawImage(image, x, y, null);
		}
	}
}
