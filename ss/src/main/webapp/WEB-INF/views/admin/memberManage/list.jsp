<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<style type="text/css">
.body-main {
	max-width: 930px;
}
</style>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/tabs.css" type="text/css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/board.css" type="text/css">

<script type="text/javascript">
$(function(){
	$("#tab-0").addClass("active");

	$("ul.tabs li").click(function() {
		let tab = $(this).attr("data-tab");
		
		$("ul.tabs li").each(function(){
			$(this).removeClass("active");
		});
		
		$("#tab-"+tab).addClass("active");
		
		let url = "${pageContext.request.contextPath}/admin/memberManage/analysis";	
		location.href = url;
	});
});

function login() {
	location.href = '${pageContext.request.contextPath}/member/login';
}

function ajaxFun(url, method, formData, dataType, fn, file = false) {
	const settings = {
			type: method, 
			data: formData,
			success:function(data) {
				fn(data);
			},
			beforeSend: function(jqXHR) {
				jqXHR.setRequestHeader('AJAX', true);
			},
			complete: function () {
			},
			error: function(jqXHR) {
				if(jqXHR.status === 403) {
					login();
					return false;
				} else if(jqXHR.status === 400) {
					alert('요청 처리가 실패 했습니다.');
					return false;
		    	}
		    	
				console.log(jqXHR.responseText);
			}
	};
	
	if(file) {
		settings.processData = false;  // file 전송시 필수. 서버로전송할 데이터를 쿼리문자열로 변환여부
		settings.contentType = false;  // file 전송시 필수. 서버에전송할 데이터의 Content-Type. 기본:application/x-www-urlencoded
	}
	
	$.ajax(url, settings);
}

function searchList() {
	const f = document.searchForm;
	f.enabled.value=$("#selectEnabled").val();
	f.action = "${pageContext.request.contextPath}/admin/memberManage/list";
	f.submit();
}
	
function profile(userId) {
	const dlg = $("#member-dialog").dialog({
		  autoOpen: false,
		  modal: true,
		  buttons: {
		       " 수정 " : function() {
		    	   updateOk(); 
		       },
		       " 삭제 " : function() {
		    	   deleteOk(userId);
			   },
		       " 닫기 " : function() {
		    	   $(this).dialog("close");
		       }
		  },
		  height: 550,
		  width: 800,
		  title: "회원상세정보",
		  close: function(event, ui) {
		  }
	});

	let url = "${pageContext.request.contextPath}/admin/memberManage/profile";
	let query = "userId="+userId;
	
	const fn = function(data){
		$('#member-dialog').html(data);
		dlg.dialog("open");
	};
	ajaxFun(url, "get", query, "text", fn);
}
	
function updateOk() {
	const f = document.deteailedMemberForm;
	
	if( ! f.stateCode.value ) {
		alert("상태코드를 선택하세요.");
		f.stateCode.focus();
		return;
	}
	if( ! f.memo.value.trim() ) {
		alert("상태메모를 선택하세요.");
		f.memo.focus();
		return;
	}
	
	let url = "${pageContext.request.contextPath}/admin/memberManage/updateMemberState";
	let query = $("#deteailedMemberForm").serialize();

	const fn = function(data){
		$('form input[name=page]').val('${page}');
		searchList();
	};
	ajaxFun(url, 'post', query, 'json', fn);
		
	$('#member-dialog').dialog('close');
}

function deleteOk(userId) {
	if(confirm("선택한 계정을 삭제 하시겠습니까 ?")) {

	}
	
	$('#member-dialog').dialog("close");
}

function memberStateDetaileView() {
	$('#memberStateDetaile').dialog({
		  modal: true,
		  minHeight: 100,
		  maxHeight: 450,
		  width: 750,
		  title: '계정상태 상세',
		  close: function(event, ui) {
			   $(this).dialog("destroy"); // 이전 대화상자가 남아 있으므로 필요
		  }
	  });
}

function selectStateChange() {
	const f = document.deteailedMemberForm;
	
	let s = f.stateCode.value;
	let txt = f.stateCode.options[f.stateCode.selectedIndex].text;
	
	f.memo.value = "";	
	if(! s) {
		return;
	}

	if(s!=="0" && s!=="6") {
		f.memo.value = txt;
	}
	
	f.memo.focus();
}
</script>

<div class="body-container">
    <div class="body-title">
		<h2><i class="fa-solid fa-user-group"></i> 회원 관리 </h2>
    </div>
    
    <div class="body-main">
    	
		<div>
			<ul class="tabs">
				<li id="tab-0" data-tab="0"><i class="fa-solid fa-person"></i>&nbsp;회원 리스트</li>
				<li id="tab-1" data-tab="1"><i class="fa-solid fa-chart-column"></i>&nbsp;회원 분석</li>
			</ul>
		</div>
		<div id="tab-content" style="padding: 20px 10px 0;">
		
			<table class="table">
				<tr>
					<td align="left" width="50%">
						${dataCount}개(${page}/${total_page} 페이지)
					</td>
					<td align="right">
						<select id="selectEnabled" class="form-select" onchange="searchList();">
							<option value="" ${enabled=="" ? "selected":""}>::계정상태::</option>
							<option value="0" ${enabled=="0" ? "selected":""}>잠금 계정</option>
							<option value="1" ${enabled=="1" ? "selected":""}>활성 계정</option>
						</select>
					</td>
				</tr>
			</table>
				
			<table class="table table-border table-list">
				<thead>
					<tr> 
						<th width="60">번호</th>
						<th width="120">아이디</th>
						<th width="100">이름</th>
						<th width="100">생년월일</th>
						<th width="120">전화번호</th>
						<th width="80">회원구분</th>
						<th width="60">상태</th>
						<th>이메일</th>
					</tr>
				</thead>
				
				<tbody>
					<c:forEach var="dto" items="${list}" varStatus="status">
						<tr class="hover" onclick="profile('${dto.userId}');"> 
							<td>${dataCount - (page-1) * size - status.index}</td>
							<td>${dto.userId}</td>
							<td>${dto.userName}</td>
							<td>${dto.birth}</td>
							<td>${dto.tel}</td>
							<td>${dto.membership==1?"일반회원":(dto.membership==31?"강사":(dto.membership==51?"사원":"기타")}</td>
							<td>${dto.enabled==1?"활성":"잠금"}</td>
							<td>${dto.email}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
					 
			<div class="page-navigation">
				${dataCount == 0 ? "등록된 게시물이 없습니다." : paging}
			</div>
					
			<table class="table">
				<tr>
					<td align="left" width="100">
						<button type="button" class="btn" onclick="location.href='${pageContext.request.contextPath}/admin/memberManage/list';">새로고침</button>
					</td>
					<td align="center">
						<form name="searchForm" action="${pageContext.request.contextPath}/admin/memberManage/list" method="post">
							<select name="schType" class="form-select">
								<option value="userId"     ${schType=="userId" ? "selected":""}>아이디</option>
								<option value="userName"   ${schType=="userName" ? "selected":""}>이름</option>
								<option value="email"      ${schType=="email" ? "selected":""}>이메일</option>
								<option value="tel"        ${schType=="tel" ? "selected":""}>전화번호</option>
							</select>
							<input type="text" name="kwd" class="form-control" value="${kwd}">
							<input type="hidden" name="enabled" value="${enabled}">
							<input type="hidden" name="page" value="1">
							<button type="button" class="btn" onclick="searchList()">검색</button>
						</form>
					</td>
					<td align="right" width="100">&nbsp;</td>
				</tr>
			</table>
		
		</div>
    	
	</div>
</div>

<div id="member-dialog" style="display: none;"></div>
