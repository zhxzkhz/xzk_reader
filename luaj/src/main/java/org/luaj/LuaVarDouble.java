package org.luaj;

import org.luaj.lib.MathLib;

public class LuaVarDouble extends LuaNumber {

    /**
     * Constant LuaDouble representing NaN (not a number)
     */
    public static final LuaDouble NAN = new LuaDouble(Double.NaN);

    /**
     * Constant LuaDouble representing positive infinity
     */
    public static final LuaDouble POSINF = new LuaDouble(Double.POSITIVE_INFINITY);

    /**
     * Constant LuaDouble representing negative infinity
     */
    public static final LuaDouble NEGINF = new LuaDouble(Double.NEGATIVE_INFINITY);

    /**
     * Constant String representation for NaN (not a number), "nan"
     */
    public static final String JSTR_NAN = "nan";

    /**
     * Constant String representation for positive infinity, "inf"
     */
    public static final String JSTR_POSINF = "inf";

    /**
     * Constant String representation for negative infinity, "-inf"
     */
    public static final String JSTR_NEGINF = "-inf";

    /**
     * The value being held by this instance.
     */
    private double v;

    public static LuaNumber valueOf(double d) {
        int id = (int) d;
        return d == id ? (LuaNumber) LuaInteger.valueOf(id) : (LuaNumber) new LuaDouble(d);
    }

   final public LuaVarDouble setValue(double d) {
        v = d;
        return this;
    }

    /**
     * Don't allow ints to be boxed by DoubleValues
     */
    public LuaVarDouble(double d) {
        this.v = d;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(v + 1);
        return ((int) (l >> 32)) + (int) l;
    }

    public boolean islong() {
        return v == (long) v;
    }

    public boolean isinttype() {
        return v == (int) v;
    }

    public boolean isint() {
        return v == (int) v;
    }

    public byte tobyte() {
        return (byte) (long) v;
    }

    public char tochar() {
        return (char) (long) v;
    }

    public double todouble() {
        return v;
    }

    public float tofloat() {
        return (float) v;
    }

    public int toint() {
        return (int) (long) v;
    }

    public long tolong() {
        return (long) v;
    }

    public short toshort() {
        return (short) (long) v;
    }


    public double optdouble(double defval) {
        return v;
    }

    public int optint(int defval) {
        return (int) (long) v;
    }

    public LuaInteger optinteger(LuaInteger defval) {
        return LuaInteger.valueOf((long) v);
    }

    public long optlong(long defval) {
        return (long) v;
    }

    public LuaInteger checkinteger() {
        return LuaInteger.valueOf((long) v);
    }

    // unary operators
    public LuaValue neg() {
        return valueOf(-v);
    }

    // object equality, used for key comparison
    public boolean equals(Object o) {
        return o instanceof LuaDouble ? ((LuaDouble) o).v == v : false;
    }

    // equality w/ metatable processing
    public LuaValue eq(LuaValue val) {
        return val.raweq(v) ? TRUE : FALSE;
    }

    public boolean eq_b(LuaValue val) {
        return val.raweq(v);
    }

    // equality w/o metatable processing
    public boolean raweq(LuaValue val) {
        return val.raweq(v);
    }

    public boolean raweq(double val) {
        return v == val;
    }

    public boolean raweq(long val) {
        return v == val;
    }

    // basic binary arithmetic
    public LuaValue add(LuaValue rhs) {
        return rhs.add(v);
    }

    public LuaValue add(double lhs) {
        return LuaDouble.valueOf(lhs + v);
    }

    public LuaValue sub(LuaValue rhs) {
        return rhs.subFrom(v);
    }

    public LuaValue sub(double rhs) {
        return LuaDouble.valueOf(v - rhs);
    }

    public LuaValue sub(long rhs) {
        return LuaDouble.valueOf(v - rhs);
    }

    public LuaValue subFrom(double lhs) {
        return LuaDouble.valueOf(lhs - v);
    }

    public LuaValue mul(LuaValue rhs) {
        return rhs.mul(v);
    }

    public LuaValue mul(double lhs) {
        return LuaDouble.valueOf(lhs * v);
    }

    public LuaValue mul(long lhs) {
        return LuaDouble.valueOf(lhs * v);
    }

    public LuaValue pow(LuaValue rhs) {
        return rhs.powWith(v);
    }

    public LuaValue pow(double rhs) {
        return MathLib.dpow(v, rhs);
    }

    public LuaValue pow(long rhs) {
        return MathLib.dpow(v, rhs);
    }

