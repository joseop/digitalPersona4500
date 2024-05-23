import javax.swing.*;
import com.digitalpersona.uareu.*;

public class UareUSampleJava extends JPanel {
	private static final long serialVersionUID = 1L;
	private ReaderCollection m_collection;
	private Reader m_reader;

	private UareUSampleJava() {
		capturar();
	}

	private void capturar() {
		try {
			m_collection = UareUGlobal.GetReaderCollection();
			m_collection.GetReaders();
				m_reader = m_collection.get(0);
				Capture.Run(m_reader);

		} catch (Exception ignore){}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(UareUSampleJava::new);
	}
}
