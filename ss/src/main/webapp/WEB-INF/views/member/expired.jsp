﻿<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<style type="text/css">
.body-container {
	max-width: 800px;
}
</style>

<div class="container">
	<div class="body-container">	

        <div class="row justify-content-md-center mt-5">
            <div class="col-md-8">
                <div class="border border-primary mt-5 p-4">
                       <h4 class="text-center fw-bold">확인</h4>
                       
	                <div class="d-grid pt-3">
						<p class="alert border bg-light text-center lh-base fs-6" >
							세션이 만료 되었습니다.
						</p>
	                </div>
                       
                       <div class="d-grid">
                           <button type="button" class="btn btn-lg btn-primary" onclick="location.href='${pageContext.request.contextPath}/';">메인화면으로 이동 <i class="bi bi-arrow-counterclockwise"></i> </button>
                       </div>
                </div>
            </div>
        </div>

	</div>
</div>