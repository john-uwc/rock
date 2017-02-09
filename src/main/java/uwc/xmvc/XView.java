package uwc.xmvc;


/**
 * XView 数据视口定义.
 * xmvc中通过xmodel传递的数据将在此消费
 * @author steven
 *
 * @see XModel
 * @see XController
 */
public interface XView<T extends XModel> {

	/**
	 * 请求成功，返回请求数据, 涉及界面更新时,必须运行在UI线程
	 * @param model
	 * @param service
	 */
	public void jetData(T model, String service);

	/**
	 * 请求异常，将错误码和错误信息返回自行处理
	 * @param service
	 * @param errorCode
	 * @param errorMessage
	 */
	public void handleAbnormal(String service, int errorCode, String errorMessage);

}
