package com.netcompss.loader;

import java.io.File;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;

import android.content.Context;
import android.util.Log;

public final class LoadJNI {

	static {
		System.loadLibrary("loader-jni");
	}

	private static boolean isSimpleLoader = false;
	
	/**
	 * 
	 * @param args ffmpeg command
	 * @param workFolder working directory 
	 * @param ctx Android context
	 * @param isValidate apply validation to the command
	 * @throws CommandValidationException
	 */
	public void run(String[] args, String workFolder, Context ctx, boolean isValidate) throws CommandValidationException {
		Log.i(Prefs.TAG, "Using new loader with abb support 3");

		if (GeneralUtils.isLibFolderEmpty(ctx)) isSimpleLoader = true;

		Log.i(Prefs.TAG, "running ffmpeg4android_lib: " + Prefs.version);
		// delete previous log: this is essential for correct progress calculation
		String vkLogPath = workFolder + "vk.log";
		GeneralUtils.deleteFileUtil(vkLogPath);
		GeneralUtils.printCommand(args);
		
		//printInternalDirStructure(ctx);
		
		if (isValidate) {
			if (GeneralUtils.isValidCommand(args)) {
				if (isSimpleLoader)
					loadSimple(args, workFolder, true);
				else
					load(args, workFolder, getVideokitLibPath(ctx), true);
			}
			else
				throw new CommandValidationException();
		}
		else {
			load(args, workFolder, getVideokitLibPath(ctx), true);
		}
		
	}

	private void handleAbbInstallation(Context ctx) {
		if (GeneralUtils.isLibFolderEmpty(ctx)) {
			Log.d(Prefs.TAG, "lib folder empty, looks like abb installation");
			String libPath = ctx.getApplicationInfo().nativeLibraryDir;
			String rootPath = libPath.substring(0, libPath.indexOf("==/") + 3);
			Log.d(Prefs.TAG, "root path: " + rootPath);
			String splitApkStr = "split_config.arm64_v8a.apk";
			try {
				String splitApkPath = rootPath + splitApkStr;
				File rootFolderSplitApkZipFolder = new File(splitApkPath + "(1)");
				boolean isOK = rootFolderSplitApkZipFolder.mkdir();
				Log.d(Prefs.TAG, "trying to create: " + rootFolderSplitApkZipFolder + " " + isOK);
				Log.i(Prefs.TAG, "trying to unzip : " + splitApkPath);
				//GeneralUtils.unzip(new File(splitApkPath), rootFolderSplitApkZipFolder);
				printInternalDirStructure(ctx,  rootPath);
			} catch (Exception e) {
				Log.e(Prefs.TAG, e.getMessage(), e);
			}
		}
		else {
			Log.d(Prefs.TAG, "lib folder is OK");
		}
	}
	
	/**
	 * 
	 * @param args ffmpeg command
	 * @param workFolder working directory
	 * @param ctx Android context
	 * @throws CommandValidationException
	 */
	public void run(String[] args, String workFolder, Context ctx) throws CommandValidationException {
		run(args, workFolder, ctx, true);
	}
	
	
	private static void printInternalDirStructure(Context ctx, String path) {
		Log.d(Prefs.TAG, "=printInternalDirStructure=");
		Log.d(Prefs.TAG, "==============================");
		File file = new File(path);
		analyzeDir(file);
		Log.d(Prefs.TAG, "==============================");
	}
	
	private static void analyzeDir(File path) {
		if (path.isDirectory()) {
			Log.d(Prefs.TAG, "Scanning dir: " + path.getAbsolutePath());
			File[] files1 = path.listFiles();
			if (files1 != null) {
				for (int i = 0; i < files1.length; i++) {
					analyzeDir(files1[i]);
				}
			}
			Log.d(Prefs.TAG, "==========");
		}
		else {
			Log.w(Prefs.TAG, path.getAbsolutePath() + " not a dir.");

		}
	}
	
	private static String getVideokitLibPath(Context ctx) {

		// working 64 bit
		String videokitLibPath = ctx.getApplicationInfo().nativeLibraryDir  + "/libvideokit.so";

		File file = new File(videokitLibPath);
		if(file.exists())  {     
		    Log.i(Prefs.TAG, "videokitLibPath exits");
			Log.i(Prefs.TAG, videokitLibPath);
		}
		else {
			Log.e(Prefs.TAG, "videokitLibPath not exits: " + videokitLibPath);

		}

		return videokitLibPath;
		
	}
	
	
	
	public void fExit( Context ctx) {
		fexit(getVideokitLibPath(ctx));
	}


	@SuppressWarnings("JniMissingFunction")
	public native String fexit(String videokitLibPath);

	@SuppressWarnings("JniMissingFunction")
	public native String unload();

	@SuppressWarnings("JniMissingFunction")
	public native String load(String[] args, String videokitSdcardPath, String videokitLibPath, boolean isComplex);

	@SuppressWarnings("JniMissingFunction")
	public native String loadSimple(String[] args, String videokitSdcardPath, boolean isComplex);

}
