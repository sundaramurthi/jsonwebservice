package util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MultiPartOutputStream extends FilterOutputStream{
	
	public static String MULTIPART_MIXED = "multipart/mixed";
	public static String MULTIPART_X_MIXED_REPLACE = "multipart/x-mixed-replace";
	
	private String boundary;
	private byte[] boundaryBytes;

	public MultiPartOutputStream(OutputStream out) throws IOException {
		super (out);
		try {
			boundary = "-----" + System.identityHashCode(this )
								+ Long.toString(System.currentTimeMillis(), 36) + "----";
			boundaryBytes = boundary.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void startPart(String contentType, String[] headers)
			throws IOException {
		out.write("\r\n".getBytes());
		out.write(boundaryBytes);
		out.write(("Content-Type: " + contentType + "\r\n").getBytes("UTF-8"));
		for (int i = 0; headers != null && i < headers.length; i++) {
			out.write(headers[i].getBytes("UTF-8"));
			out.write("\r\n".getBytes());
		}
		out.write("\r\n".getBytes());
	}
	
	public void close() throws IOException {
		out.write("\n".getBytes());
		super.close();
	}
}
