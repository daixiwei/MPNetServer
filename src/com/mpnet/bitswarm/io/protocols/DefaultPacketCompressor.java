package com.mpnet.bitswarm.io.protocols;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 
 * @ClassName: DefaultPacketCompressor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午2:11:20
 *
 */
public final class DefaultPacketCompressor implements IPacketCompressor {
	public final int	MAX_SIZE_FOR_COMPRESSION	= 1000000;
	private final int	compressionBufferSize		= 256;
	
	@Override
	public byte[] compress(byte[] data) throws Exception {
		if (data.length > 1000000) {
			return data;
		}
		Deflater compressor = new Deflater();
		compressor.setInput(data);
		compressor.finish();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		byte[] buf = new byte[compressionBufferSize];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		bos.close();
		
		return bos.toByteArray();
	}
	
	@Override
	public byte[] uncompress(byte[] zipData) throws Exception {
		Inflater unzipper = new Inflater();
		unzipper.setInput(zipData);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(zipData.length);
		byte[] buf = new byte[compressionBufferSize];
		while (!unzipper.finished()) {
			int count = unzipper.inflate(buf);
			bos.write(buf, 0, count);
		}
		bos.close();
		
		return bos.toByteArray();
	}
}