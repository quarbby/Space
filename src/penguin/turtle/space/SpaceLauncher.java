package penguin.turtle.space;

import android.graphics.Bitmap;

public class SpaceLauncher {
	private String name;
	private float height;
	private float diameter;
	private float mass;
	private float payload;
	private String bitmap;
	
	public SpaceLauncher(String name, float height, float diameter, float mass, float payload){
		this.name = name;
		this.height = height;
		this.diameter = diameter;
		this.mass = mass;
		this.payload = payload;
	}
	
	public float getPayload(){
		return this.payload;
	}

	public void setBitmapName(String bitmap){
		this.bitmap = bitmap;
	}
	
	public String getBitmap(){
		return bitmap;
	}
	
	public String getName() {
		return name;
	}

	public float getHeight() {
		return height;
	}

	public float getDiameter() {
		return diameter;
	}

	public float getMass() {
		return mass;
	}
	
}
