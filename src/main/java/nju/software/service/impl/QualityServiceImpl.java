package nju.software.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nju.software.dao.impl.CustomerDAO;
import nju.software.dao.impl.EmployeeDAO;
import nju.software.dao.impl.OrderDAO;
import nju.software.dataobject.Order;
import nju.software.model.OrderInfo;
import nju.software.model.OrderModel;
import nju.software.service.QualityService;
import nju.software.util.JbpmAPIUtil;

@Service("qualityServiceImpl")
public class QualityServiceImpl implements QualityService{
	
	
	public final static String ACTOR_QUALITY_MANAGER = "qualityManager";
	public final static String TASK_CHECK_QUALITY = "checkQuality";
	
	
	
	
	

	@Autowired
	private OrderDAO orderDAO;
	@Autowired
	private EmployeeDAO employeeDAO;
	@Autowired
	private CustomerDAO customerDAO;
	@Autowired
	private JbpmAPIUtil jbpmAPIUtil;
	@Override
	public List<OrderInfo> getCheckQualityList() {
		// TODO Auto-generated method stub
		List<TaskSummary> tasks = jbpmAPIUtil.getAssignedTasksByTaskname(
				ACTOR_QUALITY_MANAGER, TASK_CHECK_QUALITY);
		List<OrderInfo> taskSummarys = new ArrayList<>();
		for (TaskSummary task : tasks) {
			Integer orderId = (Integer) getVariable("orderId", task);
			OrderInfo oi = new OrderInfo();
			oi.setOrder(orderDAO.findById(orderId));
			oi.setTask(task);
			taskSummarys.add(oi);
		}
		return taskSummarys;
	}
	
	public Object getVariable(String name, TaskSummary task) {
		StatefulKnowledgeSession session = jbpmAPIUtil.getKsession();
		long processId = task.getProcessInstanceId();
		WorkflowProcessInstance process = (WorkflowProcessInstance) session
				.getProcessInstance(processId);
		return process.getVariable(name);
	}

	@Override
	public boolean checkQualitySubmit(int id, long taskId, long processId, boolean b) {
		// TODO Auto-generated method stub
		Map<String, Object> data = new HashMap<>();
		//data.put("", b);
		try {
			jbpmAPIUtil.completeTask(taskId, data, ACTOR_QUALITY_MANAGER);
			return true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public OrderInfo getCheckQualityDetail(int orderId, long taskId) {
		// TODO Auto-generated method stub
		List<TaskSummary> tasks = jbpmAPIUtil.getAssignedTasksByTaskname(
				ACTOR_QUALITY_MANAGER, TASK_CHECK_QUALITY);
		for (TaskSummary task : tasks) {
			Integer id = (Integer) getVariable("orderId", task);
			if(id==orderId && taskId==task.getId()){
				OrderInfo oi = new OrderInfo();
				Order o = orderDAO.findById(orderId);
				oi.setOrder(o);
				oi.setEmployee(employeeDAO.findById(o.getEmployeeId()));
				oi.setCustomer(customerDAO.findById(o.getCustomerId()));
				oi.setTask(task);
				return oi;
			}
		}
		return null;
	}
	
}