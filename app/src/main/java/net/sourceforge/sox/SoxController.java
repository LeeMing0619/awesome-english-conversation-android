package net.sourceforge.sox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import net.sourceforge.sox.ShellUtils.ShellCallback;
import com.convoenglishllc.expression.R;

public class SoxController {
	private final static String TAG = "SOX";
	String[] libraryAssets = {"sox"};
	private String soxBin;
	private File fileBinDir;
	private ShellCallback callback;

	public SoxController(Context context, ShellCallback _callback) throws IOException {
		callback = _callback;

		installBinaries(context, false);
		fileBinDir = new File(soxBin).getParentFile();
		
	}

	public void installBinaries(Context context, boolean overwrite)
	{
		soxBin = installBinary(context, R.raw.sox, "sox", overwrite);
		installBinary(context, R.raw.libffmpeg, "libffmpeg.so", overwrite);
		installBinary(context, R.raw.libflac, "libFLAC.so", overwrite);
		installBinary(context, R.raw.libfmemopen, "libfmemopen.so", overwrite);
		installBinary(context, R.raw.libgsm, "libgsm.so", overwrite);
		installBinary(context, R.raw.liblpc10, "liblpc10.so", overwrite);
		installBinary(context, R.raw.libmad, "libmad.so", overwrite);		
		installBinary(context, R.raw.libmp3lame, "libmp3lame.so", overwrite);
		installBinary(context, R.raw.libogg, "libogg.so", overwrite);		
		installBinary(context, R.raw.libpng, "libpng.so", overwrite);
		installBinary(context, R.raw.libsmr, "libsmr.so", overwrite);
		installBinary(context, R.raw.libsmrx, "libsmrx.so", overwrite);
		installBinary(context, R.raw.libsndfile, "libsndfile.so", overwrite);
		installBinary(context, R.raw.libvorbis, "libvorbis.so", overwrite);
		installBinary(context, R.raw.libvorbisenc, "libvorbisenc.so", overwrite);
		installBinary(context, R.raw.libvorbisfile, "libvorbisfile.so", overwrite);
		installBinary(context, R.raw.libwavpack, "libwavpack.so", overwrite);
	}
	
	public String getBinaryPath ()
	{
		return soxBin;
	}
	
