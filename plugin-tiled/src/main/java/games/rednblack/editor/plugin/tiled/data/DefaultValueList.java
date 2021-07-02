package games.rednblack.editor.plugin.tiled.data;

import java.util.ArrayList;

/**
 * A list that provides a default value when the index is out-of-upper-bound.
 * 
 * @author Jan-Thierry Wegener
 *
 * @param <E> The list element type.
 */
public class DefaultValueList<E> extends ArrayList<E> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultValueList() {
		super();
	}
	
	/**
	 * Returns the element at the given index or the default value if the index is greater than or equal to the size.
	 * 
	 * @param index The index of the element.
	 * @param defaultValue The default element if the index is out-of-upper-bound.
	 * 
	 * @return The element at the given index or the default value.
	 */
	public E get(int index, E defaultValue) {
		if (index >= size()) {
			return defaultValue;
		}
		return super.get(index);
	}

}
