<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>物流</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<script type="text/javascript" src="${ctx }/js/jquery-1.9.1.min.js"></script>
<script type="text/javascript">
$(function() {
	var $tw = $("#txtWare");
	var $ts = $("#txtShelf");
	var $tl = $("#txtLocation");
	function checkPackageId(pid) {
		var found = false;
		var pl = $("#package_list").find("table");
		for(var i=0;i<pl.length;i++) {
			var $p = $(pl[i]), _pid = $p.attr("id").split("_")[1];
			if(_pid===pid) {
				found = true;
				$tw.val($p.find("tr:eq(0) td:eq(1)").text());
				$ts.val($p.find("tr:eq(1) td:eq(1)").text());
				$tl.val($p.find("tr:eq(1) td:eq(1)").text());
				break;
			}
		}
		if(found) {
			$tw.attr("disabled", false);
			$ts.attr("disabled", false);
			$tl.attr("disabled", false);
		} else {
			alert("错误，扫描包号不属于当前订单！");
		}
	}
	$("#txtScan").keydown(function(e) {
		if(e.keyCode === 13) {
			checkPackageId($(this).val().trim());
		}
	});
});
</script>
<style type="text/css">
	* {
		padding: 0;
		margin: 0;
	}
	ul li {
		list-style: none;
	}
	li {
		margin-bottom: 15px;
	}
</style>
</head>
<body>
<h3>智造链-入库</h3>
<div>
<h3>订单号：${order.orderId}</h3>
</div>
<div>
<form method="post">

<table>
<tr><td>包号：</td><td><input type="text" id="txtScan" name="packageId"/></td></tr>
<tr><td>仓库：</td><td><input disabled="true" type="text" id="txtWare" name="warehouseId"/></td></tr>
<tr><td>货架：</td><td><input disabled="true" type="text" id="txtShelf" name="shelfId"/></td></tr>
<tr><td>位置：：</td><td><input disabled="true" type="text" id="txtLocation" name="location"/></td></tr>
</table>
<p><input type="submit" value="提交"/></p>

</form>
</div>

<ul id="package_list">
<c:forEach var="package" items="${packageList}">
<li>
<table id="package_${package.packageId }">
<tr><td>包号：</td><td>${package.packageId }</td></tr>
<tr><td>仓库：</td><td>${package.warehouseId }</td></tr>
<tr><td>货架：</td><td>${package.shelfId }</td></tr>
<tr><td>位置：：</td><td>${package.location }</td></tr>
</table>
</li>
</c:forEach>
</ul>
<div>

<form action="${ctx }/logistics/finishUpdateStore.do" method="post">
<input type="hidden" value="${order.orderId }" name="orderId" />
<input type="submit" value="完成入库" name="submit" />
</form>
</div>
</body>
</html>