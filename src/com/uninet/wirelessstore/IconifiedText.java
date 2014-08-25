package com.uninet.wirelessstore;

import android.graphics.drawable.Drawable; 

public class IconifiedText{
	private String mText = "";
	private String mInfo = "";
	private String mPath = "";
	private boolean misFile = false;
	private Drawable mIcon;
	
	public IconifiedText(String text, String path, boolean isfile, Drawable bullet) {
		this.mText = text;
		this.mPath = path;
		this.misFile = isfile;
		this.mIcon = bullet;
		
	}
	
	public IconifiedText(String text, String path, boolean isfile, String info,Drawable bullet) {
		this.mText = text;
		this.mPath = path;
		this.misFile = isfile;
		this.mInfo = info;
		this.mIcon = bullet;
	}
	
	public boolean isFile() {
		return misFile; 
	}
	
	public String getText() { 
		return mText; 
	}
	
	public String getInfo() { 
		return mInfo; 
	}
	
	public Drawable getIcon() { 
		return mIcon; 
	}
	
	public String getPath() {
		return mPath;
	}
//    
//     private String mText = ""; 
//     private String mInfo = "";
//     private Drawable mIcon; 
//     private boolean mSelectable = true; 
//     private boolean mSelected; // 20110621
//     private String mAbsPath;
//
//     //LC2;lilong add start
//     //file type
//     private String mFiletype;
//     //whether or not hidden file
//     private boolean mIsHidenFile;
//     //last modify time
//     private long mModifyTime;
//     //file size
//     private long mFilesize;
//     
//     public long getmFilesize() {
//        return mFilesize;
//    }
//
//    public void setmFilesize(long mFilesize) {
//        this.mFilesize = mFilesize;
//    }
//
//    public String getmFiletype() {
//         return mFiletype;
//     }
//
//     public void setmFiletype(String mFiletype) {
//         this.mFiletype = mFiletype;
//     }
//
//     public boolean ismIsHidenFile() {
//         return mIsHidenFile;
//     }
//
//     public void setmIsHidenFile(boolean mIsHidenFile) {
//         this.mIsHidenFile = mIsHidenFile;
//     }
//
//     public long getmModifyTime() {
//         return mModifyTime;
//     }
//
//     public void setmModifyTime(long mModifyTime) {
//         this.mModifyTime = mModifyTime;
//     }
//     //LC2:lilong add end
//
//    public IconifiedText(String text, String info, Drawable bullet) {
//        mIcon = bullet;
//        mText = text;
//        mInfo = info;
//    }
//
//    public IconifiedText() {
//    }
//
//     public boolean isSelected() {
//     	return mSelected;
//     }
//
// 	public void setSelected(boolean selected) {
//     	this.mSelected = selected;
//     }
//
// 	public boolean isSelectable() { 
//          return mSelectable; 
//     } 
//      
//     public void setSelectable(boolean selectable) { 
//          mSelectable = selectable; 
//     } 
//      
//     public String getText() { 
//         return mText; 
//    } 
//     
//    public void setText(String text) { 
//         mText = text; 
//    } 
//     
//    public String getInfo() { 
//        return mInfo; 
//   } 
//    
//   public void setInfo(String info) { 
//        mInfo = info; 
//   } 
//    
//     public void setIcon(Drawable icon) { 
//          mIcon = icon; 
//     } 
//      
//     public Drawable getIcon() { 
//          return mIcon; 
//     } 
//
//     /** Make IconifiedText comparable by its name */ 
//     
//     public int compareTo(IconifiedText other) { 
//          if(this.mText != null) 
//               return this.mText.compareTo(other.getText()); 
//          else 
//               throw new IllegalArgumentException(); 
//     }
//
//     public void setAbsPath(String path) {
//         mAbsPath = path;
//     }
//
//     public String getAbsPath() {
//         return mAbsPath;
//     }
//
//    @Override
//    public int hashCode() {
//        return mText.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o instanceof IconifiedText) {
//            IconifiedText field = (IconifiedText) o;
//            return mText == null ? false : mText.equals(field.mText);
//        }
//        return false;
//    }

} 

