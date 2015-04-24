<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="team.xinyuan519.wxsearch.utils.*,java.net.URLEncoder"%>
<!DOCTYPE html>
<html>
<head>
<title>vSearch</title>
<link rel="stylesheet"
	href="http://apps.bdimg.com/libs/bootstrap/3.3.0/css/bootstrap.min.css">
<script src="http://apps.bdimg.com/libs/jquery/2.1.1/jquery.min.js"></script>
<script
	src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="./response.css">
<script type="text/javascript" src="jsonpRefresh.js"></script>
</head>
<body>
	<div id="header">
		<form role="form" class="form-inline" action="response.jsp">
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">微搜一下</span> <input type="text"
						class="form-control" name="keyWords" id="keyWords"
						placeholder="输入关键词"> <span class="input-group-btn">
						<button type="submit" class="btn btn-primary">
							<span class="glyphicon glyphicon-search"></span>
						</button>
					</span>
				</div>
			</div>
		</form>
	</div>
	<div name="container">
		<div id="content">
			<div id="list">
				<%
					String keyWords = request.getParameter("keyWords").trim();
					String utf8KeyWords = URLEncoder.encode(keyWords, "utf-8");
					String utf8Callback = URLEncoder.encode("callback", "utf-8");
					DivGetter getter = new DivGetter(keyWords);
					String listConent = getter.getContent();
					out.write(listConent);
				%>
			</div>
		</div>
		<%
			String script = String
					.format("<script type=\"text/javascript\" src=\"http://%s:%d/%s/messenger.jsp?keyWords=%s&callback=%s\"></script>",
							EnvironmentInfo.serverIP,
							EnvironmentInfo.webServicePort,
							EnvironmentInfo.projectName, utf8KeyWords,
							utf8Callback);
			out.write(script);
		%>
	</div>
</body>
</html>
