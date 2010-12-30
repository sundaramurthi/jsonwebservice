package com.googlecode.jsonwebservice.attachment.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class SvgImageWriterSpi extends ImageWriterSpi {
	private final String[] mimes	= new String[]{"image/svg+xml"};
	@Override
	public boolean canEncodeImage(ImageTypeSpecifier type) {
		return true;
	}

	@Override
	public ImageWriter createWriterInstance(Object extension)
			throws IOException {
		return new SVGImageWriter((ImageWriterSpi) extension);
	}

	@Override
	public String getDescription(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getMIMETypes() {
		return mimes ;
	}
	
	public static class SVGContentImage extends BufferedImage{
		StringWriter writer	= new StringWriter();
		public SVGContentImage() {
			super(1, 1, BufferedImage.TYPE_INT_RGB);
		}

		public Writer getWriter() {
			return writer;
		}
		
		public String getSVGContent(){
			return writer.getBuffer().toString();
		}
	}
	
	public static class SVGImageWriter extends ImageWriter {

		
		protected SVGImageWriter(ImageWriterSpi originatingProvider) {
			super(originatingProvider);
		}

		@Override
		public IIOMetadata convertImageMetadata(IIOMetadata inData,
				ImageTypeSpecifier imageType, ImageWriteParam param) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IIOMetadata convertStreamMetadata(IIOMetadata inData,
				ImageWriteParam param) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IIOMetadata getDefaultImageMetadata(
				ImageTypeSpecifier imageType, ImageWriteParam param) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void write(IIOMetadata streamMetadata, IIOImage image,
				ImageWriteParam param) throws IOException {
			((ImageOutputStream)getOutput()).write(((SVGContentImage)image.getRenderedImage()).getSVGContent().getBytes());
			((ImageOutputStream)getOutput()).close();
		}
	}
}
