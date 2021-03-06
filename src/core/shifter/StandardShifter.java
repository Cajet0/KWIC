package core.shifter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StandardShifter implements ShiftStrategy {
	private List<String> fragments;
	private int shiftCounter;
	private int size = 0;
	
	public void setupRotations(String line) {
		fragments = new LinkedList<String>(Arrays.asList(line.split(" ")));
		shiftCounter = 0;
		size = fragments.size();
	}

	public String nextRotation() {
		if (size == 0 && shiftCounter == 0) {
			shiftCounter++;
			return "";	
		}
		if (shiftCounter < size) {
			String aux = fragments.remove(0);
			fragments.add(aux);	
			shiftCounter++;
			
			return String.join(" ", fragments);
		}
		
		return null;
	}

}
