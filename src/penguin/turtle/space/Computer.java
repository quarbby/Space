package penguin.turtle.space;

import java.util.ArrayList;
import java.util.Random;

public class Computer {
	
	public static SpaceLauncher play(ArrayList<SpaceLauncher>compHand, String category, float value){
		SpaceLauncher sl = null;
		for (int i=0; i<compHand.size(); i++){
			sl = compHand.get(i);
			if (category.equals("Height")){
				if (sl.getHeight() >= value){
					return sl;
				}
			}
			
			else if (category.equals("Mass")){
				if (sl.getMass() >= value){
					return sl;
				}
			}
			
			else if (category.equals("Diameter")){
				if (sl.getDiameter() >= value){
					return sl;
				}
			}
			
			else if (category.equals("Payload")){
				if (sl.getPayload() >= value){
					return sl;
				}
			}
		}
		
		if (sl == null){
			sl = findSpecialCard(compHand);
		}
		
		return null;
	}
	
	public static SpaceLauncher findSpecialCard(ArrayList<SpaceLauncher>compHand){
		SpaceLauncher sl;
		for (int i=0; i<compHand.size(); i++){
			sl = compHand.get(i);
			if (sl.getName().equals("Reverse") || sl.getName().equals("Draw Two") || 
					sl.getName().equals("Stop") || sl.getName().equals("Wild Card")){
				return sl;
			}
		}
		
		return null;
	}
	
	public static String chooseCategoryToPlay(ArrayList<SpaceLauncher> comHand){
		String category;
		//Currently choose random category
		Random rand = new Random();
		int random = rand.nextInt(4);
		switch(random){
		case 0:
			category = "Height";
			break;
		case 1:
			category = "Mass";
			break;
		case 2:
			category = "Diameter";
			break;
		case 3: 
			category = "Payload";
			break;
		default:
			category = "Height";
			break;
		}
		return category;
	}
	
	public static SpaceLauncher chooseRandomCard(ArrayList<SpaceLauncher> comHand){
		SpaceLauncher sl;
		Random rand = new Random();
		int random = rand.nextInt(comHand.size());
		sl = comHand.get(random);
		
		return sl;
	}
}
