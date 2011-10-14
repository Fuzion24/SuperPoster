package net.main;

import java.io.File;
import net.usenet.*;

public class Poster {

	public static void main(String[] args) {
		PosterSettings settings; 
		try
		{
			if(new File("settings.xml").exists())
			{
				settings = PosterSettings.loadSettings();
			}else
			{
				settings = new PosterSettings();
				PosterSettings.saveSettings(settings);
				System.out.print("Please specify your settings in settings.xml\n");
				return;
			}
			yEncPoster encoder = new yEncPoster(settings);
			if(args.length < 1)
			{
				System.out.print("Usage: SuperPoster FILE\n");
				return;
			}
			
			encoder.postFile(new File(args[0]));
			
		}catch(Exception e)
		{
			System.out.print(e.toString());
			e.printStackTrace();
		}
	}

}
