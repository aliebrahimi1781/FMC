package nju.software.service.impl;

import java.util.List;

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nju.software.dao.impl.AccountDAO;
import nju.software.dataobject.Account;
import nju.software.service.CommonService;
import nju.software.util.ActivitiAPIUtil;

@Service("commonServiceImpl")
public class CommonServiceImpl implements CommonService {

	public static final String ACTOR_ADMIN = "ADMIN";

	@Override
	public Integer getTaskNumber(String userId) {
		List<Task> tasks = isGroup(userId) ? activitiAPIUtil
				.getAssignedTasksOfGroup(userId) : activitiAPIUtil
				.getAssignedTasksOfUser(userId);
		return tasks == null ? 0 : tasks.size();
	}

	@Override
	public Integer getTaskNumber(String userId, String taskName) {
		List<Task> tasks = isGroup(userId) ? activitiAPIUtil
				.getAssignedTasksOfUserByTaskName(userId, taskName)
				: activitiAPIUtil.getAssignedTasksOfUserByTaskName(userId,
						taskName);
		return tasks == null ? 0 : tasks.size();
	}

	@Override
	public Integer getAllTaskNumber() {
		int sum = 0;
		for (int i = 0; i < groups.length; i++) {
			sum += getTaskNumber(groups[i]);
		}
		sum += getMarketStaffTaskNumber();
		return sum;
	}

	@Override
	public Integer getMarketStaffTaskNumber() {
		int sum = 0;
		List<Account> marketStaffs = accountDAO
				.findByUserRole(MarketServiceImpl.ACTOR_MARKET_STAFF);
		for (Account account : marketStaffs) {
			sum += getTaskNumber(String.valueOf(account.getUserId()));
		}
		// 这边也要加上Admin下的订单，以防万一
		List<Account> admin = accountDAO
				.findByUserRole(CommonServiceImpl.ACTOR_ADMIN);
		sum += getTaskNumber(admin.get(0).getUserId() + "");
		return sum;
	}

	@Override
	public Integer getMarketStaffTaskNumber(String taskName) {
		int sum = 0;
		List<Account> marketStaffs = accountDAO
				.findByUserRole(MarketServiceImpl.ACTOR_MARKET_STAFF);
		for (Account account : marketStaffs) {
			sum += getTaskNumber(String.valueOf(account.getUserId()), taskName);
		}
		// 这边也要加上Admin下的订单，以防万一
		List<Account> admin = accountDAO
				.findByUserRole(CommonServiceImpl.ACTOR_ADMIN);
		sum += getTaskNumber(admin.get(0).getUserId() + "", taskName);
		return sum;
	}
	
	private boolean isGroup(String userId) {
		return false;
	}

	private String[] groups = new String[] {
			BuyServiceImpl.ACTOR_PURCHASE_MANAGER,
			DesignServiceImpl.ACTOR_CRAFT_MANAGER,
			DesignServiceImpl.ACTOR_DESIGN_MANAGER,
			FinanceServiceImpl.ACTOR_FINANCE_MANAGER,
			LogisticsServiceImpl.ACTOR_LOGISTICS_MANAGER,
			MarketServiceImpl.ACTOR_MARKET_MANAGER,
			ProduceServiceImpl.ACTOR_PRODUCE_MANAGER,
			QualityServiceImpl.ACTOR_QUALITY_MANAGER,
			SweaterMakeServiceImpl.ACTOR_SWEATER_MANAGER };

	@Autowired
	private ActivitiAPIUtil activitiAPIUtil;

	@Autowired
	private AccountDAO accountDAO;
}
