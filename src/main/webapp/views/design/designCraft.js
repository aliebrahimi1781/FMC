(function($) {	
	$(function(){
		$("input[name='needcraft']").change(function(){
			if($('input:radio[name="needcraft"]:checked').val()=="0"){
				$("input[name='stampDutyMoney']").attr("disabled","disabled");
				$("input[name='washHangDyeMoney']").attr("disabled","disabled");
				$("input[name='laserMoney']").attr("disabled","disabled");
				$("input[name='embroideryMoney']").attr("disabled","disabled");
				$("input[name='crumpleMoney']").attr("disabled","disabled");
				$("input[name='openVersionMoney']").attr("disabled","disabled");
			}else{
				$("input[name='stampDutyMoney']").removeAttr("disabled");
				$("input[name='washHangDyeMoney']").removeAttr("disabled");
				$("input[name='laserMoney']").removeAttr("disabled");
				$("input[name='embroideryMoney']").removeAttr("disabled");
				$("input[name='crumpleMoney']").removeAttr("disabled");
				$("input[name='openVersionMoney']").removeAttr("disabled");
			}
		});
	});
})(jQuery);