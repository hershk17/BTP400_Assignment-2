package QRCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {

	public static BitMatrix generateQRCodeImage(String text, int width, int height, String filePath)
			throws WriterException, IOException {

		// create a qrCodeWriter object
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		// create a bitmatrix image with the text
		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

		Path path = FileSystems.getDefault().getPath(filePath);
		MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

		return bitMatrix;

	}

	public static String generateQRCodeImage(String barcodeText, int height, int width) throws Exception {
		QRCodeWriter barcodeWriter = new QRCodeWriter();
		// create a BitMatrix image with the text
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, height, width);
		// create a ByteArrayOutputStream object
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// create a bit array image
		MatrixToImageWriter.writeToStream(bitMatrix, "png", byteArrayOutputStream);
		// convert the bit array into a base64String
		return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
	}

	public static void main(String args[]) {
		System.out.println("test");
		try {
			generateQRCodeImage("42C3E0A4A28315819313E5AFF56EAB61C654491771634548B02CD8091BEEEBB7", 250, 250,
					"D:\\image.png");
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
