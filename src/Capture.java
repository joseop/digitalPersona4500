import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import com.digitalpersona.uareu.*;

public class Capture extends JPanel implements ActionListener {
	private static final long serialVersionUID = 2;
	private JDialog m_dlgParent;
	private CaptureThread m_capture;
	private Reader m_reader;
	private ImagePanel m_image;

	private Capture(Reader reader) {
		m_reader = reader;
		m_capture = new CaptureThread(m_reader, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		m_image = new ImagePanel();
		m_image.setPreferredSize(new Dimension(400, 500));
		add(m_image);
		add(Box.createVerticalStrut(5));
	}

	private void startCaptureThread() {
		m_capture = new CaptureThread(m_reader, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
		m_capture.start(this);
	}

	private void stopCaptureThread() {
		if (m_capture != null) m_capture.cancel();
	}

	private void waitForCaptureThread() {
		if (m_capture != null) m_capture.join(1000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent) e;
			boolean bCanceled = false;

			if (evt.capture_result != null && evt.capture_result.image != null && Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
				m_image.showImage(evt.capture_result.image);
			} else if (evt.capture_result != null && Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
				bCanceled = true;
			} else if (evt.exception != null || evt.reader_status != null) {
				System.out.println("CaptureThread.On_Capture() " + (evt.exception != null ? evt.exception : evt.reader_status));
				bCanceled = true;
			}

			if (!bCanceled) {
				waitForCaptureThread();
				startCaptureThread();
			}
		}
	}

	private void doModal(JDialog dlgParent) {
		try {
			m_reader.Open(Reader.Priority.COOPERATIVE);
		} catch (UareUException e) {
			System.out.println("Reader.Open() " + e);
		}

		boolean bOk = true;

		if (bOk) {
			startCaptureThread();
			m_dlgParent = dlgParent;
			m_dlgParent.setContentPane(this);
			m_dlgParent.pack();
			m_dlgParent.setLocationRelativeTo(null);
			m_dlgParent.toFront();
			m_dlgParent.setVisible(true);
			m_dlgParent.dispose();
			stopCaptureThread();
			waitForCaptureThread();
		}

		try {
			m_reader.Close();
		} catch (UareUException e) {
			System.out.println("Reader.Close() " + e);
		}
	}

	public static void Run(Reader reader) {
		JDialog dlg = new JDialog((JDialog) null, "Put your finger on the reader", true);
		Capture capture = new Capture(reader);
		capture.doModal(dlg);
	}
}