    public LuaValue powWith(double lhs) {
        return MathLib.dpow(lhs, v);
    }

    public LuaValue powWith(long lhs) {
        return MathLib.dpow(lhs, v);
    }

    public LuaValue div(LuaValue rhs) {
        return rhs.divInto(v);
    }

    public LuaValue div(double rhs) {
        return LuaDouble.ddiv(v, rhs);
    }

    public LuaValue div(long rhs) {
        return LuaDouble.ddiv(v, rhs);
    }

    public LuaValue divInto(double lhs) {
        return LuaDouble.ddiv(lhs, v);
    }

    public LuaValue mod(LuaValue rhs) {
        return rhs.modFrom(v);
    }

    public LuaValue mod(double rhs) {
        return LuaDouble.dmod(v, rhs);
    }

    public LuaValue mod(long rhs) {
        return LuaDouble.dmod(v, rhs);
    }

    public LuaValue modFrom(double lhs) {
        return LuaDouble.dmod(lhs, v);
    }


    public LuaValue idiv(LuaValue rhs) {
        return rhs.idiv((long) v);
    }

    public LuaValue idiv(long lhs) {
        return LuaInteger.valueOf(lhs / v);
    }

    public LuaValue band(LuaValue rhs) {
        return rhs.band((long) v);
    }

    public LuaValue band(long lhs) {
        return LuaInteger.valueOf(lhs & (long) v);
    }

    public LuaValue bor(LuaValue rhs) {
        return rhs.bor((long) v);
    }

    public LuaValue bor(long lhs) {
        return LuaInteger.valueOf(lhs | (long) v);
    }

    public LuaValue bxor(LuaValue rhs) {
        return rhs.bxor((long) v);
    }

    public LuaValue bxor(long lhs) {
        return LuaInteger.valueOf(lhs ^ (long) v);
    }

    public LuaValue shl(LuaValue rhs) {
        return rhs.shl((long) v);
    }

    public LuaValue shl(long lhs) {
        return LuaInteger.valueOf(lhs << (long) v);
    }

    public LuaValue shr(LuaValue rhs) {
        return rhs.shr((long) v);
    }

    public LuaValue shr(long lhs) {
        return LuaInteger.valueOf(lhs >> (long) v);
    }

    public LuaValue bnot() {
        return LuaInteger.valueOf(~(long) v);
    }


    /**
     * Divide two double numbers according to lua math, and return a {@link LuaValue} result.
     *
     * @param lhs Left-hand-side of the division.
     * @param rhs Right-hand-side of the division.
     * @return {@link LuaValue} for the result of the division,
     * taking into account positive and negiative infinity, and Nan
     * @see #ddiv_d(double, double)
     */
    public static LuaValue ddiv(double lhs, double rhs) {
        return rhs != 0 ? valueOf(lhs / rhs) : lhs > 0 ? POSINF : lhs == 0 ? NAN : NEGINF;
    }

    /**
     * Divide two double numbers according to lua math, and return a double result.
     *
     * @param lhs Left-hand-side of the division.
     * @param rhs Right-hand-side of the division.
     * @return Value of the division, taking into account positive and negative infinity, and Nan
     * @see #ddiv(double, double)
     */
    public static double ddiv_d(double lhs, double rhs) {
        return rhs != 0 ? lhs / rhs : lhs > 0 ? Double.POSITIVE_INFINITY : lhs == 0 ? Double.NaN : Double.NEGATIVE_INFINITY;
    }

    /**
     * Take modulo double numbers according to lua math, and return a {@link LuaValue} result.
     *
     * @param lhs Left-hand-side of the modulo.
     * @param rhs Right-hand-side of the modulo.
     * @return {@link LuaValue} for the result of the modulo,
     * using lua's rules for modulo
     * @see #dmod_d(double, double)
     */
    public static LuaValue dmod(double lhs, double rhs) {
        return rhs != 0 ? valueOf(lhs - rhs * Math.floor(lhs / rhs)) : NAN;
    }

    /**
     * Take modulo for double numbers according to lua math, and return a double result.
     *
     * @param lhs Left-hand-side of the modulo.
     * @param rhs Right-hand-side of the modulo.
     * @return double value for the result of the modulo,
     * using lua's rules for modulo
     * @see #dmod(double, double)
     */
    public static double dmod_d(double lhs, double rhs) {
        return rhs != 0 ? lhs - rhs * Math.floor(lhs / rhs) : Double.NaN;
    }

