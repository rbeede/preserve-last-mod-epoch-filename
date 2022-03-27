package com.rodneybeede.software.preservelastmodepochfilename;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Output is https://tools.ietf.org/html/rfc4180
 * 
 * @author rbeede
 *
 */
public class Main {
	private static final Logger log = Logger.getLogger(Main.class);
	
	public static void main(final String[] args) throws IOException, InterruptedException {
		if(null == args || args.length != 1) {
			System.err.println("Incorrect number of arguments");
			System.out.println("Usage:  java -jar " + Main.class.getProtectionDomain().getCodeSource().getLocation().getFile() + " <directory>");
			System.exit(255);
			return;
		}
		
		
		setupLogging();
				
		
		// Parse config options as Canonical paths
		final Path sourceDirectory = Paths.get(args[0]).toRealPath();
		
		log.info("Source directory (real canonical) is " + sourceDirectory);

		
		Files.walkFileTree(sourceDirectory, new FileVisitor<Path>() {

			@Override
			public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				log.info("Processing " + file);
				
				final FileTime lastMod = Files.getLastModifiedTime(file);
				
				log.debug(lastMod);
				
				final Path targetFilename = file.resolveSibling(getUTCPrefix(Date.from(lastMod.toInstant())) + file.getFileName());
				
				log.info("Renaming (moving) " + file + " ===> " + targetFilename);
				
				Files.move(file, targetFilename);
				
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				log.error("Failed to access:  " + file, exc);
				
				return FileVisitResult.CONTINUE;
			}
			
		});
		
		
		// Exit with appropriate status
		log.info("Program has completed");
		

		LogManager.shutdown();;  //Forces log to flush
		
		System.exit(0);  // All good
	}
	
	
	private static void setupLogging() {
		final Layout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS Z}\t%-5p\tThread=%t\t%c\t%m%n");

		// Setup the logger to also log to the console
		final ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		consoleAppender.setEncoding("UTF-8");
		consoleAppender.setThreshold(Level.ALL);
		Logger.getRootLogger().addAppender(consoleAppender);
	}
	
	
	private static String getUTCPrefix(final Date date) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		if(null == date) {
			return dateFormat.format(new Date()) + "_UTC__";
		} else {
			return dateFormat.format(date) + "_UTC__";
		}
	}
}
