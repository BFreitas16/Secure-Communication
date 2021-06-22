<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ include file="index.jsp"%>
<!DOCTYPE html>

<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Highly Dependable Location Tracker: Get Report of an User</title>
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
			<span class="fas fa-street-view"> Get Users in Location at Epoch </span>
		</h2>

		<form:form action="users_at_location_form" method="post" modelAttribute="appModel">
			<div class="form-group row">
				<form:label path="userId" class="col-sm-2 col-form-label">User ID:</form:label>
				<div class="col-sm-10">
					<form:input path="userId" class="form-control"/>
				</div>
			</div>
			<br>
			<div class="form-group row">
				<form:label path="epoch" class="col-sm-2 col-form-label">Epoch:</form:label>
				<div class="col-sm-10">
					<form:input path="epoch" class="form-control"/>
				</div>
			</div>
			<br>
			<div class="form-group row">
				<form:label path="x" class="col-sm-2 col-form-label">X Location:</form:label>
				<div class="col-sm-10">
					<form:input path="x" class="form-control"/>
				</div>
			</div>
			<br>
			<div class="form-group row">
				<form:label path="y" class="col-sm-2 col-form-label">Y Location:</form:label>
				<div class="col-sm-10">
					<form:input path="y" class="form-control"/>
				</div>
			</div>
			<br>
			<div class="form-group row">
				<div class="col-sm-10">
					<form:button class="btn btn-primary">Submit</form:button>
				</div>
			</div>
		</form:form>
	</div>
</body>
</html>