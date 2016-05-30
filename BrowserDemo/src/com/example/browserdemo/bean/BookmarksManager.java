package com.example.browserdemo.bean;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

public class BookmarksManager implements Serializable {
	private static final long serialVersionUID = 1L;
	public BookmarkFolder root; // 根目录
	public BookmarkFolder displayedFolder;
	private static BookmarksManager bookMarkManager;

	/**
	 * 文件夹数量
	 */
	public static int amountOfFolders = 0;
	/**
	 * 标签数量
	 */
	public static int amountOfBookmarks = 0;

	/**
	 * 获取对象
	 * 
	 * @param mContext
	 * @return
	 */
	public static BookmarksManager getInstance() {

		if (bookMarkManager == null) {
			synchronized (BookmarksManager.class) {
				bookMarkManager = new BookmarksManager();
			}
		}
		return bookMarkManager;
	}

	public BookmarksManager() {
		this.root = new BookmarkFolder("/");
		this.root.parentFolder = root;
		this.root.isRoot = true;
		this.displayedFolder = this.root;
	}

	public boolean saveBookmarksManager(Context mContext) {
		FileOutputStream fos;
		try {
			fos = mContext.openFileOutput("bookmarkData", Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(this);
			os.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		}
		return false;
	}

	public BookmarksManager loadBookmarksManager(Context mContext) {
		ObjectInputStream inputStream = null;

		try {
			// Construct the ObjectInputStream object
			inputStream = new ObjectInputStream(
					mContext.openFileInput("bookmarkData"));

			Object obj = null;

			obj = inputStream.readObject();

			if (obj instanceof BookmarksManager) {
				bookMarkManager = (BookmarksManager) obj;
				return bookMarkManager;
			}

		} catch (EOFException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return getInstance();
	}

	public static BookmarksManager loadBookmarksManager1(Context mContext) {
		ObjectInputStream inputStream = null;

		try {
			// Construct the ObjectInputStream object
			inputStream = new ObjectInputStream(
					mContext.openFileInput("bookmarkData"));

			Object obj = null;

			obj = inputStream.readObject();

			if (obj instanceof BookmarksManager) {
				return (BookmarksManager) obj;
			}

		} catch (EOFException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

}
