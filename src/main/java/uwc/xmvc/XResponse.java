package uwc.xmvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XResponse<T extends Object> {

	public final static int Error = -1;
	public final static String Error_Description = "request failed";
	public final static int TimeOut_Request = -2;
	public final static String TimeOut_Request_Description = "request timeout";
	public final static int Cancel_Request = 0;
	public final static String Cancel_Request_Description = "cancel request";
	public final static int Success = 1;
	public final static String Success_Description = "request success";

	private T mResponse; // 返回结果
	private String mDescription; // 返回码 描述
	private int mCode; // 返回码


	public int getCode() {
		return mCode;
	}

	public void setCode(int code) {
		this.mCode = code;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public T getResponse() {
		return mResponse;
	}

	public List getResponseByList() {
		if(null == mResponse)
			return null;
		ArrayList list = new ArrayList();
		list.addAll(Arrays.asList(mResponse.getClass().isArray()? (Object[])mResponse : new Object[]{mResponse}));
		return list;
	}

	public void setResponse(T response) {
		this.mResponse = response;
	}

	public XResponse() {
		this.mDescription = Error_Description;
		this.mCode = Error;
	}
}
