package org.luaj.lib;

import org.luaj.LuaValue;

class TableLibFunction extends LibFunction {
	public LuaValue call() {
		return argerror(1, "table expected, got no value");
	}
}