	private static String installBinary(Context ctx, int resId, String filename, boolean upgrade) {
		try {
			File f = new File(ctx.getDir("bin", 0), filename);
			if (f.exists()) {
				f.delete();
			}
			copyRawFile(ctx, resId, f, "0755");
			return f.getCanonicalPath();
		} catch (Exception e) {
			Log.e(TAG, "installBinary failed: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * Copies a raw resource file, given its ID to the given location
	 * @param ctx context
	 * @param resid resource id
	 * @param file destination file
	 * @param mode file permissions (E.g.: "755")
	 * @throws IOException on error
	 * @throws InterruptedException when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
	}

	private class LengthParser implements ShellCallback {
		public double length;
		public int retValue = -1;

		@Override
		public void shellOut(String shellLine) {
			if( !shellLine.startsWith("Length") )
				return;
			String[] split = shellLine.split(":");
			if(split.length != 2) return;

			String lengthStr = split[1].trim();

			try {
				length = Double.parseDouble( lengthStr );
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void processComplete(int exitValue) {
			retValue = exitValue;

		}
	}
	
	/**
	 * Retrieve the length of the audio file
	 * sox file.wav 2>&1 -n stat | grep Length | cut -d : -f 2 | cut -f 1
	 * @return the length in seconds or null
	 */
	public double getLength(String path) {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(soxBin);
		cmd.add(path);
		cmd.add("-n");
		cmd.add("stat");

		LengthParser sc = new LengthParser();

		try {
			execSox(cmd, sc);
		} catch (Exception e) {
			return -1;
		} 
		
		return sc.length;
	}

	/**
	 * Discard all audio not between start and length (length = end by default)
	 * sox <path> -e signed-integer -b 16 outFile trim <start> <length>
	 * @param start
	 * @param length (optional)
	 * @return path to trimmed audio
	 */
	public String trimAudio(String path, String output,  double start, double length) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		File file = new File(path);
		String outFile = output;
		cmd.add(soxBin);
		cmd.add("-t");
		cmd.add("mp3");
		cmd.add(path);
		cmd.add("-r");
		cmd.add("8000");
		cmd.add("-e");
		cmd.add("signed-integer");
		cmd.add("-b");
		cmd.add("16");
		cmd.add(outFile);
		cmd.add("trim");
		cmd.add(start+"");
		if( length != -1 )
			cmd.add(length+"");

		int rc = execSox(cmd, callback);
		if( rc != 0 ) {
			outFile = null;
		}

		if (file.exists())
			return outFile;
		else
			return null;
		
	}

	/**
	 * Combine and mix audio files
	 * sox -m -v 1.0 file[0] -v 1.0 file[1] ... -v 1.0 file[n] outFile
	 * 
	 * @param files
	 * @return combined and mixed file (null on failure)
	 */
	public String
	combineMix(List<String> files, String outFile) {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(soxBin);
		cmd.add("--multi-threaded");
		cmd.add("-m");
		int index=0;
		for(String file : files) {
			cmd.add("-v");
			cmd.add("1.0");
			cmd.add(file);
			index++;
		}
		cmd.add(outFile);
		
		try {
			int rc = execSox(cmd, callback);
			if(rc != 0) {
			//	Log.e(TAG, "combineMix receieved non-zero return code!");
				outFile = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outFile;
	}

	public String mp3ToWav(String mp3File, String wavFile) {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(soxBin);
		cmd.add("--multi-threaded");
		cmd.add(mp3File);
		cmd.add(wavFile);
		
		try {
			int rc = execSox(cmd, callback);
			if(rc != 0) {
			//	Log.e(TAG, "combineMix receieved non-zero return code!");
				wavFile = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wavFile;
	}

	public String wavToMp3(String wavFile, String mp3File) {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(soxBin);
		cmd.add("--multi-threaded");		
		cmd.add(wavFile);
		cmd.add(mp3File);
		
		try {
			int rc = execSox(cmd, callback);
			if(rc != 0) {
			//	Log.e(TAG, "combineMix receieved non-zero return code!");
				mp3File = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp3File;
	}
	
	public void soxHelp(){
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(soxBin);
		cmd.add("-h");
		try {
			int rc = execSox(cmd, callback);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int execSox(List<String> cmd, ShellCallback sc) throws IOException,
			InterruptedException {

		String soxBin = new File(fileBinDir, "sox").getCanonicalPath();
		
		Runtime.getRuntime().exec("chmod 700 " + soxBin);
		return execProcess(cmd, sc);
	}

	private int execProcess(List<String> cmds, ShellCallback sc)
			throws IOException, InterruptedException {

		//ensure that the arguments are in the correct Locale format
		for (String cmd :cmds)
		{
			cmd = String.format(Locale.US, "%s", cmd);
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileBinDir);

		Map<String, String> envMap = pb.environment(); 
		envMap.put("LD_LIBRARY_PATH", fileBinDir.getCanonicalPath());
		
		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds) {
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}

		sc.shellOut(cmdlog.toString());
		
		// pb.redirectErrorStream(true);
		Process process = pb.start();

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(
				process.getInputStream(), "OUTPUT", sc);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		int exitVal = process.waitFor();

		while (outputGobbler.isAlive() || errorGobbler.isAlive());
		
		sc.processComplete(exitVal);

		return exitVal;
	}

	class StreamGobbler extends Thread {
		InputStream is;
		String type;
		ShellCallback sc;

		StreamGobbler(InputStream is, String type, ShellCallback sc) {
			this.is = is;
			this.type = type;
			this.sc = sc;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					if (sc != null)
						sc.shellOut(line);

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
