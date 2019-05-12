/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */
package net.sourceforge.sox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;


import android.util.Log;

public class ShellUtils {

	public interface ShellCallback
	{
		public void shellOut (String shellLine);

		public void processComplete (int exitValue);
	}
}
