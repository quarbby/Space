package penguin.turtle.space;

public class Deck {
	private String name = "";
	private boolean isChecked = false;
	
	public Deck (String name, boolean isChecked){
		this.name = name;
		this.isChecked = isChecked;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	
	
}
