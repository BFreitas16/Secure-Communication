<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ include file="index.jsp"%>
<!DOCTYPE html>

<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Highly Dependable Location Tracker: Get User Report
		Success</title>
	<!-- Icons -->
	<link rel="stylesheet"
		href="https://use.fontawesome.com/releases/v5.7.0/css/all.css"
		integrity="sha384-lZN37f5QGtY3VHgisS14W3ExzMWZxybE1SJSEsQp9S+oqd12jhcu+A56Ebc1zFSJ"
		crossorigin="anonymous">
	<!-- bootstrap -->
	<link rel="stylesheet"
		href="https://getbootstrap.com/docs/5.0/dist/css/bootstrap.min.css"
		integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6"
		crossorigin="anonymous">
</head>
<body>
	<div class="d-flex flex-column p-3 bg-light" style="width: 50%;">
		<h2>
			<span class="fas fa-address-card"> Get User Report Succeeded:</span>
		</h2>

		<div>
			<p>
				Request of <span>${appModel.userId}</span> at epoch <span>${appModel.epoch}</span>
			</p>
			<p>
				<span>${appModel.result}</span>
			</p>
		</div>

	</div>
</body>
</html>