    // relational operators
    public LuaValue lt(LuaValue rhs) {
        return rhs instanceof LuaNumber ? (rhs.gt_b(v) ? TRUE : FALSE) : super.lt(rhs);
    }

    public LuaValue lt(double rhs) {
        return v < rhs ? TRUE : FALSE;
    }

    public LuaValue lt(long rhs) {
        return v < rhs ? TRUE : FALSE;
    }

    public boolean lt_b(LuaValue rhs) {
        return rhs instanceof LuaNumber ? rhs.gt_b(v) : super.lt_b(rhs);
    }

    public boolean lt_b(long rhs) {
        return v < rhs;
    }

    public boolean lt_b(double rhs) {
        return v < rhs;
    }

    public LuaValue lteq(LuaValue rhs) {
        return rhs instanceof LuaNumber ? (rhs.gteq_b(v) ? TRUE : FALSE) : super.lteq(rhs);
    }

    public LuaValue lteq(double rhs) {
        return v <= rhs ? TRUE : FALSE;
    }

    public LuaValue lteq(long rhs) {
        return v <= rhs ? TRUE : FALSE;
    }

    public boolean lteq_b(LuaValue rhs) {
        return rhs instanceof LuaNumber ? rhs.gteq_b(v) : super.lteq_b(rhs);
    }

    public boolean lteq_b(long rhs) {
        return v <= rhs;
    }

    public boolean lteq_b(double rhs) {
        return v <= rhs;
    }

    public LuaValue gt(LuaValue rhs) {
        return rhs instanceof LuaNumber ? (rhs.lt_b(v) ? TRUE : FALSE) : super.gt(rhs);
    }

    public LuaValue gt(double rhs) {
        return v > rhs ? TRUE : FALSE;
    }

    public LuaValue gt(long rhs) {
        return v > rhs ? TRUE : FALSE;
    }

    public boolean gt_b(LuaValue rhs) {
        return rhs instanceof LuaNumber ? rhs.lt_b(v) : super.gt_b(rhs);
    }

    public boolean gt_b(long rhs) {
        return v > rhs;
    }

    public boolean gt_b(double rhs) {
        return v > rhs;
    }

    public LuaValue gteq(LuaValue rhs) {
        return rhs instanceof LuaNumber ? (rhs.lteq_b(v) ? TRUE : FALSE) : super.gteq(rhs);
    }

    public LuaValue gteq(double rhs) {
        return v >= rhs ? TRUE : FALSE;
    }

    public LuaValue gteq(long rhs) {
        return v >= rhs ? TRUE : FALSE;
    }

    public boolean gteq_b(LuaValue rhs) {
        return rhs instanceof LuaNumber ? rhs.lteq_b(v) : super.gteq_b(rhs);
    }

    public boolean gteq_b(long rhs) {
        return v >= rhs;
    }

    public boolean gteq_b(double rhs) {
        return v >= rhs;
    }

    // string comparison
    public int strcmp(LuaString rhs) {
        typerror("attempt to compare number with string");
        return 0;
    }

    public String tojstring() {
		/*
		if ( v == 0.0 ) { // never occurs in J2me
			long bits = Double.doubleToLongBits( v );
			return ( bits >> 63 == 0 ) ? "0" : "-0";
		}
		*/
        int l = (int) v;
        if (l == v)
            return Integer.toString(l);
        if (Double.isNaN(v))
            return JSTR_NAN;
        if (Double.isInfinite(v))
            return (v < 0 ? JSTR_NEGINF : JSTR_POSINF);
        return Double.toString(v);
    }

    public LuaString strvalue() {
        return LuaString.valueOf(tojstring());
    }

    public LuaString optstring(LuaString defval) {
        return LuaString.valueOf(tojstring());
    }

    public LuaValue tostring() {
        return LuaString.valueOf(tojstring());
    }

    public String optjstring(String defval) {
        return tojstring();
    }

    public LuaNumber optnumber(LuaNumber defval) {
        return this;
    }

    public boolean isnumber() {
        return true;
    }

    public boolean isstring() {
        return true;
    }

    public LuaValue tonumber() {
        return this;
    }

    public int checkint() {
        return (int) (long) v;
    }

    public long checklong() {
        return (long) v;
    }

    public LuaNumber checknumber() {
        return this;
    }

    public double checkdouble() {
        return v;
    }

    public String checkjstring() {
        return tojstring();
    }

    public LuaString checkstring() {
        return LuaString.valueOf(tojstring());
    }

    public boolean isvalidkey() {
        return !Double.isNaN(v);
    }
}

