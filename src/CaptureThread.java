import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import com.digitalpersona.uareu.*;

public class CaptureThread extends Thread {
	public static final String ACT_CAPTURE = "capture_thread_captured";

	public class CaptureEvent extends ActionEvent {
		private static final long serialVersionUID = 101;
		public final Reader.CaptureResult capture_result;
		public final Reader.Status reader_status;
		public final UareUException exception;

		public CaptureEvent(Object source, String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
			super(source, ActionEvent.ACTION_PERFORMED, action);
			capture_result = cr;
			reader_status = st;
			exception = ex;
		}
	}

	private ActionListener m_listener;
	private volatile boolean m_bCancel;
	private final Reader m_reader;
	private final Fid.Format m_format;
	private final Reader.ImageProcessing m_proc;

	public CaptureThread(Reader reader, Fid.Format img_format, Reader.ImageProcessing img_proc) {
		m_reader = reader;
		m_format = img_format;
		m_proc = img_proc;
	}

	public void start(ActionListener listener) {
		m_listener = listener;
		super.start();
	}

	public void join(int milliseconds) {
		try {
			super.join(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void capture() {
		try {
			boolean bReady = false;
			while (!bReady && !m_bCancel) {
				Reader.Status rs = m_reader.GetStatus();
				switch (rs.status) {
					case BUSY:
						sleepWithCatch(100);
						break;
					case READY:
					case NEED_CALIBRATION:
						bReady = true;
						break;
					default:
						notifyListener(ACT_CAPTURE, null, rs, null);
						return;
				}
			}

			if (m_bCancel) {
				Reader.CaptureResult cr = new Reader.CaptureResult();
				cr.quality = Reader.CaptureQuality.CANCELED;
				notifyListener(ACT_CAPTURE, cr, null, null);
				return;
			}

			if (bReady) {
				Reader.CaptureResult cr = m_reader.Capture(m_format, m_proc, m_reader.GetCapabilities().resolutions[0], -1);
				notifyListener(ACT_CAPTURE, cr, null, null);
			}
		} catch (UareUException e) {
			notifyListener(ACT_CAPTURE, null, null, e);
		}
	}

	private void sleepWithCatch(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void notifyListener(String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
		if (m_listener == null || action == null || action.isEmpty()) return;

		final CaptureEvent evt = new CaptureEvent(this, action, cr, st, ex);
		SwingUtilities.invokeLater(() -> m_listener.actionPerformed(evt));
	}

	public void cancel() {
		m_bCancel = true;
		try {
			m_reader.CancelCapture();
		} catch (UareUException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		capture();
	}
}
