<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<style type="text/css">
.body-container {
	max-width: 870px;
}
</style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/boot-board.css" type="text/css">

<c:if test="${sessionScope.member.userId==dto.userId || sessionScope.member.membership>50}">
	<script type="text/javascript">
		function deleteOk(mode) {
			let s = (mode === "question" ? "질문" : "답변") + "을 삭제 하시 겠습니까 ? ";
			
		    if(confirm(s)) {
		    	let query = "num=${dto.num}&mode="+mode+"&${query}";
			    let url = "${pageContext.request.contextPath}/qna/delete?" + query;
		    	location.href = url;
		    }
		}
	</script>
</c:if>

<div class="container">
	<div class="body-container">	
		<div class="body-title">
			<h3><i class="bi bi-exclamation-square"></i> 강좌 질문과 답변 </h3>
		</div>
		
		<div class="body-main">

			<table class="table mt-5 mb-0 board-article">
				<tbody>
					<tr>
						<td colspan="2" class="px-0 pb-0">
							<div class="row gx-0">
								<div class="col-sm-1 bg-primary me-1">
									<p class="form-control-plaintext text-white text-center">Q.</p>
								</div>
								<div class="col bg-primary">
									<p class="form-control-plaintext text-white ps-2">${dto.title}</p>
								</div>
							</div>
						</td>
					</tr>
				
					<tr>
						<td colspan="2">
							분 류 : ${dto.category} / ${dto.subject}
						</td>
					</tr>

					<tr>
						<td width="50%">
							작성자 : ${dto.userName}					
						</td>
						<td align="right">
							문의일자 : ${dto.reg_date}
						</td>
					</tr>
					
					<tr>
						<td colspan="2" valign="top" height="150">
							${dto.content}
						</td>
					</tr>
				</tbody>
			</table>
			
			<c:if test="${not empty dto.answer}">
				<table class="table mb-0">
					<tbody>
						<tr>
							<td colspan="2" class="p-0">
								<div class="row gx-0">
									<div class="col-sm-1 bg-success me-1">
										<p class="form-control-plaintext text-white text-center">A.</p>
									</div>
									<div class="col bg-success">
										<p class="form-control-plaintext text-white ps-2">${dto.title}</p>
									</div>
								</div>
							</td>
						</tr>
					
						<tr>
							<td width="50%">
								담당자 : ${dto.answerName}				
							</td>
							<td align="right">
								답변일자 :  ${dto.answer_date}
							</td>
						</tr>
						
						<tr>
							<td colspan="2" valign="top" height="150">
								${dto.answer}
							</td>
						</tr>
					</tbody>
				</table>
			</c:if>
			
			<table class="table mb-2">
					<tr>
						<td colspan="2">
							이전글 :
							<c:if test="${not empty prevDto}">
								<c:choose>
									<c:when test="${prevDto.secret==1}">
										<c:if test="${sessionScope.member.userId==prevDto.userId || sessionScope.member.membership>30}">
											<a href="${pageContext.request.contextPath}/qna/article?${query}&num=${prevDto.num}">${prevDto.title}</a>
										</c:if>
										<c:if test="${sessionScope.member.userId!=prevDto.userId && sessionScope.member.membership<31}">
											비밀글 입니다.
										</c:if>
										<i class="bi bi-file-lock2"></i>
									</c:when>
									<c:otherwise>
										<a href="${pageContext.request.contextPath}/qna/article?${query}&num=${prevDto.num}">${prevDto.title}</a>
									</c:otherwise>
								</c:choose>
							</c:if>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							다음글 :
							<c:if test="${not empty nextDto}">
								<c:choose>
									<c:when test="${nextDto.secret==1}">
										<c:if test="${sessionScope.member.userId==nextDto.userId || sessionScope.member.membership>30}">
											<a href="${pageContext.request.contextPath}/qna/article?${query}&num=${nextDto.num}">${nextDto.title}</a>
										</c:if>
										<c:if test="${sessionScope.member.userId!=nextDto.userId && sessionScope.member.membership<31}">
											비밀글 입니다.
										</c:if>
										<i class="bi bi-file-lock2"></i>
									</c:when>
									<c:otherwise>
										<a href="${pageContext.request.contextPath}/qna/article?${query}&num=${nextDto.num}">${nextDto.title}</a>
									</c:otherwise>
								</c:choose>
							</c:if>
						</td>
					</tr>
				</tbody>
			</table>
			
			<table class="table table-borderless">
				<tr>
					<td width="50%">
						<c:if test="${sessionScope.member.userId==dto.userId && empty dto.answer}">
							<button type="button" class="btn btn-light" onclick="javascript:location.href='${pageContext.request.contextPath}/qna/update?num=${dto.num}&page=${page}';">질문수정</button>
						</c:if>
						<c:if test="${sessionScope.member.userId==dto.userId || sessionScope.member.membership>50}">
							<button type="button" class="btn btn-light" onclick="deleteOk('question');">질문삭제</button>
						</c:if>
						<c:if test="${not empty dto.answer and sessionScope.member.userId==dto.answerId}">
							<button type="button" class="btn btn-light btnUpdateAnswer" data-mode="update">답변수정</button>
						</c:if>
						<c:if test="${not empty dto.answer && (sessionScope.member.userId==dto.answerId || sessionScope.member.membership>50)}">
							<button type="button" class="btn btn-light" onclick="deleteOk('answer');">답변삭제</button>
						</c:if>
					</td>
					<td class="text-end">
						<button type="button" class="btn btn-light" onclick="location.href='${pageContext.request.contextPath}/qna/list?${query}';">리스트</button>
					</td>
				</tr>
			</table>
			
			<div class="reply" style="display: none;">
				<form name="answerForm" method="post">
					<div class='form-header'>
						<span class="bold">질문에 대한 답변</span>
					</div>
					
					<table class="table table-borderless reply-form">
						<tr>
							<td>
								<textarea class='form-control' name="answer">${dto.answer}</textarea>
							</td>
						</tr>
						<tr>
						   <td align='right'>
						   		<input type="hidden" name="num" value="${dto.num}">	
						   		<input type="hidden" name="page" value="${page}">					   
						        <button type='button' class='btn btn-light btnSendAnswer'>답변 등록</button>
						    </td>
						 </tr>
					</table>
				</form>
			</div>

		</div>
	</div>
</div>

<c:if test="${sessionScope.member.membership>30}">
	<script type="text/javascript">
		$(function(){
			let answerName = "${dto.answerName}";
			if(! answerName) {
				$(".reply").show();
			}
		});
		
		$(function(){
			$(".btnSendAnswer").click(function(){
				const f = document.answerForm;
				if(! f.answer.value.trim()) {
					f.answer.focus();
					return false;
				}
				
				f.action = "${pageContext.request.contextPath}/qna/answer";
				f.submit();
			});
		});
		
		$(function(){
			$(".btnUpdateAnswer").click(function(){
				let mode = $(this).attr("data-mode");
				if(mode === "update") {
					$(".reply").show();
					$(this).text("답변 수정 취소")
					$(this).attr("data-mode", "cancel");
				} else {
					$(".reply").hide();
					$(this).attr("data-mode", "update");
					$(this).text("답변 수정")
				}
			});
		});
	</script>
</c:if>
