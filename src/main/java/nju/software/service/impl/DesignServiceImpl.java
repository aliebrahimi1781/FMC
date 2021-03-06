package nju.software.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nju.software.dao.impl.CraftDAO;
import nju.software.dao.impl.DesignCadDAO;
import nju.software.dao.impl.OrderDAO;
import nju.software.dao.impl.QuoteDAO;
import nju.software.dataobject.Craft;
import nju.software.dataobject.DesignCad;
import nju.software.dataobject.Order;
import nju.software.dataobject.Quote;
import nju.software.service.DesignService;
import nju.software.util.ActivitiAPIUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("designServiceImpl")
public class DesignServiceImpl implements DesignService {

	// ===========================设计验证=================================
	@Override
	public List<Map<String, Object>> getVerifyDesignList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_VERIFY_DESIGN);
	}

	@Override
	public List<Map<String, Object>> getSearchVerifyDesignList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_VERIFY_DESIGN);
	}

	@Override
	public Map<String, Object> getVerifyDesignDetail(int orderId) {
		return service.getBasicOrderModelWithQuote(ACTOR_DESIGN_MANAGER,
				TASK_VERIFY_DESIGN, orderId);
	}

	@Override
	public boolean verifyDesignSubmit(String taskId, boolean result,
			String comment) {
		Map<String, Object> data = new HashMap<>();
		data.put(RESULT_DESIGN, result);
		data.put(RESULT_DESIGN_COMMENT, comment);
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<Map<String, Object>> getComputeDesignCostList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER,
				TASK_COMPUTE_DESIGN_COST);
	}

	@Override
	public List<Map<String, Object>> getSearchComputeDesignCostList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_COMPUTE_DESIGN_COST);
	}

	@Override
	public Map<String, Object> getComputeDesignCostInfo(Integer orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_COMPUTE_DESIGN_COST, orderId);
		DesignCad designcad = designCadDAO.findByOrderId(orderId).get(0);
		model.put("designCadTech", designcad.getCadTech());
		return model;
	}

	// 计算设计工艺费用
	@Override
	public void computeDesignCostSubmit(int orderId, String taskId,
			boolean result, String comment, short needCraft,
			float stampDutyMoney, float washHangDyeMoney, float laserMoney,
			float embroideryMoney, float crumpleMoney, float openVersionMoney) {

		List<Craft> craftList = craftDAO.findByOrderId(orderId);
		for (Craft craft : craftList) {
			craftDAO.delete(craft);
		}
		Craft craft = new Craft();
		craft.setNeedCraft(needCraft);
		craft.setOrderId(orderId);
		craft.setStampDutyMoney(stampDutyMoney);
		craft.setWashHangDyeMoney(washHangDyeMoney);
		craft.setLaserMoney(laserMoney);
		craft.setEmbroideryMoney(embroideryMoney);
		craft.setCrumpleMoney(crumpleMoney);
		craft.setOpenVersionMoney(openVersionMoney);
		craft.setOrderSampleStatus("1");
		craftDAO.save(craft);
		Quote quote = quoteDAO.findById(orderId);
		// 单件工艺制作费（不包含开版费用）
		float craftCost = stampDutyMoney + washHangDyeMoney + laserMoney
				+ embroideryMoney + crumpleMoney;

		if (null == quote) {
			quote = new Quote();
			quote.setCraftCost(craftCost);
			quote.setOrderId(orderId);
			quoteDAO.save(quote);
		}

		// 重新计算单件成本
		float produceCost = quote.getCutCost() + quote.getManageCost()
				+ quote.getDesignCost() + quote.getIroningCost()
				+ quote.getNailCost() + quote.getPackageCost()
				+ quote.getSwingCost() + quote.getOtherCost();
		float singleCost = craftCost + quote.getFabricCost()
				+ quote.getAccessoryCost() + produceCost;

		quote.setCraftCost(craftCost);
		quote.setSingleCost(singleCost);
		quoteDAO.attachDirty(quote);

		Map<String, Object> data = new HashMap<String, Object>();
		boolean isNeedCraft = false;
		if (needCraft == 1) {
			isNeedCraft = true;
		}
		data.put(RESULT_NEED_CRAFT, isNeedCraft);
		data.put(RESULT_DESIGN, result);
		data.put(RESULT_DESIGN_COMMENT, comment);

		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// ===========================上传版型=================================
	@Override
	public List<Map<String, Object>> getUploadDesignList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_UPLOAD_DESIGN);
	}

	@Override
	public List<Map<String, Object>> getSearchUploadDesignList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_UPLOAD_DESIGN);
	}

	@Override
	public Map<String, Object> getUploadDesignDetail(Integer orderId) {
		return service.getBasicOrderModelWithQuote(ACTOR_DESIGN_MANAGER,
				TASK_UPLOAD_DESIGN, orderId);
	}

	@Override
	public void uploadDesignSubmit(int orderId, String taskId, String url,
			Timestamp uploadTime) {
		DesignCad designCad = null;
		List<DesignCad> designCadList = designCadDAO.findByOrderId(orderId);
		if (designCadList.isEmpty()) {
			designCad = new DesignCad();
			designCad.setOrderId(orderId);
			designCad.setCadVersion((short) 1);
		} else {
			designCad = designCadList.get(0);
			short newVersion = (short) (designCad.getCadVersion() + 1);
			designCad.setCadVersion(newVersion);
		}
		designCad.setCadUrl(url);
		designCad.setUploadTime(uploadTime);
		designCadDAO.attachDirty(designCad);

		// Map<String, Object> data = new HashMap<String, Object>();
		// try {
		// jbpmAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		// return true;
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// return false;
		// }
	}

	@Override
	public void EntryCadData(int orderId, String taskId, String url,
			Timestamp uploadTime, String cadSide, Timestamp completeTime) {
		DesignCad designCad = null;
		List<DesignCad> designCadList = designCadDAO.findByOrderId(orderId);
		if (designCadList.isEmpty()) {
			designCad = new DesignCad();
			designCad.setOrderId(orderId);
			designCad.setCadVersion((short) 1);
		} else {
			designCad = designCadList.get(0);
			short newVersion = (short) (designCad.getCadVersion() + 1);
			designCad.setCadVersion(newVersion);
		}
		designCad.setCadUrl(url);
		designCad.setUploadTime(uploadTime);
		designCad.setCadSide(cadSide);
		designCad.setCompleteTime(completeTime);
		designCadDAO.attachDirty(designCad);
	}

	// ===========================样衣生产提交========================================
	@Override
	public boolean produceSampleSubmit(String taskId, boolean result) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(RESULT_PRODUCE, result);
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean produceSampleSubmit(String taskId, boolean result,
			String orderId) {
		Order order = orderDAO.findById(Integer.parseInt(orderId));
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(RESULT_PRODUCE, result);
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		
		if (result == false) {// 如果result的的值为FALSE，即为样衣生产失败，流程会异常终止，将orderState设置为1
			order.setOrderState("1");
			orderDAO.merge(order);
			
		}
		return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ===========================修改版型=================================
	@Override
	public List<Map<String, Object>> getModifyDesignList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_MODIFY_DESIGN);
	}

	@Override
	public List<Map<String, Object>> getSearchModifyDesignList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_MODIFY_DESIGN);
	}

	@Override
	public Map<String, Object> getModifyDesignDetail(Integer orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_MODIFY_DESIGN, orderId);

		if (designCadDAO.findByOrderId(orderId) != null
				&& designCadDAO.findByOrderId(orderId).size() != 0) {
			if (((Order) model.get("order")).getIsNeedSampleClothes() == 1) {
				model.put("cad", designCadDAO.findByOrderId(orderId).get(0));
			}
		}
		return model;
	}

	@Override
	public boolean modifyDesignSubmit(int orderId, String taskId, String url,
			Timestamp uploadTime) {
		DesignCad designCad = null;
		List<DesignCad> designCadList = designCadDAO.findByOrderId(orderId);
		if (designCadList.isEmpty()) {
			designCad = new DesignCad();
			designCad.setOrderId(orderId);
			designCad.setCadVersion((short) 1);
		} else {
			designCad = designCadList.get(0);
			short newVersion = (short) (designCad.getCadVersion() + 1);
			designCad.setCadVersion(newVersion);
		}
		designCad.setCadUrl(url);
		designCad.setUploadTime(uploadTime);
		designCadDAO.attachDirty(designCad);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put(RESULT_DESIGN, true);
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ===========================确认版型=================================
	@Override
	public List<Map<String, Object>> getConfirmDesignList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_CONFIRM_DESIGN);
	}

	// ===========================排版切片前最终确认版型=================================
	@Override
	public List<Map<String, Object>> getConfirmCadList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_CONFIRM_CAD);
	}

	@Override
	public List<Map<String, Object>> getSearchConfirmCadList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_CONFIRM_CAD);
	}

	// 获得需要工艺制作的大货订单列表
	@Override
	public List<Map<String, Object>> getNeedCraftList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_CRAFT_PRODUCT);
	}

	@Override
	public List<Map<String, Object>> getSearchNeedCraftList(String ordernumber,
			String customername, String stylename, String startdate,
			String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_CRAFT_PRODUCT);
	}

	// 获得需要工艺制作的大货订单
	@Override
	public Map<String, Object> getNeedCraftProductDetail(int orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_CRAFT_PRODUCT, orderId);
		DesignCad designcad = designCadDAO.findByOrderId(orderId).get(0);
		model.put("designCadTech", designcad.getCadTech());
		Craft craft = craftDAO.findByOrderId(orderId).get(0);
		model.put("craft", craft);
		return model;
	}

	@Override
	public void needCraftProductSubmit(int orderId, String taskId,
			String crafsManName, Timestamp crafsProduceDate) {
		Map<String, Object> data = new HashMap<String, Object>();
		Craft craft = craftDAO.findByOrderId(orderId).get(0);
		craft.setCrafsManName(crafsManName);
		craft.setCrafsProduceDate(crafsProduceDate);
		craftDAO.attachDirty(craft);
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	// 获得需要工艺制作的样衣订单列表
	@Override
	public List<Map<String, Object>> getNeedCraftSampleList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER, TASK_CRAFT_SAMPLE);
	}

	@Override
	public List<Map<String, Object>> getSearchNeedCraftSampleList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {

		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_CRAFT_SAMPLE);
	}

	// 获得需要工艺制作的样衣订单
	@Override
	public Map<String, Object> getNeedCraftSampleDetail(int orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_CRAFT_SAMPLE, orderId);
		DesignCad designcad = designCadDAO.findByOrderId(orderId).get(0);
		model.put("designCadTech", designcad.getCadTech());
		Craft craft = craftDAO.findByOrderId(orderId).get(0);
		model.put("sampleCraft", craft);
		return model;
	}

	@Override
	public void uploadCraftFileSubmit(int orderId, String craftFileUrl) {
		Craft craft = craftDAO.findByOrderId(orderId).get(0);
		craft.setCraftFileUrl(craftFileUrl);
		craftDAO.merge(craft);

	}

	@Override
	public void needCraftSampleSubmit(int orderId, String taskId,
			String craftLeader, Timestamp completeTime) {
		Craft craft = craftDAO.findByOrderId(orderId).get(0);
		craft.setCraftLeader(craftLeader);
		craft.setCompleteTime(completeTime);
		craft.setOrderSampleStatus("4");
		craftDAO.merge(craft);

		Map<String, Object> data = new HashMap<String, Object>();
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 获得需要排版切片订单列表
	@Override
	public List<Map<String, Object>> getTypeSettingSliceList() {
		return service.getOrderList(ACTOR_DESIGN_MANAGER,
				TASK_TYPESETTING_SLICE);
	}

	@Override
	public List<Map<String, Object>> getSearchTypeSettingSliceList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_TYPESETTING_SLICE);

	}

	// 获得需要排版切片订单
	@Override
	public Map<String, Object> getTypeSettingSliceDetail(int orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_TYPESETTING_SLICE, orderId);
		return model;
	}

	@Override
	public void getTypeSettingSliceSubmit(int orderId, String taskId) {
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getTypeSettingSliceSubmit(int orderId, String cad_side,
			String taskId) {
		DesignCad designCad = null;
		List<DesignCad> designCadList = designCadDAO.findByOrderId(orderId);
		if (designCadList.isEmpty()) {
			designCad = new DesignCad();
			designCad.setOrderId(orderId);
			designCad.setCadVersion((short) 1);
			designCad.setCadSide(cad_side);
		} else {
			designCad = designCadList.get(0);
			designCad.setCadSide(cad_side);
		}

		designCadDAO.attachDirty(designCad);
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Map<String, Object>> getSearchConfirmDesignList(
			String ordernumber, String customername, String stylename,
			String startdate, String enddate, Integer[] employeeIds) {
		return service.getSearchOrderList(ACTOR_DESIGN_MANAGER, ordernumber,
				customername, stylename, startdate, enddate, employeeIds,
				TASK_CONFIRM_DESIGN);

	}

	@Override
	public Map<String, Object> getConfirmDesignDetail(Integer orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_CONFIRM_DESIGN, orderId);
		model.put("cad", designCadDAO.findByOrderId(orderId).get(0));
		return model;
	}

	// 取得确认版型数据的详细信息
	@Override
	public Map<String, Object> getConfirmCadDetail(int orderId) {
		Map<String, Object> model = service.getBasicOrderModelWithQuote(
				ACTOR_DESIGN_MANAGER, TASK_CONFIRM_CAD, orderId);
		model.put("cad", designCadDAO.findByOrderId(orderId).get(0));
		return model;
	}

	// 重新提交版型数据
	@Override
	public boolean confirmCadSubmit(int orderId, String taskId, String cadurl,
			Timestamp uploadTime) {
		DesignCad designCad = null;
		List<DesignCad> designCadList = designCadDAO.findByOrderId(orderId);
		if (designCadList.isEmpty()) {
			designCad = new DesignCad();
			designCad.setOrderId(orderId);
			designCad.setCadVersion((short) 1);
		} else {
			designCad = designCadList.get(0);
			short newVersion = (short) (designCad.getCadVersion() + 1);
			designCad.setCadVersion(newVersion);
		}
		designCad.setCadUrl(cadurl);
		designCad.setUploadTime(uploadTime);
		designCadDAO.attachDirty(designCad);

		Map<String, Object> data = new HashMap<String, Object>();
		try {
			activitiAPIUtil.completeTask(taskId, data, ACTOR_DESIGN_MANAGER);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public final static String ACTOR_DESIGN_MANAGER = "designManager";
	public final static String ACTOR_CRAFT_MANAGER = "craftManager";
	
	public final static String TASK_VERIFY_DESIGN = "verifyDesign";
	public final static String TASK_COMPUTE_DESIGN_COST = "computeDesignCost";
	public final static String TASK_UPLOAD_DESIGN = "uploadDegisn";
	public final static String TASK_MODIFY_DESIGN = "modifyDesign";
	public final static String TASK_CONFIRM_DESIGN = "confirmDesign";
	public final static String TASK_CRAFT_SAMPLE = "craftSample";
	public final static String TASK_CRAFT_PRODUCT = "craft";
	public final static String TASK_TYPESETTING_SLICE = "typeSettingSlice";
	public final static String TASK_CONFIRM_CAD = "confirmCad";
	
	public final static String RESULT_DESIGN = "design";
	public final static String RESULT_DESIGN_COMMENT = "designComment";
	public final static String RESULT_NEED_CRAFT = "needCraft";
	public final static String RESULT_PRODUCE = "produce";

	@Autowired
	private ActivitiAPIUtil activitiAPIUtil;
	@Autowired
	private ServiceUtil service;
	@Autowired
	private DesignCadDAO designCadDAO;
	@Autowired
	private QuoteDAO quoteDAO;
	@Autowired
	private CraftDAO craftDAO;
	@Autowired
	private OrderDAO orderDAO;

	@Override
	// 获取订单样衣状态
	public String getCraftInfo(Integer orderId) {
		List<Craft> list = craftDAO.findByOrderId(orderId);
		Craft craft = list.get(0);
		return craft.getOrderSampleStatus();
	}

}