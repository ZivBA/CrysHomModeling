package alignment;

import java.util.Vector;

abstract class Sequence extends Vector<Position> {

	public int length() {
		return size();
	}
}
