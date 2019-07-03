package com.nus.memorygame.model;

import android.graphics.Bitmap;

public class ImageFile {

    public ImageFile(String fileName, Bitmap bitmap) {
        this.fileName = fileName;
        this.bitmap = bitmap;
    }

    private String fileName;

    private Bitmap bitmap;

    private boolean checked;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if (!(obj instanceof  ImageFile)) return false;
        ImageFile imf = (ImageFile) obj;
        if(this.getFileName() == null || this.getFileName().isEmpty()) return false;
        if(this.getFileName().equalsIgnoreCase(imf.getFileName())) return true;
        else return false;
    }
}
