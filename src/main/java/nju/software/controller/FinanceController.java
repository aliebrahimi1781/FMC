﻿package nju.software.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nju.software.dataobject.Account;
import nju.software.dataobject.Money;
import nju.software.model.OrderInfo;
import nju.software.service.FinanceService;
import nju.software.service.impl.FinanceServiceImpl;
import nju.software.service.impl.JbpmTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 莫其凡
 * @date 2014/04/11
 */
@Controller
public class FinanceController {

	
	// ===========================样衣金确认=================================
	@RequestMapping(value = "/finance/confirmSampleMoneyList.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmSampleMoneyList(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		List<OrderInfo> list = financeService
				.getConfirmSampleMoneyList(actorId);
		if (list.size() == 0) {
			jbpmTest.completeConfirmQuote(getActorId(request));
			list = financeService.getConfirmSampleMoneyList(actorId);
		}
		model.addAttribute("list", list);
		return "/finance/confirmSampleMoneyList";
	}

	
	@RequestMapping(value = "/finance/confirmSampleMoneyDetail.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmSampleMoneyDetail(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String orderId = request.getParameter("orderId");
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		OrderInfo orderInfo = financeService.getConfirmSampleMoneyDetail(
				actorId, Integer.parseInt(orderId));
		model.addAttribute("orderInfo", orderInfo);
		return "/finance/confirmSampleMoneyDetail";
	}

	
	@RequestMapping(value = "/finance/confirmSampleMoneySubmit.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmSampleMoneySubmit(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String orderId_string = request.getParameter("orderId");
		int orderId = Integer.parseInt(orderId_string);
		String taskId_string = request.getParameter("taskId");
		long taskId = Long.parseLong(taskId_string);
		boolean result = request.getParameter("result").equals("1");
		Money money = null;
		if (result) {
			money = getMoney(request);
			money.setOrderId(orderId);
		}
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		financeService.confirmSampleMoneySubmit(actorId, taskId,
				result, money);
		return "forward:/finance/confirmSampleMoneyList.do";
	}

	
	// ===========================定金确认===================================
	@RequestMapping(value = "/finance/confirmDepositList.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmDepositList(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		List<OrderInfo> list = financeService.getConfirmDepositList(actorId);
		model.addAttribute("list", list);
		return "/finance/confirmDepositList";
	}

	
	@RequestMapping(value = "/finance/confirmDepositDetail.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmDepositDetail(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String orderId = request.getParameter("orderId");
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		OrderInfo orderInfo = financeService.getConfirmDepositDetail(actorId,
				Integer.parseInt(orderId));
		model.addAttribute("orderInfo", orderInfo);
		return "/finance/confirmDepositDetail";
	}

	
	@RequestMapping(value = "/finance/confirmDepositSubmit.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmDepositSubmit(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {

		String orderId_string = request.getParameter("orderId");
		int orderId = Integer.parseInt(orderId_string);
		String taskId_string = request.getParameter("taskId");
		long taskId = Long.parseLong(taskId_string);
		boolean result = request.getParameter("result").equals("1");
		Money money = null;

		if (result) {
			money = getMoney(request);
			money.setOrderId(orderId);
		}
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		financeService.confirmDepositSubmit(actorId, taskId, result, money);
		return "forward:/finance/confirmDepositList.do";
	}

	
	// ===========================尾款确认===================================
	@RequestMapping(value = "finance/confirmFinalPaymentList.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmFinalPaymentList(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {

		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		List<OrderInfo> list = financeService
				.getConfirmFinalPaymentList(actorId);
		model.addAttribute("list", list);
		return "/finance/confirmFinalPaymentList";

	}

	
	@RequestMapping(value = "/finance/confirmFinalPaymentDetail.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmFinalPaymentDetail(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String orderId = request.getParameter("orderId");
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		OrderInfo orderInfo = financeService.getConfirmFinalPaymentDetail(
				actorId, Integer.parseInt(orderId));
		model.addAttribute("orderInfo", orderInfo);
		return "/finance/confirmFinalPaymentDetail";
	}

	
	@RequestMapping(value = "/finance/confirmFinalPaymentSubmit.do")
	@Transactional(rollbackFor = Exception.class)
	public String confirmFinalPaymentSubmit(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {

		String orderId_string = request.getParameter("orderId");
		int orderId = Integer.parseInt(orderId_string);
		String taskId_string = request.getParameter("taskId");
		long taskId = Long.parseLong(taskId_string);
		boolean result = request.getParameter("result").equals("1");
		Money money = null;

		if (result) {
			money = getMoney(request);
			money.setOrderId(orderId);
		}
		
		String actorId = FinanceServiceImpl.ACTOR_FINANCE_MANAGER;
		financeService.confirmFinalPaymentSubmit(actorId, taskId, result,
				money);
		return "forward:/finance/confirmFinalPaymentList.do";
	}

	
	private String getActorId(HttpServletRequest request) {
		Account account = (Account) request.getSession().getAttribute(
				"cur_user");
		System.out.println(account.getUserId());
		return account.getUserId() + "";
	}

	
	private Money getMoney(HttpServletRequest request) {
		String money_amount_string = request.getParameter("money_amount");
		double moneyAmount = Double.parseDouble(money_amount_string);
		String moneyState = request.getParameter("money_state");
		String moneyType = request.getParameter("money_type");
		String moneyBank = request.getParameter("money_bank");
		String moneyName = request.getParameter("money_name");
		String moneyNumber = request.getParameter("money_number");
		String moneyRemark = request.getParameter("money_remark");

		Money money = new Money();
		money.setMoneyAmount(moneyAmount);
		money.setMoneyState(moneyState);
		money.setMoneyType(moneyType);
		money.setMoneyBank(moneyBank);
		money.setMoneyName(moneyName);
		money.setMoneyNumber(moneyNumber);
		money.setMoneyRemark(moneyRemark);
		return money;
	}

	@Autowired
	private FinanceService financeService;
	@Autowired
	private JbpmTest jbpmTest;
}
