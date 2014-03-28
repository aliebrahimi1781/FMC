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

import nju.software.dao.impl.AccessoryDAO;
import nju.software.dao.impl.FabricDAO;
import nju.software.dao.impl.LogisticsDAO;
import nju.software.dao.impl.OrderDAO;
import nju.software.dao.impl.QuoteDAO;
import nju.software.dataobject.Quote;
import nju.software.model.QuoteConfirmTaskSummary;
import nju.software.service.MarketService;
import nju.software.util.JbpmAPIUtil;

@Service("marketServiceImpl")
public class MarketServiceImpl implements MarketService {

	@Autowired
	private OrderDAO orderDAO;
	@Autowired
	private JbpmAPIUtil jbpmAPIUtil;
	@Autowired
	private QuoteDAO quoteDAO;

	@Override
	public List<QuoteConfirmTaskSummary> getQuoteConfirmTaskSummaryList(
			Integer employeeId) {
		// TODO Auto-generated method stub
		List<TaskSummary> tasks = jbpmAPIUtil.getAssignedTasksByTaskname(
				"SHICHANGZHUANYUAN", "confirm_quoteorder");
		List<QuoteConfirmTaskSummary> taskSummarys = new ArrayList<>();
		for (TaskSummary task : tasks) {
			if (getVariable("employeeId", task).equals(employeeId)) {
				Integer orderId = (Integer) getVariable("orderId", task);
				QuoteConfirmTaskSummary summary = QuoteConfirmTaskSummary
						.getInstance(orderDAO.findById(orderId),
								(Quote) quoteDAO.findByProperty("order_id", orderId).get(0),task.getId());
				taskSummarys.add(summary);
			}
		}
		return taskSummarys;
	}

	@Override
	public void completeQuoteConfirmTaskSummary(long taskId, String result) {
		// TODO Auto-generated method stub
		Map<String,Object> data=new HashMap<String,Object>();
		if(result.equals("1")){
			data.put("confirmquote", true);
			data.put("eidtquote", false);
		}
		if(result.equals("2")){
			data.put("confirmquote", false);
			data.put("eidtquote", true);
		}
		if(result.equals("3")){
			data.put("confirmquote", false);
			data.put("eidtquote", false);
		}
		try {
			jbpmAPIUtil.completeTask(taskId, data, "SHICHANGZHUANYUAN");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public Object getVariable(String name, TaskSummary task) {
		StatefulKnowledgeSession session = jbpmAPIUtil.getKsession();
		long processId = task.getProcessInstanceId();
		WorkflowProcessInstance process = (WorkflowProcessInstance) session
				.getProcessInstance(processId);
		return process.getVariable(name);
	}
}