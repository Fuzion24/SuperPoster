package net.usenet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CRC32;



public class yEncPoster {

	public final boolean debug = true;
	private final NNTPclient nntpClient;
	PosterSettings settings;
	
	public yEncPoster(PosterSettings settings)
	{
		nntpClient = new NNTPclient(settings);
		this.settings = settings;
	}
	
	byte [] encode(byte [] fileSegment, int byteCount)
	{
		//Initialize the array expecting 10% overhead
		ByteArrayOutputStream encodedSegment = new ByteArrayOutputStream();
		
		int lineCount = 0;

		for(int i = 0; i < byteCount; i++)
		{
			byte b = fileSegment[i];
			boolean escaped = false;
			 byte c = (byte) ((b + 42) % 256);
			 
			 //Escape Chars (NULL, LF, CR, equal sign) respectively  -- the TAB (0x09) was removed in yEnc 1.2
			 if ((c == 0x00) || (c == 0x0A) || (c == 0x0D) || (c == 0x3D)) //|| (c == 0x09) || (c == 0x20) || (c == 0x2E))
			 {
				 c = (byte) ((c + 64) % 256);
				 //Write Escape char (=)
				 encodedSegment.write('=');
				 escaped = true;
			 }
			 encodedSegment.write(c);
			 lineCount++;
			 if(lineCount % settings.LINE_LENGTH == 0)
			 {
				 //Write line break (cr/lf)
				 encodedSegment.write(0x0D);
				 encodedSegment.write(0x0A);
				 
				 //NNTP RFC, if line begins with . write another .
				 //Easier and faster to do here
				 byte d = fileSegment[i+1];
				 d = (byte) ((d + 42) % 256);
				 if (d == 0x2E)
				 {
					 encodedSegment.write(0x2E);
				 }
				 
			 }else{ 
				 if(escaped)
				 lineCount++;
			 }
		}
		return encodedSegment.toByteArray();
	}
	
	public void postFile(File file) throws Exception
	{
		
		nntpClient.connect();
		
		boolean multipart = false;
		int numOfParts = 1;
		if (file.length() > settings.MAX_PART_SIZE)
		{
			multipart = true;
			//Always round up to the nearest part
			numOfParts = Math.round((file.length() / settings.MAX_PART_SIZE) + .5f);
		}
	
		FileInputStream fis = new FileInputStream(file);
		
		byte[] buffer = new byte[settings.MAX_PART_SIZE];
		byte [] encoded = null;	
		int byteCount;
		String yEncHeader;
		String yEncTrailer;
		String yEncPart;
		
		if(multipart)
		{
			int curPart = 1;
			int fileOffset = 1;

			while (curPart <= numOfParts)
			{
				byteCount = fis.read(buffer);				
				encoded = encode(buffer,byteCount);
				yEncHeader = "=ybegin part=" + curPart + " total="+ numOfParts +" line=" + settings.LINE_LENGTH + " size=" + byteCount + " name=" + file.getName() + "\r\n";
				yEncPart = "=ypart begin=" + fileOffset + " end=" + (fileOffset + byteCount -1) + "\r\n";
				yEncTrailer = "=yend size=" + byteCount + " part="+ curPart +  " pcrc32=" + generateCRC32(buffer,byteCount) + "\r\n";
				
				String header = yEncHeader + yEncPart;
			
				//yEnc subject structure:  [Comment1] "filename" yEnc (partnum/numparts) [size] [Comment2]
				String subject = settings.commentOne + " \"" + file.getName() + "\" yEnc (" + curPart + "/" + numOfParts + ") " + settings.commentTwo;// + byteCount;

				nntpClient.post(subject, settings.poster, settings.newsgroup, header, encoded, yEncTrailer);
			
				fileOffset += byteCount;
				curPart++;
			}
		}else
		{
			byteCount = fis.read(buffer);
			encoded = encode(buffer,byteCount);
			yEncHeader = "=ybegin line=" + settings.LINE_LENGTH + " size=" + byteCount + " name=" + file.getName() + "\r\n";
			yEncTrailer = "=yend size=" + byteCount + " crc32=" + generateCRC32(buffer,byteCount) + "\r\n";
					
			String subject = settings.commentOne + " \"" + file.getName() + "\" yEnc " + settings.commentTwo;// + byteCount;
			nntpClient.post(subject, settings.poster, settings.newsgroup, yEncHeader, encoded, yEncTrailer);
		}
			
		
		nntpClient.disconnect();
	}
	String generateCRC32(byte[] segment,int byteCount)
	{
		CRC32 checksum = new CRC32();
		checksum.update(segment,0,byteCount);
		return Long.toHexString(checksum.getValue()).toUpperCase();
	}
}
