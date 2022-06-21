package com.zhhz.lua;

import android.os.Bundle;

import org.luaj.Globals;
import org.luaj.LuaValue;
import org.luaj.lib.jse.JsePlatform;

public class LuaVirtual {

    Globals globals;

    public static LuaVirtual newInstance() {
        return new LuaVirtual();
    }

    public LuaVirtual() {
        globals = JsePlatform.standardGlobals();
    }

    public Object doString(String lua,Object... args){
        LuaValue luaValue = globals.load(lua);
        if (luaValue.isfunction()){
            return globals.load(lua).call().jcall(args);
        } else {
            return globals.load(lua).jcall(args);
        }
    }

}
