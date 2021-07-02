package games.rednblack.editor.plugin.tiled.data;

import java.util.ArrayList;

public class DefaultValueList<E> extends ArrayList<E> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultValueList() {
		super();
	}
	
	public E get(int index, E defaultValue) {
		if (index >= size()) {
			return defaultValue;
		}
		return super.get(index);
	}

}
