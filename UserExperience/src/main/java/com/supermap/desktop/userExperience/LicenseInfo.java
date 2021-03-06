package com.supermap.desktop.userExperience;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.supermap.data.ProductType;
import com.supermap.desktop.interfaces.UserExperienceBean;
import com.supermap.desktop.properties.CoreProperties;

/**
 * @author XiaJT
 */
public class LicenseInfo implements UserExperienceBean, JSONAware {
	private ProductType[] currentLicenseType;

	public LicenseInfo(ProductType... currentLicenseType) {
		this.currentLicenseType = currentLicenseType;
	}

	@Override
	public String getJson() {
		JSONArray array = new JSONArray();
		for (ProductType productType : currentLicenseType) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("LicenseMode", productType == null ? CoreProperties.getString("String_onlineLicense") : CoreProperties.getString("String_localLicense"));
			jsonObject.put("LicenseName", productType == null ? "" : productType.name());
			array.add(jsonObject);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("LicenseInfos", array);
		return jsonObject.toJSONString();
	}

	@Override
	public String toJSONString() {
		return getJson();
	}
}
