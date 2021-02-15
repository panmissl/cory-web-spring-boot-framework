package com.cory.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 对金额的计算类。
 * <p>
 * 所有金额都用此类计算，，精确到2位小数。
 * <p>
 * 不要直接用double相加减乘除，因为这样可能会出精度上的问题。
 * 
 * @author Cory
 *
 */
public final class CurrencyUtil {
	
	// 默认除法运算精度
	private static final int DEF_DIV_SCALE = 2;
	
	private CurrencyUtil() {
	}
	
	private static DecimalFormat format = new DecimalFormat("0.00");

	/**
	 * 格式化金额 -- 保留两位小数
	 * @param money
	 * @return
	 */
	public static String format(Double money) {
		if (null == money) {
			return "0.00";
		}
		return format.format(money);
	}

	/**
	 * 加：V1 + V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double add(Double v1, Double v2) {
		if (null == v1) {
			v1 = 0.0d;
		}
		if (null == v2) {
			v2 = 0.0d;
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}
	
	/**
	 * 减：V1 - V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double sub(Double v1, Double v2) {
		if (null == v1) {
			v1 = 0.0d;
		}
		if (null == v2) {
			v2 = 0.0d;
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}
	
	/**
	 * 乘：V1 * V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static Double multiply(Double v1, Double v2) {
		if (null == v1) {
			v1 = 0.0d;
		}
		if (null == v2) {
			v2 = 0.0d;
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}
	
	/**
	 * 乘：V1 * V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static Double multiply(Double v1, Integer v2) {
		if (null == v1) {
			v1 = 0.0d;
		}
		if (null == v2) {
			v2 = 0;
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Integer.toString(v2));
		return b1.multiply(b2).doubleValue();
	}
	
	/**
	 * 除：V1 / V2
	 * <p>
	 * 精确到2位小数
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double divide(Double v1, Double v2) {
		return divide(v1, v2, DEF_DIV_SCALE);
	}
	
	/**
	 * 除：V1 / V2
	 * <p>
	 * 指定精确的位数，默认为2位
	 * 
	 * @param v1
	 * @param v2
	 * @param scale 精确度，默认精确到2位小数点
	 * @return
	 */
	public static double divide(Double v1, Double v2, int scale) {
		if (null == v1) {
			v1 = 0.0d;
		}
		if (null == v2) {
			v2 = 0.0d;
		}
		if (!(v2 > 0)) {
			throw new IllegalArgumentException("除数V2必须大于0，不能等于或者小于0.");
		}
		if (scale < 0) {
			throw new IllegalArgumentException("精确位数必须是大于0的整数.");
		}
		
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 四舍五入，精确到2位小数
	 * 
	 * @param v
	 * @return
	 */
	public static double round(Double v) {
		return round(v, DEF_DIV_SCALE);
	}
	
	/**
	 * 四舍五入
	 * 
	 * @param v
	 * @param scale 精确位数 -- 大于0的整数.
	 * @return
	 */
	public static double round(Double v, int scale) {
		if (null == v) {
			v = 0.0d;
		}
		if (scale < 0) {
			throw new IllegalArgumentException("精确位数必须是大于0的整数.");
		}
		
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * V1 是否大于 V2
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean isBigger(Double v1, Double v2) {
		return v1 > v2;
	}
	
	/**
	 * V1 是否小于 V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean isSmaller(Double v1, Double v2) {
		return v1 < v2;
	}
	
	/**
	 * V1 是否等于 V2
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean isEqual(Double v1, Double v2) {
		return 0 == Double.compare(v1, v2);
	}
}
