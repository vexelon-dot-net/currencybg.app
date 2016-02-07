/*
 * CurrencyBG App
 * Copyright (C) 2016 Vexelon.NET Services
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.vexelon.currencybg.app.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import android.content.Context;

public class IOUtils {

	public static final int BUFFER_PAGE_SIZE = 4096; // 4k

	/**
	 * Downloads a file given URL to specified destination.
	 * 
	 * @param url
	 * @param destFile
	 * @throws IOException
	 */
	public static void downloadFile(String url, File destFile) throws IOException {
		InputStream input = null;
		FileOutputStream output = null;
		try {
			URL myUrl = new URL(url);
			URLConnection connection = myUrl.openConnection();
			input = connection.getInputStream();
			byte[] fileData = read(input);
			output = new FileOutputStream(destFile);
			output.write(fileData);
		} finally {
			closeQuitely(input);
			closeQuitely(output);
		}
	}

	/**
	 * Moves a file stored in the cache to the internal storage of the specified
	 * context.
	 * 
	 * @param context
	 * @param cacheFile
	 * @param internalStorageName
	 * @throws IOException
	 */
	public static void moveCacheFile(Context context, File cacheFile, String internalStorageName) throws IOException {
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(cacheFile);
			byte[] fileData = read(input);
			output = context.openFileOutput(internalStorageName, Context.MODE_PRIVATE);
			output.write(fileData);
			// delete cache
			cacheFile.delete();
		} finally {
			closeQuitely(input);
			closeQuitely(output);
		}
	}

	/**
	 * Writes input stream data to PRIVATE internal storage file.
	 * 
	 * @param context
	 * @param source
	 * @param internalStorageName
	 * @throws IOException
	 */
	public static void writeToInternalStorage(Context context, InputStream source, String internalStorageName)
			throws IOException {
		BufferedOutputStream bos = null;
		try {
			byte[] fileData = read(source);
			bos = new BufferedOutputStream(context.openFileOutput(internalStorageName, Context.MODE_PRIVATE));
			bos.write(fileData);
			bos.flush();
		} finally {
			closeQuitely(source);
			closeQuitely(bos);
		}
	}

	/**
	 * Reads an input stream into a byte array
	 * 
	 * @param source
	 * @return Byte array of input stream data
	 * @throws IOException
	 */
	public static byte[] read(InputStream source) throws IOException {
		ReadableByteChannel srcChannel = Channels.newChannel(source);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				source.available() > 0 ? source.available() : BUFFER_PAGE_SIZE);
		WritableByteChannel destination = Channels.newChannel(baos);
		try {
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_PAGE_SIZE);
			while (srcChannel.read(buffer) > 0) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					destination.write(buffer);
				}
				buffer.clear();
			}
			return baos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			closeQuitely(srcChannel);
			closeQuitely(source);
			closeQuitely(destination);
		}
	}

	public static void closeQuitely(Closeable source) {
		try {
			if (source != null) {
				source.close();
			}
		} catch (IOException e) {
		}
	}
}
