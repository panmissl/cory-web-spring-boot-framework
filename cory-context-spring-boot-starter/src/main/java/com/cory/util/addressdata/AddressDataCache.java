package com.cory.util.addressdata;

import com.cory.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressDataCache {
	
	private static final Logger log = LoggerFactory.getLogger(AddressDataCache.class);
	
	private static final String DELIMITER = ",";
	
	private static List<AddressData> provinceList = new ArrayList<AddressData>();
	
	/** key: provinceId, value: AddressData */
	private static Map<Integer, AddressData> cache = new HashMap<Integer, AddressData>();
	
	/** key: code, value: AddressData */
	private static Map<String, AddressData> codeMap = new HashMap<String, AddressData>();
	
	static {
		loadData();
	}

	private static void loadData() {
		try {
			InputStream inputStream = AddressDataCache.class.getClassLoader().getResourceAsStream("address.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Constants.UTF8));
			reader.lines().forEach(line -> {
				line = line.trim();
				if (line.length() > 0 && !line.startsWith("#")) {
					addLineToListAndMap(line);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 将一行地址数据添加到list（如果是省份），还有map中
	 * @param line
	 */
	private static void addLineToListAndMap(String line) {
		//110003,130200,唐山市,130000
		
		String[] arr = line.split(DELIMITER);
		if (arr.length == 3) {
			//省份
			AddressData ad = new AddressData();
			ad.setId(Integer.valueOf(arr[0].trim()));
			ad.setCode(arr[1].trim());
			ad.setName(arr[2].trim());
			
			provinceList.add(ad);
			cache.put(ad.getId(), ad);
			codeMap.put(ad.getCode(), ad);
		} else if (arr.length == 4) {
			//市，区
			AddressData ad = new AddressData();
			ad.setId(Integer.valueOf(arr[0].trim()));
			ad.setCode(arr[1].trim());
			ad.setName(arr[2].trim());
			
			cache.put(ad.getId(), ad);
			codeMap.put(ad.getCode(), ad);
			
			String parentCode = arr[3].trim();
			addToParentsChildren(parentCode, ad);
		} else {
			//错误，丢弃
		}
	}

	/**
	 * 将AddressData添加到其父的childrenList里面
	 * @param parentCode
	 */
	private static void addToParentsChildren(String parentCode, AddressData ad) {
		AddressData parent = codeMap.get(parentCode);
		if (null == parent) {
			return;
		}
		
		List<AddressData> children = cache.get(parent.getId()).getChildren();
		if (null == children) {
			children = new ArrayList<AddressData>();
			cache.get(parent.getId()).setChildren(children);
		}
		children.add(ad);
	}

	public static AddressData getProvince(Integer provinceId) {
		return cache.get(provinceId);
	}

	public static AddressData getCity(Integer provinceId, Integer cityId) {
		AddressData city = cache.get(cityId);
		
		if (null == city) {
			log.info("当前地址省市区三级数据库没有这个市(" + cityId + ")，请检查");
		}
		
		return city;
	}
	
	public static AddressData getCounty(Integer provinceId, Integer cityId, Integer countyId) {
		AddressData county = cache.get(countyId);
		
		if (null == county) {
			log.info("当前地址省市区三级数据库没有这个区(" + provinceId + ")，请检查");
		}
		
		return county;
	}
	
	public static List<AddressData> getProvinces() {
		return provinceList;
	}
	
	public static List<AddressData> getCities(Integer provinceId) {
		return getProvince(provinceId).getChildren();
	}
	
	public static List<AddressData> getCounties(Integer provinceId, Integer cityId) {
		return getCity(provinceId, cityId).getChildren();
	}
	
	public static int getProvinceIdByName(String name) {
		int id = -1;
		for (AddressData p : provinceList) {
			if (p.getName().equalsIgnoreCase(name)) {
				id = p.getId();
				break;
			}
		}
		return id;
	}
	
	public static int getCityIdByName(int provinceId, String name) {
		//如果上一级就是-1了,直接返回-1,以免出现空指针错
		if (provinceId == -1) {
			return -1;
		}
		int id = -1;
		List<AddressData> cities = getCities(provinceId);
		for (AddressData p : cities) {
			if (p.getName().equalsIgnoreCase(name)) {
				id = p.getId();
				break;
			}
		}
		return id;
	}
	
	public static int getCountyIdByName(int provinceId, int cityId, String name) {
		//如果上一级就是-1了,直接返回-1,以免出现空指针错
		if (provinceId == -1 || cityId == -1) {
			return -1;
		}
		int id = -1;
		List<AddressData> counties = getCounties(provinceId, cityId);
		for (AddressData p : counties) {
			if (p.getName().equalsIgnoreCase(name)) {
				id = p.getId();
				break;
			}
		}
		return id;
	}
	
	public static void main(String[] args) {
		List<AddressData> ps = AddressDataCache.getCounties(1000001, 110001);
		for (AddressData ad : ps) {
			System.out.println(ad.getId() + ad.getName() + ad.getCode());
		}
	}
}
