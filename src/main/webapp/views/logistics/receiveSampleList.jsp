<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@include file="/common/header.jsp"%>


<div class="maincontent">
	<div class="maincontentinner">
		<div class="row-fluid" style="min-height:300px;">
			<!--  如果是其它页面，这里是填充具体的内容。 -->
			<section>
			<table class="table">
				<caption>客户下单</caption>
				<tr>
					<th>询单编号</th>
					<th>客户姓名</th>
					<th>快递名称</th>
					<th>快递单号</th>
					<th>邮寄时间</th>
					<th>操作</th>
				</tr>
				<c:forEach var="task" items="${list}">
					<tr>
						<td>${task.order.orderId}</td>
						<td>${task.order.customerName}</td>
						<td>${task.logistics.inPostSampleClothesType}</td>
						<td>${task.logistics.inPostSampleClothesNumber}</td>
						<td>${task.logistics.inPostSampleClothesTime}</td>
						<td><a
							href="${ctx}/logistics/receiveSampleDetail.do?orderId=${task.order.orderId}">详情
						</a> 
						</td>
					</tr>
				</c:forEach>
			</table>
			</section>
		</div>


		<!--row-fluid-->
		<div class="footer">
			<div class="footer-left">
				<span>&copy; 2014. 江苏南通智造链有限公司.</span>
			</div>
		</div>
		<!--footer-->
	</div>
	<!--maincontentinner-->
</div>
<!--maincontent-->



<%@include file="/common/js_file.jsp"%>
<%@include file="/common/js_form_file.jsp"%>
<link rel="stylesheet" href="${ctx}/css/fmc/table.css">
<script type="text/javascript" src="${ctx }/js/custom.js"></script>
<%@include file="/common/footer.jsp"%>