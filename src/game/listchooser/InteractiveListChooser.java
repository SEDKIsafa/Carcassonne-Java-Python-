package game.listchooser;

import java.util.List;
import game.listchooser.util.Input;


public class InteractiveListChooser<T> implements ListChooser<T> {

	/**
	 * Allows one to choose an item from a list of items of type T.
	 * If the list of items is empty, null is returned.
	 * The list of items is presented as numbers followed by the 
	 * string representation of the item. 
	 * The possibility not to make a choice is automatically added (choice number 0), 
	 * in this case, null is returned.
	 * 
	 * @param msg The asked question.
	 * @param list The list of items of type T from which one must choose one.
	 * @return The chosen item. null if the list of items is empty or if the user chooses not to make no choice
	 */
	public T choose(String msg, List<? extends T> list) {
		// is there is not possible choice, null is returned
		if (list.isEmpty()) {
			return null;
		}
		// shows the items until the user made a valid choice
		int choice = -1;
		while ((choice < 0) || (choice > list.size())) {
			System.out.println(msg);
			System.out.println("      0 - none");
			int index = 1;
			for (T element : list) {
				System.out.println("      " + (index++) + " - " + element);
			}
			System.out.println("            choice ?");
			try {
				choice = Input.readInt();
			} catch (java.io.IOException e) {
				System.out.println("Please, enter a number between 0 and " + (index-1));
			}
		}
		if (choice == 0) {
				return null;
		}
		return list.get(choice-1);
	}
}
